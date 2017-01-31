package de.tong.player.stats;

import de.tong.player.Player;

/**
 * 
 * @author Eric Saier
 * 
 */

public class RegressionModeStats extends Stats {
    private int victories;
    private int defeats;
    private int streak;

    private int totalPoints;
    private int bestResultPoints;
    private int worstResultPoints;

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

    public int getTotalPoints() {
	return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
	this.totalPoints = totalPoints;
    }

    public int getBestResultPoints() {
	return bestResultPoints;
    }

    public void setBestResultPoints(int bestResultPoints) {
	this.bestResultPoints = bestResultPoints;
    }

    public int getWorstResultPoints() {
	return worstResultPoints;
    }

    public void setWorstResultPoints(int worstResultPoints) {
	this.worstResultPoints = worstResultPoints;
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

	totalPoints += pl1.getOptions().getDefaultPointsToReach() - pl1.getPoints();

	if (pl2.getPoints() > bestResultPoints) {
	    bestResultPoints = pl2.getPoints();
	}
	if (pl1.getPoints() > bestResultPoints) {
	    worstResultPoints = pl1.getPoints();
	}
    }

    public void addStats(RegressionModeStats s) {
	addGeneralValues(s);
	victories = s.getVictories();
	defeats = s.getDefeats();
	streak = s.getStreak();

	totalPoints += s.getTotalPoints();

	if (s.getBestResultPoints() > bestResultPoints) {
	    bestResultPoints = s.getBestResultPoints();
	}
	if (s.getWorstResultPoints() > bestResultPoints) {
	    worstResultPoints = s.getWorstResultPoints();
	}
    }
}
