package asaintsever.tinyworld.ui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ToolTipController;


public class GlobePanel extends JPanel {

    protected WorldWindow wwd;
    protected StatusBar statusBar;
    protected ToolTipController toolTipController;
    protected HighlightController highlightController;

    public GlobePanel(Dimension canvasSize, boolean includeStatusBar) {
        super(new BorderLayout());

        this.wwd = new WorldWindowGLCanvas();
        ((Component) this.wwd).setPreferredSize(canvasSize);
        
        // To work around a Swing bug the WorldWindow must be placed within a JPanel and
        // that JPanel's minimum preferred size must be set to zero (both width and height)
        this.setMinimumSize(new Dimension(0, 0));

        // Create the default model as described in the current worldwind properties.
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);

        // Setup a select listener for the worldmap click-and-go feature
        this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

        this.add((Component) this.wwd, BorderLayout.CENTER);
        if (includeStatusBar) {
            this.statusBar = new StatusBar();
            this.add(statusBar, BorderLayout.PAGE_END);
            this.statusBar.setEventSource(this.wwd);
        }

        // Add controllers to manage highlighting and tool tips.
        this.toolTipController = new ToolTipController(this.wwd, AVKey.DISPLAY_NAME, null);
        this.highlightController = new HighlightController(this.wwd, SelectEvent.ROLLOVER);
    }

    public WorldWindow getWwd() {
        return this.wwd;
    }

    public StatusBar getStatusBar() {
        return this.statusBar;
    }
}
