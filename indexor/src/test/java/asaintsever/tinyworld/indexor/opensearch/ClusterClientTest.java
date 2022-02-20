package asaintsever.tinyworld.indexor.opensearch;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.client.ResponseException;
import org.opensearch.node.Node;
import org.opensearch.node.NodeValidationException;
import org.opensearch.rest.RestStatus;

public class ClusterClientTest {
    
    private static Node node;
    private static ClusterClient client;
    
    
    @BeforeAll
    public static void setup() throws NodeValidationException, InterruptedException {
        // Create single node cluster
        node = new EmbeddedCluster().setPathHome("target/index").create(false).start();

        // Pause
        Thread.sleep(4000);
        
        // Create client
        client = new ClusterClient("localhost", 9200);
    }
    
    @Test
    void createThenDeleteIndex() throws IOException {
        client.createIndex("test.index");
        assertTrue(client.isIndexExists("test.index"));
        
        client.deleteIndex("test.index");
        assertFalse(client.isIndexExists("test.index"));
    }
    
    @Test
    void createIndexTwice() throws IOException {
        client.createIndex("test.index");
        assertTrue(client.isIndexExists("test.index"));
        
        ResponseException ex = assertThrows(ResponseException.class, () -> {
            client.createIndex("test.index");
        });
        
        assertEquals(ex.getResponse().getStatusLine().getStatusCode(), RestStatus.BAD_REQUEST.getStatus());
    }
    
    @Test
    void deleteUnknownIndex() throws IOException {
        ResponseException ex = assertThrows(ResponseException.class, () -> {
            client.deleteIndex("test2");
        });
        
        assertEquals(ex.getResponse().getStatusLine().getStatusCode(), RestStatus.NOT_FOUND.getStatus());
    }
    
    @AfterEach
    void teardown() throws IOException {
        client.deleteIndex("*");
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        client.close();
        node.close();
    }
}
