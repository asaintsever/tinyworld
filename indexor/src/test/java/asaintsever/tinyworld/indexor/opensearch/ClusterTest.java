package asaintsever.tinyworld.indexor.opensearch;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNode;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;


public class ClusterTest {
    
    @Test
    void startStopSingleNodeCluster() {
        // ClusterNode implements Closeable interface (via Node inheritance): close() method will be called (internally invoke stop() method)
        try (ClusterNode node = new Cluster().setPathHome("target/index").create(true)) {

            // Pause
            Thread.sleep(8000);
        } catch (ClusterNodeException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
