package asaintsever.tinyworld.ui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.ui.layer.TinyWorldMenuLayer;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ToolTipController;


public class Globe {

    public static class AppPanel extends JPanel {

        protected WorldWindow wwd;
        protected StatusBar statusBar;
        protected ToolTipController toolTipController;
        protected HighlightController highlightController;

        public AppPanel(Dimension canvasSize, boolean includeStatusBar) {
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

    protected static class AppFrame extends JFrame {

        private Dimension canvasSize = new Dimension(1000, 800); // the desired WorldWindow size

        protected AppPanel wwjPanel;
        protected SettingsPanel settingslPanel;
        protected Logger logger = LoggerFactory.getLogger(AppFrame.class);
        

        public AppFrame() {
            this.initialize(true);
        }

        public AppFrame(Dimension size) {
            this.canvasSize = size;
            this.initialize(true);
        }

        public AppFrame(boolean includeStatusBar) {
            this.initialize(includeStatusBar);
        }

        protected void initialize(boolean includeStatusBar) {
            // Create the WorldWindow.
            this.wwjPanel = this.createAppPanel(this.canvasSize, includeStatusBar);
            this.wwjPanel.setPreferredSize(canvasSize);
            
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);

            this.settingslPanel = new SettingsPanel(this.wwjPanel.getWwd());
            this.settingslPanel.setVisible(false);    // No visible by default (click on 'Settings' button to enable config pane)
            this.getContentPane().add(this.settingslPanel, BorderLayout.WEST);
            
            TinyWorldMenuLayer twMenuLayer = new TinyWorldMenuLayer(this.wwjPanel.getWwd(), this.settingslPanel);
            insertLayerAtTheEnd(this.wwjPanel.getWwd(), twMenuLayer);
            
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

            // Register a rendering exception listener that's notified when exceptions occur during rendering.
            this.wwjPanel.getWwd().addRenderingExceptionListener((Throwable t) -> {
                if (t instanceof WWAbsentRequirementException) {
                    String message = "Computer does not meet minimum graphics requirements.\n";
                    message += "Please install up-to-date graphics driver and try again.\n";
                    message += "Reason: " + t.getMessage() + "\n";
                    message += "This program will end when you press OK.";

                    JOptionPane.showMessageDialog(AppFrame.this, message, "Unable to Start Program", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
            });

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
                URL resource = AppFrame.class.getResource("/icon/tinyworldicon.jpg");
                BufferedImage image = ImageIO.read(resource);
                this.setIconImage(image);
            } catch (Exception e) {
                this.logger.error("Fail to set application icon", e);
            }
        }

        protected AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar) {
            return new AppPanel(canvasSize, includeStatusBar);
        }

        public Dimension getCanvasSize() {
            return canvasSize;
        }

        public AppPanel getWwjPanel() {
            return wwjPanel;
        }

        public WorldWindow getWwd() {
            return this.wwjPanel.getWwd();
        }

        public StatusBar getStatusBar() {
            return this.wwjPanel.getStatusBar();
        }

        public JPanel getSettingsPanel() {
            return this.settingslPanel;
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
    }

    public static void insertLayerAtTheEnd(WorldWindow wwd, Layer layer) {
        // Insert the layer at the end of the layer list
        LayerList layers = wwd.getModel().getLayers();
        layers.add(layer);
    }

    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName) {
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

    static {
        System.setProperty("java.net.useSystemProxies", "true");
        
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (Configuration.isWindowsOS() || (Configuration.isLinuxOS() && System.getProperty("os.version") != null && System.getProperty("os.version").toLowerCase().contains("wsl"))) {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    public static AppFrame start(String appName, asaintsever.tinyworld.cfg.Configuration.UI uiCfg) {
        if (Configuration.isMacOS() && appName != null) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try {
            final AppFrame frame =  new AppFrame();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            for (Window w : Window.getWindows()) {
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(w);
            }
            
            java.awt.EventQueue.invokeLater(() -> {
                frame.setVisible(true);
            });

            return frame;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
