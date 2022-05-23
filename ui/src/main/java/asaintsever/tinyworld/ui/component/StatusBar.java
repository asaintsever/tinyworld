package asaintsever.tinyworld.ui.component;

public class StatusBar extends gov.nasa.worldwind.util.StatusBar {

    public StatusBar() {
        super();
        
        // Remove elevation info in status bar (no need since we disabled elevation retrieval)
        this.remove(this.eleDisplay);
    }
}
