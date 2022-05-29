package asaintsever.tinyworld.ui.component;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.ui.UIStrings;


public class IndexorStatusPanel extends JPanel {

    protected static final ImageIcon CONNECTING = new ImageIcon(IndexorStatusPanel.class.getResource("/images/tw-indexor-plug-16x16.png"));
    protected static final ImageIcon CONNECTED = new ImageIcon(IndexorStatusPanel.class.getResource("/images/tw-indexor-plugged-16x16.png"));
    
    protected JLabel indexorStatusLabel;
    protected JLabel indexorIcon;
    protected String connectionString;
    protected Indexor indexor;
    
    
    public IndexorStatusPanel(Configuration.INDEXOR idxCfg) {
        this.connectionString = idxCfg.cluster.address + ":" + idxCfg.cluster.port;
        
        this.indexorStatusLabel = new JLabel(UIStrings.INDEXOR_STATUS_CONNECTING_LABEL + this.connectionString);
        this.indexorStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        this.indexorIcon = new JLabel(CONNECTING);
        this.indexorIcon.setPreferredSize(new Dimension(16,16));
        
        this.add(this.indexorStatusLabel);
        this.add(this.indexorIcon);
    }
    
    
    public void setIndexor(Indexor indexor) {
        this.indexor = indexor;
    }
}
