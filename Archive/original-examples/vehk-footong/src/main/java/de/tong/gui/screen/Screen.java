package de.tong.gui.screen;

import java.awt.event.KeyListener;

import de.tong.graphics.Drawable;
import de.tong.gui.GamePanel;

public abstract class Screen implements Drawable {

    protected GamePanel parent;

    public Screen(GamePanel parent) {
	this.parent = parent;
    }

    public boolean isGameRunning() {
	return parent.isGameRunning();
    }

    public abstract KeyListener getKeyHandler();

    public abstract void doLogic(double delta);

    public abstract void destroyScreen();

    public abstract void clearScreen();

}
