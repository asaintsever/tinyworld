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
package asaintsever.tinyworld.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.ui.layer.TinyWorldMenuLayer;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.util.WWUtil;


public class MainFrame extends JFrame {
    
    protected final static String APP_ICON = "/icon/tinyworldicon.jpg";

    private Dimension canvasSize = new Dimension(1000, 800); // the desired WorldWindow size

    protected Configuration cfg;
    protected Indexor indexor;
    protected GlobePanel globePanel;
    protected SettingsPanel settingsPanel;
    protected List<SwingWorker<?, ?>> workers = new ArrayList<SwingWorker<?, ?>>();
    protected Logger logger = LoggerFactory.getLogger(MainFrame.class);
    

    public MainFrame(Configuration cfg) {
        this.initialize(cfg);
    }

    public MainFrame(Configuration cfg, Dimension size) {
        this.canvasSize = size;
        this.initialize(cfg);
    }
    
    
    public void setIndexor(Indexor indexor) {
        this.indexor = indexor;
        this.workers.add(this.getStatusBar().getIndexorStatusPanel().getIndexorStatusWorker(indexor));
    }

    public WorldWindow getWwd() {
        return this.globePanel.getWwd();
    }
    
    /*public GlobePanel getGlobe() {
    	return this.globePanel;
    }*/

    public StatusBar getStatusBar() {
        return this.globePanel.getStatusBar();
    }

    public SettingsPanel getSettingsPanel() {
        return this.settingsPanel;
    }
    
    public void cancelSwingWorkers() {
        for(SwingWorker<?, ?> worker : this.workers)
            if (worker != null)
                worker.cancel(false);
    }
    
    
    protected void initialize(Configuration cfg) {
        this.cfg = cfg;
        
        // Create the WorldWindow.
        this.globePanel = new GlobePanel(cfg, this.canvasSize);
        this.globePanel.setPreferredSize(canvasSize);
        
        // Register a rendering exception listener that's notified when exceptions occur during rendering.
        this.globePanel.getWwd().addRenderingExceptionListener((Throwable t) -> {
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
        this.insertLayerAtTheEnd(this.globePanel.getWwd(), twMenuLayer);
        
        for (Layer layer : this.globePanel.getWwd().getModel().getLayers()) {
            // Search the layer list for layers that are also select listeners and register them with the World
            // Window. This enables interactive layers to be included without specific knowledge of them here.
            if (layer instanceof SelectListener) {
                this.globePanel.getWwd().addSelectListener((SelectListener) layer);
            }
            
            if (layer instanceof WorldMapLayer) {
                ((WorldMapLayer) layer).setPosition(AVKey.NORTHEAST);
            }
        }

        this.settingsPanel = new SettingsPanel(this);
        this.settingsPanel.setVisible(false);              // Not visible by default (click on 'Settings' button of TW menu to enable panel)
        
        this.getContentPane().add(globePanel, BorderLayout.CENTER);
        this.getContentPane().add(this.settingsPanel, BorderLayout.WEST);

        this.pack();

        // Center the application on the screen.
        WWUtil.alignComponent(null, this, AVKey.CENTER);
        this.setResizable(true);
        this.setAppIcon();
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
