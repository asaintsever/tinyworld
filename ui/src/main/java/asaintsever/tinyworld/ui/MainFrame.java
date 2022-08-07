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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.ui.component.*;
import asaintsever.tinyworld.ui.event.IndexorListener;
import asaintsever.tinyworld.ui.event.SwingWorkerListener;
import asaintsever.tinyworld.ui.layer.*;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.util.WWUtil;

/**
 *
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    protected final static String APP_ICON = "/icon/tinyworldicon.jpg";

    protected Indexor indexor;
    protected GlobePanel globePanel;
    protected SettingsPanel settingsPanel;
    protected List<SwingWorkerListener> workers = new ArrayList<SwingWorkerListener>();
    protected List<IndexorListener> indexorListeners = new ArrayList<IndexorListener>();
    protected Logger logger = LoggerFactory.getLogger(MainFrame.class);

    public MainFrame(Configuration cfg, Dimension size) {
        this.initialize(cfg, size);
    }

    public void setIndexor(Indexor indexor) {
        this.indexor = indexor;

        // Notify all listeners with Indexor
        for (IndexorListener listener : this.indexorListeners)
            if (listener != null)
                listener.created(indexor);
    }

    public WorldWindow getWwd() {
        return this.globePanel.getWwd();
    }

    public GlobePanel getGlobe() {
        return this.globePanel;
    }

    public SettingsPanel getSettingsPanel() {
        return this.settingsPanel;
    }

    public void addIndexorListener(IndexorListener listener) {
        this.indexorListeners.add(listener);
    }

    public void addWorkerListener(SwingWorkerListener worker) {
        this.workers.add(worker);
    }

    @Override
    public void dispose() {
        // Cancel all running Swing Workers
        for (SwingWorkerListener worker : this.workers)
            if (worker != null)
                worker.cancelWorkers();

        super.dispose();
    }

    protected void initialize(Configuration cfg, Dimension size) {
        // Create the WorldWindow.
        this.globePanel = new GlobePanel(cfg, size);
        this.globePanel.setPreferredSize(size);

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
        TinyWorldPhotoTreeLayer twPhotoTreeLayer = new TinyWorldPhotoTreeLayer(this);

        this.globePanel.addLayer(twMenuLayer);
        this.globePanel.addLayer(twPhotoTreeLayer);

        this.addListeners(this.globePanel.getStatusBar().getIndexorStatusPanel());

        for (Layer layer : this.globePanel.getLayers()) {
            // Search the layer list for layers that are also select listeners and register them.
            // This enables interactive layers to be included without specific knowledge of them here.
            this.addListeners(layer);

            if (layer instanceof WorldMapLayer)
                ((WorldMapLayer) layer).setPosition(AVKey.NORTHEAST);
        }

        this.settingsPanel = new SettingsPanel(this);
        this.settingsPanel.setVisible(false); // Not visible by default (click on 'Settings' button of TW menu to enable
                                              // panel)

        this.getContentPane().add(globePanel, BorderLayout.CENTER);
        this.getContentPane().add(this.settingsPanel, BorderLayout.WEST);

        this.pack();

        // Center the application on the screen.
        WWUtil.alignComponent(null, this, AVKey.CENTER);
        this.setResizable(true);
        this.setAppIcon();
    }

    protected void addListeners(Object obj) {
        if (obj instanceof SelectListener)
            this.globePanel.getWwd().addSelectListener((SelectListener) obj);

        if (obj instanceof IndexorListener)
            this.addIndexorListener((IndexorListener) obj);

        if (obj instanceof SwingWorkerListener)
            this.addWorkerListener((SwingWorkerListener) obj);
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
}
