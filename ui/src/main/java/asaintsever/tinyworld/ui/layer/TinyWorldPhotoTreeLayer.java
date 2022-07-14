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
package asaintsever.tinyworld.ui.layer;

import java.awt.Color;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.indexor.Indexor;
import asaintsever.tinyworld.ui.event.IndexorListener;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.util.tree.BasicFrameAttributes;
import gov.nasa.worldwind.util.tree.BasicTree;
import gov.nasa.worldwind.util.tree.BasicTreeAttributes;
import gov.nasa.worldwind.util.tree.BasicTreeLayout;
import gov.nasa.worldwind.util.tree.BasicTreeModel;
import gov.nasa.worldwind.util.tree.BasicTreeNode;

/**
 * 
 *
 */
public class TinyWorldPhotoTreeLayer extends RenderableLayer implements SelectListener, IndexorListener {

	protected static Logger logger = LoggerFactory.getLogger(TinyWorldPhotoTreeLayer.class);
    
    protected final static String LAYER_NAME = "TinyWorld Photo Tree";
    protected final static String ICON_PATH = "icon/tinyworldicon.jpg";
    
    protected BasicTree photoTree;
    protected Indexor indexor;
        
    
    public TinyWorldPhotoTreeLayer() {
    	// Mark the layer as hidden to prevent it being included in the layer tree's model
        this.setValue(AVKey.HIDDEN, true);
    }
    

	@Override
	public void selected(SelectEvent event) {
		if (event == null || event.isConsumed() || (event.getMouseEvent() != null && event.getMouseEvent().isConsumed()))
            return;
        
        if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() != this || !(event.getTopObject() instanceof BasicTreeNode))
            return;
        
        //logger.debug(event.toString());
        //logger.debug(event.getTopPickedObject() != null && event.getTopPickedObject().getParentLayer() != null ? event.getTopPickedObject().getParentLayer().getName() : "No Parent Layer");
        
        BasicTreeNode node = (BasicTreeNode)event.getTopObject();
        
        switch(event.getEventAction()) {
        case SelectEvent.LEFT_DOUBLE_CLICK:
        	// Add handling of double click for quicker/easier navigation between parent/child nodes
        	if (!node.isLeaf()) {
        		// Not a leaf: either expand or collapse node on double click
        		if (!this.photoTree.isNodeExpanded(node))
        			this.photoTree.expandPath(node.getPath());
        		else
        			this.photoTree.collapsePath(node.getPath());
        		}
        	break;
        case SelectEvent.LEFT_CLICK:
        	// Only consider leaf nodes. Allow for easier selection without having to precisely target the tick box on the node's left side
        	if (node.isLeaf()) {
        		boolean selectStatus = node.isSelected();
        		
        		// Update node selection status but also force its parent's one as there may be some discrepancies in some cases
        		node.setSelected(!selectStatus);
        		if (node.getParent() != null)
        			node.getParent().setSelected(!selectStatus);
        	}
        	break;
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
			this.photoTree = null;
		}
		
		this.photoTree = new BasicTree();
		
        BasicTreeLayout layout = new BasicTreeLayout(this.photoTree, 40, 140);
        layout.getFrame().setFrameTitle("Photos");
        layout.getFrame().setSize(Size.fromPixels(300, 600));	// TODO depending on screen resolution
        
        BasicTreeAttributes attributes = new BasicTreeAttributes();
        attributes.setRootVisible(false);	// Do not display root node
        layout.setAttributes(attributes);
        
        BasicFrameAttributes frameAttributes = new BasicFrameAttributes();
        frameAttributes.setBackgroundOpacity(0.8);
        frameAttributes.setTitleBarColor(new Color(29, 78, 169), new Color(93, 158, 223));	// TODO
        frameAttributes.setMinimizeButtonColor(new Color(0xEB9BA4));	// TODO
        layout.getFrame().setAttributes(frameAttributes);
        
        BasicTreeAttributes highlightAttributes = new BasicTreeAttributes(attributes);
        layout.setHighlightAttributes(highlightAttributes);
        
        BasicFrameAttributes highlightFrameAttributes = new BasicFrameAttributes(frameAttributes);
        highlightFrameAttributes.setForegroundOpacity(1.0);
        highlightFrameAttributes.setBackgroundOpacity(1.0);
        layout.getFrame().setHighlightAttributes(highlightFrameAttributes);

        this.photoTree.setLayout(layout);

        BasicTreeModel model = new BasicTreeModel();

        BasicTreeNode root = new BasicTreeNode("Root", ICON_PATH);
        model.setRoot(root);
        
 		if (this.indexor.isConnected()) {
 			try {
				if (!this.indexor.metadataIndex().exists())
					this.indexor.metadataIndex().create();
				
				// TODO default request on Indexor and dynamic photo tree construction (default request criteria to be added in config)
				
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
 		}

        /*BasicTreeNode child = new BasicTreeNode("Child 1", ICON_PATH);
        child.setDescription("This is a child node");
        child.addChild(new BasicTreeNode("Subchild 1,1"));
        child.addChild(new BasicTreeNode("Subchild 1,2"));
        child.addChild(new BasicTreeNode("Subchild 1,3", ICON_PATH));
        root.addChild(child);

        child = new BasicTreeNode("Child 2", ICON_PATH);
        child.addChild(new BasicTreeNode("Subchild 2,1"));
        child.addChild(new BasicTreeNode("Subchild 2,2"));
        child.addChild(new BasicTreeNode("Subchild 2,3"));
        root.addChild(child);

        child = new BasicTreeNode("Child 3");
        child.addChild(new BasicTreeNode("Subchild 3,1"));
        child.addChild(new BasicTreeNode("Subchild 3,2"));
        child.addChild(new BasicTreeNode("Subchild 3,3"));
        root.addChild(child);*/

        this.photoTree.setModel(model);

        this.photoTree.expandPath(root.getPath());
        
        // Add tree layout not tree itself (else, in selected(SelectEvent event) method, event.getTopPickedObject().getParentLayer() will be null whereas we want to filter on our layer)
        this.addRenderable(this.photoTree.getLayout());
	}
    
}
