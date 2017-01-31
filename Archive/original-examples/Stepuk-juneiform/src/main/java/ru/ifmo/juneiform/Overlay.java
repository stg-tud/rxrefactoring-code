/*
 * Copyright 2011-2012 The Juneiform Team.
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

/**
 *
 * @author Oleg Kuznetsov
 */
public class Overlay implements Cloneable {
    private int id;
    private int nextId = 0;
    private int xStartCoordinate;
    private int yStartCoordinate;
    private int xEndCoordinate;
    private int yEndCoordinate;
    private OverlayType overlayType;
    
    public int getId() {
        return id;
    }

    public void setId() {
        id = nextId;
        nextId++;
    }
    
    public int getxStartCoordinate() {
        return xStartCoordinate;
    }

    public void setxStartCoordinate(int xStartCoordinate) {
        this.xStartCoordinate = xStartCoordinate;
    }
    
    public int getyStartCoordinate() {
        return yStartCoordinate;
    }

    public void setyStartCoordinate(int yStartCoordinate) {
        this.yStartCoordinate = yStartCoordinate;
    }

    public int getxEndCoordinate() {
        return xEndCoordinate;
    }

    public void setxEndCoordinate(int xEndCoordinate) {
        this.xEndCoordinate = xEndCoordinate;
    }

    public int getyEndCoordinate() {
        return yEndCoordinate;
    }

    public void setyEndCoordinate(int yEndCoordinate) {
        this.yEndCoordinate = yEndCoordinate;
    }

    public OverlayType getOverlayType() {
        return overlayType;
    }

    public void setOverlayType(OverlayType overlayType) {
        this.overlayType = overlayType;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
