package asaintsever.tinyworld.ui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.formdev.flatlaf.FlatDarkLaf;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.cfg.Loader;
import asaintsever.tinyworld.ui.component.MainFrame;

public class UI {
    
    protected static Logger logger = LoggerFactory.getLogger(UI.class);
    
    // Must declare JUL loggers as static to make sure they are not garbage collected (by java.util.logging.LogManager$LoggerContext removeLoggerRef() method).
    // Failure to do so, some logs will not be routed by the SLF4J JUL bridge handler.
    // Worldwind & FlatLaf are using JUL: we will reroute their logs to SLF4J
    protected static java.util.logging.Logger wwjJULlogger = java.util.logging.Logger.getLogger(
                                                                    gov.nasa.worldwind.Configuration.getStringValue(
                                                                            gov.nasa.worldwind.avlist.AVKey.LOGGER_NAME, 
                                                                            gov.nasa.worldwind.Configuration.DEFAULT_LOGGER_NAME)
                                                                    );
    protected static java.util.logging.Logger flatlafJULlogger = java.util.logging.Logger.getLogger(com.formdev.flatlaf.FlatLaf.class.getName());


    static {
        System.setProperty("java.net.useSystemProxies", "true");
        
        if (gov.nasa.worldwind.Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (gov.nasa.worldwind.Configuration.isWindowsOS() || (gov.nasa.worldwind.Configuration.isLinuxOS() && System.getProperty("os.version") != null && System.getProperty("os.version").toLowerCase().contains("wsl"))) {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }
    

    public static void main(String[] args) {
        Configuration cfg = readConfig();
        
        if (cfg == null || cfg.ui == null) {
            logger.error("Cannot read configuration");
            System.exit(1);
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Loaded config: " + cfg.toString());
        
        routeJULtoSLF4J(cfg.ui);
        
        // Apply dark theme
        FlatDarkLaf.setup();
        
        start(UIStrings.APP_NAME, cfg.ui);
    }
    
    
    protected static Configuration readConfig() {
        return Loader.getConfig();
    }
    
    protected static void routeJULtoSLF4J(Configuration.UI uiCfg) {
        // Enable JUL to SLF4J bridge
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        
        // By default, turn off logging for dependencies
        wwjJULlogger.setLevel(java.util.logging.Level.OFF);
        flatlafJULlogger.setLevel(java.util.logging.Level.OFF);
        
        // Set log level for WWJ from config
        if (uiCfg.deps.logging.get("wwj").toLowerCase().equals("on"))
            wwjJULlogger.setLevel(java.util.logging.Level.FINER);
        
        // Set log level for FlatLaf from config
        if (uiCfg.deps.logging.get("flatlaf").toLowerCase().equals("on"))
            flatlafJULlogger.setLevel(java.util.logging.Level.CONFIG);
    }
    
    protected static MainFrame start(String appName, Configuration.UI uiCfg) {
        if (gov.nasa.worldwind.Configuration.isMacOS() && appName != null) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final MainFrame frame =  new MainFrame(new Dimension(screenSize.width - 100, screenSize.height - 100));
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            SwingUtilities.invokeLater(() -> {
                frame.setVisible(true);
            });

            return frame;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
