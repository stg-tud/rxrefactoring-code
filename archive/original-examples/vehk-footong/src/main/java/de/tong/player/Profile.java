package de.tong.player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.tong.controls.GameEvent;
import de.tong.player.stats.LifeModeStats;
import de.tong.player.stats.RegressionModeStats;
import de.tong.player.stats.RoundModeStats;
import de.tong.player.stats.SingleplayerStats;

/**
 * 
 * @author Eric Saier
 * 
 */

public class Profile {

    private static HashMap<Integer, Profile> profileMap = new HashMap<Integer, Profile>();

    private String name;
    private SingleplayerStats singleplayerStats;
    private RoundModeStats roundModeStats;
    private LifeModeStats lifeModeStats;
    private RegressionModeStats regressionModeStats;
    private int gamesWonTotal;
    private int gamesLostTotal;
    private Achievements ach;

    public Profile(String name) {
	this.name = name;
	singleplayerStats = new SingleplayerStats();
	roundModeStats = new RoundModeStats();
	lifeModeStats = new LifeModeStats();
	regressionModeStats = new RegressionModeStats();
	ach = new Achievements();
    }

    public String toString() {
	return name;
    }

    @Override
    public int hashCode() {
	return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
	if (o == null || !o.getClass().equals(getClass())) {
	    return false;
	}
	if (o == this) {
	    return true;
	}
	return name.equals(((Profile) o).getName());
    }

    // Erstellt ein Profil
    public static void createProfile(String profileName) {
	Profile.profileMap.put(profileName.hashCode(), new Profile(profileName));
    }

    public String getName() {
	return name;
    }

    public SingleplayerStats getSingleplayerStats() {
	return singleplayerStats;
    }

    public RoundModeStats getRoundModeStats() {
	return roundModeStats;
    }

    public LifeModeStats getLifeModeStats() {
	return lifeModeStats;
    }

    public RegressionModeStats getRegressionModeStats() {
	return regressionModeStats;
    }

    public Achievements getAchievements() {
	return ach;
    }

    public static HashMap<Integer, Profile> getProfileMap() {
	return profileMap;
    }

    // Aktualisiert ein Profil
    public void updateProfile(Player pl1, Player pl2, GameMode g) {
	if (g == GameMode.SINGLEPLAYER) {
	    singleplayerStats.updateStats(pl1);
	}
	if (g == GameMode.ROUNDMODE) {
	    roundModeStats.updateStats(pl1, pl2);
	}
	if (g == GameMode.LIFEMODE) {
	    lifeModeStats.updateStats(pl1, pl2);
	}
	if (g == GameMode.REGRESSIONMODE) {
	    regressionModeStats.updateStats(pl1, pl2);
	}
	if (g != GameMode.SINGLEPLAYER) {
	    if (pl1.getWon()) {
		gamesWonTotal += 1;
		if (gamesWonTotal == 50) { // Checks for Achievement 3
		    GameEvent.unlockAchievement(3, pl1);
		}
		if (gamesWonTotal == 250) { // Checks for Achievement 4
		    GameEvent.unlockAchievement(4, pl1);
		}
	    } else {
		gamesLostTotal += 1;
	    }

	    if (singleplayerStats.getMatches() == 50 && gamesWonTotal == 0 && gamesLostTotal == 0) {/* Checks for Achievement 5 */
		GameEvent.unlockAchievement(5, pl1);
	    }

	}
	serialize();
    }

    // Fügt die Werte eines anderen Profils hinzu
    public void addProfile(Profile p) {
	singleplayerStats.addStats(p.getSingleplayerStats());
	roundModeStats.addStats(p.getRoundModeStats());
	lifeModeStats.addStats(p.getLifeModeStats());
	regressionModeStats.addStats(p.getRegressionModeStats());
	ach.addAchievements(p.getAchievements());
	serialize();
    }

    // Entfernt ein Profil
    public void removeProfile() {
	profileMap.remove(hashCode());
    }

    // Speichert die Profile in die Datei profiles.txt
    public static void serialize() {
	String s = "";
	for (Profile p : profileMap.values()) {
	    s += "#" + p.getName() + ";";

	    s += p.getSingleplayerStats().getMatches()
		 + "," + p.getSingleplayerStats().getTimePlayed() + "," + p.getSingleplayerStats().getRows()[0] + "," + p.getSingleplayerStats().getRows()[1]
		 + "," + p.getSingleplayerStats().getRows()[2] + "," + p.getSingleplayerStats().getRows()[3] + "," + p.getSingleplayerStats().getRowAvg() + ","
		 + p.getSingleplayerStats().getBestRowAvg() + "," + p.getSingleplayerStats().getWorstRowAvg() + ",";
	    s += p.getSingleplayerStats().getTotalPoints() + "," + p.getSingleplayerStats().getMaxPoints() + ";";

	    s += p.getRoundModeStats().getMatches()
		 + "," + p.getRoundModeStats().getTimePlayed() + "," + p.getRoundModeStats().getRows()[0] + "," + p.getRoundModeStats().getRows()[1] + ","
		 + p.getRoundModeStats().getRows()[2] + "," + p.getRoundModeStats().getRows()[3] + "," + p.getRoundModeStats().getRowAvg() + ","
		 + p.getRoundModeStats().getBestRowAvg() + "," + p.getRoundModeStats().getWorstRowAvg() + ",";
	    s += p.getRoundModeStats().getVictories() + "," + p.getRoundModeStats().getDefeats() + "," + p.getRoundModeStats().getStreak() + ",";
	    s += p.getRoundModeStats().getRoundsWon()
		 + "," + p.getRoundModeStats().getRoundsLost() + "," + p.getRoundModeStats().getBestResultRoundsWon() + ","
		 + p.getRoundModeStats().getBestResultRoundsLost() + "," + p.getRoundModeStats().getWorstResultRoundsWon() + ","
		 + p.getRoundModeStats().getWorstResultRoundsLost() + ";";

	    s += p.getLifeModeStats().getMatches()
		 + "," + p.getLifeModeStats().getTimePlayed() + "," + p.getLifeModeStats().getRows()[0] + "," + p.getLifeModeStats().getRows()[1] + ","
		 + p.getLifeModeStats().getRows()[2] + "," + p.getLifeModeStats().getRows()[3] + "," + p.getLifeModeStats().getRowAvg() + ","
		 + p.getLifeModeStats().getBestRowAvg() + "," + p.getLifeModeStats().getWorstRowAvg() + ",";
	    s += p.getLifeModeStats().getVictories() + "," + p.getLifeModeStats().getDefeats() + "," + p.getLifeModeStats().getStreak() + ",";
	    s += p.getLifeModeStats().getLifesLost()
		 + "," + p.getLifeModeStats().getLifesTaken() + "," + p.getLifeModeStats().getBestResultLifes() + ","
		 + p.getLifeModeStats().getWorstResultLifes() + ";";

	    s += p.getRegressionModeStats().getMatches()
		 + "," + p.getRegressionModeStats().getTimePlayed() + "," + p.getRegressionModeStats().getRows()[0] + ","
		 + p.getRegressionModeStats().getRows()[1] + "," + p.getRegressionModeStats().getRows()[2] + "," + p.getRegressionModeStats().getRows()[3]
		 + "," + p.getRegressionModeStats().getRowAvg() + "," + p.getRegressionModeStats().getBestRowAvg() + ","
		 + p.getRegressionModeStats().getWorstRowAvg() + ",";
	    s += p.getRegressionModeStats().getVictories() + "," + p.getRegressionModeStats().getDefeats() + "," + p.getRegressionModeStats().getStreak() + ",";
	    s += p.getRegressionModeStats().getTotalPoints()
		 + "," + p.getRegressionModeStats().getBestResultPoints() + "," + p.getRegressionModeStats().getWorstResultPoints() + ";";

	    for (int i = 0; i < 50; i++) {
		if (p.getAchievements().isAchievementUnlocked(i)) {
		    s += "1";
		} else {
		    s += "0";
		}
	    }

	    s += ";";

	    try {

		BufferedWriter out = new BufferedWriter(new FileWriter("Profiles/profiles.txt"));
		out.write(s);
		out.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	// System.out.println(s);
    }

    // Liest die Profile aus der Datei profiles.txt
    public static void deserialize() {
	try {
	    BufferedReader reader = new BufferedReader(new FileReader("Profiles/profiles.txt"));
	    String profileString = reader.readLine();

	    String s;
	    String profileName = "";
	    String tempString = "";
	    List<String> segmentParts = new ArrayList<String>();
	    int segment = -1;

	    for (int i = 0; i < profileString.length(); i++) {
		s = profileString.substring(i, i + 1);

		if (s.equals("#")) {
		    segment = 0;
		}

		else if (s.equals(",")) {
		    segmentParts.add(tempString);
		    tempString = "";
		}

		else if (s.equals(";")) {
		    segmentParts.add(tempString);
		    tempString = "";

		    if (segment == 0) {
			profileName = segmentParts.get(0);
			Profile.createProfile(profileName);

		    } else if (segment == 1) {
			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setMatches(Integer.parseInt(segmentParts.get(0)));
			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setTimePlayed(Integer.parseInt(segmentParts.get(1)));
			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setRows(Integer.parseInt(segmentParts.get(2)),
													   Integer.parseInt(segmentParts.get(3)),
													   Integer.parseInt(segmentParts.get(4)),
													   Integer.parseInt(segmentParts.get(5)));
			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setRowAvg(Double.parseDouble(segmentParts.get(6)));
			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setBestRowAvg(Double.parseDouble(segmentParts.get(7)));
			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setWorstRowAvg(Double.parseDouble(segmentParts.get(8)));

			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setTotalPoints(Integer.parseInt(segmentParts.get(9)));
			Profile.getProfileMap().get(profileName.hashCode()).getSingleplayerStats().setMaxPoints(Integer.parseInt(segmentParts.get(10)));

		    } else if (segment == 2) {
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setMatches(Integer.parseInt(segmentParts.get(0)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setTimePlayed(Integer.parseInt(segmentParts.get(1)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setRows(Integer.parseInt(segmentParts.get(2)),
													Integer.parseInt(segmentParts.get(3)),
													Integer.parseInt(segmentParts.get(4)),
													Integer.parseInt(segmentParts.get(5)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setRowAvg(Double.parseDouble(segmentParts.get(6)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setBestRowAvg(Double.parseDouble(segmentParts.get(7)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setWorstRowAvg(Double.parseDouble(segmentParts.get(8)));

			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setVictories(Integer.parseInt(segmentParts.get(9)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setDefeats(Integer.parseInt(segmentParts.get(10)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setStreak(Integer.parseInt(segmentParts.get(11)));

			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setRoundsWon(Integer.parseInt(segmentParts.get(12)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setRoundsLost(Integer.parseInt(segmentParts.get(13)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setBestResultRoundsWon(Integer.parseInt(segmentParts.get(14)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setBestResultRoundsLost(Integer.parseInt(segmentParts.get(15)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setWorstResultRoundsWon(Integer.parseInt(segmentParts.get(16)));
			Profile.getProfileMap().get(profileName.hashCode()).getRoundModeStats().setWorstResultRoundsLost(Integer.parseInt(segmentParts.get(17)));

		    } else if (segment == 3) {
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setMatches(Integer.parseInt(segmentParts.get(0)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setTimePlayed(Integer.parseInt(segmentParts.get(1)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setRows(Integer.parseInt(segmentParts.get(2)),
												       Integer.parseInt(segmentParts.get(3)),
												       Integer.parseInt(segmentParts.get(4)),
												       Integer.parseInt(segmentParts.get(5)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setRowAvg(Double.parseDouble(segmentParts.get(6)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setBestRowAvg(Double.parseDouble(segmentParts.get(7)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setWorstRowAvg(Double.parseDouble(segmentParts.get(8)));

			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setVictories(Integer.parseInt(segmentParts.get(9)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setDefeats(Integer.parseInt(segmentParts.get(10)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setStreak(Integer.parseInt(segmentParts.get(11)));

			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setLifesLost(Integer.parseInt(segmentParts.get(12)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setLifesTaken(Integer.parseInt(segmentParts.get(13)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setBestResultLifes(Integer.parseInt(segmentParts.get(14)));
			Profile.getProfileMap().get(profileName.hashCode()).getLifeModeStats().setWorstResultLifes(Integer.parseInt(segmentParts.get(15)));

		    } else if (segment == 4) {
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setMatches(Integer.parseInt(segmentParts.get(0)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setTimePlayed(Integer.parseInt(segmentParts.get(1)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setRows(Integer.parseInt(segmentParts.get(2)),
													     Integer.parseInt(segmentParts.get(3)),
													     Integer.parseInt(segmentParts.get(4)),
													     Integer.parseInt(segmentParts.get(5)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setRowAvg(Double.parseDouble(segmentParts.get(6)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setBestRowAvg(Double.parseDouble(segmentParts.get(7)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setWorstRowAvg(Double.parseDouble(segmentParts.get(8)));

			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setVictories(Integer.parseInt(segmentParts.get(9)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setDefeats(Integer.parseInt(segmentParts.get(10)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setStreak(Integer.parseInt(segmentParts.get(11)));

			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setTotalPoints(Integer.parseInt(segmentParts.get(12)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setBestResultPoints(Integer.parseInt(segmentParts.get(13)));
			Profile.getProfileMap().get(profileName.hashCode()).getRegressionModeStats().setWorstResultPoints(Integer.parseInt(segmentParts.get(14)));

		    } else if (segment == 5) {
			for (int j = 0; j < 50; j++) {
			    if (segmentParts.get(0).substring(j, j + 1).equals("1")) {
				Profile.getProfileMap().get(profileName.hashCode()).getAchievements().unlockAchievement(j);
			    }
			}
		    }

		    segmentParts = new ArrayList<String>();
		    segment++;
		} else {
		    tempString = tempString + s;
		}

	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    // Importiert Profile (noch nicht fertig)
    public static void importProfiles(String filename) {
	HashMap<Integer, Profile> importedProfileMap = null;

	try {
	    FileInputStream file = new FileInputStream(filename);
	    ObjectInputStream input = new ObjectInputStream(file);
	    importedProfileMap = (HashMap<Integer, Profile>) input.readObject();
	    input.close();
	} catch (IOException e) {
	    System.err.println(e);
	} catch (ClassNotFoundException e) {
	    System.err.println(e);
	}

	boolean bool;
	for (Profile p1 : importedProfileMap.values()) {
	    bool = false;
	    for (Profile p2 : profileMap.values()) {
		if (p1.equals(p2)) {
		    bool = true;
		}
	    }
	    if (true) { // Abfragen, ob das Profil hinzugefuegt werden soll
		if (bool) {
		    if (true) { // Abfragen, ob die Werte des Profils dem
				// gleichnamigen hinzugefuegt werden soll
			Profile.getProfileMap().get(p1.hashCode()).addProfile(p1);
		    } else {
			Profile.createProfile("newProfileName");
			Profile.getProfileMap().get("newProfileName".hashCode()).addProfile(p1);
		    }
		} else {
		    Profile.createProfile(p1.getName());
		}
	    }
	}
    }
}
