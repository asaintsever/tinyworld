package asaintsever.tinyworld.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.formdev.flatlaf.FlatDarkLaf;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.cfg.Loader;
import asaintsever.tinyworld.ui.globe.Globe;

public class UI extends Globe {
    
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
        
        Globe.start("TinyWorld", AppFrame.class, cfg.ui);
    }
}
