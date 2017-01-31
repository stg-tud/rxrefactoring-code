package de.tong.controls;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import de.tong.graphics.Block;
import de.tong.gui.screen.Screen;
import de.tong.player.Player;

public class MultiplayerKeyHandler implements KeyListener {

    private Player player1;
    private Player player2;
    private Block currentBlock1;
    private Block currentBlock2;
    private boolean locked;
    private boolean locked1;
    private Screen owner;
    private DeveloperMode cheats;

    public MultiplayerKeyHandler(Player player1, Player player2, Screen s) {
	this.player1 = player1;
	this.player2 = player2;
	this.owner = s;
	cheats = new DeveloperMode(this);
    }

    public void setBlock1(Block newBlock) {
	this.currentBlock1 = newBlock;

    }

    public void setBlock2(Block newBlock) {
	this.currentBlock2 = newBlock;
    }

    @Override
    public void keyTyped(KeyEvent e) {
	// TODO Auto-generated method stub

    }

    public Block getCurrentBlock1() {
	return currentBlock1;
    }

    public Block getCurrentBlock2() {
	return currentBlock2;
    }

    @Override
    public void keyPressed(KeyEvent e) {
	if (owner.isGameRunning()) {
	    if (e.getKeyCode() == player1.getOptions().getPl1Rotation()) {
		currentBlock1.move(Direction.ROTATE, true);
	    }
	    if (e.getKeyCode() == player1.getOptions().getPl1MoveToLeft()) {
		currentBlock1.move(Direction.LEFT, true);
	    }
	    if (e.getKeyCode() == player1.getOptions().getPl1MoveToRight()) {
		currentBlock1.move(Direction.RIGHT, true);
	    }
	    if (e.getKeyCode() == KeyEvent.VK_I) {
		currentBlock1.getField().info();
	    }
	    if (e.getKeyCode() == player1.getOptions().getPl1Accelerate() && !locked) {
		locked = true;
		currentBlock1.setSpeed(2, true);

	    }
	    if (e.getKeyCode() == player1.getOptions().getPl1FastDown()) {
		currentBlock1.fastDown();
	    }

	    if (e.getKeyCode() == player2.getOptions().getPl2Rotation()) {
		currentBlock2.move(Direction.ROTATE, true);
	    }
	    if (e.getKeyCode() == player2.getOptions().getPl2MoveToLeft()) {
		currentBlock2.move(Direction.LEFT, true);
	    }
	    if (e.getKeyCode() == player2.getOptions().getPl2MoveToRight()) {
		currentBlock2.move(Direction.RIGHT, true);
	    }
	    if (e.getKeyCode() == KeyEvent.VK_I) {
		currentBlock2.getField().info();
	    }
	    if (e.getKeyCode() == player2.getOptions().getPl2Accelerate() && !locked1) {
		locked1 = true;
		currentBlock2.setSpeed(2, true);

	    }
	    if (e.getKeyCode() == player2.getOptions().getPl2FastDown()) {
		currentBlock2.fastDown();
	    }
	    if (e.getKeyCode() == KeyEvent.VK_0
		|| e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_2 || e.getKeyCode() == KeyEvent.VK_3 || e.getKeyCode() == KeyEvent.VK_4
		|| e.getKeyCode() == KeyEvent.VK_5 || e.getKeyCode() == KeyEvent.VK_6 || e.getKeyCode() == KeyEvent.VK_7 || e.getKeyCode() == KeyEvent.VK_8
		|| e.getKeyCode() == KeyEvent.VK_9) {
		if (cheats.checkPassword(specifyNumber(e.getKeyCode()))) {
		    cheats.activateDeveloperMode(player2, player1);
		}
	    }
	    if (e.getKeyCode() == KeyEvent.VK_NUMPAD0
		|| e.getKeyCode() == KeyEvent.VK_NUMPAD1 || e.getKeyCode() == KeyEvent.VK_NUMPAD2 || e.getKeyCode() == KeyEvent.VK_NUMPAD3
		|| e.getKeyCode() == KeyEvent.VK_NUMPAD4 || e.getKeyCode() == KeyEvent.VK_NUMPAD5 || e.getKeyCode() == KeyEvent.VK_NUMPAD6
		|| e.getKeyCode() == KeyEvent.VK_NUMPAD7 || e.getKeyCode() == KeyEvent.VK_NUMPAD8 || e.getKeyCode() == KeyEvent.VK_NUMPAD9) {
		if (cheats.checkPassword(specifyNumber(e.getKeyCode()))) {
		    cheats.activateDeveloperMode(player1, player2);
		}
	    }
	    if (e.getKeyCode() == KeyEvent.VK_F1 && cheats.getEnabled()) {
		cheats.reflectBall();
	    }
	    if (e.getKeyCode() == KeyEvent.VK_F2 && cheats.getEnabled()) {
		cheats.setDownBlocks();
	    }
	    if (e.getKeyCode() == KeyEvent.VK_F3 && cheats.getEnabled()) {
		cheats.speedUpBall();
	    }

	    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		GameEvent.exitGame();
	    }
	}

    }

    private int specifyNumber(int keyCode) { // converts the KeyCode to the
					     // corresponding int
	switch (keyCode) {

	    case KeyEvent.VK_0:
		return 0;
	    case KeyEvent.VK_1:
		return 1;
	    case KeyEvent.VK_2:
		return 2;
	    case KeyEvent.VK_3:
		return 3;
	    case KeyEvent.VK_4:
		return 4;
	    case KeyEvent.VK_5:
		return 5;
	    case KeyEvent.VK_6:
		return 6;
	    case KeyEvent.VK_7:
		return 7;
	    case KeyEvent.VK_8:
		return 8;
	    case KeyEvent.VK_9:
		return 9;

	    case KeyEvent.VK_NUMPAD0:
		return 0;
	    case KeyEvent.VK_NUMPAD1:
		return 1;
	    case KeyEvent.VK_NUMPAD2:
		return 2;
	    case KeyEvent.VK_NUMPAD3:
		return 3;
	    case KeyEvent.VK_NUMPAD4:
		return 4;
	    case KeyEvent.VK_NUMPAD5:
		return 5;
	    case KeyEvent.VK_NUMPAD6:
		return 6;
	    case KeyEvent.VK_NUMPAD7:
		return 7;
	    case KeyEvent.VK_NUMPAD8:
		return 8;
	    case KeyEvent.VK_NUMPAD9:
		return 9;

	    default:
		return 0;
	}
    }

    @Override
    public void keyReleased(KeyEvent e) {
	if (owner.isGameRunning()) {
	    if (e.getKeyCode() == player1.getOptions().getPl1Accelerate() && locked) {
		currentBlock1.setSpeed(2, false);
		locked = false;
	    }
	    if (e.getKeyCode() == player2.getOptions().getPl2Accelerate() && locked1) {
		currentBlock2.setSpeed(2, false);
		locked1 = false;
	    }
	}

    }

}
