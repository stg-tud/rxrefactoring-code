package de.tong.player;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Eric Saier
 * 
 */

public class Achievements {
    private static List<Achievement> ach = new ArrayList<Achievement>();

    public static List<Achievement> getAchievementList() {
	if (ach == null) {
	    init();
	}
	return ach;
    }

    public static void initialize() {
	init();
    }

    public static Achievement getAchievement(int i) {
	if (ach == null) {
	    init();
	}
	return ach.get(i);
    }

    private static void init() {
	ach.add(new Achievement(0, "127.0.0.1", "Play Singleplayer.", "Spiele Singleplayer."));
	ach.add(new Achievement(1, "The Button For The Internet", "Play Multiplayer.", "Spiele Multiplayer."));
	ach.add(new Achievement(2, "Lucky Beggar", "Win a game", "Gewinne ein Spiel."));
	ach.add(new Achievement(3, "Ph33r My 1337 Sk1llz", "Win 50 games.", "Gewinne 50 Spiele."));
	ach.add(new Achievement(4, "The Chosen One", "Win 250 games.", "Gewinne 250 Spiele."));
	ach.add(new Achievement(5,
				"Forever Alone",
				"Play 50 singleplayer games without having played multiplayer.",
				"Spiele 50 Singleplayerspiele ohne je Multiplayer gespielt zu haben."));
	ach.add(new Achievement(6, "Alexander Seitz", "Unlock the cheats.", "Schalte die Cheats frei."));
	ach.add(new Achievement(7, "kthxbye", "Score a point with the first ball you play.", "Erziele einen Punkt mit dem ersten gespielten Ball."));
	ach.add(new Achievement(8, "Efficiency", "Play the ball twice with the same block.", "Spiele den Ball zweimal mit dem selben Block."));
	ach.add(new Achievement(9,
				"None Shall Pass!",
				"Play the ball without letting it get into your field.",
				"Spiele den Ball ohne ihn in dein Feld zu lassen."));
	ach.add(new Achievement(10,
				"Never Gonna Give You Up",
				"Don't lose the ball once in an entire game.",
				"Verliere den Ball nicht einmal in einem gesamten Spiel."));
	ach.add(new Achievement(11,
				"Tong",
				"Remove four rows with one block and score a point with it.",
				"Baue 4 Reihen mit einem Block ab und erziele mit selbigem einen Punkt."));
	ach.add(new Achievement(12, "Infinity", "Get the same block 4 times in a row.", "Erhalte den selben Block 4 mal hintereinander."));
	ach.add(new Achievement(13, "FFFFFFUUUUU", "Get a s-block or a z-block at the beginning.", "Erhalte einen s-Block oder einen z-Block zu Beginn."));
	ach.add(new Achievement(14,
				"Blocks Begone!",
				"Remove all of your blocks with one single block.",
				"Baue deine gesamten Blöcke mit einem einzigen Block ab."));
	ach.add(new Achievement(15,
				"Unity",
				"Remove a row containing only one type of block.",
				"Baue eine Reihe ab, die nur eine Sorte von Blöcken beinhaltet."));
	ach.add(new Achievement(16,
				"Harakiri",
				"Lose at Tetris while the ball is on your opponent's side.",
				"Verliere Tetris während der Ball auf der Seite deines Gegners ist."));
	ach.add(new Achievement(17,
				"Swiftness",
				"Remove 6 rows while the ball is on your opponent's side.",
				"Baue 6 Reihen ab, während der Ball auf der Seite deines Gegners ist."));
	ach.add(new Achievement(18,
				"You Spin Me Right Round",
				"Rotate a block 100 times before it touches the ground.",
				"Rotiere einen Block 100 Mal bevor er den Boden erreicht."));
	ach.add(new Achievement(19,
				"Never Tell Me The Odds",
				"Hit the ball with the shorter side of an I-block.",
				"Treffe den Ball mit der kürzeren Seite des I-Blocks."));
	ach.add(new Achievement(20, "~", "Win a game with a rowaverage below 2.", "Gewinne ein Spiel mit einem Reihendurchschnitt unter 2."));
	ach.add(new Achievement(21, "~", "Win a game with a rowaverage above 3.", "Gewinne ein Spiel mit einen Reihendurchschnitt über 3."));
	ach.add(new Achievement(22,
				"~",
				"Win a game with a rowverage 2 rows below your opponent's.",
				"Gewinne ein Spiel mit einem Reihendurchschnitt 2 Reihen niedriger als der deines Gegners."));
	ach.add(new Achievement(23, "No Small Fry", "Win a game without removing single rows.", "Gewinne ein Spiel ohne einzelne Reihen abzubauen."));
	ach.add(new Achievement(24,
				"Minimalist",
				"Win a game without removing more than 3 rows at once.",
				"Gewinne ein Spiel ohne 3 Reihen oder mehr auf einmal abzubauen."));
	ach.add(new Achievement(25, "~", "Unlock all achievements.", "Schalte alle Achievements frei."));

	ach.add(new Achievement(26,
				"Minuteman",
				"Win a game with each round lasting less than a minute.",
				"Gewinne ein Spiel bei dem jede Runde weniger als eine Minute dauert."));
	ach.add(new Achievement(27, "Whitewash", "Win all rounds (at least 5) of a game.", "Gewinne alle Runden (mindestens 5) eines Spiels."));
	ach.add(new Achievement(28,
				"No Mercy",
				"Win the last 5 rounds with your opponent having to score only one round for the victory.",
				"Gewinne die letzten 5 Runden während dein Gegner nur eine Runde für den Sieg braucht."));
	ach.add(new Achievement(29,
				"On The Brink Of Defeat",
				"Win the last and deciding round of a game.",
				"Gewinne die letzte und entscheinende Runde eines Spiels."));
	ach.add(new Achievement(30, "Gargle!", "Lose the last and deciding round of a game", "Verliere die letzte und entscheinende Runde eines Spiels."));
	ach.add(new Achievement(31, "Novice of Round Mode", "Win 10 Round mode games.", "Gewinne 10 Roundmodespiele"));
	ach.add(new Achievement(32, "Journeyman of Round Mode", "Win 50 Round mode games.", "Gewinne 50 Roundmodespiele"));
	ach.add(new Achievement(33, "Master Of Round Mode", "Win 100 Round mode games.", "Gewinne 100 Roundmodespiele"));

	ach.add(new Achievement(34,
				"Easy Come Easy Go",
				"Lose a gained life within 10 seconds.",
				"Verliere innerhalb von 10 Sekunden ein gerade erlangtes Leben."));
	ach.add(new Achievement(35,
				"Comeback",
				"Win a game after a 3 life lead of your opponent.",
				"Gewinne ein Spiel, nachdem dein Gegner mit 3 Leben im Vorsprung war."));
	ach.add(new Achievement(36,
				"With The Last Breath",
				"Defeat your opponent with just one life remaining.",
				"Besiege den Gegner mit nur einem verbleibenden Leben."));
	ach.add(new Achievement(37, "Unbreakable", "Defeat your opponent without losing a life.", "Besiege den Gegner ohne ein Leben zu verlieren."));
	ach.add(new Achievement(38,
				"Profitable Gameplay",
				"Win a game with more lifes than you had at the beginning of the game.",
				"Gewinne ein Spiel mit mehr Leben als zu Beginn des Spiels."));
	ach.add(new Achievement(39, "Novice of Life Mode", "Win 10 Life mode games.", "Gewinne 10 Lifemodespiele"));
	ach.add(new Achievement(40, "Journeyman of Life Mode", "Win 50 Life mode games.", "Gewinne 50 Lifemodespiele"));
	ach.add(new Achievement(41, "Master Of Life Mode", "Win 100 Life mode games.", "Gewinne 100 Lifemodespiele"));

	ach.add(new Achievement(42, "Game Shot And The Match", "Win a game in the Regression mode.", "Gewinne ein Spiel im Regressionmode"));
	ach.add(new Achievement(43,
				"I Am Disappoint",
				"Win a game with your opponent still having more than half of the points left.",
				"Gewinne ein Spiel während dein Gegner noch über die Hälfte der Punkte übrig hat."));
	ach.add(new Achievement(44,
				"Photo Finish",
				"Win a game with your oppontent having only 1000 points left.",
				"Gewinne ein Spiel während dein Gegner nur noch 1000 Punkte übrig hat."));
	ach.add(new Achievement(45,
				"Late Starter",
				"Score the last quarter of the points as fast as the other three quarters.",
				"Erziele das letzte Viertel der Punkte ebenso schnell wie die anderen drei Viertel."));
	ach.add(new Achievement(46,
				"Retarded",
				"Recieve the pentalty points for losing the ball just 1000 points before the finish.",
				"Erhalte die Strafpunkte für einen Ballverlust nur 1000 Punkte vor dem Ziel."));
	ach.add(new Achievement(47, "Novice of Regression Mode", "Win 10 Regression mode games.", "Gewinne 10 Regressionmodespiele"));
	ach.add(new Achievement(48, "Journeyman of Regression Mode", "Win 50 Regression mode games.", "Gewinne 50 Regressionmodespiele"));
	ach.add(new Achievement(49, "Master Of Regression Mode", "Win 100 Regression mode games.", "Gewinne 100 Regressionmodespiele"));
	System.out.println("Initialized Achievements");
    }

    public void unlockAchievement(int id) {
	ach.get(id).unlock();
    }

    public void addAchievements(Achievements a) {
	// for (int i = 0; i < achUnlocked.length; i++) {
	// if (a.getAchUnlocked()[i]) {
	// achUnlocked[i] = true;
	// }
	// }
	for (Achievement achievement : Achievements.getAchievementList()) {
	    if (ach.contains(achievement) && achievement.isUnlocked()) {
		int index = ach.indexOf(achievement);
		ach.get(index).unlock();
	    }
	}
    }

    public boolean isAchievementUnlocked(int id) {
	return ach.get(id).isUnlocked();
    }
}
