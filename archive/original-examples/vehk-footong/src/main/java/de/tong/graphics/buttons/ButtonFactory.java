package de.tong.graphics.buttons;

import de.tong.gui.screen.MenuScreen;

public class ButtonFactory {

    public static final int MARGIN = 14;
    public static final int BUTTON_HEIGHT = 50;
    public static final int BUTTON_WIDTH = 400;

    /**
     * 
     * Creates a button under another one.
     * 
     * @param b
     *            the other (top) button
     * @param s
     *            the title of the button
     * @return
     */
    public static MenuButton createButton(MenuButton b, String s) {
	double x = b.getX();
	double y = b.getY() + b.getHeight() + MARGIN;

	return new MenuButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, s);
    }

    public static MenuButton createButton(MenuScreen screen, String s) {

	double x = (screen.getWidth() / 2) - (BUTTON_WIDTH / 2);
	double y = 50;

	System.out.println(screen.getWidth());
	return new MenuButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, s);

    }
}
