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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.indexor.IIndex;
import asaintsever.tinyworld.indexor.IPhoto;
import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.indexor.search.results.TermsAggregation;
import asaintsever.tinyworld.ui.MainFrame;
import asaintsever.tinyworld.ui.component.GlobeGLCanvas;
import asaintsever.tinyworld.ui.component.GlobePanel;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.tree.BasicTreeNode;
import gov.nasa.worldwind.util.tree.TreeModel;
import gov.nasa.worldwind.util.tree.TreeNode;

public class TinyWorldPhotoTreeLayerTest {

    private MainFrame mainFrame;
    private WorldWindow worldWindow;
    private View view;
    private GlobePanel globePanel;
    private Globe globe;
    private GlobeGLCanvas glCanvas;
    private Configuration configuration;
    private Indexor indexor;
    private IIndex indexMock;
    private IPhoto photos;
    private TinyWorldPhotoTreeLayer photoTreeLayer;

    @BeforeEach
    void setUp() {
        mainFrame = Mockito.mock(MainFrame.class);
        worldWindow = Mockito.mock(WorldWindow.class);
        view = Mockito.mock(View.class);
        globePanel = Mockito.mock(GlobePanel.class);
        globe = Mockito.mock(Globe.class);
        glCanvas = Mockito.mock(GlobeGLCanvas.class);
        indexor = Mockito.mock(Indexor.class);
        indexMock = Mockito.mock(IIndex.class);
        photos = Mockito.mock(IPhoto.class);

        configuration = new Configuration();
        Configuration.UI ui = configuration.new UI();
        Configuration.UI.PhotoTree photoTree = ui.new PhotoTree();
        configuration.ui = ui;
        configuration.ui.photoTree = photoTree;
        configuration.ui.photoTree.filter = photoTree.new Filter();

        Mockito.when(mainFrame.getWwd()).thenReturn(worldWindow);
        Mockito.when(mainFrame.getGlobe()).thenReturn(globePanel);
        Mockito.when(globePanel.getGLCanvas()).thenReturn(glCanvas);
        Mockito.when(worldWindow.getView()).thenReturn(view);
        Mockito.when(mainFrame.getCfg()).thenReturn(configuration);
        Mockito.when(indexor.metadataIndex()).thenReturn(indexMock);

        photoTreeLayer = new TinyWorldPhotoTreeLayer(mainFrame);
    }

    @Test
    void testTreeConstruction() throws IOException {
        // --- Mock configuration ---
        configuration.ui.photoTree.filter.template = "year_month";

        // --- Mock Indexor response ---
        Mockito.when(indexor.isConnected()).thenReturn(true);
        Mockito.when(indexMock.exists()).thenReturn(true);
        Mockito.when(indexor.photos()).thenReturn(photos);

        // --- Mock Aggregation response ---
        // Month aggregation
        TermsAggregation monthAgg = new TermsAggregation();
        TermsAggregation.Bucket monthBucket1 = monthAgg.new Bucket();
        monthBucket1.setKey("1");
        TermsAggregation.Bucket monthBucket2 = monthAgg.new Bucket();
        monthBucket2.setKey("2");
        monthAgg.setName("month");
        monthAgg.setBuckets(Arrays.asList(monthBucket1, monthBucket2));

        // Year aggregation
        TermsAggregation yearAgg = new TermsAggregation();
        TermsAggregation.Bucket yearBucket2022 = yearAgg.new Bucket();
        yearBucket2022.setKey("2022");
        yearBucket2022.setSubAggregations(Collections.singletonList(monthAgg));
        TermsAggregation.Bucket yearBucket2023 = yearAgg.new Bucket();
        yearBucket2023.setKey("2023");
        yearAgg.setName("year");
        yearAgg.setBuckets(Arrays.asList(yearBucket2022, yearBucket2023));

        Mockito.when(photos.getAggregations("year_month")).thenReturn(List.of(yearAgg));

        // --- Trigger tree construction ---
        photoTreeLayer.created(indexor);

        // --- Asserts ---
        TreeModel treeModel = photoTreeLayer.photoTree.getModel();
        BasicTreeNode root = (BasicTreeNode) treeModel.getRoot();

        List<TreeNode> yearNodes = new ArrayList<>();
        root.getChildren().forEach(yearNodes::add);
        assertEquals(2, yearNodes.size());

        BasicTreeNode yearNode2023 = (BasicTreeNode) yearNodes.get(0);
        assertEquals("2023 (0)", yearNode2023.getText());
        BasicTreeNode yearNode2022 = (BasicTreeNode) yearNodes.get(1);
        assertEquals("2022 (0)", yearNode2022.getText());

        List<TreeNode> monthNodes = new ArrayList<>();
        yearNode2022.getChildren().forEach(monthNodes::add);
        assertEquals(2, monthNodes.size());
        BasicTreeNode monthNode1 = (BasicTreeNode) monthNodes.get(0);
        assertEquals("1 (0)", monthNode1.getText());
        BasicTreeNode monthNode2 = (BasicTreeNode) monthNodes.get(1);
        assertEquals("2 (0)", monthNode2.getText());
    }
}
