package asaintsever.tinyworld.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.ui.layer.TinyWorldMenuLayer;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ToolTipController;


public class MainFrame extends JFrame {
    
    protected final static String APP_ICON = "/icon/tinyworldicon.jpg";

    private Dimension canvasSize = new Dimension(1000, 800); // the desired WorldWindow size

    protected GlobePanel wwjPanel;
    protected SettingsPanel settingsPanel;
    protected Logger logger = LoggerFactory.getLogger(MainFrame.class);
    

    public MainFrame() {
        this.initialize(true);
    }

    public MainFrame(Dimension size) {
        this.canvasSize = size;
        this.initialize(true);
    }

    public MainFrame(boolean includeStatusBar) {
        this.initialize(includeStatusBar);
    }
    
    
    public Dimension getCanvasSize() {
        return canvasSize;
    }

    public WorldWindow getWwd() {
        return this.wwjPanel.getWwd();
    }

    public StatusBar getStatusBar() {
        return this.wwjPanel.getStatusBar();
    }

    public SettingsPanel getSettingsPanel() {
        return this.settingsPanel;
    }
    
    public void setToolTipController(ToolTipController controller) {
        if (this.wwjPanel.toolTipController != null) {
            this.wwjPanel.toolTipController.dispose();
        }

        this.wwjPanel.toolTipController = controller;
    }

    public void setHighlightController(HighlightController controller) {
        if (this.wwjPanel.highlightController != null) {
            this.wwjPanel.highlightController.dispose();
        }

        this.wwjPanel.highlightController = controller;
    }
    
    protected void initialize(boolean includeStatusBar) {
        // Create the WorldWindow.
        this.wwjPanel = new GlobePanel(this.canvasSize, includeStatusBar);
        this.wwjPanel.setPreferredSize(canvasSize);
        
        // Register a rendering exception listener that's notified when exceptions occur during rendering.
        this.wwjPanel.getWwd().addRenderingExceptionListener((Throwable t) -> {
            if (t instanceof WWAbsentRequirementException) {
                String message = "Computer does not meet minimum graphics requirements.\n";
                message += "Please install up-to-date graphics driver and try again.\n";
                message += "Reason: " + t.getMessage() + "\n";
                message += "This program will end when you press OK.";

                JOptionPane.showMessageDialog(this, message, "Unable to Start Program", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        });
        
        TinyWorldMenuLayer twMenuLayer = new TinyWorldMenuLayer(this);
        this.insertLayerAtTheEnd(this.wwjPanel.getWwd(), twMenuLayer);
        
        for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers()) {
            // Search the layer list for layers that are also select listeners and register them with the World
            // Window. This enables interactive layers to be included without specific knowledge of them here.
            if (layer instanceof SelectListener) {
                this.wwjPanel.getWwd().addSelectListener((SelectListener) layer);
            }
            
            if (layer instanceof WorldMapLayer) {
                ((WorldMapLayer) layer).setPosition(AVKey.NORTHEAST);
            }
        }

        this.settingsPanel = new SettingsPanel(this);
        this.settingsPanel.setVisible(false);              // Not visible by default (click on 'Settings' button of TW menu to enable panel)
        
        this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
        this.getContentPane().add(this.settingsPanel, BorderLayout.WEST);

        this.pack();

        // Center the application on the screen.
        WWUtil.alignComponent(null, this, AVKey.CENTER);
        this.setResizable(true);
        this.setAppIcon();
        
        // Eliminate elevations by simply setting the globe's elevation model to ZeroElevationModel.
        // Elevation info have also been removed from status bar (see custom StatusBar class) and on/off checkbox from panel (see custom LayerPanel class)
        this.wwjPanel.getWwd().getModel().getGlobe().setElevationModel(new ZeroElevationModel());
    }
    
    protected void setAppIcon() {
        try {
            URL resource = MainFrame.class.getResource(APP_ICON);
            BufferedImage image = ImageIO.read(resource);
            this.setIconImage(image);
        } catch (Exception e) {
            this.logger.error("Fail to set application icon", e);
        }
    }
    
    protected void insertLayerAtTheEnd(WorldWindow wwd, Layer layer) {
        // Insert the layer at the end of the layer list
        LayerList layers = wwd.getModel().getLayers();
        layers.add(layer);
    }

    protected void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName) {
        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        
        for (Layer l : layers) {
            if (l.getName().contains(targetName)) {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        
        layers.add(targetPosition, layer);
    }
}
