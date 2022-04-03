package asaintsever.tinyworld.metadata.extractor;

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
        if (defaultMetadata != null) {
            this.metadata = defaultMetadata;
        } else {
            this.metadata = new PhotoMetadata();
        }
    }
    
    
    public PhotoMetadata getMetadata() {
        return this.metadata;
    }
    
    public String getMetadataAsJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this.metadata);
    }
    
    public PhotoObject extractMetadata(URI uri, FileType fileType, Metadata metadata) throws ParseException, IOException {
        logger.info("Extracting metadata from " + uri);
        
        ExifSubIFDDirectory exfSubDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exfSubDir != null) {
            ExifSubIFDDescriptor exfSubDesc = new ExifSubIFDDescriptor(exfSubDir);
            
            logger.debug("taken Date: " + exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
            
            SimpleDateFormat df = new SimpleDateFormat(PhotoMetadata.EXIF_DATE_PATTERN);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            df.setLenient(false);
            this.metadata.takenDate = df.parse(exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
            
            this.metadata.timeZoneOffset = exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET_TIFF_EP);
            
            // Get width and height from TAG_EXIF_IMAGE_WIDTH and TAG_EXIF_IMAGE_HEIGHT
            // If no values in EXIF: get from TAG_IMAGE_WIDTH and TAG_IMAGE_HEIGHT (in JpegDirectory or PngDirectory depending on file type)
            Integer imgWidth = exfSubDir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
            Integer imgHeight = exfSubDir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
            if (imgWidth == null || imgHeight == null) {
                switch(fileType) {
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
                default:
                    break;
                }
            }
            
            this.metadata.pixelRes = imgWidth + "x" + imgHeight;
        }
        
        IptcDirectory iptcDir = metadata.getFirstDirectoryOfType(IptcDirectory.class);
        if (iptcDir != null) {
            IptcDescriptor iptcDesc = new IptcDescriptor(iptcDir);
            this.metadata.countryCode = iptcDesc.getDescription(IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION_CODE);
            this.metadata.country = iptcDesc.getCountryOrPrimaryLocationDescription();
            this.metadata.stateOrProvince = iptcDesc.getProvinceOrStateDescription();
            this.metadata.city = iptcDesc.getCityDescription();
            this.metadata.sublocation = iptcDesc.getDescription(IptcDirectory.TAG_SUB_LOCATION);
            this.metadata.caption = iptcDesc.getCaptionDescription();
            this.metadata.title = iptcDesc.getObjectNameDescription();
            this.metadata.headline = iptcDesc.getHeadlineDescription();
        }
    
        GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDir != null) {
            this.metadata.gpsLatLong = gpsDir.getGeoLocation() != null ? gpsDir.getGeoLocation().getLatitude() + "," + gpsDir.getGeoLocation().getLongitude() : null;
            
            GpsDescriptor gpsDesc = new GpsDescriptor(gpsDir);
            this.metadata.gpsDatum = gpsDesc.getDescription(GpsDirectory.TAG_MAP_DATUM);
        }
        
        FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
        if (fsDir != null) {
            Long photoSize = fsDir.getLongObject(FileSystemDirectory.TAG_FILE_SIZE);
            this.metadata.sizeMb = photoSize != null ? photoSize/(1024.0f*1024.0f) : null;
            
            FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
            this.metadata.fileName = fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME);
        }
        
        this.metadata.path = uri.toURL();
        
        ExifIFD0Directory exfDir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exfDir != null) {
            ExifIFD0Descriptor exfDesc = new ExifIFD0Descriptor(exfDir);
            this.metadata.camModelMake = exfDesc.getDescription(ExifIFD0Directory.TAG_MODEL) + " (" + exfDesc.getDescription(ExifIFD0Directory.TAG_MAKE) + ")";
        }
        
        // Get thumbnail
        this.extractThumbnail(uri, fileType, metadata);
        
        return this;
    }
    
    public PhotoObject extractThumbnail(URI uri, FileType fileType, Metadata metadata) throws MalformedURLException, IOException {
        return this.extractThumbnail(uri, fileType, metadata, null);
    }

    public PhotoObject extractThumbnail(URI uri, FileType fileType, Metadata metadata, String dumpPath) throws MalformedURLException, IOException {
        logger.info("Extracting thumbnail from " + uri);
        
        String filename = "";
        
        if (dumpPath != null) {
            FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
            if (fsDir != null) {
                FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
                filename = fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME);
            }
        }
        
        byte[] data = null;
        ExifThumbnailDirectory exfThumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
        if (exfThumbDir != null) data = (byte[]) exfThumbDir.getObject(Extract.TAG_THUMBNAIL_DATA);
        	
        if(data != null && data.length > 0) {
            // Base64-encoded thumbnail
            this.metadata.thumbnail = Base64.getEncoder().encodeToString(data);
            
            if (dumpPath != null) {
                File outputFile = new File(dumpPath + File.separator + filename + "_thumbnail." + fileType.getCommonExtension());
                Files.write(outputFile.toPath(), data);
            }
        } else {
            logger.warn("No thumbnail found in metadata for " + uri + " -> generating thumbnail from photo");
            
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                // Create thumbnail from photo, resized to a maximum dimension of 160 x 160, maintaining the aspect ratio of the original image
                Thumbnails.of(uri.toURL())
                    .size(160, 160)
                    .toOutputStream(baos);
                
                // Base64-encoded thumbnail
                this.metadata.thumbnail = Base64.getEncoder().encodeToString(baos.toByteArray());
                
                if (dumpPath != null) {
                    Files.write(new File(dumpPath + File.separator + filename + "_thumbnail.jpg").toPath(), baos.toByteArray());
                }
            }
        }
        
        return this;
    }
    
}
