package asaintsever.tinyworld.metadata.extractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentType;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifThumbnailDirectory;


public class Extract {
    protected static Logger logger = LoggerFactory.getLogger(Extract.class);

    
    public static int exploreFS(String rootDir, int depth, IPhotoProcess photoProcess) {
        int nb_processed_photo = 0;
        
        try {
            Set<URI> photos = listFilesUsingFileWalk(rootDir, depth);
            
            for (URI photo : photos) {
                File photoFile = new File(photo);
                try (InputStream photoStream = new FileInputStream(photoFile)) {
                    try (FilterInputStream photoFltStream = new BufferedInputStream(photoStream)) {
                        // Check this is a supported media
                        FileType fileType = FileTypeDetector.detectFileType(photoFltStream);
                        
                        if (fileType == FileType.Jpeg || fileType == FileType.Png) {
                            Metadata metadata = ImageMetadataReader.readMetadata(photoFile); // Use File here, not stream, to get FileSystemDirectory info (photo name and size)
                            photoProcess.task(photo, fileType, metadata);
                            nb_processed_photo++;
                        } else {
                            logger.warn("Skipping " + photo + ": unsupported media type");
                        }
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }
        
        return nb_processed_photo;
    }
    
    private static Set<URI> listFilesUsingFileWalk(String dir, int depth) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir), depth, FileVisitOption.FOLLOW_LINKS)) {
            return stream
              .filter(file -> !Files.isDirectory(file))
              .map(Path::toUri)
              .collect(Collectors.toSet());
        }
    }

    //=======================================================================================================================
    // Hack to get thumbnail: https://github.com/drewnoakes/metadata-extractor/issues/276#issuecomment-677767368
    // Must be declared in class accessing ImageMetadataReader for the first time (so that our overridden 'readJpegSegments()' method is registered before first call to ImageMetadataReader.readMetadata)
    public static final int TAG_THUMBNAIL_DATA = 0x10000;
    
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
    //=======================================================================================================================

}
