package de.tong.gui.screen;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tong.graphics.Drawable;
import de.tong.graphics.buttons.ButtonFactory;
import de.tong.graphics.buttons.MenuButton;

public class Menu implements Drawable {

    private List<MenuButton> buttons = new ArrayList<MenuButton>();
    private MenuScreen parent;

    public Menu(MenuScreen parent, MenuButton... buttons) {
	this.parent = parent;
	this.buttons.addAll(Arrays.asList(buttons));
    }

    public Menu(final MenuScreen parent, final Menu previous, MenuButton... buttons) {
	this.parent = parent;
	this.buttons.addAll(Arrays.asList(buttons));
	MenuButton cancel = ButtonFactory.createButton(buttons[buttons.length - 1], "CANCEL");
	cancel.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		parent.setMenu(previous);

	    }
	});
	this.buttons.add(cancel);
    }

    public List<MenuButton> getButtons() {
	return buttons;
    }

    @Override
    public void draw(Graphics2D g) {
	for (MenuButton b : buttons) {
	    b.draw(g);
	}
    }
}
