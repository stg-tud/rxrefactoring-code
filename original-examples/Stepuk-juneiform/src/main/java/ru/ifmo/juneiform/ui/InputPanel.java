/*
 * Copyright 2012 The Juneiform Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ifmo.juneiform.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import ru.ifmo.juneiform.Document;
import ru.ifmo.juneiform.Overlay;
import ru.ifmo.juneiform.OverlayType;

/**
 *
 * @author Oleg Kuznetsov
 */
public class InputPanel extends JPanel {
    private static Logger log = Logger.getLogger(InputPanel.class);
    private BufferedImage original;
    private Image scaled;
    private Document document;
    private boolean cleanUpRequired;
    public static InputPanel uniqueInstance = new InputPanel();
    private MouseCoordinatesListener mouseCoordinatesListener = MouseCoordinatesListener.getInstance();

    private InputPanel() {
        this.addMouseListener(mouseCoordinatesListener);
        this.addMouseMotionListener(mouseCoordinatesListener);
    }
    
    public static InputPanel getInstance() {
        return uniqueInstance;
    }
        
    public void renderDocument(Document document) {
        this.document = document;
        cleanUpRequired = false;
        original = (BufferedImage) this.document.getImage();
        
        scale(document.getZoomFactor());
        repaint();
    }

    private void scale(double factor) {
        int width = (int) (original.getWidth() * factor);
        int height = (int) (original.getHeight() * factor);
        scaled = original.getScaledInstance(width, height, Image.SCALE_DEFAULT);
    }
    
    public void setOverlayType(OverlayType overlayType) {
        mouseCoordinatesListener.setOverlayType(overlayType);
    }
        
    public void clearRenderArea() {
        this.document = null;
        this.original = null;
        this.scaled = null;
        cleanUpRequired = true;
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (scaled != null) {
            g.drawImage(scaled, 0, 0, null);
        } else if (original != null) {
            g.drawImage(original, 0, 0, null);
        }
        
        if (cleanUpRequired) {
            g.clearRect(0, 0, getWidth(), getHeight());
            cleanUpRequired = false;
        }
        
        if (document != null && !document.getOverlays().isEmpty()) {
            for (Overlay overlay : document.getOverlays()) {
                switch (overlay.getOverlayType()) {
                    
                    /**
                     *  "NONE" type of Overlay mustn't be send to the renderer, 
                     *  but if it does, when warn logger and render it as white 
                     *  transparent rectangular
                     */
                    case NONE : 
                        g.setColor(new Color(255, 255, 255, 0)); 
                        log.error("Rendering 'NONE' Overlay type!");
                        break;
                    case TEXT : 
                        g.setColor(new Color(79, 195, 230, 75)); 
                        break;
                    case TABLE : 
                        g.setColor(new Color(84, 184,123, 75)); 
                        break;
                    case PICTURE : 
                        g.setColor(new Color(235, 95, 84, 75)); 
                        break;
                }
                g.fillRect(overlay.getxStartCoordinate(), overlay.getyStartCoordinate(), overlay.getxEndCoordinate() - overlay.getxStartCoordinate(), overlay.getyEndCoordinate() - overlay.getyStartCoordinate());
            }
        }
    }
}
