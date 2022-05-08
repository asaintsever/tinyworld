package asaintsever.tinyworld.cfg;

import java.util.Map;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;
import lombok.ToString;

@ToString
public class Configuration {
    public UI ui;
    public INDEXOR indexor;
    
    @ToString
    public class UI {
        public Deps deps;
        
        @ToString
        public class Deps {
            public Map<String, String> logging;
        }
    }
    
    @ToString
    public class INDEXOR {
        public Cluster cluster;
        public Photo photo;
        
        @ToString
        public class Cluster {
            public Embedded embedded;
            public String address;
            public int port;
            public String index;
            
            @ToString
            public class Embedded {
                public boolean enabled;
                public boolean expose;
            }
        }
        
        @ToString
        public class Photo {
            public PhotoMetadata defaultMetadata;
        }
    }
}
