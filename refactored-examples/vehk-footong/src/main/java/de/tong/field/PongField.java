package de.tong.field;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import de.tong.graphics.Ball;
import de.tong.graphics.Drawable;
import de.tong.gui.screen.GameScreen;
import de.tong.player.Player;

public class PongField extends Field {

    private Ball b;
    private Player ownerRight;

    public PongField(double x, double y, double width, double height, boolean border, Player ownerLeft, Player ownerRight, GameScreen parent) {
	super(x, y, width, 25 * height, border, false, ownerLeft, parent);

	this.ownerRight = ownerRight;
	b = new Ball(this, 20, 20);
	moveables.add(b);
	drawables.add(b);

    }

    @Override
    public void draw(Graphics2D g) {
	super.draw(g);

	for (Drawable d : drawables) {
	    d.draw(g);
	}
    }

    public Ball getPongBall() {
	return b;
    }

    @Override
    public void drawBorder(Graphics2D g) {

	g.setColor(Color.RED);
	g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[] { 6.0f }, 0.0f));
	g.draw(new Line2D.Double(x, y + height, x + width, y + height));

    }

    public Player getOwnerRight() {
	return ownerRight;
    }
}
