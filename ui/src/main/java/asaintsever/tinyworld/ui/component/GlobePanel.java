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
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import asaintsever.tinyworld.cfg.Configuration;
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

    public GlobePanel(Configuration cfg, Dimension canvasSize) {
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

        this.statusBar = new StatusBar(cfg);
        this.add(statusBar, BorderLayout.PAGE_END);
        this.statusBar.setEventSource(this.wwd);

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
