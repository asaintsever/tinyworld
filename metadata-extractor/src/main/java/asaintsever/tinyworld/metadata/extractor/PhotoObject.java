/*
 * Copyright 2021-2022 A. Saint-Sever
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://github.com/asaintsever/tinyworld
 */
package asaintsever.tinyworld.metadata.extractor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageCommand;
import org.im4java.core.Stream2BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.FileType;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileSystemDescriptor;
import com.drew.metadata.file.FileSystemDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.iptc.IptcDescriptor;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.coobird.thumbnailator.Thumbnails;

public class PhotoObject {
    protected static Logger logger = LoggerFactory.getLogger(PhotoObject.class);
    private static ObjectMapper mapper;
    private PhotoMetadata metadata;

    static {
        mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public PhotoObject() {
        this(null);
    }

    /**
     * Constructor to set default metadata values for attributes not found in photo
     */
    public PhotoObject(PhotoMetadata defaultMetadata) {
        this.metadata = new PhotoMetadata().from(defaultMetadata);
    }

    public PhotoMetadata getMetadata() {
        return this.metadata;
    }

    public String getMetadataAsJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this.metadata);
    }

    public PhotoObject extractMetadata(URI uri, FileType fileType, Metadata metadata)
            throws ParseException, IOException {
        logger.info("Extracting metadata from " + uri);

        this.metadata.setPath(uri.toURL());

        ExifSubIFDDirectory exfSubDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exfSubDir != null) {
            ExifSubIFDDescriptor exfSubDesc = new ExifSubIFDDescriptor(exfSubDir);

            logger.debug("taken Date: " + exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));

            SimpleDateFormat df = new SimpleDateFormat(PhotoMetadata.EXIF_DATE_PATTERN);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            df.setLenient(false);
            this.metadata.setTakenDate(df.parse(exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)))
                    .setTimeZoneOffset(exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET_TIFF_EP));

            // Get width and height from TAG_EXIF_IMAGE_WIDTH and TAG_EXIF_IMAGE_HEIGHT
            // If no values in EXIF: get from TAG_IMAGE_WIDTH and TAG_IMAGE_HEIGHT
            Integer imgWidth = exfSubDir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
            Integer imgHeight = exfSubDir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
            if (imgWidth == null || imgHeight == null) {
                switch (fileType) {
                case Jpeg:
                    JpegDirectory jpegDir = metadata.getFirstDirectoryOfType(JpegDirectory.class);
                    imgWidth = jpegDir.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
                    imgHeight = jpegDir.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
                    break;
                case Png:
                    PngDirectory pngDir = metadata.getFirstDirectoryOfType(PngDirectory.class);
                    imgWidth = pngDir.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
                    imgHeight = pngDir.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
                    break;
                case Heif:
                    HeifDirectory heifDir = metadata.getFirstDirectoryOfType(HeifDirectory.class);
                    imgWidth = heifDir.getInteger(HeifDirectory.TAG_IMAGE_WIDTH);
                    imgHeight = heifDir.getInteger(HeifDirectory.TAG_IMAGE_HEIGHT);
                    break;
                default:
                    break;
                }
            }

            this.metadata.setPixelRes(imgWidth + "x" + imgHeight);
        }

        IptcDirectory iptcDir = metadata.getFirstDirectoryOfType(IptcDirectory.class);
        if (iptcDir != null) {
            IptcDescriptor iptcDesc = new IptcDescriptor(iptcDir);
            this.metadata.setCountryCode(iptcDesc.getDescription(IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION_CODE))
                    .setCountry(iptcDesc.getCountryOrPrimaryLocationDescription())
                    .setStateOrProvince(iptcDesc.getProvinceOrStateDescription()).setCity(iptcDesc.getCityDescription())
                    .setSublocation(iptcDesc.getDescription(IptcDirectory.TAG_SUB_LOCATION))
                    .setCaption(iptcDesc.getCaptionDescription()).setTitle(iptcDesc.getObjectNameDescription())
                    .setHeadline(iptcDesc.getHeadlineDescription());

            if (iptcDesc.getKeywordsDescription() != null) {
                // IPTC keywords have been concatenated with ";" as separator
                String[] keywords = iptcDesc.getKeywordsDescription().split(";");
                this.metadata.setTags(keywords);
            }
        }

        GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDir != null) {
            this.metadata.setGpsLatLong(gpsDir.getGeoLocation() != null
                    ? gpsDir.getGeoLocation().getLatitude() + "," + gpsDir.getGeoLocation().getLongitude() : null);

            GpsDescriptor gpsDesc = new GpsDescriptor(gpsDir);
            this.metadata.setGpsDatum(gpsDesc.getDescription(GpsDirectory.TAG_MAP_DATUM));
        }

        FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
        if (fsDir != null) {
            Long photoSize = fsDir.getLongObject(FileSystemDirectory.TAG_FILE_SIZE);
            this.metadata.setSizeMb(photoSize != null ? photoSize / (1024.0f * 1024.0f) : null);

            FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
            this.metadata.setFileName(fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME));
        }

        ExifIFD0Directory exfDir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exfDir != null) {
            ExifIFD0Descriptor exfDesc = new ExifIFD0Descriptor(exfDir);
            this.metadata.setCamModelMake(exfDesc.getDescription(ExifIFD0Directory.TAG_MODEL) + " ("
                    + exfDesc.getDescription(ExifIFD0Directory.TAG_MAKE) + ")");
        }

        // Get thumbnail
        return this.extractThumbnail(uri, fileType, metadata);
    }

    public PhotoObject extractThumbnail(URI uri, FileType fileType, Metadata metadata)
            throws MalformedURLException, IOException {
        return this.extractThumbnail(uri, fileType, metadata, null);
    }

    public PhotoObject extractThumbnail(URI uri, FileType fileType, Metadata metadata, String dumpPath)
            throws MalformedURLException, IOException {
        String filename = "";

        if (dumpPath != null) {
            logger.info("Extracting thumbnail from " + uri);

            FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
            if (fsDir != null) {
                FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
                filename = fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME);
            }
        }

        byte[] thumbnail = null;
        ExifThumbnailDirectory exfThumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
        if (exfThumbDir != null)
            thumbnail = (byte[]) exfThumbDir.getObject(Extract.TAG_THUMBNAIL_DATA);

        if (thumbnail == null || thumbnail.length == 0) {
            logger.warn("No thumbnail found in metadata for " + uri + " -> generating thumbnail from photo");

            // Test if HEIF format: Java Image I/O API does not support it
            // Make use of ImageMagick to generate a thumbnail (JPG format)
            if (fileType == FileType.Heif) {
                ImageCommand cmd = new ImageCommand("magick");
                // cmd.setSearchPath(""); //In case magick binary is not in path
                Stream2BufferedImage s2b = new Stream2BufferedImage();
                cmd.setOutputConsumer(s2b);

                IMOperation op = new IMOperation();
                op.addImage(uri.toString());
                op.thumbnail(160, 160).addImage("jpg:-"); // Generate thumbnail on stdout (jpg format)

                try {
                    cmd.run(op);
                } catch (IOException | InterruptedException | IM4JavaException e) {
                    throw new IOException(e);
                }

                BufferedImage img = s2b.getImage();

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(img, "jpg", baos);
                    thumbnail = baos.toByteArray();
                }
            } else {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    // Create thumbnail from photo, resized to a maximum dimension of 160 x 160, maintaining the aspect
                    // ratio of the original image
                    Thumbnails.of(uri.toURL()).size(160, 160).toOutputStream(baos);
                    thumbnail = baos.toByteArray();
                }
            }
        }

        if (thumbnail != null && thumbnail.length > 0) {
            // Base64-encoded thumbnail
            this.metadata.setThumbnail(Base64.getEncoder().encodeToString(thumbnail));

            if (dumpPath != null) {
                File outputFile = new File(dumpPath + File.separator + filename + "_thumbnail."
                        + (fileType == FileType.Heif ? "jpg" : fileType.getCommonExtension()));
                Files.write(outputFile.toPath(), thumbnail);
            }
        }

        return this;
    }

}
