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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Formatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;

import asaintsever.tinyworld.ui.UIStrings;
import gov.nasa.worldwindx.examples.util.FileStoreDataSet;
import gov.nasa.worldwindx.examples.util.cachecleaner.CacheTable;


/**
 *
 *
 */
@SuppressWarnings("serial")
public class LayersCachePanel extends JPanel {

    protected CacheTable table;
    protected JButton delBtn;
    protected JSpinner ageSpinner;
    protected JComboBox<String> ageUnit;
    protected JLabel deleteSizeLabel;
    
    
    public LayersCachePanel(File cacheRoot) {
        super(new BorderLayout(5, 5));
        
        JLabel rootLabel = new JLabel(UIStrings.LAYERS_CACHE_ROOT_LABEL + cacheRoot.getPath());
        rootLabel.setBorder(new EmptyBorder(10, 15, 10, 10));
        this.add(rootLabel, BorderLayout.NORTH);

        this.table = new CacheTable();
        this.table.setDataSets(cacheRoot.getPath(), FileStoreDataSet.getDataSets(cacheRoot));
        JScrollPane sp = new JScrollPane(table);
        this.add(sp, BorderLayout.CENTER);

        JPanel pa = new JPanel(new BorderLayout(10, 10));
        pa.add(new JLabel(UIStrings.LAYERS_CACHE_DELETE_PANEL_LABEL), BorderLayout.WEST);
        this.ageSpinner = new JSpinner(new SpinnerNumberModel(6, 0, 10000, 1));
        this.ageSpinner.setToolTipText(UIStrings.LAYERS_CACHE_AGE_TOOLTIP);
        JPanel pas = new JPanel();
        pas.add(this.ageSpinner);
        pa.add(pas, BorderLayout.CENTER);
        this.ageUnit = new JComboBox<String>(new String[] {"Hours", "Days", "Weeks", "Months", "Years"});
        this.ageUnit.setSelectedItem("Months");
        this.ageUnit.setEditable(false);
        pa.add(this.ageUnit, BorderLayout.EAST);

        JPanel pb = new JPanel(new BorderLayout(5, 10));
        this.deleteSizeLabel = new JLabel(UIStrings.LAYERS_CACHE_SIZE_PANEL_LABEL + "N/A");
        pb.add(this.deleteSizeLabel, BorderLayout.WEST);
        this.delBtn = new JButton(UIStrings.LAYERS_CACHE_DELETE_BUTTON_LABEL);
        this.delBtn.setEnabled(false);
        JPanel pbb = new JPanel();
        pbb.add(this.delBtn);
        pb.add(pbb, BorderLayout.CENTER);

        JPanel pc = new JPanel(new BorderLayout(5, 10));
        pc.add(pa, BorderLayout.WEST);
        pc.add(pb, BorderLayout.EAST);

        JPanel ctlPanel = new JPanel(new BorderLayout(10, 10));
        ctlPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        ctlPanel.add(pc, BorderLayout.CENTER);

        this.add(ctlPanel, BorderLayout.SOUTH);
        
        
        this.ageUnit.addItemListener((ItemEvent e) -> {
            update();
        });

        this.ageSpinner.addChangeListener((ChangeEvent e) -> {
            update();
        });

        this.table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            update();
        });

        this.delBtn.addActionListener((ActionEvent actionEvent) -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            Thread t = new Thread(() -> {
                try {
                    List<FileStoreDataSet> dataSets = table.getSelectedDataSets();
                    int age = Integer.parseInt(ageSpinner.getValue().toString());
                    String unit = getUnitKey();

                    for (FileStoreDataSet ds : dataSets) {
                        ds.deleteOutOfScopeFiles(unit, age, false);
                        
                        if (ds.getSize() == 0) {
                            table.deleteDataSet(ds);
                            ds.delete(false);
                        }
                    }
                } finally {
                    update();
                    
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                    });
                }
            });
            
            t.start();
        });
    }
    
    protected void update() {
        java.util.List<FileStoreDataSet> dataSets = this.table.getSelectedDataSets();
        int age = Integer.parseInt(this.ageSpinner.getValue().toString());

        if (dataSets.size() == 0) {
            this.deleteSizeLabel.setText(UIStrings.LAYERS_CACHE_SIZE_PANEL_LABEL + "N/A");
            this.delBtn.setEnabled(false);
            return;
        }

        String unit = this.getUnitKey();

        long totalSize = 0;
        for (FileStoreDataSet ds : dataSets) {
            totalSize += ds.getOutOfScopeSize(unit, age);
        }

        try (Formatter formatter = new Formatter()) {
			formatter.format("%5.1f", ((float) totalSize) / 1e6);
			this.deleteSizeLabel.setText(UIStrings.LAYERS_CACHE_SIZE_PANEL_LABEL + formatter.toString() + " MB");
		}
        
        this.delBtn.setEnabled(true);
    }

    protected String getUnitKey() {
        String unit = null;
        String unitString = (String) this.ageUnit.getSelectedItem();
        
        if (unitString.equals("Hours"))
            unit = FileStoreDataSet.HOUR;
        else if (unitString.equals("Days"))
            unit = FileStoreDataSet.DAY;
        else if (unitString.equals("Weeks"))
            unit = FileStoreDataSet.WEEK;
        else if (unitString.equals("Months"))
            unit = FileStoreDataSet.MONTH;
        else if (unitString.equals("Years"))
            unit = FileStoreDataSet.YEAR;

        return unit;
    }
}
