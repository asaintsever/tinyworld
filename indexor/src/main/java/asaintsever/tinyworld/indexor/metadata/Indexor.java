package asaintsever.tinyworld.indexor.metadata;

import java.io.Closeable;
import java.io.IOException;

import asaintsever.tinyworld.indexor.opensearch.ClusterClient;
import asaintsever.tinyworld.indexor.opensearch.Document;
import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;

public class Indexor implements Closeable {
    
    public final static String DEFAULT_HOST = "localhost";  // Use embedded cluster by default
    public final static int DEFAULT_PORT = 9200; 
    public final static String DEFAULT_INDEX = "photos";
    
    private String host;
    private int port;
    private String index;
    private ClusterClient clusterClient;
    
    private MetadataIndex mtdIndx;
    private Photo photos;

    
    public Indexor() {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_INDEX);
    }
    
    public Indexor(String host, int port, String index) {
        this.host = host;
        this.port = port;
        this.index = index;
        
        this.clusterClient = new ClusterClient(this.host, this.port);
        this.mtdIndx = new MetadataIndex().setClient(this.clusterClient).setIndex(this.index);
        this.photos = new Photo().setClient(this.clusterClient).setIndex(this.index);
    }
    
    
    public void reset() throws IOException {
        close();
        
        this.clusterClient = new ClusterClient(this.host, this.port);
        this.mtdIndx.setClient(this.clusterClient);
        this.photos.setClient(this.clusterClient);
    }
    
    @Override
    public void close() throws IOException {
        this.clusterClient.close();
    }
    
    public IIndex metadataIndex() {
        return this.mtdIndx;
    }
    
    public IPhoto photos() {
        return this.photos;
    }
    
        
    private class MetadataIndex implements IIndex {
        private String index;
        private ClusterClient clusterClient;
                
        public MetadataIndex setClient(ClusterClient clusterClient) {
            this.clusterClient = clusterClient;
            return this;
        }
        
        public MetadataIndex setIndex(String index) {
            this.index = index;
            return this;
        }
        
        @Override
        public Boolean create() throws IOException {
            return this.clusterClient.createIndex(this.index);
        }
        
        @Override
        public Boolean clear() throws IOException {       
            if(this.clusterClient.deleteIndex(this.index)) {
                return this.clusterClient.createIndex(this.index);
            } else {
                return false;
            }
        }
    }
    
    private class Photo implements IPhoto {
        private Document<PhotoMetadata> document;
        
        
        public Photo() {
            this.document = new Document<>();
        }
        
        public Photo setClient(ClusterClient clusterClient) {
            this.document.setClient(clusterClient.getClient());
            return this;
        }
        
        public Photo setIndex(String index) {
            this.document.setIndex(index);
            return this;
        }
        
        @Override
        public void add(PhotoMetadata photo) throws IOException {
            this.document.add(photo);
        }
    }
}
