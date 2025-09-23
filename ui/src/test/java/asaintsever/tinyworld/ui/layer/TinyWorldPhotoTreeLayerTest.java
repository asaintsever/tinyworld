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
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import asaintsever.tinyworld.cfg.Configuration;
import asaintsever.tinyworld.indexor.IPhoto;
import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.indexor.search.results.TermsAggregation;
import asaintsever.tinyworld.ui.MainFrame;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.tree.BasicTreeNode;
import gov.nasa.worldwind.util.tree.TreeModel;

@ExtendWith(MockitoExtension.class)
public class TinyWorldPhotoTreeLayerTest {

    @Mock
    private MainFrame mainFrame;

    @Mock
    private WorldWindow worldWindow;

    @Mock
    private Globe globe;

    @Mock
    private Component glCanvas;

    @Mock
    private Configuration configuration;

    @Mock
    private Configuration.UI uiConfig;

    @Mock
    private Configuration.UI.PhotoTree photoTreeConfig;

    @Mock
    private Configuration.UI.PhotoTree.Filter filterConfig;

    @Mock
    private Indexor indexor;

    @Mock
    private IPhoto photos;

    @InjectMocks
    private TinyWorldPhotoTreeLayer photoTreeLayer;

    @BeforeEach
    void setUp() {
        when(mainFrame.getWwd()).thenReturn(worldWindow);
        when(worldWindow.getSceneController()).thenReturn(null); // Avoids NPE in RenderableLayer constructor
        when(worldWindow.getGlobe()).thenReturn(globe);
        when(globe.getGLCanvas()).thenReturn(glCanvas);
        when(mainFrame.getCfg()).thenReturn(configuration);
        when(configuration.ui).thenReturn(uiConfig);
        when(uiConfig.photoTree).thenReturn(photoTreeConfig);
        when(photoTreeConfig.filter).thenReturn(filterConfig);
    }

    @Test
    void testTreeConstruction() throws IOException {
        // --- Mock configuration ---
        when(filterConfig.template).thenReturn("year_month");

        // --- Mock Indexor response ---
        when(indexor.isConnected()).thenReturn(true);
        when(indexor.metadataIndex()).thenReturn(null); // Not used in this path
        when(indexor.photos()).thenReturn(photos);

        // --- Mock Aggregation response ---
        // Month aggregation
        TermsAggregation.Bucket monthBucket1 = new TermsAggregation.Bucket();
        monthBucket1.setKey("1");
        TermsAggregation.Bucket monthBucket2 = new TermsAggregation.Bucket();
        monthBucket2.setKey("2");
        TermsAggregation monthAgg = new TermsAggregation();
        monthAgg.setName("month");
        monthAgg.setBuckets(Arrays.asList(monthBucket1, monthBucket2));

        // Year aggregation
        TermsAggregation.Bucket yearBucket2022 = new TermsAggregation.Bucket();
        yearBucket2022.setKey("2022");
        yearBucket2022.setSubAggregations(Collections.singletonList(monthAgg));
        TermsAggregation.Bucket yearBucket2023 = new TermsAggregation.Bucket();
        yearBucket2023.setKey("2023");
        TermsAggregation yearAgg = new TermsAggregation();
        yearAgg.setName("year");
        yearAgg.setBuckets(Arrays.asList(yearBucket2022, yearBucket2023));

        when(photos.getAggregations("year_month")).thenReturn(List.of(yearAgg));

        // --- Trigger tree construction ---
        photoTreeLayer.created(indexor);

        // --- Asserts ---
        TreeModel treeModel = photoTreeLayer.photoTree.getModel();
        BasicTreeNode root = (BasicTreeNode) treeModel.getRoot();

        assertEquals(2, root.getChildCount());
        BasicTreeNode yearNode2023 = (BasicTreeNode) root.getChildAt(0);
        assertEquals("2023 (0)", yearNode2023.getText());
        BasicTreeNode yearNode2022 = (BasicTreeNode) root.getChildAt(1);
        assertEquals("2022 (0)", yearNode2022.getText());

        assertEquals(1, yearNode2022.getChildCount());
        BasicTreeNode monthNode = (BasicTreeNode) yearNode2022.getChildAt(0);
        assertEquals("month", monthNode.getText());
    }
}