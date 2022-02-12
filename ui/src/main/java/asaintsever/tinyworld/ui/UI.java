package asaintsever.tinyworld.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.formdev.flatlaf.FlatDarkLaf;

import asaintsever.tinyworld.ui.cfg.Configuration;
import asaintsever.tinyworld.ui.cfg.Loader;
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
        
        Globe.start("TinyWorld", AppFrame.class);
    }
}
