package asaintsever.tinyworld.cfg;

import java.util.Map;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;

public class Configuration {
    public UI ui;
    public INDEXOR indexor;
    
    @Override
    public String toString() {
        return this.ui.toString() + "," + this.indexor.toString();
    }
    
    
    public class UI {
        public Deps deps;
        
        public class Deps {
            public Map<String, String> logging;
            
            @Override
            public String toString() {
                return "deps[" + this.logging.toString() + "]";
            }
        }
        
        @Override
        public String toString() {
            return "ui[" + this.deps.toString() + "]";
        }
    }
    
    public class INDEXOR {
        public Cluster cluster;
        public Photo photo;
        
        public class Cluster {
            public Embedded embedded;
            public String address;
            public int port;
            
            public class Embedded {
                public boolean enabled;
                public boolean expose;
                
                @Override
                public String toString() {
                    return "embedded[enabled=" + this.enabled + ",expose=" + this.expose + "]";
                }
            }
            
            @Override
            public String toString() {
                return "cluster[" + this.embedded.toString() + ",address=" + this.address + ":" + this.port + "]";
            }
        }
        
        public class Photo {
            public PhotoMetadata defaultMetadata;
            
            @Override
            public String toString() {
                return "photo[" + this.defaultMetadata.toString() + "]";
            }
        }
        
        @Override
        public String toString() {
            return "indexor[" + this.cluster.toString() + "," + this.photo.toString() + "]";
        }
    }
}
