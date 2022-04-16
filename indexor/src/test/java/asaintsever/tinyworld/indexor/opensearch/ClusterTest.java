package asaintsever.tinyworld.indexor.opensearch;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;


public class ClusterTest {
    
    @Test
    void startStopSingleNodeCluster() {
        // Cluster implements Closeable interface: close() method will be called
        try (Cluster cluster = new Cluster().setPathHome("target/index").create(true)) {

            // Pause
            Thread.sleep(8000);
        } catch (ClusterNodeException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
