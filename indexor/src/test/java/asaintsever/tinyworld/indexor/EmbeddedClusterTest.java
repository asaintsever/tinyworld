package asaintsever.tinyworld.indexor;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.opensearch.node.Node;
import org.opensearch.node.NodeValidationException;


public class EmbeddedClusterTest {

    @Test
    void startStopSingleNodeCluster() {
        // Node implements Closeable interface: close() method will be called (internally invoke stop() method)
        try (Node node = new EmbeddedCluster().setPathHome("target/index").create(true)) {

            node.start();
            
            // Pause
            Thread.sleep(8000);
        } catch (NodeValidationException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
