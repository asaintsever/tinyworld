/*
 * Copyright 2021-2024 A. Saint-Sever
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://github.com/asaintsever/tinyworld
 */
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
import org.opensearch.script.mustache.MustachePlugin;
import org.opensearch.transport.Netty4Plugin;

public class Cluster implements Closeable {

    public class ClusterNode extends Node {
        public ClusterNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, new HashMap<String, String>(), null,
                    null), classpathPlugins, true);
        }
    }

    public class ClusterNodeException extends Exception {
        private static final long serialVersionUID = 1L;

        public ClusterNodeException(Exception e) {
            super(e);
        }
    }

    private String pathHome; // location for index storage
    private String host;
    private int httpPort;
    private ClusterNode node;

    public Cluster() {
        // will listen on localhost
        this("index", "_local_", 9200);
    }

    public Cluster(String pathHome, String host, int port) {
        this.pathHome = pathHome;
        this.host = host;
        this.httpPort = port;
    }

    public Cluster setPathHome(String pathHome) {
        this.pathHome = pathHome;
        return this;
    }

    public Cluster setHost(String host) {
        this.host = host;
        return this;
    }

    public Cluster setHttpPort(int port) {
        this.httpPort = port;
        return this;
    }

    public Cluster create(boolean expose) throws ClusterNodeException {
        String clusterName = "tinyworld";
        String nodeName = "node";
        String transportType = "netty4";
        String httpType = "netty4";

        Builder settingsBuilder = Settings.builder().put("cluster.name", clusterName).put("node.name", nodeName)
                .put("path.home", this.pathHome).put("transport.type", transportType).put("http.type", httpType)
                .put("http.port", this.httpPort).put("network.host", this.host);

        // Enable CORS
        if (expose)
            settingsBuilder.put("http.cors.enabled", "true").put("http.cors.allow-origin", "*");

        try {
            // Create and start node all at once
            this.node = new ClusterNode(settingsBuilder.build(), Arrays.asList(Netty4Plugin.class, // Netty plugin for
                                                                                                   // transport
                    MustachePlugin.class // Mustache plugin as supported language in Search Template scripts
            ));
            this.node.start();
        } catch (NodeValidationException e) {
            throw new ClusterNodeException(e);
        }

        return this;
    }

    @Override
    public void close() throws IOException {
        if (this.node != null)
            this.node.close();
        this.node = null;
    }
}
