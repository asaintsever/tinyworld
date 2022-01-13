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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;


public class Extract {
	
	public static int exploreFS(String rootDir, int depth, IPhotoProcess photoProcess) {
		int nb_processed_photo = 0;
		
		try {
			Set<URI> photos = listFilesUsingFileWalk(rootDir, depth);
			
			for (URI photo : photos) {
				File photoFile = new File(photo);
				try(InputStream photoStream = new FileInputStream(photoFile)) {
					try(FilterInputStream photoFltStream = new BufferedInputStream(photoStream)) {
						// Check this is a supported media
						FileType fileType = FileTypeDetector.detectFileType(photoFltStream);
						
						if (fileType == FileType.Jpeg || fileType == FileType.Png) {
							System.out.println("\n" + photo + "\n=================================================");	//TODO log
							
							Metadata metadata = ImageMetadataReader.readMetadata(photoFile); // Use File here, not stream, to get FileSystemDirectory info (photo name and size)
							photoProcess.task(photo, fileType, metadata);
							nb_processed_photo++;
						} else {
							System.out.println("\n!! Skipping " + photo + ": unsupported media type");	//TODO log
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

}
