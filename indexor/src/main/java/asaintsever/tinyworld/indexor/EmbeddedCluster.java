package asaintsever.tinyworld.indexor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.Settings.Builder;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.plugins.Plugin;
import org.opensearch.transport.Netty4Plugin;


public class EmbeddedCluster {
    
    private class EmbeddedClusterNode extends Node {
        public EmbeddedClusterNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, new HashMap<String, String>(), null, null), classpathPlugins, true);
        }
    }
    
    private final String clusterName = "tinyworld";
    private final String nodeName = "photos";
    private final String transportType = "netty4";
    private final String httpType = "netty4";
    private final String networkHost = "_local_";   // will listen on localhost
    
    private String pathHome = "index";              // location for index storage
    
    
    public EmbeddedCluster() {}

    public EmbeddedCluster setPathHome(String pathHome) {
        this.pathHome = pathHome;
        return this;
    }


    public Node create(boolean expose) {
        Builder settingsBuilder = Settings.builder()
                                        .put("cluster.name", this.clusterName)
                                        .put("node.name", this.nodeName)
                                        .put("path.home", this.pathHome)       
                                        .put("transport.type", this.transportType)
                                        .put("http.type", this.httpType)
                                        .put("network.host", this.networkHost);
        
        // Enable external access (useful for tests or UI tools like https://github.com/cars10/elasticvue)
        if (expose) {
            settingsBuilder
                .put("network.host", this.networkHost + ", _site_") // will also bind IP address (https://opensearch.org/docs/latest/opensearch/cluster/#step-3-bind-a-cluster-to-specific-ip-addresses)
                .put("http.cors.enabled", "true")
                .put("http.cors.allow-origin", "*");
        }
        
        return new EmbeddedClusterNode(settingsBuilder.build(), Arrays.asList(Netty4Plugin.class));
    }
}
