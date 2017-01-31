package de.tong.gui.screen;

import java.awt.Graphics2D;
import java.awt.event.KeyListener;

import de.tong.block.BlockType;
import de.tong.controls.MultiplayerKeyHandler;
import de.tong.field.PongField;
import de.tong.field.StatusField;
import de.tong.field.TetrisField;
import de.tong.graphics.Ball;
import de.tong.graphics.Block;
import de.tong.graphics.Moveable;
import de.tong.gui.GamePanel;
import de.tong.player.Player;

public class MultiplayerScreen extends GameScreen {

    private Player player1;
    private Player player2;
    private TetrisField tField1;
    private TetrisField tField2;
    private PongField pField;
    private StatusField sField1;
    private StatusField sField2;
    private MultiplayerKeyHandler handler;
    private int counter1;
    private int counter2;

    public MultiplayerScreen(GamePanel parent, Player player1, Player player2) {
	super(parent);
	this.player1 = player1;
	this.player2 = player2;

	handler = new MultiplayerKeyHandler(player1, player2, this);

	this.sField1 = new StatusField(10, 10, 6, 24, true, true, player1, this);
	this.sField2 = new StatusField(690, 10, 6, 24, true, true, player2, this);
	this.pField = new PongField(170, 10, 510, 12, true, player1, player2, this);
	this.tField1 = new TetrisField(170, 10, 10, 24, true, player1, pField, this);
	this.tField2 = new TetrisField(430, 10, 10, 24, true, player2, pField, this);

    }

    @Override
    public void newRound() {
	tField1.newRound();
	tField2.newRound();
	pField.getPongBall().reset();
	parent.queueGameStart();
    }

    public TetrisField getTetrisField1() {
	return tField1;
    }

    public TetrisField getTetrisField2() {
	return tField2;
    }

    public PongField getPongField() {
	return pField;
    }

    @Override
    public void clearScreen() {
	tField1.clearField();
	tField2.clearField();

    }

    @Override
    public void draw(Graphics2D g) {

	tField1.draw(g);
	tField2.draw(g);
	pField.draw(g);
	sField2.draw(g);
	sField1.draw(g);
    }

    @Override
    public void doLogic(double delta) {

	pField.getPongBall().collidedWith(tField1.getBlock());
	pField.getPongBall().collidedWith(tField2.getBlock());
	for (Moveable b : pField.getMoveables()) {
	    b.move(delta);
	    b.doLogic();
	}

    }

    public void moveBlocks(TetrisField field) {

	field.moveBlocks();
    }

    @Override
    public int getBlockSpeed() {
	return tField1.getSpeed();

    }

    @Override
    public void updateBlock(Block b, BlockType preview, TetrisField trigger) {
	System.out.println("KeyHandler received new block");
	System.out.println("Preview is " + preview);

	if (trigger.getOwner().equals(player1)) {
	    System.out.println("eq1");
	    sField1.setPreviewBlockType(preview);
	    handler.setBlock1(b);
	} else if (trigger.getOwner().equals(player2)) {
	    System.out.println("eq2");
	    sField2.setPreviewBlockType(preview);
	    handler.setBlock2(b);
	}
	System.out.println();

    }

    @Override
    public KeyListener getKeyHandler() {
	return handler;
    }

    @Override
    public void destroyScreen() {
	// TODO Auto-generated method stub

    }

    @Override
    public Ball getBall() {
	return pField.getPongBall();
    }

}
