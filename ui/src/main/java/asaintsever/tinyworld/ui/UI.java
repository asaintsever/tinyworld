/*
 * Copyright 2021-2022 A. Saint-Sever
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://github.com/asaintsever/tinyworld
 */
package asaintsever.tinyworld.ui;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.formdev.flatlaf.FlatDarkLaf;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.cfg.Env;
import asaintsever.tinyworld.cfg.Loader;
import asaintsever.tinyworld.indexor.Indexor;


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
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", UIStrings.APP_NAME);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (gov.nasa.worldwind.Configuration.isWindowsOS() || (gov.nasa.worldwind.Configuration.isLinuxOS() && System.getProperty("os.version") != null && System.getProperty("os.version").toLowerCase().contains("wsl"))) {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }
    

    public static void main(String[] args) throws Exception {
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
        
        start(UIStrings.APP_NAME, cfg);
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
        if (uiCfg.deps.logging.get(UIStrings.DEP_WORLDWIND).toLowerCase().equals("on"))
            wwjJULlogger.setLevel(java.util.logging.Level.FINER);
        
        // Set log level for FlatLaf from config
        if (uiCfg.deps.logging.get(UIStrings.DEP_FLATLAF).toLowerCase().equals("on"))
            flatlafJULlogger.setLevel(java.util.logging.Level.CONFIG);
    }
    
    /**
     * Use a Swing Worker to create Indexor in background thread to not block UI
     *
     */
    protected static class IndexorLoaderWorker extends SwingWorker<Indexor, Object> {
        private MainFrame frame;
        private Configuration.INDEXOR indexorCfg;
        
        
        public IndexorLoaderWorker(MainFrame frame, Configuration.INDEXOR indexorCfg) {
            this.frame = frame;
            this.indexorCfg = indexorCfg;
        }
        
        
        @Override
        protected Indexor doInBackground() throws Exception {
            // Store index in user's home directory
            Indexor.setClusterPathHome(Paths.get(Env.TINYWORLD_USER_HOME_PATH.toString(), "index").toString());
            return new Indexor(this.indexorCfg);
        }
        
        @Override
        protected void done() {
            try {
                // When done: assign Indexor to main frame
                this.frame.setIndexor(this.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Fail to create Indexor", e);
            }
        }
    }
    
    protected static MainFrame start(String appName, Configuration cfg) throws Exception {
        if (gov.nasa.worldwind.Configuration.isMacOS() && appName != null) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }        
        
        if (logger.isDebugEnabled()) {
            // Loop through all detected screens and get size + UI scaling factor
        	for(GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
        		GraphicsConfiguration gConf = gd.getDefaultConfiguration();
        		
        		Rectangle screenArea = gConf.getBounds();
        		logger.debug("[Screen id " + gd.getIDstring() + "] Detected Screen Size: " + screenArea.width + "x" + screenArea.height); // Beware: won't return "real" size if High DPI scaling is in use
        		
        		AffineTransform globalTransform = gConf.getDefaultTransform();
                double scaleX = globalTransform.getScaleX();	// Only get X, we expect same scaling factor on Y
                logger.debug("[Screen id " + gd.getIDstring() + "] Detected UI Scaling Factor: " + scaleX);
        	}
        }
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        MainFrame frame =  new MainFrame(cfg, new Dimension(screenSize.width - 100, screenSize.height - 100));
        frame.setTitle(appName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            
            // Create indexor in background thread
            IndexorLoaderWorker idxLoader = new IndexorLoaderWorker(frame, cfg.indexor);
            idxLoader.execute();
            
            frame.addWindowListener(new WindowAdapter() {
                /**
                 * Make sure to properly release Indexor before closing
                 * (SwingWorker's get() method will wait in case Indexor creation is still on-going)
                 */
                @Override
                public void windowClosing(WindowEvent event) {                                        
                    try {
                        Indexor indexor = idxLoader.get();
                        indexor.close();
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        logger.error("Error while trying to release Indexor", e);
                    }
                    
                    frame.dispose();
                }
            });
        });

        return frame;
    }
}
