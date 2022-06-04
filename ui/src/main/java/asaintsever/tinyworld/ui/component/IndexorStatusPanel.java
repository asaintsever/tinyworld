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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

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
    protected IndexorStatusWorker indexorStatusWorker;
    
    // Swing Worker to continuously test Indexor isConnected() and update status
    // Should run until app is closed. Closing will send cancellation signal to this worker (from MainFrame cancelSwingWorkers())
    protected class IndexorStatusWorker extends SwingWorker<Void, Void> {
        private IndexorStatusPanel indexorStatusPanel;
        
        public IndexorStatusWorker(IndexorStatusPanel indexorStatusPanel) {
            this.indexorStatusPanel = indexorStatusPanel;
        }

        @Override
        protected Void doInBackground() throws Exception {
            long pingInterval = 2000;
            
            while (!isCancelled()) {
                Thread.sleep(pingInterval);
                
                if (this.indexorStatusPanel.getIndexor().isConnected()) {
                    this.indexorStatusPanel.setStatus(UIStrings.INDEXOR_STATUS_CONNECTED_LABEL);
                    pingInterval = 60000;
                }
                else {
                    this.indexorStatusPanel.setStatus(UIStrings.INDEXOR_STATUS_CONNECTING_LABEL);
                    pingInterval = 2000;
                }
            }
            
            return null;
        }
    }
    
    
    public IndexorStatusPanel(Configuration.INDEXOR idxCfg) {
        this.connectionString = idxCfg.cluster.address + ":" + idxCfg.cluster.port;
        
        this.indexorStatusLabel = new JLabel();
        this.indexorStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.indexorIcon = new JLabel();
        this.indexorIcon.setPreferredSize(new Dimension(16,16));
        
        this.setStatus(UIStrings.INDEXOR_STATUS_CONNECTING_LABEL);
        
        this.add(this.indexorStatusLabel);
        this.add(this.indexorIcon);
    }
    
    
    public SwingWorker<Void, Void> getIndexorStatusWorker(Indexor indexor) {
        this.indexor = indexor;
        
        this.indexorStatusWorker = new IndexorStatusWorker(this);
        this.indexorStatusWorker.execute();
        
        return this.indexorStatusWorker;
    }
    
    
    protected Indexor getIndexor() {
        return this.indexor;
    }
    
    protected IndexorStatusPanel setStatus(String status) {
        switch(status) {
        case UIStrings.INDEXOR_STATUS_CONNECTING_LABEL:
            this.indexorStatusLabel.setText(String.format(UIStrings.INDEXOR_STATUS_CONNECTING_LABEL, this.connectionString));
            this.indexorStatusLabel.setForeground(new Color(255,0,0));
            this.indexorIcon.setIcon(CONNECTING);
            break;
        case UIStrings.INDEXOR_STATUS_CONNECTED_LABEL:
            this.indexorStatusLabel.setText(String.format(UIStrings.INDEXOR_STATUS_CONNECTED_LABEL, this.connectionString));
            this.indexorStatusLabel.setForeground(new Color(255,255,255));
            this.indexorIcon.setIcon(CONNECTED);
            break;
        }
        
        return this;
    }
}
