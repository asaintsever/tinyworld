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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.xmp.XmpDirectory;

import asaintsever.tinyworld.metadata.extractor.Extract.Result;

public class ExtractMetadataFromPhotoTest {
    
    static boolean isImageMagickInPath;

    
    @BeforeAll
    public static void setup() {
        // Check if ImageMagick is available in PATH or not.
        // ImageMagick is needed only to generate thumbnails from HEIC photos.
        try {
            Runtime.getRuntime().exec("magick -version");
        } catch (IOException e) {
            isImageMagickInPath = false;
            return;
        }
        
        isImageMagickInPath = true;
    }

    @Test
    void getAllPhotoMetadata() {
        // Using MAX_VALUE to indicate that all levels should be visited.
        Result res = Extract.exploreFS("src/test/resources/photos", Integer.MAX_VALUE, new IPhotoProcess() {

            @Override
            public void task(URI uri, FileType fileType, Metadata metadata) throws PhotoProcessException {
                System.out.println("\n" + uri + "\n=================================================");

                for (Directory directory : metadata.getDirectories()) {
                    System.out.println("\n> " + directory);

                    for (Tag tag : directory.getTags()) {
                        System.out.println(tag);
                    }

                    // XMP is not handled like other metadata
                    // From XmpDirectory class:
                    // "XMP uses a namespace and path format for identifying values, which does not map to
                    // metadata-extractor's
                    // integer based tag identifiers. Therefore, XMP data is extracted and exposed via {@link
                    // XmpDirectory#getXMPMeta()}
                    // which returns an instance of Adobe's {@link XMPMeta} which exposes the full XMP data set."
                    if (directory instanceof XmpDirectory) {
                        XmpDirectory xmpDir = (XmpDirectory) directory;
                        System.out.println("Dump XMP metadata:\n" + xmpDir.getXMPMeta().dumpObject());
                    }
                }
            }
        });

        assertEquals(28, res.getProcessed_ok());
        assertEquals(3, res.getSkipped());
        assertEquals(0, res.getProcessed_nok());
        System.out.println("\n====\nResult=" + res.toString());
    }

    @Test
    void getTinyWorldPhotoMetadata() {
        // Using 1 to indicate that only first level should be visited.
        Result res = Extract.exploreFS("src/test/resources/photos", 1, new IPhotoProcess() {

            @Override
            public void task(URI uri, FileType fileType, Metadata metadata) throws PhotoProcessException {
                System.out.println("\n" + uri + "\n=================================================");

                try {
                    PhotoObject photo = new PhotoObject();
                    System.out.println(photo.extractMetadata(uri, fileType, metadata).getMetadataAsJson());
                } catch (IOException | ParseException e1) {
                    e1.printStackTrace();
                    throw new PhotoProcessException(e1);
                }
            }
        });

        assertEquals(isImageMagickInPath ? 15 : 13, res.getProcessed_ok());
        assertEquals(2, res.getSkipped());
        assertEquals(isImageMagickInPath ? 0 : 2, res.getProcessed_nok());
        System.out.println("\n====\nResult=" + res.toString());
    }

    @Test
    void getTinyWorldPhotoMetadataWithDefaults() {
        PhotoMetadata defaultMetadata = new PhotoMetadata();
        defaultMetadata.setCountry("France").setCountryCode("FRA").setGpsLatLong("48.85,2.35");

        // Using 1 to indicate that only first level should be visited.
        Result res = Extract.exploreFS("src/test/resources/photos/level1", 1, new IPhotoProcess() {

            @Override
            public void task(URI uri, FileType fileType, Metadata metadata) throws PhotoProcessException {
                System.out.println("\n" + uri + "\n=================================================");

                try {
                    PhotoObject photo = new PhotoObject(defaultMetadata); // Provide default metadata to be used if not
                                                                          // found in photos
                    System.out.println(photo.extractMetadata(uri, fileType, metadata).getMetadataAsJson());
                } catch (IOException | ParseException e1) {
                    e1.printStackTrace();
                    throw new PhotoProcessException(e1);
                }
            }
        });

        assertEquals(6, res.getProcessed_ok());
        assertEquals(0, res.getSkipped());
        assertEquals(0, res.getProcessed_nok());
        System.out.println("\n====\nResult=" + res.toString());
    }

    @Test
    void getPhotoThumbnails() {
        // Using MAX_VALUE to indicate that all levels should be visited.
        Result res = Extract.exploreFS("src/test/resources/photos", Integer.MAX_VALUE, new IPhotoProcess() {

            @Override
            public void task(URI uri, FileType fileType, Metadata metadata) throws PhotoProcessException {
                System.out.println("\n" + uri + "\n=================================================");

                try {
                    PhotoObject photo = new PhotoObject();
                    photo.extractThumbnail(uri, fileType, metadata, "target/test-classes");
                    System.out.println("Done");
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new PhotoProcessException(e);
                }
            }
        });

        assertEquals(isImageMagickInPath ? 28 : 26, res.getProcessed_ok());
        assertEquals(3, res.getSkipped());
        assertEquals(isImageMagickInPath ? 0 : 2, res.getProcessed_nok());
        System.out.println("\n====\nResult=" + res.toString());
    }

}
