package de.tong.gui.screen;

import java.awt.Graphics2D;
import java.awt.event.KeyListener;

import de.tong.block.BlockType;
import de.tong.controls.SingleplayerKeyHandler;
import de.tong.field.PongField;
import de.tong.field.StatusField;
import de.tong.field.TetrisField;
import de.tong.graphics.Ball;
import de.tong.graphics.Block;
import de.tong.graphics.Moveable;
import de.tong.gui.GamePanel;
import de.tong.player.Player;

public class SingleplayerScreen extends GameScreen {

    private Player player;
    private TetrisField tField;
    private PongField pField;
    private StatusField sField;
    private SingleplayerKeyHandler handler;
    private int counter = 0;

    public SingleplayerScreen(GamePanel parent, Player player) {
	super(parent);
	this.player = player;

	handler = new SingleplayerKeyHandler(player, this);

	this.sField = new StatusField(10, 10, 6, 24, true, true, player, this);
	this.pField = new PongField(170, 10, 510, 12, true, player, null, this);
	this.tField = new TetrisField(170, 10, 10, 24, true, player, pField, this);

    }

    @Override
    public void newRound() {
	tField.newRound();
	pField.getPongBall().reset();
	parent.queueGameStart();
    }

    public TetrisField getTetrisField() {
	return tField;
    }

    public PongField getPongField() {
	return pField;
    }

    @Override
    public void clearScreen() {
	tField.clearField();

    }

    @Override
    public void draw(Graphics2D g) {

	tField.draw(g);
	pField.draw(g);
	sField.draw(g);
    }

    @Override
    public void doLogic(double delta) {

	pField.getPongBall().collidedWith(tField.getBlock());
	for (Moveable b : pField.getMoveables()) {
	    b.move(delta);
	    b.doLogic();
	}

    }

    public void moveBlocks() {

	tField.moveBlocks();

    }

    @Override
    public int getBlockSpeed() {
	return tField.getSpeed();

    }

    @Override
    public void updateBlock(Block b, BlockType preview, TetrisField trigger) {
	System.out.println("KeyHandler received new block");
	System.out.println("Preview is " + preview);

	sField.setPreviewBlockType(preview);

	handler.setBlock(b);

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
