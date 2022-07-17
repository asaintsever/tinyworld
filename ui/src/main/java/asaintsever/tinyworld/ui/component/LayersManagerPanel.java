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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import asaintsever.tinyworld.ui.MainFrame;
import asaintsever.tinyworld.ui.UIStrings;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicDataFileStore;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.TiledImageLayer;

/**
 *
 *
 */
@SuppressWarnings("serial")
public class LayersManagerPanel extends JPanel {

    protected JPanel layerNamesPanel;
    protected List<LayerPanel> layerPanels = new ArrayList<LayerPanel>();
    protected Font plainFont;
    protected Font boldFont;

    public LayersManagerPanel(final MainFrame frame) {
        super(new BorderLayout(10, 10));

        this.layerNamesPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        this.layerNamesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Must put the layer grid in a container to prevent the scroll pane from stretching vertical
        // spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(this.layerNamesPanel, BorderLayout.NORTH);

        // Add layers cache management button to panel
        JButton layersCache = new JButton(UIStrings.LAYERS_CACHE_MGMT_LABEL);
        layersCache.addActionListener((ActionEvent actionEvent) -> {
            SwingUtilities.invokeLater(() -> {
                JDialog layersCacheDialog = new JDialog(frame, UIStrings.APP_NAME, true);
                layersCacheDialog.setPreferredSize(new Dimension(800, 300));
                layersCacheDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                FileStore store = new BasicDataFileStore();
                File cacheRoot = store.getWriteLocation();
                LayersCachePanel layersCachePanel = new LayersCachePanel(cacheRoot);
                layersCacheDialog.getContentPane().add(layersCachePanel, BorderLayout.CENTER);
                layersCacheDialog.pack();

                // Center the application on the screen.
                Dimension prefSize = layersCacheDialog.getPreferredSize();
                Dimension parentSize;
                java.awt.Point parentLocation = new java.awt.Point(0, 0);
                parentSize = Toolkit.getDefaultToolkit().getScreenSize();
                int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
                int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
                layersCacheDialog.setLocation(x, y);
                layersCacheDialog.setVisible(true);
            });
        });

        dummyPanel.add(layersCache, BorderLayout.SOUTH);

        // Put the layers panel in a scroll pane.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);

        // Suppress the scroll pane's default border.
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Add the scroll pane to a titled panel that will resize with the main window.
        JPanel titlePanel = new JPanel(new GridLayout(0, 1, 0, 10));
        titlePanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder(UIStrings.LAYERS_LABEL)));
        titlePanel.setToolTipText(UIStrings.LAYERS_TOOLTIP);
        titlePanel.add(scrollPane);
        titlePanel.setPreferredSize(new Dimension(200, 500));
        this.add(titlePanel, BorderLayout.CENTER);

        this.fill(frame.getWwd());

        this.plainFont = this.getFont().deriveFont(Font.PLAIN);
        this.boldFont = this.getFont().deriveFont(Font.BOLD);

        // Register a rendering listener that updates the was-rendered state of each image layer.
        frame.getWwd().addRenderingListener((RenderingEvent event) -> {
            updateLayerActivity(frame.getWwd());
        });

        // Add a property change listener that causes this layer panel to be updated whenever the layer list
        // changes.
        frame.getWwd().getModel().getLayers().addPropertyChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
            if (propertyChangeEvent.getPropertyName().equals(AVKey.LAYERS))
                SwingUtilities.invokeLater(() -> {
                    update(frame.getWwd());
                });
        });
    }

    public void update(WorldWindow wwd) {
        // Repopulate this layer manager.
        this.fill(wwd);
    }

    protected void fill(WorldWindow wwd) {
        // Populate this layer manager with an entry for each layer in the WorldWindow's layer list.

        if (this.isUpToDate(wwd))
            return;

        // First remove all the existing entries.
        this.layerPanels.clear();
        this.layerNamesPanel.removeAll();

        // Fill the layers panel with the titles of all layers in the WorldWindow's current model.
        for (Layer layer : wwd.getModel().getLayers()) {
            if (layer.getValue(AVKey.IGNORE) == Boolean.TRUE || layer.getValue(AVKey.HIDDEN) == Boolean.TRUE)
                continue;

            LayerPanel layerPanel = new LayerPanel(wwd, layer);
            this.layerNamesPanel.add(layerPanel);
            this.layerPanels.add(layerPanel);
        }

        this.updateLayerActivity(wwd);
    }

    protected boolean isUpToDate(WorldWindow wwd) {
        // Determines whether this layer manager's layer list is consistent with the specified
        // WorldWindow's. Knowing
        // this prevents redundant updates.

        LayerList layerList = wwd.getModel().getLayers();

        if (this.layerPanels.size() != layerList.size())
            return false;

        for (int i = 0; i < layerList.size(); i++) {
            if (layerList.get(i) != this.layerPanels.get(i).getLayer())
                return false;
        }

        return true;
    }

    /**
     * Loops through this layer panel's layer/checkbox list and updates the checkbox font to indicate
     * whether the corresponding layer was just rendered. This method is called by a rendering listener
     * -- see comment below.
     *
     * @param wwd the WorldWindow.
     */
    protected void updateLayerActivity(WorldWindow wwd) {
        for (LayerPanel layerPanel : this.layerPanels) {
            // The frame timestamp from the layer indicates the last frame in which it rendered something. If
            // that
            // timestamp matches the current timestamp of the scene controller, then the layer rendered
            // something
            // during the most recent frame. Note that this frame timestamp protocol is only in place by default
            // for TiledImageLayer and its subclasses. Applications could, however, implement it for the layers
            // they design.

            Long layerTimeStamp = (Long) layerPanel.getLayer().getValue(AVKey.FRAME_TIMESTAMP);
            Long frameTimeStamp = (Long) wwd.getSceneController().getValue(AVKey.FRAME_TIMESTAMP);

            if (layerTimeStamp != null && frameTimeStamp != null
                    && layerTimeStamp.longValue() == frameTimeStamp.longValue()) {
                // Set the font to bold if the layer was just rendered.
                layerPanel.setLayerNameFont(this.boldFont);
            } else if (layerPanel.getLayer() instanceof TiledImageLayer) {
                // Set the font to plain if the layer was not just rendered.
                layerPanel.setLayerNameFont(this.plainFont);
            } else if (layerPanel.getLayer().isEnabled()) {
                // Set enabled layer types other than TiledImageLayer to bold.
                layerPanel.setLayerNameFont(this.boldFont);
            } else if (!layerPanel.getLayer().isEnabled()) {
                // Set disabled layer types other than TiledImageLayer to plain.
                layerPanel.setLayerNameFont(this.plainFont);
            }
        }
    }
}
