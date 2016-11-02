package de.tong.field;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tong.block.BlockColor;
import de.tong.block.BlockFactory;
import de.tong.block.BlockType;
import de.tong.controls.Direction;
import de.tong.controls.GameEvent;
import de.tong.graphics.Block;
import de.tong.graphics.BlockPart;
import de.tong.gui.screen.GameScreen;
import de.tong.player.Player;

public class TetrisField extends Field implements Runnable {

    private byte[][] field;
    private int fieldWidth;
    private int fieldHeight;
    private Block playerBlock;
    private BlockType nextBlock = BlockFactory.getRandomBlockType();
    private int speed = 1000;
    private int minSpeed = 100;
    private PongField pongField;
    private Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f);
    private Thread t = new Thread(this);
    private int sameBlockCount = 0; // needed for some Achievements
    private boolean firstBlock = true;
    private int opponentSideBlockCount; // needed for Achievement 17

    public TetrisField(double x, double y, int width, int height, boolean border, Player owner, PongField pongField, GameScreen parent) {
	super(x, y, 25 * width, 25 * height, border, true, owner, parent);

	this.pongField = pongField;
	field = new byte[width][height];
	addNewBlock();

	t.start();
	System.out.println(t.isAlive());

	this.fieldWidth = width;
	this.fieldHeight = height;

    }

    public PongField getPongField() {
	return pongField;
    }

    private void addBlock(Block b) {

	this.drawables.add(b);

    }

    public void addBlockToField(int x, int y, Block b) {
	byte[][] map = b.getBlockMap();

	drawables.remove(b);

	for (int i = 0; i < map.length; i++) {
	    for (int j = 0; j < map[i].length; j++) {
		if (map[i][j] == 1) {
		    field[x + j][y + i] = 1;
		    drawables.add(new BlockPart(x + j, y + i, BlockColor.getColorFor(b.getBlockType()), this));
		}
	    }
	}

    }

    public void newRound() {
	if (playerBlock.getColor() == BlockColor.getColorFor(nextBlock)) {
	    sameBlockCount += 1;
	    if (sameBlockCount == 4) {
		GameEvent.unlockAchievement(12, owner);
	    }
	} else {
	    sameBlockCount = 0;
	}
	drawables.remove(playerBlock);
	firstBlock = true;
	addNewBlock();

    }

    public void addNewBlock() {

	deleteFilledRows();
	if (playerBlock != null) {
	    if (playerBlock.getBallOnOpponentsSide()) {
		System.out.println(playerBlock.getBallOnOpponentsSide());
		opponentSideBlockCount += 1;
		if (opponentSideBlockCount == 6) {
		    GameEvent.unlockAchievement(17, owner);
		}
	    }
	}
	this.playerBlock = BlockFactory.createBlock(nextBlock, this);
	if (firstBlock
	    && (playerBlock.getColor() == BlockColor.getColorFor(BlockType.S_BLOCK) || playerBlock.getColor() == BlockColor.getColorFor(BlockType.Z_BLOCK))) {
	    GameEvent.unlockAchievement(13, owner);
	}
	firstBlock = false;
	this.nextBlock = BlockFactory.getRandomBlockType();
	this.addBlock(playerBlock);
	parent.updateBlock(getBlock(), nextBlock, this);

    }

    private int[] completedRows() {
	int[] completedRows;
	int counter = 0;

	for (int i = 0; i < fieldHeight; i++) {
	    if (isRowFilled(i)) {
		counter++;
	    }
	}
	completedRows = new int[counter];
	counter = 0;
	for (int i = 0; i < fieldHeight; i++) {
	    if (isRowFilled(i)) {
		completedRows[counter] = i;
		counter++;
	    }

	}

	return completedRows;
    }

    private void deleteFilledRows() {
	int[] completedRows = completedRows();
	if (completedRows.length == 4 && playerBlock.getNumberOfContacts() >= 1) {
	    pongField.getPongBall().setFourRowContact(owner);
	}
	List<BlockPart> del = new ArrayList<BlockPart>();
	if (completedRows.length > 0) {
	    GameEvent.rowsDeleted(completedRows.length, owner);
	    System.out.println(owner.getPoints());
	}
	if (completedRows.length > 0) {
	    for (int y = 0; y < completedRows.length; y++) {

		Color compareColor = getBlockPartAt(0, completedRows[y]).getColor();
		Boolean oneType = true;
		for (int x = 0; x < fieldWidth; x++) {

		    BlockPart p = getBlockPartAt(x, completedRows[y]);

		    if (p.getColor() != compareColor) {
			oneType = false;
		    }
		    drawables.remove(p);

		}
		if (oneType) {
		    GameEvent.unlockAchievement(15, owner);
		}

		for (int i = completedRows[y] - 1; i > 0; i--) {
		    if (!isRowEmpty(i)) {
			moveRowDown(i);
		    }
		}
	    }
	}
	if (completedRows.length > 0) {
	    boolean everythingGone = true;
	    for (int i = 0; i < field.length; i++) {
		for (int j = 0; j < field[i].length; j++) {
		    if (field[i][j] == 1) {
			everythingGone = false;
		    }
		}
	    }
	    if (everythingGone) {
		GameEvent.unlockAchievement(14, owner);
	    }
	}

    }

    @Override
    public void draw(Graphics2D g) {
	super.draw(g);
	g.setColor(Color.DARK_GRAY);
	// nextBlock.draw(g);

    }

    public Block getBlock() {
	return playerBlock;
    }

    private BlockPart getBlockPartAt(int x, int y) {
	for (int i = 0; i < drawables.size(); i++) {
	    if (drawables.get(i) instanceof BlockPart) {
		BlockPart p = (BlockPart) drawables.get(i);
		int[] rel = p.getRelativePosition();
		if (rel[0] == x && rel[1] == y) {
		    return p;
		}
	    }
	}
	return null;
    }

    public byte[][] getField() {
	return field;
    }

    public int getFieldHeight() {
	return fieldHeight;
    }

    public int getFieldWidth() {
	return fieldWidth;
    }

    public int getSpeed() {
	return speed;
    }

    public void info() {
	for (byte[] by : field) {
	    System.out.println(Arrays.toString(by));
	}
    }

    private boolean isRowEmpty(int row) {
	for (int j = 0; j < fieldWidth; j++) {
	    if (field[j][row] == 1) {
		return false;
	    }
	}
	return true;

    }

    public boolean isRowFilled(int i) {
	for (int j = 0; j < field.length; j++) {
	    if (field[j][i] == 0) {
		return false;
	    }
	}
	return true;
    }

    public void moveBlocks() {

	playerBlock.move(Direction.DOWN, true);

    }

    private void moveRowDown(int y) {
	for (int i = 0; i < fieldWidth; i++) {

	    field[i][y + 1] = field[i][y];
	    field[i][y] = 0;

	    BlockPart p = getBlockPartAt(i, y);
	    if (p != null) {
		p.move(0, 1);
	    }

	}
    }

    public void setSpeed(int speed) {
	if (speed > minSpeed) {
	    this.speed = speed;
	}
    }

    @Override
    public void drawBorder(Graphics2D g) {
	g.setColor(Color.GREEN);
	g.setStroke(stroke);
	g.draw(this);

    }

    public void clearField() {
	for (int i = 0; i < field.length; i++) {
	    Arrays.fill(field[i], (byte) 0);
	}

	drawables.clear();
    }

    @Override
    public void run() {
	int counter = 0;
	while (true) {
	    try {

		if (parent.isGameRunning()) {
		    counter += 25;
		    if (counter >= speed) {
			moveBlocks();
			counter = 0;
		    }

		}
		Thread.sleep(25);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public void setOpponentSideBlockCount(int opponentSideBlockCount) {
	this.opponentSideBlockCount = opponentSideBlockCount;
    }

    public int getOpponentSideBlockCount() {
	return opponentSideBlockCount;
    }
}
