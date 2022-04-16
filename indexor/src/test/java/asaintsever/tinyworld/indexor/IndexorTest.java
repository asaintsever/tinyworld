package asaintsever.tinyworld.indexor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;


public class IndexorTest {
    
    private static Indexor indexor;
    
    
    @BeforeAll
    public static void setup() throws Exception {
        // Change defaults for our tests
        Indexor.setClusterPathHome("target/index");
        Indexor.setIndex("indexor.test");
        
        // Create indexor
        indexor = new Indexor();
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        indexor.close();
    }
    
    @AfterEach
    void teardown() throws IOException {
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
    
    /*@Test
    void addMetadataToIndex() throws IOException {
        
        assertEquals(indexor.photos().add(null), "");
    }*/
    
}
