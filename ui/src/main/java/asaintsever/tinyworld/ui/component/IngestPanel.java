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

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.metadata.extractor.Extract;
import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;
import asaintsever.tinyworld.metadata.extractor.PhotoObject;
import asaintsever.tinyworld.metadata.extractor.PhotoProcessException;
import asaintsever.tinyworld.ui.MainFrame;
import asaintsever.tinyworld.ui.UIStrings;
import asaintsever.tinyworld.ui.event.IndexorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@SuppressWarnings("serial")
public class IngestPanel extends JPanel implements IndexorListener {

    private static final Logger logger = LoggerFactory.getLogger(IngestPanel.class);

    private Indexor indexor;
    private final MainFrame frame;

    private final JButton browseButton = new JButton(UIStrings.BROWSE_BUTTON_LABEL);
    private final JLabel selectedDirectoryLabel = new JLabel();
    private final JCheckBox allowUpdateCheckBox = new JCheckBox(UIStrings.ALLOW_UPDATE_CHECKBOX_LABEL);
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextArea logArea = new JTextArea(10, 40);
    private final JButton startButton = new JButton(UIStrings.START_INGEST_BUTTON_LABEL);
    private File selectedDirectory;

    public IngestPanel(MainFrame frame) {
        super(new BorderLayout(10, 10));
        this.frame = frame;
        this.initComponents();
    }

    @Override
    public void created(Indexor indexor) {
        this.indexor = indexor;
    }

    private void initComponents() {
        this.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder(UIStrings.MENU_INDEX_DISPLAYNAME)));

        // Top panel for directory selection
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(browseButton, BorderLayout.WEST);
        topPanel.add(selectedDirectoryLabel, BorderLayout.CENTER);
        topPanel.add(allowUpdateCheckBox, BorderLayout.EAST);

        // Center panel for logging
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Bottom panel for progress bar and start button
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(startButton, BorderLayout.EAST);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(logScrollPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        browseButton.addActionListener(e -> this.selectDirectory());
        startButton.addActionListener(e -> this.startIngestion());
    }

    private void selectDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this.frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            this.selectedDirectory = chooser.getSelectedFile();
            this.selectedDirectoryLabel.setText(this.selectedDirectory.getAbsolutePath());
        }
    }

    private void startIngestion() {
        if (this.selectedDirectory == null) {
            JOptionPane.showMessageDialog(this.frame, "Please select a directory to ingest.", "No directory selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (this.indexor == null) {
            JOptionPane.showMessageDialog(this.frame, "Indexor is not ready.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.startButton.setEnabled(false);
        this.browseButton.setEnabled(false);
        this.progressBar.setIndeterminate(true);
        this.logArea.setText("");

        IngestionWorker worker = new IngestionWorker(this.frame.getCfg(), this.selectedDirectory.getAbsolutePath(),
                this.allowUpdateCheckBox.isSelected());
        worker.execute();
    }

    private class IngestionWorker extends SwingWorker<Extract.Result, String> {

        private final Configuration cfg;
        private final String path;
        private final boolean allowUpdate;

        public IngestionWorker(Configuration cfg, String path, boolean allowUpdate) {
            this.cfg = cfg;
            this.path = path;
            this.allowUpdate = allowUpdate;
        }

        @Override
        protected Extract.Result doInBackground() throws Exception {
            PhotoMetadata defaultMetadata = new PhotoMetadata().from(this.cfg.indexor.photo.defaultMetadata);

            return Extract.exploreFS(this.path, Integer.MAX_VALUE, (uri, fileType, metadata) -> {
                try {
                    PhotoObject photo = new PhotoObject(defaultMetadata);
                    PhotoMetadata mtd = photo.extractMetadata(uri, fileType, metadata).getMetadata();
                    indexor.photos().add(mtd, allowUpdate);
                    publish(uri.toString());
                } catch (IOException | ParseException e) {
                    throw new PhotoProcessException(e);
                }
            });
        }

        @Override
        protected void process(List<String> chunks) {
            for (String uri : chunks) {
                logArea.append("Processing: " + uri + "\n");
            }
        }

        @Override
        protected void done() {
            try {
                Extract.Result result = get();
                logArea.append("\n--- Ingestion complete ---\n");
                logArea.append("Successfully ingested: " + result.getProcessed_ok() + "\n");
                logArea.append("Skipped: " + result.getSkipped() + "\n");
                logArea.append("Errors: " + result.getProcessed_nok() + "\n");
                if (result.getProcessed_nok() > 0) {
                    logArea.append("\n--- Errors ---\n");
                    for (String error : result.getErrorMsg()) {
                        logArea.append(error + "\n");
                    }
                }
            } catch (Exception e) {
                logger.error("Error during ingestion", e);
                logArea.append("\n--- Error ---\n");
                logArea.append(e.getMessage());
            } finally {
                startButton.setEnabled(true);
                browseButton.setEnabled(true);
                progressBar.setIndeterminate(false);
            }
        }
    }
}
