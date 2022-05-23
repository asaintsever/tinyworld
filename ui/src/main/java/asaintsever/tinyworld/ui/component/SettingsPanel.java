package asaintsever.tinyworld.ui.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwindx.examples.FlatWorldPanel;


public class SettingsPanel extends JPanel {

    public SettingsPanel(final WorldWindow wwd) {
        super(new BorderLayout(10, 10));
        
        this.add(new FlatWorldPanel(wwd), BorderLayout.NORTH);
        this.add(new LayerManagerPanel(wwd), BorderLayout.CENTER);
        this.add(this.createNetworkStatusPanel(wwd), BorderLayout.SOUTH);
    }
    
    
    protected JPanel createNetworkStatusPanel(final WorldWindow wwd) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 15, 10), new EtchedBorder()));

        JCheckBox modeSwitch = new JCheckBox(new AbstractAction(" Online") {

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
