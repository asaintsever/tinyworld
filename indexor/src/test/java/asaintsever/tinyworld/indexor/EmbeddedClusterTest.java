package asaintsever.tinyworld.indexor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.opensearch.common.settings.Settings;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.node.NodeValidationException;
import org.opensearch.plugins.Plugin;
import org.opensearch.transport.Netty4Plugin;

public class EmbeddedClusterTest {
    
    private static class MyNode extends Node {
        public MyNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, new HashMap<String, String>(), null, null), classpathPlugins, true);
        }
    }

    @Test
    void startStopSingleNodeCluster() {
        // Node implements Closeable interface: close() method will be called (internally invoke stop() method)
        try (Node node = new MyNode(
                        Settings.builder()
                                .put("transport.type", "netty4")
                                .put("http.type", "netty4")
                                .put("path.home", "target/index")
                                .put("node.name", "test-node")
                                .build(),
                        Arrays.asList(Netty4Plugin.class))) {

            node.start();
            
            // Pause
            Thread.sleep(8000);
        } catch (NodeValidationException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
