package asaintsever.tinyworld.metadata.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.xmp.XmpDirectory;


public class ExtractMetadataFromPhotoTest {
    
    @Test
    void getAllPhotoMetadata() {
        // Using MAX_VALUE to indicate that all levels should be visited.
        int nb_photo = Extract.exploreFS("src/test/resources/photos", Integer.MAX_VALUE, new IPhotoProcess() {
            
            @Override
            public void task(URI uri, FileType fileType, Metadata metadata) {
                System.out.println("\n" + uri + "\n=================================================");
                
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
        int nb_photo = Extract.exploreFS("src/test/resources/photos", 1, new IPhotoProcess() {
            
            @Override
            public void task(URI uri, FileType fileType, Metadata metadata) {
                System.out.println("\n" + uri + "\n=================================================");
                
                try {
                    PhotoObject photo = new PhotoObject();
                    System.out.println(photo.extractMetadata(uri, fileType, metadata).getMetadataAsJson());
                } catch (IOException | ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        assertEquals(12, nb_photo);
    }

    @Test
    void getPhotoThumbnails() {
        // Using MAX_VALUE to indicate that all levels should be visited.
        int nb_photo = Extract.exploreFS("src/test/resources/photos", Integer.MAX_VALUE, new IPhotoProcess() {
        
            @Override
            public void task(URI uri, FileType fileType, Metadata metadata) {
                System.out.println("\n" + uri + "\n=================================================");

                try {
                    PhotoObject photo = new PhotoObject();
                    photo.extractThumbnail(uri, fileType, metadata, "target/test-classes");
                    System.out.println("Done");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        
        assertEquals(25, nb_photo);
    }

}
