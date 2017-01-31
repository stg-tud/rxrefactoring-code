package de.tong.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.tong.controls.Direction;
import de.tong.field.StatusField;
import de.tong.field.TetrisField;

public class BlockPart extends Rectangle2D.Double implements Drawable {

    private Color color;
    private int relX;
    private int relY;
    private TetrisField field;
    private BasicStroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f);

    public BlockPart(int relX, int relY, Color color, TetrisField field) {
	this.relX = relX;
	this.relY = relY;
	this.x = field.getX() + 25 * relX;
	this.y = field.getY() + 25 * relY;
	this.width = 25;
	this.height = 25;
	this.color = color;
	this.field = field;

    }

    public BlockPart(int relX, int relY, Color color, StatusField field) {
	this.relX = relX;
	this.relY = relY;
	this.x = -75 + field.getX() + 25 * relX;
	this.y = 10 + field.getY() + 25 * relY;
	this.width = 25;
	this.height = 25;
	this.color = color;

    }

    public BlockPart(BlockPart b, Direction d) {

	this.width = 25;
	this.height = 25;
	this.color = b.getColor();
	this.field = b.getField();

	switch (d) {

	    case DOWN:
		this.relX = b.getRelX();
		this.relY = b.getRelY() + 1;
		break;

	    case LEFT:
		this.relX = b.getRelX() - 1;
		this.relY = b.getRelY();
		break;

	    case RIGHT:
		this.relX = b.getRelX() + 1;
		this.relY = b.getRelY();
		break;

	    case ROTATE:
		System.out.println("This won't work RETARD! This is an ERROR code");
		this.relX = b.getRelX();
		this.relY = b.getRelY();
		break;

	    default:
		this.relX = b.getRelX();
		this.relY = b.getRelY();
		break;
	}

	this.x = field.getX() + 25 * relX;
	this.y = field.getY() + 25 * relY;
    }

    public Color getColor() {
	return color;
    }

    public int getRelX() {
	return relX;
    }

    public int getRelY() {
	return relY;
    }

    public TetrisField getField() {
	return field;
    }

    public boolean isInBounds(Direction type) {

	switch (type) {

	    case LEFT:
		return relX > 0;

	    case RIGHT:
		return relX + 1 < field.getFieldWidth();

	    case DOWN:
		return relY + 1 < field.getFieldHeight();

	    case ROTATE:
		return true;

	    default:
		return false;

	}
    }

    /**
     * This method moves the already existing BlockPart x or y fields further
     * left/right or down.
     * 
     * @param x
     *            number added to current horizontal relative position
     * @param ynumber
     *            added to current vertical relative position
     */
    public void move(int x, int y) {
	this.relX += x;
	this.relY += y;
	updateRealPos();
    }

    /**
     * This method recalculates the real x and y positions of the BlockPart.
     */
    private void updateRealPos() {
	this.x = field.getX() + 25 * relX;
	this.y = field.getY() + 25 * relY;

    }

    @Override
    public void draw(Graphics2D g) {
	g.setColor(color);
	g.fill(this);
	g.setColor(Color.BLACK);
	g.setStroke(stroke);
	g.drawRect((int) x, (int) y, (int) width, (int) height);

    }

    public int[] getRelativePosition() {
	return new int[] { relX, relY };
    }

    public void setColor(Color c) {
	this.color = c;
    }

    public boolean isOccupied(Direction type, byte[][] field) {

	switch (type) {

	    case LEFT:
		return field[relX - 1][relY] == 1;

	    case RIGHT:
		return field[relX + 1][relY] == 1;

	    case DOWN:
		return field[relX][relY + 1] == 1;

	    case ROTATE:
		System.out.println("ERROR rotation CASE of isOccupied not executed correctly");
		break;

	    default:
		System.out.println("ERROR look up isOccupied()");
		break;
	}
	return true;
    }

    public boolean isOccupied(byte[][] field) {
	return field[relX][relY] == 1;
    }

    public boolean isInBounds() {
	return relX >= 0 && relX < field.getFieldWidth() && relY < field.getFieldHeight();

    }

    @Override
    public String toString() {
	return "BlockPart[" + relX + "," + relY + "]";
    }

}
