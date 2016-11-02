package de.tong.field;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.tong.graphics.Drawable;
import de.tong.graphics.Moveable;
import de.tong.gui.screen.GameScreen;
import de.tong.player.Player;

public abstract class Field extends Rectangle2D.Double implements Drawable {

    protected boolean border = false;
    protected boolean visible = true;
    protected List<Drawable> drawables = new ArrayList<Drawable>();
    protected List<Moveable> moveables = new ArrayList<Moveable>();
    protected Player owner;
    protected GameScreen parent;

    public Field(double x, double y, double width, double height, boolean border, boolean visible, Player owner, GameScreen parent) {
	super(x, y, width, height);
	this.owner = owner;
	this.border = border;
	this.visible = visible;
	this.parent = parent;
    }

    @Override
    public void draw(Graphics2D g) {

	if (visible) {
	    g.setColor(new Color(0, 0, 0));
	    g.fill(this);
	}

	for (int i = 0; i < drawables.size(); i++) {

	    drawables.get(i).draw(g);
	}
	if (border) {
	    drawBorder(g);
	}

	g.setStroke(new BasicStroke());

    }

    public abstract void drawBorder(Graphics2D g);

    public Player getOwner() {
	return owner;
    }

    public List<Drawable> getDrawables() {
	return drawables;

    }

    public List<Moveable> getMoveables() {
	return moveables;

    }

}
