# Photo Ingestion UI Plan

This document outlines the plan to implement a new photo ingestion UI in TinyWorld, based on the logic from `IndexorCmd`.

## 1. Create `IngestPanel`

- Create a new class `IngestPanel` that extends `javax.swing.JPanel`.
- This panel will contain:
    - A `JButton` to open a `JFileChooser` for selecting the root directory to ingest.
    - A `JLabel` to display the selected directory path.
    - A `JCheckBox` for the `allowUpdate` flag, allowing users to choose whether to overwrite existing photo metadata in the index.
    - A `JProgressBar` (likely indeterminate) to show that the ingestion process is running.
    - A `JTextArea` (within a `JScrollPane`) to log the ingestion progress, including the file currently being processed and any errors.
    - A `JButton` to start the ingestion process.

## 2. Connect `IngestPanel` to Index Button

- In `TinyWorldMenuLayer.java`, modify the `selected` method.
- The `case TWLayerOperations.MENU_INDEX:` will be changed to make the `IngestPanel` visible. This will likely involve calling a method on the `MainFrame` to show the panel.

## 3. Implement Ingestion Logic

- The core logic will be triggered by the "Start Ingest" button in the `IngestPanel`.
- It will create and execute a `SwingWorker` to prevent blocking the UI.
- The `SwingWorker`'s `doInBackground()` method will call `asaintsever.tinyworld.metadata.extractor.Extract.exploreFS()`.
- This call will be provided with a new implementation of the `asaintsever.tinyworld.metadata.extractor.IPhotoProcess` interface.
- The `IPhotoProcess.task(URI uri, FileType fileType, Metadata metadata)` method will:
    1. Create a `PhotoObject` instance.
    2. Call `photo.extractMetadata(uri, fileType, metadata)` to get the `PhotoMetadata`.
    3. Get the `IPhoto` interface from the main `Indexor` instance via `indexor.photos()`.
    4. Call `photos.add(metadata, allowUpdate)`, using the value from the `JCheckBox`.
    5. Use the `SwingWorker.publish()` method to send the URI of the processed file back to the UI for logging.

## 4. Background Processing & Real-time Feedback

- The `SwingWorker` is essential for keeping the UI responsive.
- The `process(List<V> chunks)` method of the `SwingWorker` will receive file URIs from the `publish()` call and append them to the `JTextArea` log.
- The `done()` method of the `SwingWorker` will be called when `exploreFS` completes.
    - It will retrieve the `Extract.Result` object returned by `exploreFS`.
    - It will update the UI with the final statistics: number of photos ingested, skipped, and errors.
    - It will log any detailed error messages from `res.getErrorMsg()` into the `JTextArea`.

## 5. Component Integration

- The `IngestPanel` will be instantiated in the `UI` class and added to the `MainFrame`, likely kept hidden by default.
- The `UI` class will pass the application's single `Indexor` instance to the `IngestPanel` upon creation.
- The `MainFrame` will need a method like `getIngestPanel().setVisible(true)` that can be called from the `TinyWorldMenuLayer` to show the panel.