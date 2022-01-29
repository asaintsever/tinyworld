package asaintsever.tinyworld.ui.cfg;

import java.util.Map;

public class Configuration {

    public class UI {
        public class Deps {
            public Map<String, String> logging;
            
            @Override
            public String toString() {
                return this.logging.toString();
            }
        }
        
        public Deps deps;
        
        @Override
        public String toString() {
            return this.deps.toString();
        }
    }

    
    public UI ui;
    
    @Override
    public String toString() {
        return this.ui.toString();
    }
}
