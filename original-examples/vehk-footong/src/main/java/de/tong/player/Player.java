package de.tong.player;

import de.tong.controls.GameEvent;

/**
 * 
 * @author Eric Saier
 * 
 */

public class Player {
    private String name;
    private int playTime;
    private boolean won;

    private int points;
    private int rounds;
    private int roundsWon;
    private int lifes;
    private int pointsLeft;

    private boolean lostBall = false; // Variable needed for Achievement 10

    private int[] rows;

    private Options options;

    private int smallestRowsRemoved;
    private int greatestRowsRemoved;

    private boolean lostLifes = false;

    // Die Anzahl der Punkte, die man fuer n Reihen bekommt
    private static final int[] ROW_POINTS = { 100, 300, 900, 2700 };

    public Player(String name) {
	this.name = name;
	options = new Options(this);
	rows = new int[4];
	rounds = 0;
	roundsWon = 0;
	lifes = options.getDefaultLifes();
	pointsLeft = options.getDefaultPointsToReach();
	smallestRowsRemoved = 5;
	greatestRowsRemoved = 0;
    }

    public String getName() {
	return name;
    }

    public int getRoundsWon() {
	return roundsWon;
    }

    public Options getOptions() {
	return options;
    }

    public int getPlayTime() {
	return playTime;
    }

    public boolean getWon() {
	return won;
    }

    public int getRounds() {
	return rounds;
    }

    public int getLifes() {
	return lifes;
    }

    public int getPointsLeft() {
	return pointsLeft;
    }

    public int getPoints() {
	return points;
    }

    public int[] getRows() {
	return rows;
    }

    // Fuegt Punkte hinzu
    public void addPoints(int p) {
	points += p;
    }

    // Fuegt Punkte hinzu
    public void addPoints(int rows, int m) {
	this.rows[rows - 1]++;
	points += ROW_POINTS[rows - 1] * m;
	pointsLeft -= ROW_POINTS[rows - 1] * m;
	if (smallestRowsRemoved == 5 && greatestRowsRemoved == 0) {
	    smallestRowsRemoved = rows;
	    greatestRowsRemoved = rows;
	}
	if (rows > greatestRowsRemoved) {
	    greatestRowsRemoved = rows;
	}
	if (rows < smallestRowsRemoved) {
	    smallestRowsRemoved = rows;
	}
	if (pointsLeft <= 0 && options.getCurrentMode() == GameMode.REGRESSIONMODE) {
	    pointsLeft = 0;
	    this.won = true;
	    GameEvent.endRegressionMode(this);
	}
    }

    public void resetPoints() {
	points = 0;
    }

    // Beendet eine Runde
    public void endRound(boolean won, Player pl) {

	if (won) {
	    GameEvent.unlockAchievement(2, this); // TODO Has to be transfered
	    // to endGame()
	}

	switch (options.getCurrentMode()) {

	    case LIFEMODE:
		if (!won) {
		    lifes--;
		    lostLifes = true;
		    if (lifes == 0) {
			this.won = false;
			endGame(this, options.getCurrentMode(), false);
		    }
		}
		break;

	    case ROUNDMODE:
		if (!won) {
		    rounds++;
		} else {
		    rounds++;
		    roundsWon++;
		    if (roundsWon == options.getDefaultRounds()) {
			this.won = true;
			endGame(pl, options.getCurrentMode(), false);
		    }
		}
		break;

	    case REGRESSIONMODE:
		if (!won) {
		    if (pointsLeft <= 1000) {
			GameEvent.unlockAchievement(46, this);
		    }
		    pointsLeft = pointsLeft + options.getDefaultPointLoss();
		}
		break;

	    case SINGLEPLAYER:
		resetPoints();
		break;

	    default:
		System.out.println("You play a strange mode, the only winning move is not to play");
		break;
	}
    }

    // Beendet ein Spiel und speichert die Werte in das entsprechende Profil
    public void endGame(Player pl, GameMode g, boolean forcedExit) {
	if (!forcedExit) {
	    if (!lostBall) {
		GameEvent.unlockAchievement(10, this);
	    } else {
		lostBall = false;
	    }

	    if (greatestRowsRemoved <= 3) {
		GameEvent.unlockAchievement(24, this);
	    }

	    if (smallestRowsRemoved > 1) {
		GameEvent.unlockAchievement(23, this);
	    }
	    if (roundsWon == rounds && rounds >= 5 && options.getCurrentMode() == GameMode.ROUNDMODE) {
		GameEvent.unlockAchievement(27, this);
	    }
	    if (lifes == 1 && options.getCurrentMode() == GameMode.LIFEMODE) {
		GameEvent.unlockAchievement(36, this);
	    }
	    if (!lostLifes && options.getCurrentMode() == GameMode.LIFEMODE) {
		GameEvent.unlockAchievement(37, this);
	    }
	    if (lifes > options.getDefaultLifes() && options.getCurrentMode() == GameMode.LIFEMODE) {
		GameEvent.unlockAchievement(38, this);
	    }
	    if (won && options.getCurrentMode() == GameMode.REGRESSIONMODE) {
		GameEvent.unlockAchievement(42, this);
	    }
	}

	Profile.getProfileMap().get(name.hashCode()).updateProfile(this, pl, g);
	Profile.getProfileMap().get(pl.getName().hashCode()).updateProfile(pl, this, g);
	GameEvent.endGame();
	this.reset();
	pl.reset();

    }

    // Setzt die Werte zurueck
    private void reset() {
	resetPoints();
	rounds = 0;
	roundsWon = 0;
	lifes = options.getDefaultLifes();
	pointsLeft = options.getDefaultPointsToReach();
	smallestRowsRemoved = 5;
	greatestRowsRemoved = 0;
    }

    public void setLostBall() {
	lostBall = true;

    }
}
