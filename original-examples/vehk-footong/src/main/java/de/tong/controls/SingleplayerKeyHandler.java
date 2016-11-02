package de.tong.controls;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import de.tong.graphics.Block;
import de.tong.gui.screen.Screen;
import de.tong.player.Player;

public class SingleplayerKeyHandler implements KeyListener {

    private Player player;
    private Block currentBlock;
    private boolean locked;
    private Screen owner;

    public SingleplayerKeyHandler(Player player, Screen s) {
	this.player = player;
	this.owner = s;
    }

    public void setBlock(Block newBlock) {
	this.currentBlock = newBlock;
    }

    @Override
    public void keyTyped(KeyEvent e) {
	// TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
	if (owner.isGameRunning()) {
	    if (e.getKeyCode() == player.getOptions().getRotation()) {
		currentBlock.move(Direction.ROTATE, true);
	    }
	    if (e.getKeyCode() == player.getOptions().getMoveToLeft()) {
		currentBlock.move(Direction.LEFT, true);
	    }
	    if (e.getKeyCode() == player.getOptions().getMoveToRight()) {
		currentBlock.move(Direction.RIGHT, true);
	    }
	    if (e.getKeyCode() == KeyEvent.VK_I) {
		currentBlock.getField().info();
	    }
	    if (e.getKeyCode() == player.getOptions().getAccelerate() && !locked) {
		locked = true;
		currentBlock.setSpeed(4, true);

	    }
	    if (e.getKeyCode() == player.getOptions().getFastDown()) {
		currentBlock.fastDown();
	    }

	    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		GameEvent.exitGame();
	    }
	}

    }

    @Override
    public void keyReleased(KeyEvent e) {
	if (owner.isGameRunning()) {
	    if (e.getKeyCode() == player.getOptions().getAccelerate() && locked) {
		currentBlock.setSpeed(4, false);
		locked = false;
	    }
	}

    }

}
