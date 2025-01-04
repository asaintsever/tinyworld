/*
 * Copyright 2021-2024 A. Saint-Sever
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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

/**
 *
 *
 */
@SuppressWarnings("serial")
public class GlobeGLCanvas extends WorldWindowGLCanvas {

    @Override
    public int getWidth() {
        // The pixel scale is already taken into account by the super.getWidth() method
        // No need to multiply by the pixel scale using getPixelScaleX()
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        // The pixel scale is already taken into account by the super.getHeight() method
        // No need to multiply by the pixel scale using getPixelScaleY()
        return super.getHeight();
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        int x = (int) (e.getPoint().x * this.getPixelScaleX());
        int y = (int) (e.getPoint().y * this.getPixelScaleY());

        MouseEvent scaledEvent = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiersEx(),
                x, y, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
        super.processMouseEvent(scaledEvent);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        int x = (int) (e.getPoint().x * this.getPixelScaleX());
        int y = (int) (e.getPoint().y * this.getPixelScaleY());

        MouseEvent scaledEvent = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiersEx(),
                x, y, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
        super.processMouseMotionEvent(scaledEvent);
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        int x = (int) (e.getPoint().x * this.getPixelScaleX());
        int y = (int) (e.getPoint().y * this.getPixelScaleY());

        MouseWheelEvent scaledEvent = new MouseWheelEvent((Component) e.getSource(), e.getID(), e.getWhen(),
                e.getModifiersEx(), x, y, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(),
                e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());
        super.processMouseWheelEvent(scaledEvent);
    }

    protected double getPixelScaleX() {
        // Must get scale of *current* screen device, not default one as our app may be displayed on another
        // screen (on multi-screens config)
        Graphics2D g2d = (Graphics2D) getGraphics();
        AffineTransform globalTransform = g2d.getTransform();
        return globalTransform.getScaleX();
    }

    protected double getPixelScaleY() {
        // Must get scale of *current* screen device, not default one as our app may be displayed on another
        // screen (on multi-screens config)
        Graphics2D g2d = (Graphics2D) getGraphics();
        AffineTransform globalTransform = g2d.getTransform();
        return globalTransform.getScaleY();
    }
}
