package asaintsever.tinyworld.ui.cfg;

import java.util.Map;

public class Configuration {
    public UI ui;
    public INDEXOR indexor;
    
    @Override
    public String toString() {
        return "ui[" + this.ui.toString() + "],indexor[" + this.indexor.toString() + "]";
    }
    
    
    public class UI {
        public Deps deps;
        
        public class Deps {
            public Map<String, String> logging;
            
            @Override
            public String toString() {
                return "deps=" + this.logging.toString();
            }
        }
        
        @Override
        public String toString() {
            return this.deps.toString();
        }
    }
    
    public class INDEXOR {
        public Cluster cluster;
        
        public class Cluster {
            public Embedded embedded;
            public String address;
            public int port;
            
            public class Embedded {
                public boolean enabled;
                public boolean expose;
                
                @Override
                public String toString() {
                    return "enabled=" + this.enabled + ",expose=" + this.expose;
                }
            }
            
            @Override
            public String toString() {
                return "embedded[" + this.embedded.toString() + "],address=" + this.address + ":" + this.port;
            }
        }
        
        @Override
        public String toString() {
            return "cluster[" + this.cluster.toString() + "]";
        }
    }
}
