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
package ru.ifmo.juneiform;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import ru.ifmo.juneiform.ui.InputPanel;
import ru.ifmo.juneiform.ui.MouseCoordinatesListener;
import ru.ifmo.juneiform.ui.Preview;

/**
 *
 * @author Ivan Stepuk
 * @author Oleg Kuznetsov
 */
public class Document implements Observer {

    public static final double MIN_ZOOM_FACTOR = 0.125;
    public static final double MAX_ZOOM_FACTOR = 4;
    private String name;
    private String path;
    private Language language;
    private Image image;
    private List<Overlay> overlayList = new ArrayList<Overlay>();
    private String text = L10n.forString("msg.not.recognized.yet");
    private double zoomFactor = 1;
    Observable observable;
    InputPanel inputPanel = InputPanel.getInstance();

    public Document(String name, String path, Language language, Image image) {
        this.name = name;
        this.path = path;
        this.language = language;
        this.image = image;
    }
    
    public void registerObserver(Observable observable) {
        this.observable = observable;
        observable.addObserver(this);
    }
    
    public void unregisterObserver() {
        observable.deleteObserver(this);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Image getImage() {
        return image;
    }
    
    public void addOverlay(Overlay overlay) {
        overlayList.add(overlay);
    }
    
    public void removeOverlay(int id) {
        if (id < overlayList.size() - 1) {
            overlayList.remove(id);
        }
    }
    
    public void removeOverlay(Overlay overlay) {
        if (overlayList.contains(overlay)) {
            overlayList.remove(overlay);
        }
    }
    
    public List<Overlay> getOverlays() {
        return overlayList;
    }

    public Preview getPreview() {
        return new Preview(image, name);
    }

    public Language getLanguage() {
        return language;
    }

    public String getText() {
        return text;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public double modifyZoomFactor(double delta) throws DocumentException {
        double newValue = zoomFactor + delta;
        if (newValue >= MIN_ZOOM_FACTOR && newValue <= MAX_ZOOM_FACTOR) {
            zoomFactor = newValue;
            return newValue;
        }
        throw new DocumentException("Can't zoom any further");
    }

    public void rotateImageLeft(){
        //TO-DO
    }

    public void rotateImageRight(){
        //TO-DO
    }

    public void deleteOverlays(){
        overlayList.clear();
        inputPanel.repaint();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Document other = (Document) obj;
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.path != null ? this.path.hashCode() : 0);
        return hash;
    }
    
    @Override
    public void update (Observable obs, Object arg) {
        if (obs instanceof MouseCoordinatesListener && arg instanceof Overlay) {
            overlayList.add((Overlay) arg);
            inputPanel.repaint();
        }
    }
}
