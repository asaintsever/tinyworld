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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingWorker;

import asaintsever.tinyworld.ui.UIStrings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

public class SplashScreen extends JWindow {

    public SplashScreen(Dimension size) {
        this.initialize(size);
    }

    protected void initialize(Dimension size) {
        JPanel content = (JPanel) this.getContentPane();
        content.setBackground(Color.BLUE);

        // Set the window's bounds, centering the window
        int width = 512;
        int height = 512;
        int x = (size.width - width) / 2;
        int y = (size.height - height) / 2;
        this.setBounds(x, y, width, height);

        // Build the splash screen
        JLabel label = new JLabel(new ImageIcon(getClass().getResource("/images/tinyworld.jpeg")));
        JLabel app = new JLabel(UIStrings.APP_NAME, JLabel.CENTER) {
            /*
             * This code overrides the paintComponent method of the JLabel to first draw the outline (in black)
             * and then draw the main text (using original color). The outline is drawn by painting the text
             * multiple times at different offsets around the original position.
             *
             * The setOpaque(false) call is necessary to prevent the label from painting its background, which
             * would otherwise cover up the outline.
             */
            @Override
            public void paintComponent(Graphics g) {
                Color originalColor = g.getColor();
                g.setColor(Color.BLACK);
                for (int i = -2; i <= 2; i++) {
                    for (int j = -2; j <= 2; j++) {
                        g.drawString(getText(), i + getWidth() / 2 - g.getFontMetrics().stringWidth(getText()) / 2,
                                j + getHeight() / 2 + g.getFontMetrics().getHeight() / 4);
                    }
                }
                g.setColor(originalColor);
                super.paintComponent(g);
            }
        };
        JLabel copyright = new JLabel(UIStrings.APP_COPYRIGHT, JLabel.RIGHT);

        content.add(label, BorderLayout.CENTER);

        label.setLayout(new BorderLayout());
        Font font = app.getFont();
        app.setFont(font.deriveFont(Font.BOLD, 48f));
        app.setForeground(Color.WHITE);
        app.setOpaque(false);

        label.add(app, BorderLayout.CENTER);
        label.add(copyright, BorderLayout.PAGE_END);

        content.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
    }

    public void display() {
        this.setVisible(true);
        this.toFront();

        new SplashLoader().execute();
    }

    public class SplashLoader extends SwingWorker<Object, Object> {
        @Override
        protected Object doInBackground() throws Exception {
            try {
                Thread.sleep(4000);
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void done() {
            setVisible(false);
        }
    }
}
