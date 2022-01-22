package asaintsever.tinyworld.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

public class UI extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {
        
        public AppFrame() {
            super(true, true, false);

            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);
        }

        protected JPanel makeControlPanel() {
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
                        getWwd().redraw();
                }
            });
            
            modeSwitch.setSelected(true); // WW starts out online
            panel.add(modeSwitch, BorderLayout.CENTER);
            return panel;
        }
    }

    public static void main(String[] args) {
        // Enable JUL to SLF4J bridge
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        
        // Set log level for WWJ
        java.util.logging.Logger.getLogger(Configuration.getStringValue(AVKey.LOGGER_NAME, Configuration.DEFAULT_LOGGER_NAME)).setLevel(java.util.logging.Level.FINEST);
        
        // Set log level for FlatLaf
        java.util.logging.Logger.getLogger(FlatLaf.class.getName()).setLevel(java.util.logging.Level.FINEST);
        
        // Apply dark theme
        FlatDarkLaf.setup();
        
        ApplicationTemplate.start("TinyWorld", AppFrame.class);
    }
}
