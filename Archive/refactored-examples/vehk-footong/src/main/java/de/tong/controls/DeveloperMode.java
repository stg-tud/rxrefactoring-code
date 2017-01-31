package de.tong.controls;

import de.tong.gui.screen.GameScreen;
import de.tong.player.Player;

public class DeveloperMode {

    public Player activater;
    public Player opponent;
    public boolean enabled = false;
    public int[] password = new int[4];
    public int passwordCount;
    public MultiplayerKeyHandler parent;

    public DeveloperMode(MultiplayerKeyHandler parent) {
	this.parent = parent;
	passwordCount = 0;
	password[0] = 1;
	password[1] = 3;
	password[2] = 3;
	password[3] = 7;

    }

    public void activateDeveloperMode(Player activater, Player opponent) {
	this.activater = activater;
	this.opponent = opponent;
	enabled = true;
	GameEvent.unlockAchievement(6, activater); // unlocks achievement 6
	System.out.println("Cheats Unlocked");
    }

    public boolean checkPassword(int i) {
	if (password[passwordCount] == i) {
	    if (passwordCount == 3) {
		return true;
	    } else {
		passwordCount += 1;
	    }
	} else {
	    passwordCount = 0;
	}
	return false;
    }

    public boolean getEnabled() {
	return enabled;
    }

    public void reflectBall() {
	((GameScreen) GameEvent.getPanel().getCurrentScreen()).getBall().setSpeedX(((GameScreen) GameEvent.getPanel().getCurrentScreen()).getBall().getSpeedX()
										   * (-1)); /* reverses the direction the ball is heading to */

    }

    public void setDownBlocks() {
	parent.getCurrentBlock2().fastDown();
	parent.getCurrentBlock1().fastDown();
    }

    public void speedUpBall() {
	((GameScreen) GameEvent.getPanel().getCurrentScreen()).getBall().setSpeedX(((GameScreen) GameEvent.getPanel().getCurrentScreen()).getBall().getSpeedX() * 4);
    }
}
