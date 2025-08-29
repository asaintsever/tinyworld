# Feature Implementation Plan: Photo Treeview

## 📋 Todo Checklist
- [ ] Implement dynamic photo tree construction in `TinyWorldPhotoTreeLayer`.
- [ ] Handle user interaction for expanding/collapsing nodes.
- [ ] Final Review and Testing.

## 🔍 Analysis & Investigation

### Codebase Structure
The investigation focused on the `ui`, `indexor`, and `metadata-extractor` modules.
- **`ui/src/main/java/asaintsever/tinyworld/ui/MainFrame.java`**: The main application frame, which instantiates and displays UI components.
- **`ui/src/main/java/asaintsever/tinyworld/ui/layer/TinyWorldPhotoTreeLayer.java`**: An existing, but incomplete, implementation of a photo tree layer using NASA WorldWind's `BasicTree`. It includes a `TODO` to dynamically construct the tree.
- **`indexor/src/main/java/asaintsever/tinyworld/indexor/Indexor.java`**: Provides an API to interact with an OpenSearch index, including a `getAggregations` method that can be used to fetch hierarchical data.
- **`metadata-extractor/src/main/java/asaintsever/tinyworld/metadata/extractor/PhotoObject.java`**: Defines the `PhotoMetadata` object, which contains the data that will be displayed in the tree.

### Current Architecture
The application uses a layered architecture. The UI part is built with Java Swing and NASA WorldWind. The `TinyWorldPhotoTreeLayer` is a `RenderableLayer` that is added to the WorldWind globe. Data is provided by the `Indexor` component, which queries an OpenSearch database.

The `TinyWorldPhotoTreeLayer` is already integrated into the `MainFrame`, but it doesn't display any data. The plan is to populate it using data from the `Indexor`.

### Dependencies & Integration Points
The main integration point is between the `TinyWorldPhotoTreeLayer` and the `Indexor`. The layer needs to call the indexor to get the data for the tree. The `IndexorListener` interface is already implemented by the layer, so the `created(Indexor indexor)` method is the perfect place to trigger the data loading.

No new external dependencies are required.

### Considerations & Challenges
- The OpenSearch aggregation queries need to be chosen carefully to provide a good user experience. The existing search templates (`country_year_month`, `year_country_month`, `year_month`) are a good starting point.
- The tree can potentially contain a large number of nodes. The implementation should be efficient to avoid UI freezes.
- User interaction (clicking, double-clicking) on the tree nodes needs to be handled to expand/collapse branches and potentially to display the photos on the globe. The `selected` method in `TinyWorldPhotoTreeLayer` already provides a basic implementation for this.

## 📝 Implementation Plan

### Prerequisites
- A running OpenSearch instance with some indexed photos.

### Step-by-Step Implementation
1. **Step 1: Implement dynamic photo tree construction**
   - **Files to modify**: `ui/src/main/java/asaintsever/tinyworld/ui/layer/TinyWorldPhotoTreeLayer.java`
   - **Changes needed**:
     - In the `initialize()` method, after the `indexor` is available, read the `ui.photoTree.filter.template` from the configuration.
     - Call `this.indexor.photos().getAggregations(templateId)` to fetch the aggregation results.
     - Create a recursive method that takes a list of `TermsAggregation` and a `BasicTreeNode` as input. This method will iterate over the aggregations and create the tree structure.
     - The hierarchy of the tree will depend on the search template used (e.g., Country -> Year -> Month).
     - The leaf nodes can represent the final aggregation level (e.g., month), and could show the number of photos in that group.

2. **Step 2: Refine user interaction**
   - **Files to modify**: `ui/src/main/java/asaintsever/tinyworld/ui/layer/TinyWorldPhotoTreeLayer.java`
   - **Changes needed**:
     - Review and enhance the `selected(SelectEvent event)` method to handle the dynamically created tree.
     - Ensure that expanding and collapsing nodes works as expected.
     - For leaf nodes, consider what action should be performed on click or double-click. For example, it could trigger a search to display the photos of that group on the globe.

### Testing Strategy
- **Unit Tests**: Add unit tests for the tree construction logic in `TinyWorldPhotoTreeLayer`. Mock the `Indexor` to provide sample aggregation results and verify that the tree is built correctly.
- **Manual Testing**:
  - Run the application and verify that the photo tree is displayed.
  - Check that the tree structure corresponds to the configured search template.
  - Test expanding and collapsing nodes.
  - Test with a large number of photos to check for performance issues.
  - Test with an empty index to ensure the application handles it gracefully.

## 🎯 Success Criteria
- The photo tree view is populated with data from the OpenSearch index.
- The tree structure is hierarchical and reflects the chosen aggregation (e.g., by country, year, month).
- The user can interact with the tree to expand and collapse nodes.
- The feature is documented and tested.
