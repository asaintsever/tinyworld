package asaintsever.tinyworld.ui.component;

import asaintsever.tinyworld.cfg.Configuration;


public class StatusBar extends gov.nasa.worldwind.util.StatusBar {

    protected IndexorStatusPanel indexorStatusPanel;

    
    public StatusBar(Configuration cfg) {
        super();
        
        // Remove elevation info in status bar (no need since we disabled elevation retrieval)
        this.remove(this.eleDisplay);
        
        indexorStatusPanel = new IndexorStatusPanel(cfg.indexor);
        this.add(indexorStatusPanel);
    }
    
    
    public IndexorStatusPanel getIndexorStatusPanel() {
        return this.indexorStatusPanel;
    }
}
