package asaintsever.tinyworld.ui.globe;

import gov.nasa.worldwind.WorldWindow;

public class LayerPanel extends gov.nasa.worldwindx.examples.LayerPanel {

    public LayerPanel(WorldWindow wwd) {
        super(wwd);
        
        // Remove elevation on/off checkbox from panel (no need since we disabled elevation retrieval)
        this.remove(this.elevationModelManagerPanel);
    }
}
