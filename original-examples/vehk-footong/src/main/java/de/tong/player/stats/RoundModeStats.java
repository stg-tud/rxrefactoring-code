package de.tong.player.stats;

import de.tong.player.Player;

/**
 * 
 * @author Eric Saier
 * 
 */

public class RoundModeStats extends Stats {
    private int victories;
    private int defeats;
    private int streak;

    private int roundsWon;
    private int roundsLost;
    private int bestResultRoundsWon;
    private int bestResultRoundsLost;
    private int worstResultRoundsWon;
    private int worstResultRoundsLost;

    public int getVictories() {
	return victories;
    }

    public void setVictories(int victories) {
	this.victories = victories;
    }

    public int getDefeats() {
	return defeats;
    }

    public void setDefeats(int defeats) {
	this.defeats = defeats;
    }

    public int getStreak() {
	return streak;
    }

    public void setStreak(int streak) {
	this.streak = streak;
    }

    public int getRoundsWon() {
	return roundsWon;
    }

    public void setRoundsWon(int roundsWon) {
	this.roundsWon = roundsWon;
    }

    public int getRoundsLost() {
	return roundsLost;
    }

    public void setRoundsLost(int roundsLost) {
	this.roundsLost = roundsLost;
    }

    public int getBestResultRoundsWon() {
	return bestResultRoundsWon;
    }

    public void setBestResultRoundsWon(int bestResultRoundsWon) {
	this.bestResultRoundsWon = bestResultRoundsWon;
    }

    public int getBestResultRoundsLost() {
	return bestResultRoundsLost;
    }

    public void setBestResultRoundsLost(int bestResultRoundsLost) {
	this.bestResultRoundsLost = bestResultRoundsLost;
    }

    public int getWorstResultRoundsWon() {
	return worstResultRoundsWon;
    }

    public void setWorstResultRoundsWon(int worstResultRoundsWon) {
	this.worstResultRoundsWon = worstResultRoundsWon;
    }

    public int getWorstResultRoundsLost() {
	return worstResultRoundsLost;
    }

    public void setWorstResultRoundsLost(int worstResultRoundsLost) {
	this.worstResultRoundsWon = worstResultRoundsLost;
    }

    public void updateStats(Player pl1, Player pl2) {
	matches++;
	timePlayed += pl1.getPlayTime();
	updateRows(pl1.getRows());

	if (pl1.getWon()) {
	    victories++;
	    if (this.streak < 0) {
		streak = 1;
	    } else {
		streak++;
	    }
	} else {
	    defeats++;
	    if (this.streak > 0) {
		streak = -1;
	    } else {
		streak--;
	    }
	}

	roundsWon += pl1.getRounds();
	roundsLost += pl2.getRounds();

	if (pl1.getRounds() - pl2.getRounds() > bestResultRoundsWon - bestResultRoundsLost || matches == 1) {
	    bestResultRoundsWon = pl1.getRounds();
	    bestResultRoundsLost = pl2.getRounds();
	}

	if (pl1.getRounds() - pl2.getRounds() < worstResultRoundsWon - worstResultRoundsLost || matches == 1) {
	    worstResultRoundsWon = pl1.getRounds();
	    worstResultRoundsLost = pl2.getRounds();
	}
    }

    public void addStats(RoundModeStats s) {
	addGeneralValues(s);
	victories = s.getVictories();
	defeats = s.getDefeats();
	streak = s.getStreak();

	roundsWon += s.getRoundsWon();
	roundsLost += s.getRoundsLost();

	if (s.getBestResultRoundsWon() - s.getBestResultRoundsLost() > bestResultRoundsWon - bestResultRoundsLost) {
	    bestResultRoundsWon = s.getBestResultRoundsWon();
	    bestResultRoundsLost = s.getBestResultRoundsLost();
	}
	if (s.getWorstResultRoundsWon() - s.getWorstResultRoundsLost() < worstResultRoundsWon - worstResultRoundsLost) {
	    worstResultRoundsWon = s.getWorstResultRoundsWon();
	    worstResultRoundsLost = s.getWorstResultRoundsLost();
	}
    }
}
