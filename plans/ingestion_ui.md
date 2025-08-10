# Feature Implementation Plan: Photo Ingestion UI

## 📋 Todo Checklist
- [x] Create `IngestPanel` Swing component.
- [x] Integrate `IngestPanel` into the main UI frame.
- [x] Implement the background ingestion logic using `SwingWorker`.
- [x] Connect the "Index" menu button to show the `IngestPanel`.
- [x] Add logging to the `IngestPanel` to show progress.
- [x] Final Review and Testing.

## 🔍 Analysis & Investigation

### Codebase Structure
- **`indexor` module**: Contains the core indexing logic. `IndexorCmd.java` provides a command-line interface for ingestion, and its logic will be adapted for the new UI.
- **`ui` module**: Contains the main application UI, built with Swing and NASA WorldWind.
- **`UI.java`**: The main entry point for the application. It initializes the `MainFrame` and the `Indexor`.
- **`MainFrame.java`**: The main application window. It holds the `GlobePanel` and other UI components like the `SettingsPanel`. The new `IngestPanel` will be added here.
- **`TinyWorldMenuLayer.java`**: A WorldWind layer that displays the main menu buttons (Index, Filter, Settings). The "Index" button will be used to trigger the display of the `IngestPanel`.

### Files Inspected
- `indexor/src/main/java/asaintsever/tinyworld/indexor/IndexorCmd.java`
- `ui/src/main/java/asaintsever/tinyworld/ui/UI.java`
- `ui/src/main/java/asaintsever/tinyworld/ui/MainFrame.java`
- `ui/src/main/java/asaintsever/tinyworld/ui/layer/TinyWorldMenuLayer.java`

### Current Architecture
The application uses a Swing-based UI with the NASA WorldWind library for the 3D globe. The UI is separated from the core logic (like indexing). A central `Indexor` object handles all interactions with the search index. The UI components communicate with the `Indexor` through listeners and events. The use of `SwingWorker` in `UI.java` to load the `Indexor` is a good pattern to follow for long-running tasks to avoid blocking the Event Dispatch Thread (EDT).

### Dependencies & Integration Points
- **Swing**: The UI is built using Swing components. The new `IngestPanel` will be a `JPanel`.
- **NASA WorldWind**: The menu is a WorldWind layer. We will need to modify the `TinyWorldMenuLayer` to show the `IngestPanel`.
- **Indexor**: The `IngestPanel` will need a reference to the `Indexor` instance to perform the ingestion. This will be done by implementing the `IndexorListener` interface.

### Considerations & Challenges
- **Thread Safety**: The ingestion process can be time-consuming. It must be run in a background thread to avoid freezing the UI. A `SwingWorker` is the ideal solution for this.
- **UI Updates**: The UI must be updated from the EDT. The `SwingWorker`'s `publish`/`process` and `done` methods will be used to safely update the UI with progress information.
- **Error Handling**: The ingestion process can fail. Errors must be caught and displayed to the user in the `IngestPanel`.

## 📝 Implementation Plan

### Prerequisites
- No new dependencies are required.

### Step-by-Step Implementation

1.  **Create `IngestPanel.java`**
    -   Create a new file `IngestPanel.java` in `ui/src/main/java/asaintsever/tinyworld/ui/component/`.
    -   The class `IngestPanel` will extend `javax.swing.JPanel` and implement `asaintsever.tinyworld.ui.event.IndexorListener`.
    -   Add the following components to the panel:
        -   A `JButton` to open a `JFileChooser`.
        -   A `JLabel` to show the selected directory.
        -   A `JCheckBox` for the `allowUpdate` option.
        -   A `JProgressBar` for visual feedback.
        -   A `JTextArea` inside a `JScrollPane` for logging.
        -   A `JButton` to start the ingestion.
    -   The panel should be hidden by default.

2.  **Implement Ingestion Logic in `IngestPanel`**
    -   Implement the `created(Indexor indexor)` method from the `IndexorListener` interface to receive the `Indexor` instance.
    -   Add a `startIngestion()` method that will be called when the "Start" button is clicked.
    -   This method will create a `SwingWorker<Extract.Result, String>`.
    -   The `doInBackground()` method of the worker will:
        -   Get the selected directory and `allowUpdate` value from the UI components.
        -   Call `Extract.exploreFS()` with a new `IPhotoProcess` implementation.
        -   The `IPhotoProcess.task()` method will:
            -   Create a `PhotoObject`.
            -   Extract metadata.
            -   Add the photo to the index using `indexor.photos().add()`.
            -   Call `publish()` with the URI of the processed file to update the log.
    -   The `process(List<String> chunks)` method will append the file URIs to the `JTextArea`.
    -   The `done()` method will update the UI with the final result from `Extract.Result` and display any errors.

3.  **Integrate `IngestPanel` into `MainFrame.java`**
    -   In `MainFrame.java`, add a new field `private IngestPanel ingestPanel;`.
    -   In the `initialize` method, instantiate the `IngestPanel` and add it to the frame's content pane (e.g., `BorderLayout.EAST`).
    -   Set the `IngestPanel` to be invisible by default: `ingestPanel.setVisible(false);`.
    -   Add the `ingestPanel` to the list of `IndexorListener`s.
    -   Add a getter for the `ingestPanel`.

4.  **Modify `TinyWorldMenuLayer.java`**
    -   In the `selected` method, locate the `case TWLayerOperations.MENU_INDEX:`.
    -   Replace the `JOptionPane` with a call to toggle the visibility of the `IngestPanel`:
        ```java
        boolean panelStatus = this.frame.getIngestPanel().isVisible();
        this.frame.getIngestPanel().setVisible(!panelStatus);
        ```

### Testing Strategy
-   **Manual Testing**:
    1.  Run the application.
    2.  Click the "Index" button in the menu. The `IngestPanel` should appear.
    3.  Use the "Browse" button to select a directory with photos.
    4.  Check the "Allow Update" checkbox.
    5.  Click "Start Ingest".
    6.  Verify that the progress bar is active and the log area shows the files being processed.
    7.  Verify that the photos are added to the index (e.g., by checking the photo tree).
    8.  Verify that the final statistics are displayed correctly.
-   **Unit Testing**:
    -   It would be difficult to write a unit test for the `SwingWorker` and the UI interaction. However, the `IPhotoProcess` implementation could be tested separately to ensure it correctly calls the `Indexor`.

## 🎯 Success Criteria
- The "Index" button in the main menu opens a dedicated ingestion panel.
- The user can select a directory and start the ingestion process from the UI.
- The UI remains responsive during ingestion.
- The user receives visual feedback on the progress and result of the ingestion process.
- The new UI correctly uses the existing `Indexor` and `metadata-extractor` components.
