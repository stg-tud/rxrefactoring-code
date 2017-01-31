package de.tong.player.stats;

import de.tong.player.Player;

/**
 * 
 * @author Eric Saier
 * 
 */

public class SingleplayerStats extends Stats {
    private int totalPoints;
    private int maxPoints;

    public int getTotalPoints() {
	return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
	this.totalPoints = totalPoints;
    }

    public int getMaxPoints() {
	return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
	this.maxPoints = maxPoints;
    }

    public void updateStats(Player pl) {
	matches++;
	timePlayed += pl.getPlayTime();
	updateRows(pl.getRows());

	totalPoints += pl.getPoints();

	if (pl.getPoints() > maxPoints) {
	    maxPoints = pl.getPoints();
	}
    }

    public void addStats(SingleplayerStats s) {
	addGeneralValues(s);
	totalPoints += s.getTotalPoints();

	if (s.getMaxPoints() > maxPoints) {
	    maxPoints = s.getMaxPoints();
	}
    }
}
