package de.tong.player.stats;

/**
 * 
 * @author Eric Saier
 * 
 */

public abstract class Stats {
    protected int matches;
    protected int timePlayed;

    protected int[] rows;
    protected double rowAvg;
    protected double bestRowAvg;
    protected double worstRowAvg;

    public Stats() {
	rows = new int[4];
    }

    public int getMatches() {
	return matches;
    }

    public void setMatches(int matches) {
	this.matches = matches;
    }

    public int getTimePlayed() {
	return timePlayed;
    }

    public void setTimePlayed(int timePlayed) {
	this.timePlayed = timePlayed;
    }

    public int[] getRows() {
	return rows;
    }

    public void setRows(int... rows) {
	this.rows = rows;
    }

    public double getRowAvg() {
	return rowAvg;
    }

    public void setRowAvg(double rowAvg) {
	this.rowAvg = rowAvg;
    }

    public double getBestRowAvg() {
	return bestRowAvg;
    }

    public void setBestRowAvg(double bestRowAvg) {
	this.bestRowAvg = bestRowAvg;
    }

    public double getWorstRowAvg() {
	return worstRowAvg;
    }

    public void setWorstRowAvg(double worstRowAvg) {
	this.worstRowAvg = worstRowAvg;
    }

    public static int totalRows(int[] rows) {
	return (rows[0] + rows[1] * 2 + rows[2] * 3 + rows[3] * 4);
    }

    public static int totalBlocksRemovingRows(int[] rows) {
	return (rows[0] + rows[1] + rows[2] + rows[3]);
    }

    public static double calcRowAvg(int[] rows) {
	return (double) totalRows(rows) / totalBlocksRemovingRows(rows);
    }

    public void updateRows(int[] rows) {
	for (int i = 0; i < 4; i++) {
	    this.rows[i] += rows[i];
	}

	rowAvg = calcRowAvg(this.rows);

	if (calcRowAvg(this.rows) > bestRowAvg) {
	    bestRowAvg = calcRowAvg(rows);
	}
	if (calcRowAvg(this.rows) < worstRowAvg || worstRowAvg == 0) {
	    worstRowAvg = calcRowAvg(rows);
	}
    }

    public void addGeneralValues(Stats s) {
	matches += s.getMatches();
	timePlayed += s.getTimePlayed();
	for (int i = 0; i < 4; i++) {
	    rows[i] += s.getRows()[i];
	}

	if (s.getBestRowAvg() > bestRowAvg) {
	    bestRowAvg = s.getBestRowAvg();
	}
	if (s.getWorstRowAvg() > worstRowAvg) {
	    worstRowAvg = s.getWorstRowAvg();
	}
    }
}
