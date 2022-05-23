package asaintsever.tinyworld.ui.component;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;


public class LayerPanel extends JPanel {

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

    
    public Layer getLayer() {
        return this.layer;
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
            if (((JCheckBox) actionEvent.getSource()).isSelected())
                this.layer.setEnabled(true);
            else
                this.layer.setEnabled(false);

            wwd.redraw();
        }
    }
}
