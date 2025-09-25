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
package asaintsever.tinyworld.ui.layer;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.SwingUtilities;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.indexor.IPhoto;
import asaintsever.tinyworld.indexor.search.results.IndexPage;
import asaintsever.tinyworld.indexor.search.results.TermsAggregation;
import asaintsever.tinyworld.ui.MainFrame;
import asaintsever.tinyworld.ui.component.PhotoMetadataDialog;
import asaintsever.tinyworld.ui.event.IndexorListener;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.tree.BasicFrameAttributes;
import gov.nasa.worldwind.util.tree.BasicTree;
import gov.nasa.worldwind.util.tree.BasicTreeAttributes;
import gov.nasa.worldwind.util.tree.BasicTreeLayout;
import gov.nasa.worldwind.util.tree.BasicTreeModel;
import gov.nasa.worldwind.util.tree.BasicTreeNode;
import gov.nasa.worldwind.util.tree.TreeNode;
import gov.nasa.worldwind.view.orbit.OrbitView;

/**
 *
 *
 */
public class TinyWorldPhotoTreeLayer extends RenderableLayer
        implements SelectListener, IndexorListener, PropertyChangeListener {

    protected static Logger logger = LoggerFactory.getLogger(TinyWorldPhotoTreeLayer.class);

    protected final static String LAYER_NAME = "TinyWorld - Photo Tree";
    protected final static String ICON_PATH = "icon/tinyworldicon.jpg";

    protected MainFrame frame;
    protected BasicTree photoTree;
    protected Indexor indexor;

    protected String treeTemplateId;
    protected List<String> treeTemplateFields;

    protected List<PointPlacemark> currentPlacemarks = new ArrayList<>();

    public TinyWorldPhotoTreeLayer(final MainFrame frame) {
        if (frame == null || frame.getWwd() == null) {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.frame = frame;

        // Mark the layer as hidden to prevent it being included in the layer tree's model
        this.setValue(AVKey.HIDDEN, true);
    }

    @Override
    public void selected(SelectEvent event) {
        if (event == null || event.isConsumed()
                || (event.getMouseEvent() != null && event.getMouseEvent().isConsumed()))
            return;

        // Discard rollover and hover events
        if (Set.of(SelectEvent.ROLLOVER, SelectEvent.HOVER).contains(event.getEventAction()))
            return;

        if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() != this
                || !(event.getTopObject() instanceof BasicTreeNode))
            return;

//        logger.debug(event.toString());
//        logger.debug(event.getTopPickedObject() != null && event.getTopPickedObject().getParentLayer() != null
//                ? event.getTopPickedObject().getParentLayer().getName()
//                : "No Parent Layer");

        BasicTreeNode node = (BasicTreeNode) event.getTopObject();

        switch (event.getEventAction()) {
        case SelectEvent.LEFT_DOUBLE_CLICK:
            // Add handling of double click for quicker/easier navigation between parent/child nodes
            if (node.isLeaf()) {
                PhotoMetadata metadata = (PhotoMetadata) node.getValue("photo_metadata");

                if (metadata != null) {
                    // Open a modal dialog with the photo metadata
                    SwingUtilities.invokeLater(() -> {
                        PhotoMetadataDialog dialog = new PhotoMetadataDialog(this.frame, metadata);
                        dialog.setVisible(true);
                    });
                }
            } else {
                // Not a leaf: either expand or collapse node on double click
                if (!this.photoTree.isNodeExpanded(node)) {
                    this.photoTree.expandPath(node.getPath());
                } else {
                    this.photoTree.collapsePath(node.getPath());
                }
            }
            break;
        case SelectEvent.LEFT_PRESS: // listen for left press instead of SelectEvent.LEFT_CLICK to avoid conflict with
                                     // SelectEvent.DRAG
            List<PhotoMetadata> photosToDisplay = new ArrayList<>();

            // Only consider leaf nodes. Allow for easier selection without having to precisely target the tick
            // box on the node's left side
            if (node.isLeaf()) {
                boolean selectStatus = node.isSelected();

                // Update node selection status but also force its parents' one as there may be some discrepancies
                // in some cases
                node.setSelected(!selectStatus);

                BasicTreeNode tmp_node = node;
                while (tmp_node.getParent() != null && tmp_node.getParent() != this.photoTree.getModel().getRoot()) {
                    tmp_node = (BasicTreeNode) tmp_node.getParent();
                    tmp_node.setSelected(!selectStatus);
                }

                // A leaf node is a photo, center the globe on it
                PhotoMetadata metadata = (PhotoMetadata) node.getValue("photo_metadata");

                if (metadata != null) {
                    photosToDisplay.add(metadata);
                }
            } else {
                collectPhotos(node, photosToDisplay);
            }

            displayPhotos(photosToDisplay);
            this.frame.getWwd().redraw();
            break;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) {
            return;
        }

        if (AVKey.TREE.equals(evt.getPropertyName())) {
            Object newValue = evt.getNewValue();
            if (newValue instanceof BasicTree tree) {
                if (tree.getModel() != null && tree.getModel().getRoot() != null) {
                    // Iterate over all nodes to find expanded ones
                    for (TreeNode childNode : tree.getModel().getRoot().getChildren()) {
                        this.checkNodeAndLoad(tree, (BasicTreeNode) childNode);
                    }
                }
            }
        }
    }

    @Override
    public void created(Indexor indexor) {
        this.indexor = indexor;
        this.initialize();
    }

    @Override
    public String toString() {
        return LAYER_NAME;
    }

    protected void initialize() {
        if (this.photoTree != null && this.photoTree.getLayout() != null) {
            this.removeRenderable(this.photoTree.getLayout());
        }

        this.photoTree = new BasicTree();
        this.photoTree.addPropertyChangeListener(this);

        BasicTreeLayout layout = new BasicTreeLayout(this.photoTree, 40, 140);
        layout.getFrame().setFrameTitle("Photos");
        layout.getFrame().setSize(Size.fromPixels((int) (this.frame.getGlobe().getGLCanvas().getWidth() * 0.18),
                (int) (this.frame.getGlobe().getGLCanvas().getHeight() * 0.7))); // Depending on width/height of Globe
                                                                                 // GL canvas

        BasicTreeAttributes attributes = new BasicTreeAttributes();
        attributes.setRootVisible(false); // Do not display root node
        layout.setAttributes(attributes);

        BasicFrameAttributes frameAttributes = new BasicFrameAttributes();
        frameAttributes.setBackgroundOpacity(0.8);
        frameAttributes.setTitleBarColor(new Color(51, 51, 77), new Color(194, 194, 214));
        frameAttributes.setMinimizeButtonColor(new Color(102, 163, 255));
        layout.getFrame().setAttributes(frameAttributes);

        BasicTreeAttributes highlightAttributes = new BasicTreeAttributes(attributes);
        layout.setHighlightAttributes(highlightAttributes);

        BasicFrameAttributes highlightFrameAttributes = new BasicFrameAttributes(frameAttributes);
        highlightFrameAttributes.setForegroundOpacity(1.0);
        highlightFrameAttributes.setBackgroundOpacity(1.0);
        layout.getFrame().setHighlightAttributes(highlightFrameAttributes);

        this.photoTree.setLayout(layout);

        BasicTreeModel model = new BasicTreeModel();

        BasicTreeNode root = new BasicTreeNode("Root");
        model.setRoot(root);

        if (this.indexor.isConnected()) {
            try {
                if (!this.indexor.metadataIndex().exists())
                    this.indexor.metadataIndex().create();

                this.treeTemplateId = this.frame.getCfg().ui.photoTree.filter.template;
                this.treeTemplateFields = Arrays.asList(this.treeTemplateId.split("_"));

                List<TermsAggregation> aggregations = this.indexor.photos().getAggregations(this.treeTemplateId);
                if (aggregations != null) {
                    this.buildTree(aggregations, root, 0);
                }

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        this.photoTree.setModel(model);

        this.photoTree.expandPath(root.getPath());

        // Add tree layout not tree itself (else, in selected(SelectEvent event) method,
        // event.getTopPickedObject().getParentLayer() will be null whereas we want to filter on our layer)
        this.addRenderable(this.photoTree.getLayout());
    }

    private void buildTree(List<TermsAggregation> aggregations, BasicTreeNode parentNode, int fieldIndex) {
        if (fieldIndex >= this.treeTemplateFields.size()) {
            return;
        }

        String field = this.treeTemplateFields.get(fieldIndex);
        TermsAggregation termsAgg = aggregations.stream().filter(agg -> field.equals(agg.getName())).findFirst()
                .orElse(null);

        if (termsAgg != null) {
            List<String> keys = termsAgg.getBuckets().stream().map(TermsAggregation.Bucket::getKey)
                    .collect(Collectors.toList());
            sortKeys(keys, field);

            for (String key : keys) {
                TermsAggregation.Bucket bucket = termsAgg.getBuckets().stream().filter(b -> key.equals(b.getKey()))
                        .findFirst().orElse(null);
                if (bucket == null)
                    continue;

                long docCount = bucket.getDoc_count();

                String displayKey = key;
                if ("month".equals(field)) {
                    try {
                        displayKey = getMonthName(Integer.parseInt(key));
                    } catch (NumberFormatException e) {
                        // Keep original key if not a number
                    }
                }

                BasicTreeNode node = new BasicTreeNode(String.format("%s (%d)", displayKey, docCount));
                node.setValue("agg_key", key); // Store original key
                parentNode.addChild(node);

                List<TermsAggregation> subAggs = bucket.getSubAggregations();
                if (fieldIndex < this.treeTemplateFields.size() - 1 && subAggs != null && !subAggs.isEmpty()) {
                    buildTree(subAggs, node, fieldIndex + 1);
                } else {
                    // Add a dummy node to allow expansion and loading of photos
                    node.addChild(new BasicTreeNode("..."));
                }
            }
        }
    }

    private String getMonthName(int month) {
        // Calendar month is 0-based, so we need to subtract 1
        if (month >= 1 && month <= 12) {
            return new DateFormatSymbols().getShortMonths()[month - 1];
        }
        return String.valueOf(month);
    }

    private String getQueryField(String templateField) {
        switch (templateField) {
        case "country":
            return "country.keyword";
        case "year":
            return "takenYear";
        case "month":
            return "takenMonth";
        default:
            return templateField;
        }
    }

    private void checkNodeAndLoad(BasicTree tree, BasicTreeNode node) {
        if (tree.isNodeExpanded(node)) {
            // if node is at last aggregation level, load photos
            if (node.getPath().size() - 1 == this.treeTemplateFields.size()) {
                try {
                    loadPhotoNodes(node);
                } catch (IOException e) {
                    logger.error("Error loading photo nodes", e);
                }
            }

            for (TreeNode childNode : node.getChildren()) {
                this.checkNodeAndLoad(tree, (BasicTreeNode) childNode);
            }
        }
    }

    private void loadPhotoNodes(BasicTreeNode parentNode) throws IOException {
        List<String> path = new ArrayList<>();
        TreeNode current = parentNode;
        // Build path from root to current node
        while (current != null && current != this.photoTree.getModel().getRoot()) {
            path.add(0, (String) ((BasicTreeNode) current).getValue("agg_key"));
            current = current.getParent();
        }

        StringBuilder query = new StringBuilder("{\"bool\": {\"must\": [");
        for (int i = 0; i < path.size(); i++) {
            String field = getQueryField(this.treeTemplateFields.get(i));
            String value = path.get(i);
            if (field.equals("takenYear") || field.equals("takenMonth")) {
                query.append("{\"term\": {\"" + field + "\": " + value + "}},");
            } else {
                query.append("{\"term\": {\"" + field + "\": \"" + value + "\"}},");
            }
        }
        query.deleteCharAt(query.length() - 1); // remove last comma
        query.append("]}}");

        IPhoto photoIndexer = this.indexor.photos();
        IndexPage<PhotoMetadata> searchResponse = photoIndexer.search(query.toString(), 0, 1000); // TODO handle
                                                                                                  // pagination if >
                                                                                                  // 1000 photos

        if (searchResponse != null && searchResponse.get() != null) {
            List<PhotoMetadata> photosFromIndex = new ArrayList<>(searchResponse.get());

            // Get existing photo paths from tree
            Set<String> existingPhotoPaths = StreamSupport.stream(parentNode.getChildren().spliterator(), false)
                    .filter(c -> ((BasicTreeNode) c).getValue("photo_metadata") != null)
                    .map(c -> ((PhotoMetadata) ((BasicTreeNode) c).getValue("photo_metadata")).getPath().toString())
                    .collect(Collectors.toSet());

            // Get paths from index
            Set<String> indexPhotoPaths = photosFromIndex.stream().map(metadata -> metadata.getPath().toString())
                    .collect(Collectors.toSet());

            Iterator<TreeNode> childrenIterator = parentNode.getChildren().iterator();
            boolean isInitialLoad = false;
            if (childrenIterator.hasNext()) {
                TreeNode firstChild = childrenIterator.next();
                if ("...".equals(firstChild.getText()) && !childrenIterator.hasNext()) {
                    isInitialLoad = true;
                }
            }

            if (isInitialLoad || !existingPhotoPaths.equals(indexPhotoPaths)) {
                // Remove dummy node or all existing photos
                parentNode.removeAllChildren();

                photosFromIndex.sort(Comparator.comparing(PhotoMetadata::getFileName));

                for (PhotoMetadata metadata : photosFromIndex) {
                    if (metadata != null) {
                        BasicTreeNode photoNode = new BasicTreeNode(metadata.getFileName(), ICON_PATH);
                        photoNode.setValue("photo_metadata", metadata);
                        parentNode.addChild(photoNode);
                    }
                }
            }
        }
    }

    private void sortKeys(List<String> keys, String field) {
        if ("country".equals(field)) {
            Collections.sort(keys);
        } else if ("year".equals(field)) {
            keys.sort(Collections.reverseOrder());
        } else if ("month".equals(field)) {
            keys.sort(Comparator.comparingInt(Integer::parseInt));
        }
    }

    private void displayPhotos(List<PhotoMetadata> photos) {
        clearPlacemarks();

        for (PhotoMetadata metadata : photos) {
            if (metadata.getGpsLatLong() != null) {
                String[] latlon = metadata.getGpsLatLong().split(",");
                Position pos = Position.fromDegrees(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]), 1000);

                PointPlacemark placemark = new PointPlacemark(pos);
                placemark.setLabelText(metadata.getFileName());
                this.addRenderable(placemark);
                this.currentPlacemarks.add(placemark);
            }
        }

        if (!photos.isEmpty()) {
            PhotoMetadata firstPhotoMetadata = photos.get(0);
            if (firstPhotoMetadata.getGpsLatLong() != null) {
                String[] latlon = firstPhotoMetadata.getGpsLatLong().split(",");
                Position pos = Position.fromDegrees(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]), 1000);
                OrbitView view = (OrbitView) this.frame.getWwd().getView();
                view.goTo(pos, 2000e3);
            }
        }
    }

    private void clearPlacemarks() {
        for (PointPlacemark placemark : this.currentPlacemarks) {
            this.removeRenderable(placemark);
        }
        this.currentPlacemarks.clear();
    }

    private void collectPhotos(BasicTreeNode node, List<PhotoMetadata> photos) {
        if (node.isLeaf()) {
            // ignore dummy node
            if ("...".equals(node.getText())) {
                return;
            }
            PhotoMetadata metadata = (PhotoMetadata) node.getValue("photo_metadata");
            if (metadata != null) {
                photos.add(metadata);
            }
        } else {
            for (TreeNode child : node.getChildren()) {
                collectPhotos((BasicTreeNode) child, photos);
            }
        }
    }
}
