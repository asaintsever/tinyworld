package asaintsever.tinyworld.indexor.opensearch;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.jeasy.random.api.Randomizer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.ResponseException;
import org.opensearch.rest.RestStatus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import asaintsever.tinyworld.indexor.IndexPage;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;
import asaintsever.tinyworld.metadata.extractor.CustomDateSerializer;
import lombok.ToString;


public class ClusterClientTest {
    
    private static Cluster cluster;
    private static ClusterClient client;
    private static long seed;
    
    @ToString
    static class DocObject {
        public String attr1;
        public String attr2;
        public Float attr3;
        public URL attr4;
        
        @JsonSerialize(using = CustomDateSerializer.class)
        public Date creationDate;
        
        public String latlong;
    }
    
    class LatLongGenerator implements Randomizer<String> {
        private Random random = new Random();

        @Override
        public String getRandomValue() {
            // return random, but valid, "latitude,longitude" as per geo_point string format
            return random.nextDouble(-90.0, 90.0) + "," + random.nextDouble(-180.0, 180.0);
        }
    }
        
    
    @BeforeAll
    public static void setup() throws ClusterNodeException {
        // Create single node cluster
        cluster = new Cluster().setPathHome("target/index").create(true);
        
        // Create client
        client = new ClusterClient("localhost", 9200);
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        client.close();
        cluster.close();
    }
    
    @BeforeEach
    void setupTest() {
        // Get new random seed to init our EasyRandom instance
        seed = new Random().nextLong();
    }
    
    @AfterEach
    void teardownTest() throws IOException {
        client.deleteIndex("*");
    }
    
    
    @Test
    void createThenDeleteIndex() throws IOException {
        assertTrue(client.createIndex("test.index", null));
        assertTrue(client.isIndexExists("test.index"));
        
        assertTrue(client.deleteIndex("test.index"));
        assertFalse(client.isIndexExists("test.index"));
    }
    
    @Test
    void createIndexTwice() throws IOException {
        assertTrue(client.createIndex("test.index", null));
        assertTrue(client.isIndexExists("test.index"));
        
        assertEquals(
                assertThrows(ResponseException.class, () -> {
                    client.createIndex("test.index", null);
                })
                .getResponse().getStatusLine().getStatusCode(), 
                RestStatus.BAD_REQUEST.getStatus());
    }
    
    @Test
    void deleteUnknownIndex() {       
        assertEquals(
                assertThrows(ResponseException.class, () -> {
                    client.deleteIndex("test2");
                })
                .getResponse().getStatusLine().getStatusCode(), 
                RestStatus.NOT_FOUND.getStatus());
    }
    
    @Test
    void insertCountAndGetDocument() throws IOException, InterruptedException {
        assertFalse(client.isIndexExists("test.index"));
                       
        try(Document<DocObject> doc = new Document<>(client)) {
            doc.setIndex("test.index").getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            
            // No mapping provided: creationDate and latlong fields will be stored using text type
            String id = doc.add(new EasyRandom(new EasyRandomParameters().seed(seed)).nextObject(DocObject.class));
            assertTrue((id != null) && !id.isEmpty());
            
            // Index is created automatically if not exist when inserting documents
            assertTrue(client.isIndexExists("test.index"));
            
            // Pause before asking # of doc in index
            Thread.sleep(2000);
            assertEquals(doc.count(), 1);
            
            DocObject docObj = doc.get(id, DocObject.class);
            System.out.println("Document=" + docObj.toString());
        }
    }
    
    @Test
    void insertThenSearchDocuments() throws IOException, InterruptedException {
        assertFalse(client.isIndexExists("test.index"));
        
        // Set explicit mapping so that dates and coordinates are handled the way we want
        String mapping = ""
                + "{\n"
                + " \"properties\": {\n"
                + "   \"attr1\": {\n"
                + "     \"type\": \"text\"\n"
                + "   },\n"
                + "   \"attr2\": {\n"
                + "     \"type\": \"text\"\n"
                + "   },\n"
                + "   \"attr3\": {\n"
                + "     \"type\": \"float\"\n"
                + "   },\n"
                + "   \"attr4\": {\n"
                + "     \"type\": \"text\"\n"
                + "   },\n"
                + "   \"creationDate\": {\n"
                + "     \"type\": \"date\",\n"
                + "     \"format\": \"yyyy-MM-dd HH:mm:ss\"\n"
                + "   },\n"
                + "   \"latlong\": {\n"
                + "     \"type\": \"geo_point\"\n"
                + "   }\n"
                + " }\n"
                + "}";
        
        assertTrue(client.createIndex("test.index", mapping));
        
        try(Document<DocObject> doc = new Document<>(client)) {
            // Set date format for your Document mapper to match defined format for DocObject
            doc.setIndex("test.index").getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            
            EasyRandomParameters parameters = new EasyRandomParameters()
                                                    .seed(seed)
                                                    .dateRange(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 10))
                                                    .randomize(FieldPredicates.named("latlong"), new LatLongGenerator());
            EasyRandom easyRandom = new EasyRandom(parameters);
            
            for(int i=0;i<10;i++) {
                assertTrue(() -> {
                    try {
                        // Insert documents
                        String id = doc.add(easyRandom.nextObject(DocObject.class));
                        return ((id != null) && !id.isEmpty());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            
            // Pause before asking # of doc in index
            Thread.sleep(2000);
            assertEquals(doc.count(), 10);
            
            // Search all documents
            IndexPage<DocObject> docObjList = doc.search("{\"simple_query_string\": {\"query\": \"*\"}}", DocObject.class);
            System.out.println("Total=" + docObjList.total() + ", Size=" + docObjList.size() + ", Result=" + docObjList.get().toString());
            
            // Search for documents with creationDate date before 2022
            String queryDSL = ""
                    + "{\n"
                    + " \"range\": {\n"
                    + "   \"creationDate\": {\n"
                    + "      \"lt\": \"2022-01-01\",\n"
                    + "      \"format\": \"yyyy-MM-dd\"\n"
                    + "   }\n"
                    + " }\n"
                    + "}";
            
            docObjList = doc.search(queryDSL, 0, 5, DocObject.class);
            System.out.println("Total=" + docObjList.total() + ", Size=" + docObjList.size() + ", Result=" + docObjList.get().toString());
            
            // Search for documents in south hemisphere
            queryDSL = ""
                    + "{\n"
                    + " \"geo_bounding_box\": {\n"
                    + "   \"latlong\": {\n"
                    + "      \"top_left\": \"0,-180\",\n"
                    + "      \"bottom_right\": \"-90,180\"\n"
                    + "   }\n"
                    + " }\n"
                    + "}";
            
            docObjList = doc.search(queryDSL, 0, 5, DocObject.class);
            System.out.println("Total=" + docObjList.total() + ", Size=" + docObjList.size() + ", Result=" + docObjList.get().toString());
            
            // Search for documents within given distance
            queryDSL = ""
                    + "{\n"
                    + " \"geo_distance\": {\n"
                    + "   \"distance\": \"5000km\",\n"
                    + "   \"latlong\": \"48.85,2.35\""
                    + " }\n"
                    + "}";
            
            docObjList = doc.search(queryDSL, 0, 5, DocObject.class);
            System.out.println("Total=" + docObjList.total() + ", Size=" + docObjList.size() + ", Result=" + docObjList.get().toString());
        }
    }
}
