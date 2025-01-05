/*
 * Copyright 2021-2025 A. Saint-Sever
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
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import lombok.Getter;

/**
 *
 *
 */
@SuppressWarnings("serial")
public class LayerPanel extends JPanel {

    @Getter
    protected Layer layer; // the layer represented by this instance
    protected JCheckBox checkBox; // the checkbox of this instance

    public LayerPanel(final WorldWindow wwd, final Layer layer) {
        super(new BorderLayout(10, 10));

        this.layer = layer;

        SelectLayerAction action = new SelectLayerAction(wwd, layer, layer.isEnabled());
        this.checkBox = new JCheckBox(action);
        this.checkBox.setSelected(action.selected);
        this.add(this.checkBox, BorderLayout.CENTER);
    }

    public Font getLayerNameFont() {
        return this.checkBox.getFont();
    }

    public void setLayerNameFont(Font font) {
        this.checkBox.setFont(font);
    }

    protected static class SelectLayerAction extends AbstractAction {
        // This action handles layer selection and de-selection.

        protected WorldWindow wwd;
        protected Layer layer;
        protected boolean selected;

        public SelectLayerAction(WorldWindow wwd, Layer layer, boolean selected) {
            super(layer.getName());

            this.wwd = wwd;
            this.layer = layer;
            this.selected = selected;
            this.layer.setEnabled(this.selected);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            // Simply enable or disable the layer based on its toggle button.
            this.layer.setEnabled(((JCheckBox) actionEvent.getSource()).isSelected());

            wwd.redraw();
        }
    }
}
