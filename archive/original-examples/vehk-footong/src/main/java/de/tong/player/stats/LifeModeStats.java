package de.tong.player.stats;

import de.tong.player.Player;

/**
 * 
 * @author Eric Saier
 * 
 */

public class LifeModeStats extends Stats {
    private int victories;
    private int defeats;
    private int streak;

    private int lifesLost;
    private int lifesTaken;
    private int bestResultLifes;
    private int worstResultLifes;

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

    public int getLifesLost() {
	return lifesLost;
    }

    public void setLifesLost(int lifesLost) {
	this.lifesLost = lifesLost;
    }

    public int getLifesTaken() {
	return lifesTaken;
    }

    public void setLifesTaken(int lifesTaken) {
	this.lifesTaken = lifesTaken;
    }

    public void getLifesTaken(int lifesTaken) {
	this.lifesTaken = lifesTaken;
    }

    public int getBestResultLifes() {
	return bestResultLifes;
    }

    public void setBestResultLifes(int bestResultLifes) {
	this.bestResultLifes = bestResultLifes;
    }

    public int getWorstResultLifes() {
	return worstResultLifes;
    }

    public void setWorstResultLifes(int worstResultLifes) {
	this.worstResultLifes = worstResultLifes;
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

	lifesLost += pl1.getOptions().getDefaultLifes() - pl1.getLifes();
	lifesTaken += pl1.getOptions().getDefaultLifes() - pl2.getLifes();

	if (pl1.getLifes() > bestResultLifes) {
	    bestResultLifes = pl1.getLifes();
	}
	if (pl2.getLifes() > worstResultLifes) {
	    worstResultLifes = pl2.getLifes();
	}
    }

    public void addStats(LifeModeStats s) {
	addGeneralValues(s);
	victories = s.getVictories();
	defeats = s.getDefeats();
	streak = s.getStreak();

	lifesLost += s.getLifesLost();
	lifesTaken += s.getLifesTaken();

	if (s.getBestResultLifes() > bestResultLifes) {
	    bestResultLifes = s.getBestResultLifes();
	}
	if (s.getWorstResultLifes() > worstResultLifes) {
	    worstResultLifes = s.getWorstResultLifes();
	}
    }
}
