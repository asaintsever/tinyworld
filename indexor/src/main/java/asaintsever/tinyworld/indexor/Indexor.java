package asaintsever.tinyworld.indexor;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.indexor.opensearch.Cluster;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;
import asaintsever.tinyworld.indexor.opensearch.ClusterClient;
import asaintsever.tinyworld.indexor.opensearch.Document;
import asaintsever.tinyworld.indexor.opensearch.DocumentAlreadyExistsException;
import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;


public class Indexor implements Closeable {
    protected static Logger logger = LoggerFactory.getLogger(Indexor.class);
    
    public final static String DEFAULT_HOST = "localhost";  // Use local cluster by default (TinyWorld's embedded or external local one)
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
    
    // Default for TinyWorld's date format, mapping and storage path. Can be modified using static setters.
    private static String CLUSTER_PATH_HOME = "index";
    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String MAPPING = ""
            + "{\n"
            + " \"properties\": {\n"
            + "   \"path\": {\n"
            + "     \"type\": \"text\"\n"
            + "   },\n"
            + "   \"fileName\": {\n"
            + "     \"type\": \"text\"\n"
            + "   },\n"
            + "   \"sizeMb\": {\n"
            + "     \"type\": \"float\"\n"
            + "   },\n"
            + "   \"takenDate\": {\n"
            + "     \"type\": \"date\",\n"
            + "     \"format\": \"yyyy-MM-dd HH:mm:ss\"\n"
            + "   },\n"
            + "   \"timeZoneOffset\": {\n"
            + "     \"type\": \"text\"\n"
            + "   },\n"
            + "   \"thumbnail\": {\n"
            + "     \"type\": \"text\"\n"
            + "   },\n"
            + "   \"camModelMake\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"pixelRes\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"countryCode\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"country\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"stateOrProvince\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"city\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"sublocation\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"caption\": {\n"
            + "     \"type\": \"text\"\n"
            + "   },\n"
            + "   \"title\": {\n"
            + "     \"type\": \"text\"\n"
            + "   },\n"
            + "   \"headline\": {\n"
            + "     \"type\": \"text\"\n"
            + "   },\n"
            + "   \"gpsDatum\": {\n"
            + "     \"type\": \"text\",\n"
            + "     \"fields\": {\"keyword\": {\"type\": \"keyword\"}}\n"
            + "   },\n"
            + "   \"gpsLatLong\": {\n"
            + "     \"type\": \"geo_point\"\n"
            + "   }\n"
            + " }\n"
            + "}";
    
        
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
        this(indexorCfg.cluster.address, indexorCfg.cluster.port, indexorCfg.cluster.index, indexorCfg.cluster.embedded.enabled, indexorCfg.cluster.embedded.expose);
        this.indexorCfg = indexorCfg;
    }
    
    public Indexor(String host, int port, String index, boolean useEmbeddedCluster, boolean exposeEmbeddedCluster) throws Exception {
        this.host = host;
        this.port = port;
        this.index = index; 
        this.useEmbeddedCluster = useEmbeddedCluster;
        
        if(this.useEmbeddedCluster) {
            try {
                this.embeddedCluster = new Cluster().setHttpPort(port).setPathHome(CLUSTER_PATH_HOME).create(exposeEmbeddedCluster);
            } catch (ClusterNodeException e) {
                logger.error("Fail to create and start embedded cluster: " + e.getMessage());
                throw e;
            }
        }
        
        this.clusterClient = new ClusterClient(this.host, this.port);
        this.mtdIndx = new MetadataIndex().setConnection(this.clusterClient, this.index);
        this.photos = new Photo().setConnection(this.clusterClient, this.index);
    }
    
    
    public PhotoMetadata getDefaultMetadata() {
        return (this.indexorCfg != null && this.indexorCfg.photo != null) ? this.indexorCfg.photo.defaultMetadata : null;
    }
    
    public boolean isConnected() throws IOException {
        return this.clusterClient.isStarted();
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
        
        if(this.useEmbeddedCluster) {
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
            String id = DigestUtils.sha256Hex(photo.path.toString());
            
            try {
                return this.document.add(id, photo, allowUpdate);
            } catch(DocumentAlreadyExistsException e) {
                String msg = "Photo [id=" + id + ", path=" + photo.path + "] already exists in index " + this.document.getIndex();
                logger.error(msg);
                throw new IOException(msg, e);
            }
        }
        
        @Override
        public PhotoMetadata get(String id, Class<PhotoMetadata> docClass) throws IOException {
            return this.document.get(id, docClass);
        }

        @Override
        public long count() throws IOException {
            return this.document.count();
        }

        @Override
        public IndexPage<PhotoMetadata> search(String query, int from, int size, Class<PhotoMetadata> docClass) throws IOException {
            return this.document.search(query, from, size, docClass);
        }

        @Override
        public IndexPage<PhotoMetadata> next(IndexPage<PhotoMetadata> page, Class<PhotoMetadata> docClass) throws IOException {
            return this.document.next(page, docClass);
        }

        @Override
        public void close() throws IOException {
            if(this.document != null) this.document.close();
        }
    }
}
