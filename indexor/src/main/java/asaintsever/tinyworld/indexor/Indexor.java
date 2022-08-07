/*
 * Copyright 2021-2022 A. Saint-Sever
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
package asaintsever.tinyworld.indexor;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.cfg.utils.Utils;
import asaintsever.tinyworld.indexor.opensearch.Cluster;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;
import asaintsever.tinyworld.indexor.opensearch.ClusterClient;
import asaintsever.tinyworld.indexor.opensearch.Document;
import asaintsever.tinyworld.indexor.opensearch.DocumentAlreadyExistsException;
import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;

public class Indexor implements Closeable {
    protected static Logger logger = LoggerFactory.getLogger(Indexor.class);

    public final static String DEFAULT_HOST = "localhost"; // Use local cluster by default (TinyWorld's embedded or
                                                           // external local one)
    public final static int DEFAULT_PORT = 9200;

    private Configuration.INDEXOR indexorCfg;
    private String host;
    private int port;
    private String index;
    private boolean useEmbeddedCluster;

    private ClusterClient clusterClient;
    private Cluster embeddedCluster;

    private MetadataIndex mtdIndx;
    private Photo photos;

    private final static String DEFAULT_MAPPING = "mapping/tinyworld_photo.json";
    private final static String[] SEARCH_TEMPLATES = { "search_templates/country_year_month.json",
            "search_templates/year_country_month.json", "search_templates/year_month.json" };

    // Default for TinyWorld's date format, mapping and storage path. Can be modified using static
    // setters.
    private static String CLUSTER_PATH_HOME = "index";
    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String MAPPING;

    static {
        // Load default mapping from internal resource
        try {
            MAPPING = new String(Utils.getInternalResource(DEFAULT_MAPPING));
        } catch (IOException | URISyntaxException e) {
            logger.error("Fail to load internal mapping", e);
        }
    }

    public static void setDateFormat(String format) {
        DATE_FORMAT = format;
    }

    public static void setMapping(String mapping) {
        MAPPING = mapping;
    }

    public static void setClusterPathHome(String path) {
        CLUSTER_PATH_HOME = path;
    }

    // Using defaults
    public Indexor(String index) throws Exception {
        this(DEFAULT_HOST, DEFAULT_PORT, index, true, false);
    }

    // From config file
    public Indexor(Configuration.INDEXOR indexorCfg) throws Exception {
        this(indexorCfg.cluster.address, indexorCfg.cluster.port, indexorCfg.cluster.index,
                indexorCfg.cluster.embedded.enabled, indexorCfg.cluster.embedded.expose);
        this.indexorCfg = indexorCfg;
    }

    @SuppressWarnings("resource")
    public Indexor(String host, int port, String index, boolean useEmbeddedCluster, boolean exposeEmbeddedCluster)
            throws Exception {
        this.host = host;
        this.port = port;
        this.index = index;
        this.useEmbeddedCluster = useEmbeddedCluster;

        if (this.useEmbeddedCluster) {
            try {
                this.embeddedCluster = new Cluster().setHttpPort(port).setPathHome(CLUSTER_PATH_HOME)
                        .create(exposeEmbeddedCluster);
            } catch (ClusterNodeException e) {
                logger.error("Fail to create and start embedded cluster: " + e.getMessage());
                throw e;
            }
        }

        this.clusterClient = new ClusterClient(this.host, this.port);
        this.mtdIndx = new MetadataIndex().setConnection(this.clusterClient, this.index);
        this.photos = new Photo().setConnection(this.clusterClient, this.index);

        // TODO call this.clusterClient.loadSearchTemplate() to load all search templates into cluster
    }

    public PhotoMetadata getDefaultMetadata() {
        return (this.indexorCfg != null && this.indexorCfg.photo != null) ? this.indexorCfg.photo.defaultMetadata
                : null;
    }

    public boolean isConnected() {
        return this.clusterClient.isConnected();
    }

    public void reset() throws IOException {
        this.photos.close();
        this.clusterClient.close();

        this.clusterClient = new ClusterClient(this.host, this.port);

        this.mtdIndx.setConnection(this.clusterClient, this.index);
        this.photos.setConnection(this.clusterClient, this.index);
    }

    @Override
    public void close() throws IOException {
        this.photos.close();
        this.clusterClient.close();
        this.clusterClient = null;

        if (this.useEmbeddedCluster) {
            this.embeddedCluster.close();
        }
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getIndex() {
        return this.index;
    }

    public IIndex metadataIndex() {
        return this.mtdIndx;
    }

    public IPhoto photos() {
        return this.photos;
    }

    private class MetadataIndex implements IIndex {
        private ClusterClient clusterClient;
        private String index;

        public MetadataIndex setConnection(ClusterClient clusterClient, String index) {
            this.clusterClient = clusterClient;
            this.index = index;
            return this;
        }

        @Override
        public Boolean create() throws IOException {
            return this.clusterClient.createIndex(this.index, MAPPING);
        }

        @Override
        public Boolean exists() throws IOException {
            return this.clusterClient.isIndexExists(this.index);
        }

        @Override
        public Boolean delete() throws IOException {
            return this.clusterClient.deleteIndex(this.index);
        }

        @Override
        public Boolean clear() throws IOException {
            try {
                this.delete();
            } catch (IOException e) {
                // Warning in case clear is invoked and index does not exist
                logger.warn("Fail to delete index " + this.index + ": " + e.getMessage());
            }

            return this.create();
        }
    }

    private class Photo implements IPhoto, Closeable {
        private Document<PhotoMetadata> document;

        public Photo setConnection(ClusterClient clusterClient, String index) {
            this.document = new Document<>(clusterClient);

            // Set index and date format
            this.document.setIndex(index).getMapper().setDateFormat(new SimpleDateFormat(DATE_FORMAT));
            return this;
        }

        @Override
        public String add(PhotoMetadata photo, boolean allowUpdate) throws IOException {
            // Compute unique photo metadata id from path
            String id = DigestUtils.sha256Hex(photo.getPath().toString());

            try {
                return this.document.add(id, photo, allowUpdate);
            } catch (DocumentAlreadyExistsException e) {
                String msg = "Photo [id=" + id + ", path=" + photo.getPath() + "] already exists in index "
                        + this.document.getIndex();
                logger.error(msg);
                throw new IOException(msg, e);
            }
        }

        @Override
        public PhotoMetadata get(String id) throws IOException {
            return this.document.get(id, PhotoMetadata.class);
        }

        @Override
        public long count() throws IOException {
            return this.document.count();
        }

        @Override
        public IndexPage<PhotoMetadata> search(String query, int from, int size) throws IOException {
            return this.document.search(query, from, size, PhotoMetadata.class);
        }

        @Override
        public IndexPage<PhotoMetadata> next(IndexPage<PhotoMetadata> page) throws IOException {
            return this.document.next(page, PhotoMetadata.class);
        }

        @Override
        public void close() throws IOException {
            if (this.document != null)
                this.document.close();
        }
    }
}
