package asaintsever.tinyworld.ui.layer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asaintsever.tinyworld.ui.UIStrings;
import asaintsever.tinyworld.ui.component.MainFrame;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.util.Logging;

/**
 * 
 *
 */
public class TinyWorldMenuLayer extends RenderableLayer implements SelectListener {
    
    protected static Logger logger = LoggerFactory.getLogger(TinyWorldMenuLayer.class);
    
    protected final static String LAYER_NAME = "TinyWorld Menu";
    
    protected final static String IMAGE_INDEX = "images/tw-index-48x48.png";
    protected final static String IMAGE_FILTER = "images/tw-filter-48x48.png";
    protected final static String IMAGE_SETTINGS = "images/tw-settings-48x48.png";
    
    protected MainFrame frame;
    
    // The annotations used to display the menu buttons.
    protected ScreenAnnotation buttonIndex;
    protected ScreenAnnotation buttonFilter;
    protected ScreenAnnotation buttonSettings;
    protected ScreenAnnotation currentButton;
    
    protected String position = AVKey.NORTHWEST;
    protected String layout = AVKey.HORIZONTAL;
    protected double scale = 1;
    protected int borderWidth = 36;
    protected int buttonSize = 48;
    protected boolean initialized = false;
    protected Rectangle referenceViewport;
    
    protected ScreenAnnotation pressedButton;
    protected String pressedButtonType;

    
    public TinyWorldMenuLayer(final MainFrame frame) {
        if (frame == null || frame.getWwd() == null) {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        this.frame = frame;
        
        // Mark the layer as hidden to prevent it being included in the layer tree's model
        this.setValue(AVKey.HIDDEN, true);
    }
    
    
    public int getBorderWidth() {
        return this.borderWidth;
    }
    
    /**
     * Sets the view controls offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the view controls from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        this.clearControls();
    }
    
    /**
     * Get the controls display scale.
     *
     * @return the controls display scale.
     */
    public double getScale() {
        return this.scale;
    }
    
    /**
     * Set the controls display scale.
     *
     * @param scale the controls display scale.
     */
    public void setScale(double scale) {
        this.scale = scale;
        this.clearControls();
    }
    
    /**
     * Returns the current relative view controls position.
     *
     * @return the current view controls position.
     */
    public String getPosition() {
        return this.position;
    }
    
    /**
     * Sets the relative viewport location to display the view controls. Can be one of {@link AVKey#NORTHEAST}, {@link
     * AVKey#NORTHWEST}, {@link AVKey#SOUTHEAST}, or {@link AVKey#SOUTHWEST}. These indicate the corner of
     * the viewport to place view controls.
     *
     * @param position the desired view controls position, in screen coordinates.
     */
    public void setPosition(String position) {
        if (position == null) {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        
        this.position = position;
        this.clearControls();
    }
    
    /**
     * Returns the current layout. Can be one of {@link AVKey#HORIZONTAL} or {@link AVKey#VERTICAL}.
     *
     * @return the current layout.
     */
    public String getLayout() {
        return this.layout;
    }
    
    /**
     * Sets the desired layout. Can be one of {@link AVKey#HORIZONTAL} or {@link AVKey#VERTICAL}.
     *
     * @param layout the desired layout.
     */
    public void setLayout(String layout) {
        if (layout == null) {
            String message = Logging.getMessage("nullValue.StringIsNull");
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        
        if (!this.layout.equals(layout)) {
            this.layout = layout;
            this.clearControls();
        }
    }
    
    /**
     * Layer opacity is not applied to layers of this type. Opacity is controlled by the alpha values of the operation
     * images.
     *
     * @param opacity the current opacity value, which is ignored by this layer.
     */
    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);
    }
    
    /**
     * Returns the layer's opacity value, which is ignored by this layer. Opacity is controlled by the alpha values of
     * the operation images.
     *
     * @return The layer opacity, a value between 0 and 1.
     */
    @Override
    public double getOpacity() {
        return super.getOpacity();
    }
    
    /**
     * Indicates the currently highlighted button, if any.
     *
     * @return the currently highlighted button, or null if no button is highlighted.
     */
    public Object getHighlightedObject() {
        return this.currentButton;
    }
    
    /**
     * Specifies the button to highlight. Any currently highlighted button is un-highlighted.
     *
     * @param control the button to highlight.
     */
    public void highlight(Object button) {
        // Manage highlighting of controls.
        if (this.currentButton == button)
            return; // same thing selected

        // Turn off highlight if on.
        if (this.currentButton != null) {
            this.currentButton.getAttributes().setImageOpacity(-1); // use default opacity
            this.currentButton = null;
        }

        // Turn on highlight if object selected.
        if (button != null && button instanceof ScreenAnnotation) {
            this.currentButton = (ScreenAnnotation) button;
            this.currentButton.getAttributes().setImageOpacity(1);
        }
    }
    
    @Override
    public void doRender(DrawContext dc) {
        if (!this.initialized)
            this.initialize(dc);

        if (!this.referenceViewport.equals(dc.getView().getViewport()))
            this.updatePositions(dc);

        super.doRender(dc);
    }
    
    @Override
    public void selected(SelectEvent event) {
        if (this.getHighlightedObject() != null) {
            this.highlight(null);
            this.frame.getWwd().redraw(); // must redraw so the de-highlight can take effect
        }
        
        if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed())
            return;
        
        if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() != this || !(event.getTopObject() instanceof AVList))
            return;
        
        //logger.debug("Mouse event: " + event.getEventAction());
        
        String menuOpType = ((AVList) event.getTopObject()).getStringValue(LayerOperations.MENU_OPERATION);
        if (menuOpType == null)
            return;

        ScreenAnnotation selectedObject = (ScreenAnnotation) event.getTopObject();
        
        switch(event.getEventAction()) {
        case SelectEvent.ROLLOVER:
        case SelectEvent.HOVER:
            // Highlight
            this.highlight(selectedObject);
            this.frame.getWwd().redraw();
            break;
        case SelectEvent.LEFT_PRESS:
            this.pressedButton = selectedObject;
            this.pressedButtonType = menuOpType;
            
            this.highlight(this.pressedButton);
            this.frame.getWwd().redraw();
            break;
        case SelectEvent.LEFT_CLICK:
            // Release pressed button
            if (this.pressedButton != null)
                event.consume();

            this.pressedButton = null;
            
            switch(menuOpType) {
            case LayerOperations.MENU_INDEX:
                JOptionPane.showMessageDialog(this.frame, "Not implemented yet", UIStrings.APP_NAME + " - " + UIStrings.MENU_INDEX_DISPLAYNAME, JOptionPane.INFORMATION_MESSAGE);
                break;
            case LayerOperations.MENU_FILTER:
                JOptionPane.showMessageDialog(this.frame, "Not implemented yet", UIStrings.APP_NAME + " - " + UIStrings.MENU_FILTER_DISPLAYNAME, JOptionPane.INFORMATION_MESSAGE);
                break;
            case LayerOperations.MENU_SETTINGS:
                if (this.frame.getSettingsPanel() != null) {
                    boolean panelStatus = this.frame.getSettingsPanel().isVisible();
                    this.frame.getSettingsPanel().setVisible(!panelStatus);
                } else {
                    logger.warn("SettingsPanel has not been set!");
                }
                break;
            }
            break;
        case SelectEvent.DRAG:
            event.consume();
            break;
        }
    }
    
    public void setToolTips(boolean enable) {
        // Enable/Disable tooltips (see ToolTipController in GlobePanel)
        if (this.buttonIndex != null) this.buttonIndex.setValue(AVKey.DISPLAY_NAME, enable ? UIStrings.MENU_INDEX_DISPLAYNAME : null);
        if (this.buttonFilter != null) this.buttonFilter.setValue(AVKey.DISPLAY_NAME, enable ? UIStrings.MENU_FILTER_DISPLAYNAME : null);
        if (this.buttonSettings != null) this.buttonSettings.setValue(AVKey.DISPLAY_NAME, enable ? UIStrings.MENU_SETTINGS_DISPLAYNAME : null);
    }
    
    @Override
    public String toString() {
        return LAYER_NAME;
    }
    
    
    protected boolean isInitialized() {
        return this.initialized;
    }
    
    protected void initialize(DrawContext dc) {
        if (this.initialized)
            return;

        // Setup user interface - common default attributes
        AnnotationAttributes ca = new AnnotationAttributes();
        ca.setAdjustWidthToText(AVKey.SIZE_FIXED);
        ca.setInsets(new Insets(0, 0, 0, 0));
        ca.setBorderWidth(0);
        ca.setCornerRadius(0);
        ca.setSize(new Dimension(buttonSize, buttonSize));
        ca.setBackgroundColor(new Color(0, 0, 0, 0));
        ca.setImageOpacity(.5);
        ca.setScale(scale);

        final String NOTEXT = "";
        final Point ORIGIN = new Point(0, 0);
        
        // Index
        this.buttonIndex = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
        this.buttonIndex.setValue(LayerOperations.MENU_OPERATION, LayerOperations.MENU_INDEX);
        this.buttonIndex.getAttributes().setImageSource(IMAGE_INDEX);
        this.addRenderable(this.buttonIndex);

        // Filter
        this.buttonFilter = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
        this.buttonFilter.setValue(LayerOperations.MENU_OPERATION, LayerOperations.MENU_FILTER);
        this.buttonFilter.getAttributes().setImageSource(IMAGE_FILTER);
        this.addRenderable(this.buttonFilter);

        // Settings
        this.buttonSettings = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
        this.buttonSettings.setValue(LayerOperations.MENU_OPERATION, LayerOperations.MENU_SETTINGS);
        this.buttonSettings.getAttributes().setImageSource(IMAGE_SETTINGS);
        this.addRenderable(this.buttonSettings);
        
        // Set tooltips on/off depending on actual choice in Settings panel
        this.setToolTips(this.frame.getSettingsPanel().isMenuTooltipEnabled());
        
        // Place controls according to layout and viewport dimension
        this.updatePositions(dc);

        this.initialized = true;
    }
    
    // Set controls positions according to layout and viewport dimension
    protected void updatePositions(DrawContext dc) {
        boolean horizontalLayout = this.layout.equals(AVKey.HORIZONTAL);

        // horizontal layout
        int width = 3 * this.buttonSize;
        int height = this.buttonSize;
        width = (int) (width * this.scale);
        height = (int) (height * this.scale);
        int xOffset = 0;
        int yOffset = (int) (this.buttonSize * this.scale);

        if (!horizontalLayout) {
         // vertical layout
            int temp = height;
            height = width;
            width = temp;
            xOffset = (int) (this.buttonSize * this.scale);
            yOffset = 0;
        }

        int halfButtonSize = (int) (this.buttonSize * this.scale / 2);

        Rectangle controlsRectangle = new Rectangle(width, height);
        Point locationSW = computeLocation(dc.getView().getViewport(), controlsRectangle);

        // Layout start point
        int x = locationSW.x;
        int y = horizontalLayout ? locationSW.y : locationSW.y + height;

        // Index
        if (!horizontalLayout)
            y -= (int) (this.buttonSize * this.scale);
        this.buttonIndex.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
        this.buttonIndex.setScreenPoint(new Point(x + halfButtonSize, y));
        if (horizontalLayout)
            x += (int) (this.buttonSize * this.scale);
        
        // Filter
        if (!horizontalLayout)
            y -= (int) (this.buttonSize * this.scale);
        this.buttonFilter.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
        this.buttonFilter.setScreenPoint(new Point(x + halfButtonSize, y));
        if (horizontalLayout)
            x += (int) (this.buttonSize * this.scale);
        
        // Settings
        if (!horizontalLayout)
            y -= (int) (this.buttonSize * this.scale);
        this.buttonSettings.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
        this.buttonSettings.setScreenPoint(new Point(x + halfButtonSize, y));
        if (horizontalLayout)
            x += (int) (this.buttonSize * this.scale);

        this.referenceViewport = dc.getView().getViewport();
    }
    
    /**
     * Compute the screen location of the controls overall rectangle bottom right corner according to 
     * the screen position.
     *
     * @param viewport the current viewport rectangle.
     * @param controls the overall controls rectangle
     *
     * @return the screen location of the bottom left corner - south west corner.
     */
    protected Point computeLocation(Rectangle viewport, Rectangle controls) {
        double x;
        double y;

        if (this.position.equals(AVKey.NORTHEAST)) {
            x = viewport.getWidth() - controls.width - this.borderWidth;
            y = viewport.getHeight() - controls.height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHEAST)) {
            x = viewport.getWidth() - controls.width - this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else if (this.position.equals(AVKey.NORTHWEST)) {
            x = 0d + this.borderWidth;
            y = viewport.getHeight() - controls.height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHWEST)) {
            x = 0d + this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else { // use North East as default
            x = viewport.getWidth() - controls.width - this.borderWidth;
            y = viewport.getHeight() - controls.height - this.borderWidth;
        }

        return new Point((int) x, (int) y);
    }
    
    protected void clearControls() {
        this.removeAllRenderables();

        this.buttonIndex = null;
        this.buttonFilter = null;
        this.buttonSettings = null;

        this.initialized = false;
    }
}

