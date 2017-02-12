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

import java.awt.event.MouseEvent;
import java.util.Observable;
import javax.swing.event.MouseInputListener;
import org.apache.log4j.Logger;
import ru.ifmo.juneiform.Overlay;
import ru.ifmo.juneiform.OverlayType;

/**
 *
 * @author Oleg Kuznetsov
 */
public class MouseCoordinatesListener extends Observable implements MouseInputListener {
    private static Logger log = Logger.getLogger(MouseCoordinatesListener.class);
    private Overlay overlay = new Overlay();
    private boolean cursorOutsideOfArea = false;
    private boolean endCoordinatesFlushed = true;
    private OverlayType overlayType = OverlayType.NONE;
    private InputPanel inputPanel;
    public static MouseCoordinatesListener uniqueInstance = new MouseCoordinatesListener();
    
    private MouseCoordinatesListener() {
    }
    
    public static MouseCoordinatesListener getInstance() {
        return uniqueInstance;
    }

    public void setOverlayType(OverlayType overlayType) {
        this.overlayType = overlayType;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        endCoordinatesFlushed = false;
        overlay.setxStartCoordinate(e.getX());
        overlay.setyStartCoordinate(e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        inputPanel = InputPanel.getInstance();
        
        if (overlayType != OverlayType.NONE) {
            if (!cursorOutsideOfArea && !endCoordinatesFlushed) {
                try {
                    overlay.setxEndCoordinate(e.getX());
                    overlay.setyEndCoordinate(e.getY());
                    overlay.setId();
                    overlay.setOverlayType(overlayType);  
                    sortCoordinates();
                    setChanged();
                    notifyObservers((Overlay)overlay.clone());
                } catch (CloneNotSupportedException ex) {
                    log.fatal(MouseCoordinatesListener.class.getName() + ex);
                }
            }
            endCoordinatesFlushed = true;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        cursorOutsideOfArea = false;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        inputPanel = InputPanel.getInstance();
        
        if (overlayType != OverlayType.NONE) {
            if (!endCoordinatesFlushed) {
                try {
                    overlay.setxEndCoordinate(e.getX());
                    overlay.setyEndCoordinate(e.getY());
                    overlay.setId();
                    overlay.setOverlayType(overlayType);
                    sortCoordinates();
                    setChanged();
                    notifyObservers((Overlay)overlay.clone());
                    endCoordinatesFlushed = true;
                } catch (CloneNotSupportedException ex) {
                    log.fatal(MouseCoordinatesListener.class.getName() + ex);
                }
            }
            cursorOutsideOfArea = true;
        }
    }
    
    private void sortCoordinates() {
        int tempCoordinate;
        
        if (overlay.getxEndCoordinate() < overlay.getxStartCoordinate()) {
           tempCoordinate = overlay.getxStartCoordinate();
           overlay.setxStartCoordinate(overlay.getxEndCoordinate());
           overlay.setxEndCoordinate(tempCoordinate);
        }
        
        if (overlay.getyEndCoordinate() < overlay.getyStartCoordinate()) {
           tempCoordinate = overlay.getyStartCoordinate();
           overlay.setyStartCoordinate(overlay.getyEndCoordinate());
           overlay.setyEndCoordinate(tempCoordinate);
        }
    }
 }
