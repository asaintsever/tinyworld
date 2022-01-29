package asaintsever.tinyworld.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import asaintsever.tinyworld.ui.cfg.Configuration;
import asaintsever.tinyworld.ui.cfg.Loader;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

public class UI extends ApplicationTemplate {
    
    protected static Logger logger = LoggerFactory.getLogger(UI.class);
    
    // Must declare JUL loggers as static to make sure they are not garbage collected (by ava.util.logging.LogManager$LoggerContext removeLoggerRef() method). Failure to do so, some logs will not be routed by the SLF4J JUL bridge handler.
    // Worldwind & FlatLaf are using JUL: we will reroute their logs to SLF4J
    protected static java.util.logging.Logger wwjJULlogger = java.util.logging.Logger.getLogger(gov.nasa.worldwind.Configuration.getStringValue(AVKey.LOGGER_NAME, gov.nasa.worldwind.Configuration.DEFAULT_LOGGER_NAME));
    protected static java.util.logging.Logger flatlafJULlogger = java.util.logging.Logger.getLogger(FlatLaf.class.getName());

    public static class AppFrame extends ApplicationTemplate.AppFrame {
                
        public AppFrame() {
            super(true, true, false);
            
            // Eliminate elevations by simply setting the globe's elevation model to ZeroElevationModel.
            this.getWwd().getModel().getGlobe().setElevationModel(new ZeroElevationModel());
            // Remove elevation panel and status
            // TODO need custom LayerPanel class, need custom StatusBar class
                     
            this.setAppIcon();
            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);
        }
        
        protected void setAppIcon() {
            try {
                URL resource = UI.class.getResource("/icon/tinyworldicon.jpg");
                BufferedImage image = ImageIO.read(resource);
                this.setIconImage(image);
            } catch (Exception e) {
                logger.error("Fail to set application icon", e);
            }
        }

        protected JPanel makeControlPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 15, 10), new EtchedBorder()));

            JCheckBox modeSwitch = new JCheckBox(new AbstractAction(" Online") {

                public void actionPerformed(ActionEvent actionEvent) {
                    // Get the current status
                    boolean offline = WorldWind.getNetworkStatus().isOfflineMode();

                    // Change it to its opposite
                    offline = !offline;
                    WorldWind.getNetworkStatus().setOfflineMode(offline);

                    // Cause data retrieval to resume if now online
                    if (!offline)
                        getWwd().redraw();
                }
            });
            
            modeSwitch.setSelected(true); // WW starts out online
            panel.add(modeSwitch, BorderLayout.CENTER);
            return panel;
        }
    }
    
    protected static Configuration readConfig() {
        return Loader.getConfig();
    }
    
    protected static void routeJULtoSLF4J() {
        // Enable JUL to SLF4J bridge
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
               
        // Set log level for WWJ
        wwjJULlogger.setLevel(java.util.logging.Level.FINER);
        
        // Set log level for FlatLaf
        flatlafJULlogger.setLevel(java.util.logging.Level.CONFIG);
    }

    public static void main(String[] args) {
        Configuration cfg = readConfig();
        if (logger.isDebugEnabled())
            logger.debug("Configuration: " + cfg.toString());
        
        routeJULtoSLF4J();
        
        // Apply dark theme
        FlatDarkLaf.setup();
        
        ApplicationTemplate.start("TinyWorld", AppFrame.class);
    }
}
