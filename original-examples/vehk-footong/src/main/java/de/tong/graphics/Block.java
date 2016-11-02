package de.tong.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.tong.block.BlockMap;
import de.tong.block.BlockType;
import de.tong.controls.Direction;
import de.tong.controls.GameEvent;
import de.tong.field.StatusField;
import de.tong.field.TetrisField;

/**
 * A class that combines single BlockPart objects to create the whole tetris
 * block
 * 
 * @author Wolfgang Mueller
 * 
 */
public class Block extends Rectangle2D.Double implements Drawable {

    private List<BlockPart> blockParts = new ArrayList<BlockPart>();
    private Color c;
    private int rotation = 0;
    private int plx = 4;
    private int ply = 0; // specifies the top left position of a block
    private BlockMap map;
    private int max;
    private TetrisField currentField;
    private BlockType type;
    private boolean lock;
    private int numberOfContacts = 0;
    private int spinCount = 0;
    private boolean ballOnOpponentsSide = false; // needed for Achievement 17

    public Block(BlockMap map, Color c, BlockType type, TetrisField currentField) {

	this.c = c;
	this.map = map;
	this.max = map.getSize() - 1;
	this.currentField = currentField;
	this.type = type;

	createBlockParts();

    }

    public Block(BlockMap map, Color c, BlockType type, StatusField field, int xOffset, int yOffset) {

	this.c = c;
	this.map = map;
	this.max = map.getSize() - 1;
	this.currentField = null;
	this.type = type;

	createPreviewBlockParts(field, xOffset, yOffset);

    }

    public Block(BlockMap map, Color c, BlockType type, TetrisField currentField, int rotation, int plx, int ply) {

	this.ply = ply;
	this.plx = plx;
	this.rotation = rotation;
	this.c = c;
	this.map = map;
	this.max = map.getSize() - 1;
	this.currentField = currentField;
	this.type = type;
	// currentField.addBlock(this);

	createBlockParts();

    }

    public Block(Block b) {

	this.ply = b.ply;
	this.plx = b.plx;
	this.rotation = b.rotation;
	this.c = b.c;
	this.map = b.map;
	this.max = b.map.getSize() - 1;
	this.currentField = b.currentField;
	this.type = b.type;
	// currentField.addBlock(this);

	createBlockParts();

    }

    public boolean getBallOnOpponentsSide() {
	return ballOnOpponentsSide;
    }

    /**
     * Create every block using a {@link BlockMap}
     * 
     * 
     */
    private void createBlockParts() {
	byte[][] base = map.getByteListAt(rotation);
	for (int i = 0; i < base.length; i++) {
	    for (int j = 0; j < base[i].length; j++) {
		if (base[i][j] == 1) {

		    blockParts.add(new BlockPart(j + plx, i + ply, c, currentField));

		}

	    }

	}

    }

    public Color getColor() {
	return c;
    }

    private void createPreviewBlockParts(StatusField preview, int xOffset, int yOffset) {
	byte[][] base = map.getByteListAt(rotation);
	for (int i = 0; i < base.length; i++) {
	    for (int j = 0; j < base[i].length; j++) {
		if (base[i][j] == 1) {

		    blockParts.add(new BlockPart(j + plx + xOffset, i + ply + yOffset, c, preview));

		}

	    }

	}

    }

    @Override
    public void draw(Graphics2D g) {

	for (BlockPart b : blockParts) {
	    b.draw(g);
	}
	g.setColor(Color.YELLOW);
	g.draw(this.getBounds2D());

    }

    public void fastDown() {
	lock = true;
	int count = 0;
	Block ghostBlock;
	ghostBlock = new Block(map, c, this.type, currentField, rotation, plx, ply);
	for (int i = 1; ghostBlock.isInBounds(Direction.DOWN) && !ghostBlock.isOccupied(Direction.DOWN); i++) {
	    count = i;
	    ghostBlock.move(Direction.DOWN, false);

	}

	for (BlockPart p : blockParts) {
	    p.move(0, count);

	}

	ply += count;

	if (!isInTetrisBounds()) {
	    if (currentField.getOwner() == currentField.getPongField().getOwner()) { /*Distinct the right from the left player */
		GameEvent.passedBorders(currentField.getOwner(), currentField.getPongField().getOwnerRight());
		if (currentField.getPongField().getPongBall().getCenterX() > currentField.getPongField().getCenterX()) {
		    GameEvent.unlockAchievement(16, currentField.getOwner());
		}
	    }
	    if (currentField.getOwner() == currentField.getPongField().getOwnerRight()) { /*Distinct the right from the left player */
		GameEvent.passedBorders(currentField.getOwner(), currentField.getPongField().getOwner());
		if (currentField.getPongField().getPongBall().getCenterX() < currentField.getPongField().getCenterX()) {
		    GameEvent.unlockAchievement(16, currentField.getOwner());
		}
	    }

	} else {
	    currentField.addBlockToField(plx, ply, this);
	    currentField.addNewBlock();
	}

	lock = false;
    }

    public byte[][] getBlockMap() {
	return map.getByteListAt(rotation);
    }

    public List<BlockPart> getBlockParts() {
	return blockParts;
    }

    public BlockType getBlockType() {
	return type;
    }

    public TetrisField getField() {
	return currentField;
    }

    private boolean isInBounds() {
	boolean res = true;
	for (BlockPart b : blockParts) {
	    if (!b.isInBounds()) {
		res = false;
	    }
	}
	return res;
    }

    public boolean isInBounds(Direction dir) {
	boolean res = true;
	int rot = rotation;

	if (dir == Direction.ROTATE) {

	    if (rot < max) {
		rot++;
	    } else {
		rot = 0;
	    }
	    Block ghostBlock;
	    ghostBlock = new Block(map, c, this.type, currentField, rot, plx, ply);
	    return ghostBlock.isInBounds();
	} else {
	    for (BlockPart b : blockParts) {
		if (!b.isInBounds(dir)) {
		    res = false;
		}
	    }

	}
	return res;
    }

    private boolean isRotationPossible() {
	byte[][] field = currentField.getField();
	boolean res = false;

	for (BlockPart b : blockParts) {
	    if (b.isOccupied(field)) {
		res = true;
	    }

	}
	return res;
    }

    public synchronized boolean isOccupied(Direction type) {
	byte[][] field = currentField.getField();
	boolean res = false;
	int rot = rotation;

	if (type == Direction.ROTATE) {
	    if (rot < max) {
		rot++;
	    } else {
		rot = 0;
	    }
	    Block ghostBlock = new Block(map, c, this.type, currentField, rot, plx, ply);
	    if (ghostBlock.isInBounds()) {
		return ghostBlock.isRotationPossible();
	    }
	} else {

	    for (BlockPart b : blockParts) {
		if (b.isOccupied(type, field)) {
		    res = true;
		}
	    }

	}
	return res;

    }

    public boolean intersectsBall(Direction type) {
	Block ghostBlock = new Block(this);

	switch (type) {

	    case LEFT:
		for (BlockPart b : ghostBlock.getBlockParts()) {
		    b.move(-1, 0);
		}
		break;

	    case DOWN:
		if (!lock) {

		    for (BlockPart b : ghostBlock.getBlockParts()) {
			b.move(0, 1);
		    }
		}

		break;

	    case RIGHT:
		for (BlockPart b : ghostBlock.getBlockParts()) {
		    b.move(1, 0);
		}
		break;

	    case ROTATE:
		ghostBlock.rotate();
		break;

	    default:
		System.err.println("DIRECTION not recognized.");
		break;

	}

	if (currentField.getPongField().getPongBall().intersects(ghostBlock)) {
	    return true;
	} else {
	    return false;
	}
    }

    public void move(Direction direction, boolean fastDown) {
	if (currentField.getOwner().getName() == currentField.getPongField().getOwner().getName()) {
	    if (currentField.getPongField().getPongBall().getCenterX() > currentField.getPongField().getCenterX()) {
		ballOnOpponentsSide = true;
	    } else {
		ballOnOpponentsSide = false;
		currentField.setOpponentSideBlockCount(0);
	    }
	}
	if (currentField.getOwner() == currentField.getPongField().getOwnerRight()) {
	    if (currentField.getPongField().getPongBall().getCenterX() < currentField.getPongField().getCenterX()) {
		ballOnOpponentsSide = true;
	    } else {
		ballOnOpponentsSide = false;
		currentField.setOpponentSideBlockCount(0);
	    }
	}
	boolean inter;
	if (!fastDown) {
	    inter = true;
	} else {
	    inter = !this.intersectsBall(direction);
	}
	System.out.println(this.isInBounds(direction));
	if (this.isInBounds(direction) && !this.isOccupied(direction) && inter) {

	    switch (direction) {

		case LEFT:
		    plx -= 1;
		    for (int i = 0; i < blockParts.size(); i++) {
			BlockPart b = blockParts.get(i);
			b.move(-1, 0);
		    }

		    break;

		case DOWN:
		    if (!lock) {
			ply += 1;
			for (int i = 0; i < blockParts.size(); i++) {
			    BlockPart b = blockParts.get(i);
			    b.move(0, 1);
			}

		    } else {
			System.out.println("movement lockedgargle");
		    }

		    break;

		case RIGHT:
		    plx += 1;
		    for (int i = 0; i < blockParts.size(); i++) {
			BlockPart b = blockParts.get(i);
			b.move(1, 0);
		    }

		    break;

		case ROTATE:
		    rotate();
		    spinCount += 1;
		    if (spinCount == 100) {
			GameEvent.unlockAchievement(18, currentField.getOwner());
		    }
		    break;

		default:
		    System.err.println("DIRECTION not recognized.");
		    break;

	    }

	} else if (direction == Direction.DOWN && inter) {
	    if (!isInTetrisBounds()) {
		if (currentField.getOwner() == currentField.getPongField().getOwner()) { /*Distinct the right from the left player */
		    GameEvent.passedBorders(currentField.getOwner(), currentField.getPongField().getOwnerRight());
		    if (currentField.getPongField().getPongBall().getCenterX() > currentField.getPongField().getCenterX()) {
			GameEvent.unlockAchievement(16, currentField.getOwner());
		    }
		}
		if (currentField.getOwner() == currentField.getPongField().getOwnerRight()) { /*Distinct the right from the left player */
		    GameEvent.passedBorders(currentField.getOwner(), currentField.getPongField().getOwner());
		    if (currentField.getPongField().getPongBall().getCenterX() < currentField.getPongField().getCenterX()) {
			GameEvent.unlockAchievement(16, currentField.getOwner());
		    }
		}

	    } else {
		currentField.addBlockToField(plx, ply, this);
		currentField.addNewBlock();
	    }
	}
    }

    /**
     * This method checks if the block is in the bounds of the bottom part of
     * the tetris field.
     * 
     * @return true when in bounds, false otherwise
     */
    private boolean isInTetrisBounds() {

	int maxHeight = currentField.getFieldHeight() / 2;
	boolean res = true;

	for (BlockPart p : blockParts) {

	    if (p.getRelY() < maxHeight) {
		res = false;
		break;
	    }
	}

	return res;

    }

    public synchronized void rotate() {
	if (rotation < max) {
	    rotation++;
	} else {
	    rotation = 0;
	}
	byte[][] next = map.getByteListAt(rotation);
	blockParts.clear();
	// currentField.setField(plx, ply, next);

	for (int i = 0; i < next.length; i++) {
	    for (int j = 0; j < next[i].length; j++) {
		if (next[i][j] == 1) {
		    blockParts.add(new BlockPart((j + plx), (i + ply), c, currentField));

		}

	    }

	}

    }

    public void setSpeed(int i, boolean type) {

	if (type) {
	    currentField.setSpeed(currentField.getSpeed() * 1 / i);
	} else {
	    currentField.setSpeed(currentField.getSpeed() * i);
	}
    }

    public void increaseNumberOfContacts() {
	numberOfContacts += 1;
	if (numberOfContacts == 2) {
	    GameEvent.unlockAchievement(8, currentField.getOwner());
	}

    }

    public int getNumberOfContacts() {
	return numberOfContacts;
    }

    public int getRotation() {
	return rotation;
    }

}
