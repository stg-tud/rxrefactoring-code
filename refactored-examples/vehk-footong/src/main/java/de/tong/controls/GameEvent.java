package de.tong.controls;

import de.tong.gui.GamePanel;
import de.tong.gui.screen.GameScreen;
import de.tong.gui.screen.MenuScreen;
import de.tong.player.Achievements;
import de.tong.player.GameMode;
import de.tong.player.Player;
import de.tong.player.Profile;

/**
 * This class maintains all the events that can be called in the game.
 * 
 * @author Simon Dieck
 * 
 */
public class GameEvent {

    private static GamePanel panel;

    public static void rowsDeleted(int quantity, Player trigger) {
	trigger.addPoints(quantity, 1);
    }

    public static void unlockAchievement(int i, Player trigger) {
	if (!Profile.getProfileMap().get(trigger.getName().hashCode()).getAchievements().isAchievementUnlocked(i)) {
	    Profile.getProfileMap().get(trigger.getName().hashCode()).getAchievements().unlockAchievement(i);
	    System.out.println("Unlocked Achievement " + Achievements.getAchievementList().get(i).getName() + " for " + trigger.getName());
	}

    }

    public static void lostBall(Player trigger, Player winner) {
	trigger.endRound(false, winner);
	trigger.setLostBall();

	if (trigger.getOptions().getCurrentMode() != GameMode.SINGLEPLAYER) {
	    System.out.println(winner.getName());
	    if (panel.isGameRunning() && ((GameScreen) panel.getCurrentScreen()).getBall().getFourRowContact()) {
		if (((GameScreen) panel.getCurrentScreen()).getBall().getFourRowHitter() == winner) {
		    unlockAchievement(11, winner);
		}
	    }
	    winner.endRound(true, trigger);
	}

	if (trigger.getOptions().getCurrentMode() == GameMode.SINGLEPLAYER || trigger.getOptions().getCurrentMode() == GameMode.ROUNDMODE) {
	    clearAll();
	}
	startNewRound();
    }

    private static void startNewRound() {
	if (panel.isGameRunning()) {
	    ((GameScreen) panel.getCurrentScreen()).newRound();
	}

    }

    private static void clearAll() {
	panel.getCurrentScreen().clearScreen();

    }

    public static void passedBorders(Player trigger, Player winner) {

	trigger.endRound(false, winner);
	if (trigger.getOptions().getCurrentMode() != GameMode.SINGLEPLAYER) {
	    winner.endRound(true, trigger);
	}
	clearAll();
	startNewRound();
    }

    public static void setPanel(GamePanel gPanel) {
	panel = gPanel;
    }

    public static GamePanel getPanel() {
	return panel;
    }

    public static void endGame() {

	panel.endGame();
	panel.setScreen(new MenuScreen(panel));
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public static void exitGame() {
	panel.pl1.endGame(panel.pl1, panel.pl1.getOptions().getCurrentMode(), true);
    }

    public static void endRegressionMode(Player trigger) {
	if (((GameScreen) panel.getCurrentScreen()).getBall().getParent().getOwner() == trigger) { // Distinct
	    // between
	    // players
	    trigger.endGame(((GameScreen) panel.getCurrentScreen()).getBall().getParent().getOwnerRight(), trigger.getOptions().getCurrentMode(), false);
	} else if (((GameScreen) panel.getCurrentScreen()).getBall().getParent().getOwnerRight() == trigger) {
	    trigger.endGame(((GameScreen) panel.getCurrentScreen()).getBall().getParent().getOwner(), trigger.getOptions().getCurrentMode(), false);
	}
    }
}
