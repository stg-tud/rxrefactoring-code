package de.tong.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Random;

import de.tong.block.BlockType;
import de.tong.controls.GameEvent;
import de.tong.field.PongField;
import de.tong.player.GameMode;
import de.tong.player.Player;

public class Ball extends Ellipse2D.Double implements Drawable, Moveable {

    private double speedX;
    private double speedY;
    private PongField parent;
    private Random rand = new Random();
    private int speedMultiply;
    private int numberOfBlockContacts;
    private boolean fourRowContact = false;
    private Player fourRowHitter; // Needed for Achievement 11

    public Ball(PongField f, double width, double height) {
	this.parent = f;
	this.x = f.getCenterX() - width / 2;
	this.y = f.getCenterY() - height / 2;
	this.width = width;
	this.height = height;
	speedX = parent.getOwner().getOptions().getDefaultBallSpeed();
	speedY = ((speedX * 4 / 5) + (speedX / Math.abs(speedX)) * rand.nextInt((int) Math.abs(speedX * 2 / 5)));
	speedMultiply = 1;
	numberOfBlockContacts = 0;

    }

    @Override
    public void move(double delta) {
	x += speedX * (delta / 1e9);
	y += speedY * (delta / 1e9);

    }

    public boolean intersects(Block b) {
	for (int i = 0; i < b.getBlockParts().size(); i++) {
	    BlockPart p = b.getBlockParts().get(i);
	    if (this.intersects(p)) {
		return true;
	    }
	}
	return false;
    }

    public void collidedWith(Block b) {

	for (int i = 0; i < b.getBlockParts().size(); i++) {
	    BlockPart p = b.getBlockParts().get(i);
	    if (this.intersects(p)) {

		b.increaseNumberOfContacts();

		fourRowContact = false;
		numberOfBlockContacts += 1;

		if (this.y < p.getY() && p.getCenterY() - this.getCenterY() >= (p.getWidth() / 2) + (this.getWidth() / 2) - 3) {
		    System.out.println("Up touch");
		    if (b.getBlockType() == BlockType.I_BLOCK && b.getRotation() == 0) {
			GameEvent.unlockAchievement(19, b.getField().getOwner());
		    }
		    speedY = -speedY;
		    y = p.getY() - 25;
		}

		if (this.y > p.getY() && this.getCenterY() - p.getCenterY() >= (p.getWidth() / 2) + (this.getWidth() / 2) - 3) {
		    System.out.println("Down touch");
		    if (b.getBlockType() == BlockType.I_BLOCK && b.getRotation() == 0) {
			GameEvent.unlockAchievement(19, b.getField().getOwner());
		    }
		    speedY = -speedY;
		    y = p.getY() + 25;
		}

		if (this.x > p.getX() && this.getCenterX() - p.getCenterX() >= (p.getWidth() / 2) + (this.getWidth() / 2) - 3) {
		    System.out.println("Right touch");
		    if (b.getField().getOwner() == parent.getOwner() && this.getCenterX() >= parent.getCenterX()) {
			GameEvent.unlockAchievement(9, b.getField().getOwner());
		    }
		    if (b.getBlockType() == BlockType.I_BLOCK && b.getRotation() == 1) {
			GameEvent.unlockAchievement(19, b.getField().getOwner());
		    }
		    speedX = -speedX;
		    x = p.getX() + 25;
		}

		if (this.x < p.getX() && p.getCenterX() - this.getCenterX() >= (p.getWidth() / 2) + (this.getWidth() / 2) - 3) {
		    System.out.println("Left touch");
		    if (b.getField().getOwner() == parent.getOwnerRight() && this.getCenterX() < parent.getCenterX()) {
			GameEvent.unlockAchievement(9, b.getField().getOwner());
		    }
		    if (b.getBlockType() == BlockType.I_BLOCK && b.getRotation() == 1) {
			GameEvent.unlockAchievement(19, b.getField().getOwner());
		    }
		    speedX = -speedX;
		    x = p.getX() - 25;

		}

	    }
	}
    }

    @Override
    public void draw(Graphics2D g) {

	g.setColor(Color.BLACK);
	g.draw(this);
	g.setColor(Color.RED);
	g.fillOval((int) x + 1, (int) y + 1, (int) width - 1, (int) height - 1);

    }

    @Override
    public void doLogic() {

	if (x < parent.getMinX()) {
	    if (numberOfBlockContacts <= 1 && !(parent.getOwner().getOptions().getCurrentMode() == GameMode.SINGLEPLAYER)) {
		GameEvent.unlockAchievement(7, parent.getOwnerRight());
	    }

	    GameEvent.lostBall(parent.getOwner(), parent.getOwnerRight());
	    speedX = -speedX;

	}
	if (x + width > parent.getMaxX()) {
	    if (!(parent.getOwner().getOptions().getCurrentMode() == GameMode.SINGLEPLAYER)) {
		if (numberOfBlockContacts <= 1) {
		    GameEvent.unlockAchievement(7, parent.getOwner());
		}
		GameEvent.lostBall(parent.getOwnerRight(), parent.getOwner());
	    } else {
		x = parent.getMaxX() - width;
	    }
	    speedX = -speedX;

	}
	if (y < parent.getMinY()) {
	    speedY = -speedY;
	    y = parent.getMinY();
	}
	if (y + height > parent.getMaxY()) {
	    speedY = -speedY;
	    y = parent.getMaxY() - height;
	}

    }

    public void reset() {
	this.x = parent.getCenterX() - width / 2;
	this.y = parent.getCenterY() - height / 2;
	System.out.println(this.x = parent.getCenterX() - width / 2);

	speedMultiply *= -1;
	if (parent.getOwner().getOptions().getCurrentMode() == GameMode.SINGLEPLAYER) {
	    speedX = parent.getOwner().getOptions().getDefaultBallSpeed();
	} else if (parent.getOwner().getOptions().getCurrentMode() == GameMode.ROUNDMODE) {
	    speedX = speedMultiply * parent.getOwner().getOptions().getDefaultBallSpeed();
	} else {
	    speedX *= -1;
	}

	speedY = ((speedX * 4 / 5) + (speedX / Math.abs(speedX)) * rand.nextInt((int) Math.abs(speedX * 2 / 5)));

    }

    public double getSpeedX() {
	return speedX;
    }

    public void setSpeedX(double d) {
	speedX = d;

    }

    public void setFourRowContact(Player owner) {
	fourRowContact = true;

    }

    public boolean getFourRowContact() {
	return fourRowContact;
    }

    public Player getFourRowHitter() {
	return fourRowHitter;
    }

    public PongField getParent() {

	return parent;
    }
}
