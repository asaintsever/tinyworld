package asaintsever.tinyworld.indexor.opensearch;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.client.ResponseException;
import org.opensearch.rest.RestStatus;

import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNode;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;

public class ClusterClientTest {
    
    private static ClusterNode node;
    private static ClusterClient client;
    
    
    @BeforeAll
    public static void setup() throws ClusterNodeException {
        // Create single node cluster
        node = new Cluster().setPathHome("target/index").create(false);
        
        // Create client
        client = new ClusterClient("localhost", 9200);
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        client.close();
        node.close();
    }
    
    @AfterEach
    void teardown() throws IOException {
        client.deleteIndex("*");
    }
    
    
    @Test
    void createThenDeleteIndex() throws IOException {
        assertTrue(client.createIndex("test.index"));
        assertTrue(client.isIndexExists("test.index"));
        
        assertTrue(client.deleteIndex("test.index"));
        assertFalse(client.isIndexExists("test.index"));
    }
    
    @Test
    void createIndexTwice() throws IOException {
        assertTrue(client.createIndex("test.index"));
        assertTrue(client.isIndexExists("test.index"));
        
        assertEquals(
                assertThrows(ResponseException.class, () -> {
                    client.createIndex("test.index");
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
}
