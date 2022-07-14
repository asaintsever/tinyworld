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
package asaintsever.tinyworld.indexor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;


public class IndexorTest {
    
    private static Indexor indexor;
    private static EasyRandom easyRandom;
    
    @BeforeAll
    public static void setup() throws Exception {
        // Change defaults for our tests
        Indexor.setClusterPathHome("target/index");
        
        // Create indexor
        indexor = new Indexor("indexor.test");
    }
    
    @AfterAll
    public static void teardown() throws IOException {
        indexor.close();
    }
    
    @BeforeEach
    void setupTest() {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .seed(new Random().nextLong())
                .dateRange(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 10))
                .randomize(FieldPredicates.named("path"), new UrlGenerator())
                .randomize(FieldPredicates.named("gpsLatLong"), new LatLongGenerator());
        
        easyRandom = new EasyRandom(parameters);
    }
    
    @AfterEach
    void teardownTest() throws IOException {
        indexor.metadataIndex().delete();
    }

    
    @Test
    void createThenClearMetadataIndex() throws IOException {
        assertTrue(indexor.metadataIndex().create());
        assertTrue(indexor.metadataIndex().clear());
    }
    
    @Test
    void resetClientThenCreateMetadataIndex() throws IOException {
        indexor.reset();
        assertTrue(indexor.metadataIndex().create());
    }
    
    @Test
    void addCountGetMetadata() throws IOException, InterruptedException {
        assertTrue(indexor.metadataIndex().create());   // Index must be explicitly created as our mapping is set here
        
        PhotoMetadata mtd = easyRandom.nextObject(PhotoMetadata.class);
        
        String id = indexor.photos().add(mtd, false);
        assertTrue((id != null) && !id.isEmpty());
        
        // Pause before asking # of photos in index
        Thread.sleep(2000);
        assertEquals(indexor.photos().count(), 1);
        
        mtd = indexor.photos().get(id);
        System.out.println("mtd=" + mtd.toString());
    }
    
    @Test
    void insertThenSearchMetadata() throws IOException, InterruptedException {
        assertTrue(indexor.metadataIndex().create());   // Index must be explicitly created as our mapping is set here
        
        for(int i=0;i<10;i++) {
            assertTrue(() -> {
                try {
                    // Insert photo metadata
                    PhotoMetadata mtd = easyRandom.nextObject(PhotoMetadata.class);
                    
                    String id = indexor.photos().add(mtd, false);
                    return ((id != null) && !id.isEmpty());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        
        // Pause before asking # of photos in index
        Thread.sleep(2000);
        assertEquals(indexor.photos().count(), 10);
        
        // Search all photos
        IndexPage<PhotoMetadata> mtdList = indexor.photos().search("{\"simple_query_string\": {\"query\": \"*\"}}", 0, 10);
        System.out.println("Total=" + mtdList.total() + ", Size=" + mtdList.size() + ", Result=" + mtdList.get().toString());
        
        // Search for photos with takenDate date before 2022
        String queryDSL = ""
                + "{\n"
                + " \"range\": {\n"
                + "   \"takenDate\": {\n"
                + "      \"lt\": \"2022-01-01\",\n"
                + "      \"format\": \"yyyy-MM-dd\"\n"
                + "   }\n"
                + " }\n"
                + "}";
        
        mtdList = indexor.photos().search(queryDSL, 0, 5);
        System.out.println("Total=" + mtdList.total() + ", Size=" + mtdList.size() + ", Result=" + mtdList.get().toString());
        
        // Search for photos in south hemisphere
        queryDSL = ""
                + "{\n"
                + " \"geo_bounding_box\": {\n"
                + "   \"gpsLatLong\": {\n"
                + "      \"top_left\": \"0,-180\",\n"
                + "      \"bottom_right\": \"-90,180\"\n"
                + "   }\n"
                + " }\n"
                + "}";
        
        mtdList = indexor.photos().search(queryDSL, 0, 5);
        System.out.println("Total=" + mtdList.total() + ", Size=" + mtdList.size() + ", Result=" + mtdList.get().toString());
        
        // Search for photos within given distance
        queryDSL = ""
                + "{\n"
                + " \"geo_distance\": {\n"
                + "   \"distance\": \"5000km\",\n"
                + "   \"gpsLatLong\": \"48.85,2.35\""
                + " }\n"
                + "}";
        
        mtdList = indexor.photos().search(queryDSL, 0, 5);
        System.out.println("Total=" + mtdList.total() + ", Size=" + mtdList.size() + ", Result=" + mtdList.get().toString());
    }
}
