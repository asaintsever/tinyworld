package asaintsever.tinyworld.metadata.extractor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.drew.imaging.FileType;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentType;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifReader;
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
	private static ObjectMapper mapper;
	private PhotoMetadata metadata;
	
	static {	
		mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
	
	public PhotoObject() {
		this.metadata = new PhotoMetadata();
	}
	
	
	public PhotoMetadata getMetadata() {
		return this.metadata;
	}

	public String getMetadataAsJson() throws JsonProcessingException {
		return mapper.writeValueAsString(this.metadata);
	}
	
	public PhotoObject extractMetadata(URI uri, FileType fileType, Metadata metadata) throws ParseException, MalformedURLException {		
		ExifSubIFDDirectory exfSubDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		if (exfSubDir != null) {
			ExifSubIFDDescriptor exfSubDesc = new ExifSubIFDDescriptor(exfSubDir);
			
			System.out.println("taken Date: " + exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));	//TODO log
			
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
			GpsDescriptor gpsDesc = new GpsDescriptor(gpsDir);
			this.metadata.gpsLat = gpsDesc.getDescription(GpsDirectory.TAG_LATITUDE_REF) != null ? (gpsDesc.getDescription(GpsDirectory.TAG_LATITUDE_REF) + " " + gpsDesc.getGpsLatitudeDescription()) : null;
			this.metadata.gpsLong = gpsDesc.getDescription(GpsDirectory.TAG_LONGITUDE_REF) != null ? (gpsDesc.getDescription(GpsDirectory.TAG_LONGITUDE_REF) + " " + gpsDesc.getGpsLongitudeDescription()) : null;
			this.metadata.gpsDatum = gpsDesc.getDescription(GpsDirectory.TAG_MAP_DATUM);
		}
		
		FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
		if (fsDir != null) {					
			Long photoSize = fsDir.getLongObject(FileSystemDirectory.TAG_FILE_SIZE);
			this.metadata.sizeMb = photoSize != null ? photoSize/(1024.0f*1024.0f) : null;
			
			FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
			this.metadata.fileName = fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME);
		}
		
		System.out.println("uri: " + uri);	//TODO log
		this.metadata.path = uri.toURL();
		
		ExifIFD0Directory exfDir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (exfDir != null) {
			ExifIFD0Descriptor exfDesc = new ExifIFD0Descriptor(exfDir);
			this.metadata.camModelMake = exfDesc.getDescription(ExifIFD0Directory.TAG_MODEL) + " (" + exfDesc.getDescription(ExifIFD0Directory.TAG_MAKE) + ")";
		}
		
		//TODO get thumbnail
		//this.extractThumbnail(uri, fileType, metadata);
		
		return this;
	}
	
	public PhotoObject extractThumbnail(URI uri, FileType fileType, Metadata metadata) throws MalformedURLException, IOException {
		return this.extractThumbnail(uri, fileType, metadata, false, null);
	}
	
	public PhotoObject extractThumbnail(URI uri, FileType fileType, Metadata metadata, boolean dumpInFile, String dumpPath) throws MalformedURLException, IOException {
		String filename = "";
		
		if (dumpInFile) {
			FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
			if (fsDir != null) {
				FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
				filename = fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME);
			}
		}
		
		byte[] data = null;
		ExifThumbnailDirectory exfThumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
		if (exfThumbDir != null) data = (byte[]) exfThumbDir.getObject(PhotoObject.TAG_THUMBNAIL_DATA);
			
		if(data != null && data.length > 0) {
			// Base64-encoded thumbnail
			//this.metadata.thumbnail = ;	//TODO
			
			if (dumpInFile) {
				File outputFile = null;
				
				switch(fileType) {
				case Jpeg:
					outputFile = new File(dumpPath + File.separator + filename + "_thumbnail.jpg");
					break;
				case Png:
					outputFile = new File(dumpPath + File.separator + filename + "_thumbnail.png");
					break;
				default:
					break;
				}
				
				if(outputFile != null) {
					try {
						Files.write(outputFile.toPath(), data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("!! No thumbnail found in metadata -> generating thumbnail from photo");	//TODO log
			
			if (dumpInFile) {
				// Create thumbnail from photo, resized to a maximum dimension of 160 x 160, maintaining the aspect ratio of the original image
				Thumbnails.of(uri.toURL())
				    .size(160, 160)
				    .toFile(dumpPath + File.separator + filename + "_thumbnail.jpg");	//TODO get data 
			}
			
			// Base64-encoded thumbnail
			//this.metadata.thumbnail = ;	//TODO
		}
		
		return this;
	}
	
	// Hack to get thumbnail: https://github.com/drewnoakes/metadata-extractor/issues/276#issuecomment-677767368
	private static int TAG_THUMBNAIL_DATA = 0x10000;
	
	static {
		List<JpegSegmentMetadataReader> allReaders = (List<JpegSegmentMetadataReader>) JpegMetadataReader.ALL_READERS;
		for (int n = 0, cnt = allReaders.size(); n < cnt; n++) {
			if (allReaders.get(n).getClass() != ExifReader.class) {
				continue;
			}
			
			allReaders.set(n, new ExifReader() {
				@Override
				public void readJpegSegments(@NotNull final Iterable<byte[]> segments, @NotNull final Metadata metadata, @NotNull final JpegSegmentType segmentType) {
					super.readJpegSegments(segments, metadata, segmentType);

				    for (byte[] segmentBytes : segments) {
				        // Filter any segments containing unexpected preambles
				        if (!startsWithJpegExifPreamble(segmentBytes)) {
				        	continue;
				        }
				        
				        // Extract the thumbnail
				        try {
				            ExifThumbnailDirectory tnDirectory = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
				            if (tnDirectory != null && tnDirectory.containsTag(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET)) {
				            	int offset = tnDirectory.getInt(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
				            	int length = tnDirectory.getInt(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
				            	
				            	byte[] tnData = new byte[length];
				            	System.arraycopy(segmentBytes, JPEG_SEGMENT_PREAMBLE.length() + offset, tnData, 0, length);
				            	tnDirectory.setObject(TAG_THUMBNAIL_DATA, tnData);
				            }
				        } catch (MetadataException e) {
				            e.printStackTrace();
				        }
				    }
				}				
			});
			break;
		}
	}

}
