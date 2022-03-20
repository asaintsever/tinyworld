package asaintsever.tinyworld.indexor;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.indexor.opensearch.Cluster;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNode;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;
import asaintsever.tinyworld.indexor.opensearch.ClusterClient;
import asaintsever.tinyworld.indexor.opensearch.Document;
import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;


public class Indexor implements Closeable {
    protected static Logger logger = LoggerFactory.getLogger(Indexor.class);
    
    public final static String DEFAULT_HOST = "localhost";  // Use local cluster by default (TinyWorld's embedded or external one)
    public final static int DEFAULT_PORT = 9200;
    
    private String host;
    private int port;
    private boolean useEmbeddedCluster;
    
    private ClusterClient clusterClient;
    private ClusterNode embeddedClusterNode;
    
    private MetadataIndex mtdIndx;
    private Photo photos;
    
    // Default for TinyWorld's index name, mapping and storage path. Can be modified using static setters.
    private static String INDEX = "photos";
    private static String CLUSTER_PATH_HOME = "index";
    private static String MAPPING = null; //TODO
    
    
    public static void setIndex(String index) {
        INDEX = index;
    }
    
    public static void setMapping(String mapping) {
        MAPPING = mapping;
    }
    
    public static void setClusterPathHome(String path) {
        CLUSTER_PATH_HOME = path;
    }

    
    public Indexor() throws Exception {
        this(DEFAULT_HOST, DEFAULT_PORT, true, false);
    }
    
    public Indexor(String host, int port, boolean useEmbeddedCluster, boolean exposeEmbeddedCluster) throws Exception {
        this.host = host;
        this.port = port;
        this.useEmbeddedCluster = useEmbeddedCluster;
        
        if(this.useEmbeddedCluster) {
            try {
                this.embeddedClusterNode = new Cluster().setPathHome(CLUSTER_PATH_HOME).create(exposeEmbeddedCluster);
            } catch (ClusterNodeException e) {
                logger.error("Fail to create and start embedded cluster: " + e.getMessage());
                throw e;
            }
        }
        
        this.clusterClient = new ClusterClient(this.host, this.port);
        this.mtdIndx = new MetadataIndex().setClient(this.clusterClient);
        this.photos = new Photo().setClient(this.clusterClient);
    }
    
    
    public void reset() throws IOException {
        this.photos.getDocument().close();
        this.clusterClient.close();
        
        this.clusterClient = new ClusterClient(this.host, this.port);
        
        this.mtdIndx.setClient(this.clusterClient);
        this.photos.setClient(this.clusterClient);
    }
    
    @Override
    public void close() throws IOException {
        this.photos.getDocument().close();
        this.clusterClient.close();
        this.clusterClient = null;
        
        if(this.useEmbeddedCluster) {
            this.embeddedClusterNode.close();
        }
    }
    
    public IIndex metadataIndex() {
        return this.mtdIndx;
    }
    
    public IPhoto photos() {
        return this.photos;
    }
    
        
    private class MetadataIndex implements IIndex {
        private ClusterClient clusterClient;
                
        public MetadataIndex setClient(ClusterClient clusterClient) {
            this.clusterClient = clusterClient;
            return this;
        }       
        
        @Override
        public Boolean create() throws IOException {
            return this.clusterClient.createIndex(INDEX, MAPPING);
        }
        
        @Override
        public Boolean delete() throws IOException {
            return this.clusterClient.deleteIndex(INDEX);
        }
        
        @Override
        public Boolean clear() throws IOException {
            try {
                this.clusterClient.deleteIndex(INDEX);
            } catch (IOException e) {
                // Warning only in case clear is invoked before create
                logger.warn("Fail to delete index " + INDEX + ": " + e.getMessage());
            }
            
            return this.clusterClient.createIndex(INDEX, MAPPING);
        }
    }
    
    private class Photo implements IPhoto {
        private Document<PhotoMetadata> document;
        
        public Photo setClient(ClusterClient clusterClient) {
            this.document = new Document<>(clusterClient);
            this.document.setIndex(INDEX);
            return this;
        }        
        
        public Document<PhotoMetadata> getDocument() {
            return this.document;
        }
        
        @Override
        public String add(PhotoMetadata photo) throws IOException {
            return this.document.add(photo);
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
    }
}
