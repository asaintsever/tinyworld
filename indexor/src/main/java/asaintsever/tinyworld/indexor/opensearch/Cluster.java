package asaintsever.tinyworld.indexor.opensearch;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.Settings.Builder;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.node.NodeValidationException;
import org.opensearch.plugins.Plugin;
import org.opensearch.transport.Netty4Plugin;


public class Cluster implements Closeable {
    
    public class ClusterNode extends Node {
        public ClusterNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, new HashMap<String, String>(), null, null), classpathPlugins, true);
        }
    }
    
    public class ClusterNodeException extends Exception {
        public ClusterNodeException(Exception e) {
            super(e);
        }
    }
    
    private final String DEFAULT_PATH_HOME = "index";
    private final int DEFAULT_PORT = 9200;
    
    private final String clusterName = "tinyworld";
    private final String nodeName = "node";
    private final String transportType = "netty4";
    private final String httpType = "netty4";
    private final String networkHost = "_local_";   // will listen on localhost
    
    private String pathHome;    // location for index storage
    private int httpPort;
    private ClusterNode node;
    
    
    public Cluster() {
        this.pathHome = DEFAULT_PATH_HOME;
        this.httpPort = DEFAULT_PORT;
    }

    
    public Cluster setPathHome(String pathHome) {
        this.pathHome = pathHome;
        return this;
    }
    
    public Cluster setHttpPort(int port) {
        this.httpPort = port;
        return this;
    }

    public Cluster create(boolean expose) throws ClusterNodeException {
        Builder settingsBuilder = Settings.builder()
                                        .put("cluster.name", this.clusterName)
                                        .put("node.name", this.nodeName)
                                        .put("path.home", this.pathHome)       
                                        .put("transport.type", this.transportType)
                                        .put("http.type", this.httpType)
                                        .put("http.port", this.httpPort)
                                        .put("network.host", this.networkHost);
        
        // Enable external access (useful for tests or UI tools like https://github.com/cars10/elasticvue)
        if (expose) {
            settingsBuilder
                .put("network.host", this.networkHost + ", _site_") // will also bind IP address (https://opensearch.org/docs/latest/opensearch/cluster/#step-3-bind-a-cluster-to-specific-ip-addresses)
                .put("http.cors.enabled", "true")
                .put("http.cors.allow-origin", "*");
        }
        
        try {
            // Create and start node all at once
            this.node = new ClusterNode(settingsBuilder.build(), Arrays.asList(Netty4Plugin.class));
            this.node.start();
        } catch (NodeValidationException e) {
            throw new ClusterNodeException(e);
        }
        
        return this;
    }

    @Override
    public void close() throws IOException {
        if (this.node != null) this.node.close();
        this.node = null;
    }
}
