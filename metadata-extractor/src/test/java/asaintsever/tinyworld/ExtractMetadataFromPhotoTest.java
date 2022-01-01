package asaintsever.tinyworld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentType;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
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
import com.drew.metadata.xmp.XmpDirectory;

import net.coobird.thumbnailator.Thumbnails;

public class ExtractMetadataFromPhotoTest {
	
	@Test
	void getAllPhotoMetadata() {
		// Using MAX_VALUE to indicate that all levels should be visited.
		int nb_photo = exploreFS("src/test/resources/photos", Integer.MAX_VALUE, new PhotoProcess() {
			
			@Override
			public void task(String path, FileType fileType, Metadata metadata) {
				for (Directory directory : metadata.getDirectories()) {
					System.out.println("\n> " + directory);
					
				    for (Tag tag : directory.getTags()) {
				        System.out.println(tag);
				    }
				    
				    // XMP is not handled like other metadata
				    // From XmpDirectory class:
				    // "XMP uses a namespace and path format for identifying values, which does not map to metadata-extractor's
				    // integer based tag identifiers. Therefore, XMP data is extracted and exposed via {@link XmpDirectory#getXMPMeta()}
				    // which returns an instance of Adobe's {@link XMPMeta} which exposes the full XMP data set."
				    if (directory instanceof XmpDirectory) {
				    	XmpDirectory xmpDir = (XmpDirectory)directory;
				    	System.out.println("Dump XMP metadata:\n" + xmpDir.getXMPMeta().dumpObject());
				    }
				}
			}
		});
		
		assertEquals(25, nb_photo);
	}
	
	@Test
	void getTinyWorldPhotoMetadata() {
		// Using 1 to indicate that only first level should be visited.
		int nb_photo = exploreFS("src/test/resources/photos", 1, new PhotoProcess() {
			
			@Override
			public void task(String path, FileType fileType, Metadata metadata) {
				ExifSubIFDDirectory exfSubDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
				if (exfSubDir != null) {
					ExifSubIFDDescriptor exfSubDesc = new ExifSubIFDDescriptor(exfSubDir);
					System.out.println("Taken Date: " + exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
					if(exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET_TIFF_EP) != null) System.out.println("Time Zone Offset: " + exfSubDesc.getDescription(ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET_TIFF_EP));
					
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
					
					System.out.println("Width x Height: " + imgWidth + "x" + imgHeight);
				}
				
				IptcDirectory iptcDir = metadata.getFirstDirectoryOfType(IptcDirectory.class);
				if (iptcDir != null) {
					IptcDescriptor iptcDesc = new IptcDescriptor(iptcDir);
					if(iptcDesc.getCountryOrPrimaryLocationDescription() != null) System.out.println("Country: " + iptcDesc.getCountryOrPrimaryLocationDescription());
					if(iptcDesc.getProvinceOrStateDescription() != null) System.out.println("State/Province: " + iptcDesc.getProvinceOrStateDescription());
					if(iptcDesc.getCityDescription() != null) System.out.println("City: " + iptcDesc.getCityDescription());
					if(iptcDesc.getDescription(IptcDirectory.TAG_SUB_LOCATION) != null) System.out.println("Sublocation: " + iptcDesc.getDescription(IptcDirectory.TAG_SUB_LOCATION));
					if(iptcDesc.getCaptionDescription() != null) System.out.println("Caption: " + iptcDesc.getCaptionDescription());
					if(iptcDesc.getObjectNameDescription() != null) System.out.println("Title: " + iptcDesc.getObjectNameDescription());
					if(iptcDesc.getHeadlineDescription() != null) System.out.println("Headline: " + iptcDesc.getHeadlineDescription());
				}
				
				GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
				if (gpsDir != null) {
					GpsDescriptor gpsDesc = new GpsDescriptor(gpsDir);
					if(gpsDesc.getGpsLatitudeDescription() != null) System.out.println("Lat: " + gpsDesc.getDescription(GpsDirectory.TAG_LATITUDE_REF) + " " + gpsDesc.getGpsLatitudeDescription());
					if(gpsDesc.getGpsLongitudeDescription() != null) System.out.println("Lon: " + gpsDesc.getDescription(GpsDirectory.TAG_LONGITUDE_REF) + " " + gpsDesc.getGpsLongitudeDescription());
					if(gpsDesc.getDescription(GpsDirectory.TAG_MAP_DATUM) != null) System.out.println("Datum: " + gpsDesc.getDescription(GpsDirectory.TAG_MAP_DATUM));
				}
				
				FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
				if (fsDir != null) {					
					Long photoSize = fsDir.getLongObject(FileSystemDirectory.TAG_FILE_SIZE);
					System.out.println(fsDir.getTagName(FileSystemDirectory.TAG_FILE_SIZE) + ": " + (photoSize != null ? String.format("%.3f Mb", photoSize/(1024.0*1024.0)) : "Unknown"));
					
					FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
					System.out.println(fsDir.getTagName(FileSystemDirectory.TAG_FILE_NAME) + ": " + fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME));
				}
				
				System.out.println("Path: " + path);
				
				ExifIFD0Directory exfDir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
				if (exfDir != null) {
					ExifIFD0Descriptor exfDesc = new ExifIFD0Descriptor(exfDir);
					System.out.println("Camera Model (Manufacturer): " + exfDesc.getDescription(ExifIFD0Directory.TAG_MODEL) + " (" + exfDesc.getDescription(ExifIFD0Directory.TAG_MAKE) + ")");
				}
			}
		});
		
		assertEquals(10, nb_photo);
	}
	
	@Test
	void getPhotoThumbnails() {
		// Using MAX_VALUE to indicate that all levels should be visited.
		int nb_photo = exploreFS("src/test/resources/photos", Integer.MAX_VALUE, new PhotoProcess() {

			@Override
			public void task(String path, FileType fileType, Metadata metadata) {
				System.out.println("Extracting thumbnail from " + path + " ...");
				
				String filename = "";
				FileSystemDirectory fsDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
				if (fsDir != null) {
					FileSystemDescriptor fsDesc = new FileSystemDescriptor(fsDir);
					filename = fsDesc.getDescription(FileSystemDirectory.TAG_FILE_NAME);
				}
				
				byte[] data = null;
				ExifThumbnailDirectory exfThumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
				if (exfThumbDir != null) data = (byte[]) exfThumbDir.getObject(TAG_THUMBNAIL_DATA);
					
				if(data != null && data.length > 0) {
					File outputFile = null;
					
					switch(fileType) {
					case Jpeg:
						outputFile = new File("target/test-classes/" + filename + "_thumbnail.jpg");
						break;
					case Png:
						outputFile = new File("target/test-classes/" + filename + "_thumbnail.png");
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
						
						System.out.println("... Done");
					}
				} else {
					System.out.println("!! No thumbnail found in metadata -> generating thumbnail from photo");
					
					try {
						// Create thumbnail from photo, resized to a maximum dimension of 160 x 160, maintaining the aspect ratio of the original image
						Thumbnails.of(path)
						    .size(160, 160)
						    .toFile("target/test-classes/" + filename + "_thumbnail.jpg");
						
						System.out.println("... Done");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		assertEquals(25, nb_photo);
	}
	
	interface PhotoProcess {
		void task(String path, FileType fileType, Metadata metadata);
	}
	
	static int exploreFS(String rootDir, int depth, PhotoProcess photoProcess) {
		int nb_processed_photo = 0;
		
		try {
			Set<String> photos = listFilesUsingFileWalk(rootDir, depth);
			
			for (String photo : photos) {
				File photoFile = new File(photo);
				try(InputStream photoStream = new FileInputStream(photoFile)) {
					try(FilterInputStream photoFltStream = new BufferedInputStream(photoStream)) {
						// Check this is a supported media
						FileType fileType = FileTypeDetector.detectFileType(photoFltStream);
						
						if (fileType == FileType.Jpeg || fileType == FileType.Png) {
							System.out.println("\n" + photo + "\n=================================================");
							
							Metadata metadata = ImageMetadataReader.readMetadata(photoFile); // Use File here, not stream, to get FileSystemDirectory info (photo name and size)
							photoProcess.task(photo, fileType, metadata);
							nb_processed_photo++;
						} else {
							System.out.println("\n!! Skipping " + photo + ": unsupported media type");
						}
					}
				}
			}
		} catch (ImageProcessingException | IOException e) {
			e.printStackTrace();
		}
		
		return nb_processed_photo;
	}
	
	static Set<String> listFilesUsingFileWalk(String dir, int depth) throws IOException {
	    try (Stream<Path> stream = Files.walk(Paths.get(dir), depth, FileVisitOption.FOLLOW_LINKS)) {
	        return stream
	          .filter(file -> !Files.isDirectory(file))
	          .map(Path::toString)
	          .collect(Collectors.toSet());
	    }
	}

	// Hack to get thumbnail: https://github.com/drewnoakes/metadata-extractor/issues/276#issuecomment-677767368
	static int TAG_THUMBNAIL_DATA = 0x10000;
	
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
