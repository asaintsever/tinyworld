package asaintsever.tinyworld.ui.component;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.ui.UIStrings;
import asaintsever.tinyworld.ui.layer.TinyWorldMenuLayer;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwindx.examples.FlatWorldPanel;


public class SettingsPanel extends JPanel {
    
    protected static Logger logger = LoggerFactory.getLogger(SettingsPanel.class);
    
    protected TinyWorldMenuLayer twMenuLayer;
    protected JCheckBox twMenuTooltipSwitch;
    

    public SettingsPanel(final MainFrame frame) {
        super(new BorderLayout(10, 10));
        
        if (frame == null || frame.getWwd() == null) {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        // Find TinyWorldMenuLayer layer and keep reference to it
        for (Layer layer : frame.getWwd().getModel().getLayers()) {
            if (layer instanceof TinyWorldMenuLayer) {
                this.twMenuLayer = (TinyWorldMenuLayer) layer;
            }
        }
        
        if (this.twMenuLayer == null) {
            String msg = "TinyWorldMenuLayer layer not found. Make sure it has been set before SettingsPanel creation.";
            logger.error(msg);
            throw new NullPointerException(msg);
        }
        
        JPanel layersPanel = new JPanel();
        layersPanel.setLayout(new BorderLayout(10, 10));
        layersPanel.add(new FlatWorldPanel(frame.getWwd()), BorderLayout.NORTH);
        layersPanel.add(new LayerManagerPanel(frame), BorderLayout.CENTER);
        
        this.add(this.createTWMenuPanel(frame.getWwd()), BorderLayout.NORTH);
        this.add(layersPanel, BorderLayout.CENTER);
        this.add(this.createNetworkStatusPanel(frame.getWwd()), BorderLayout.SOUTH);
    }
    
    
    public boolean isMenuTooltipEnabled() {
        return this.twMenuTooltipSwitch != null ? this.twMenuTooltipSwitch.isSelected() : false;
    }
    
    protected JPanel createTWMenuPanel(final WorldWindow wwd) {
        JPanel twMenuPanel = new JPanel();
        twMenuPanel.setLayout(new BoxLayout(twMenuPanel, BoxLayout.Y_AXIS));
        twMenuPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder(UIStrings.MENU_LABEL)));
        
        // Radio buttons - layout
        JPanel layoutPanel = new JPanel(new GridLayout(0, 3, 0, 0));
        layoutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        layoutPanel.add(new JLabel(UIStrings.LAYOUT_LABEL));
        ButtonGroup group = new ButtonGroup();
        JRadioButton button = new JRadioButton(UIStrings.LAYOUT_HORIZONTAL, true);
        group.add(button);
        button.addActionListener((ActionEvent actionEvent) -> {
            this.twMenuLayer.setLayout(AVKey.HORIZONTAL);
            wwd.redraw();
        });
        layoutPanel.add(button);
        button = new JRadioButton(UIStrings.LAYOUT_VERTICAL, false);
        group.add(button);
        button.addActionListener((ActionEvent actionEvent) -> {
            this.twMenuLayer.setLayout(AVKey.VERTICAL);
            wwd.redraw();
        });
        layoutPanel.add(button);
        
        // Tooltip on/off
        JPanel tooltipPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        tooltipPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.twMenuTooltipSwitch = new JCheckBox(UIStrings.MENU_TOOLTIP_CHECKBOX_LABEL);
        this.twMenuTooltipSwitch.addActionListener((ActionEvent actionEvent) -> {
            this.twMenuLayer.setToolTips(this.twMenuTooltipSwitch.isSelected());
        });
        this.twMenuTooltipSwitch.setSelected(true);  // on by default
        tooltipPanel.add(this.twMenuTooltipSwitch);

        // Scale slider
        JPanel scalePanel = new JPanel(new GridLayout(0, 1, 0, 0));
        scalePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scalePanel.add(new JLabel(UIStrings.LAYER_SCALE_LABEL));
        JSlider scaleSlider = new JSlider(1, 20, 10);
        scaleSlider.addChangeListener((ChangeEvent event) -> {
            this.twMenuLayer.setScale(((JSlider) event.getSource()).getValue() / 10d);
            wwd.redraw();
        });
        scalePanel.add(scaleSlider);
        
        twMenuPanel.add(layoutPanel);
        twMenuPanel.add(tooltipPanel);
        twMenuPanel.add(scalePanel);
        
        return twMenuPanel;
    }
    
    protected JPanel createNetworkStatusPanel(final WorldWindow wwd) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 15, 10), new EtchedBorder()));

        JCheckBox modeSwitch = new JCheckBox(new AbstractAction(UIStrings.NETWORK_CHECKBOX_LABEL) {

            public void actionPerformed(ActionEvent actionEvent) {
                // Get the current status
                boolean offline = WorldWind.getNetworkStatus().isOfflineMode();

                // Change it to its opposite
                offline = !offline;
                WorldWind.getNetworkStatus().setOfflineMode(offline);

                // Cause data retrieval to resume if now online
                if (!offline)
                    wwd.redraw();
            }
        });
        
        modeSwitch.setSelected(true); // WW starts out online
        panel.add(modeSwitch, BorderLayout.CENTER);
        return panel;
    }
}
