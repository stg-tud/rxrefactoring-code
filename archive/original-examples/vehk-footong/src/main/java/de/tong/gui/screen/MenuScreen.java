package de.tong.gui.screen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import de.tong.controls.GameEvent;
import de.tong.graphics.buttons.ButtonFactory;
import de.tong.graphics.buttons.ButtonState;
import de.tong.graphics.buttons.MenuButton;
import de.tong.gui.GamePanel;
import de.tong.player.GameMode;

public class MenuScreen extends Screen {

    private Menu currentMenu;

    private Menu mainMenu;
    private Menu modeMenu;

    public MenuScreen(GamePanel parent) {
	super(parent);
	initializeMenus();
	parent.setBackground(Color.BLACK);
	setMenu(mainMenu);
    }

    private void initializeMenus() {
	// Buttons for mainMenu
	MenuButton newGame = ButtonFactory.createButton(this, "NEW GAME [SINGLE]");

	newGame.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {

		parent.getPl1().getOptions().setCurrentMode(GameMode.SINGLEPLAYER);
		parent.setScreen(new SingleplayerScreen(parent, parent.pl1));
		GameEvent.unlockAchievement(0, parent.pl1);
		parent.queueGameStart();

	    }
	});

	MenuButton newGameM = ButtonFactory.createButton(newGame, "NEW GAME [MULTI]");

	newGameM.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {

		setMenu(modeMenu);

	    }
	});

	MenuButton profile = ButtonFactory.createButton(newGameM, "PROFILE");
	MenuButton options = ButtonFactory.createButton(profile, "OPTIONS");
	MenuButton exit = ButtonFactory.createButton(options, "EXIT");
	exit.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {

		System.out.println("Exiting...");
		System.exit(0);
	    }
	});

	mainMenu = new Menu(this, newGame, newGameM, profile, options, exit);

	MenuButton lifeMode = ButtonFactory.createButton(this, "LIFE MODE");
	lifeMode.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		parent.pl1.getOptions().setCurrentMode(GameMode.LIFEMODE);
		parent.pl2.getOptions().setCurrentMode(GameMode.LIFEMODE);
		parent.setScreen(new MultiplayerScreen(parent, parent.pl1, parent.pl2));
		GameEvent.unlockAchievement(1, parent.pl1);
		GameEvent.unlockAchievement(1, parent.pl2);
		parent.queueGameStart();

	    }
	});
	MenuButton roundMode = ButtonFactory.createButton(lifeMode, "ROUND MODE");
	roundMode.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		parent.pl1.getOptions().setCurrentMode(GameMode.ROUNDMODE);
		parent.pl2.getOptions().setCurrentMode(GameMode.ROUNDMODE);
		parent.setScreen(new MultiplayerScreen(parent, parent.pl1, parent.pl2));
		GameEvent.unlockAchievement(1, parent.pl1);
		GameEvent.unlockAchievement(1, parent.pl2);
		parent.queueGameStart();

	    }
	});
	MenuButton regressionMode = ButtonFactory.createButton(roundMode, "REGRESSION MODE");
	regressionMode.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		parent.pl1.getOptions().setCurrentMode(GameMode.REGRESSIONMODE);
		parent.pl2.getOptions().setCurrentMode(GameMode.REGRESSIONMODE);
		parent.setScreen(new MultiplayerScreen(parent, parent.pl1, parent.pl2));
		GameEvent.unlockAchievement(1, parent.pl1);
		GameEvent.unlockAchievement(1, parent.pl2);
		parent.queueGameStart();

	    }
	});

	modeMenu = new Menu(this, mainMenu, lifeMode, roundMode, regressionMode);

    }

    public void setMenu(Menu newMenu) {
	currentMenu = newMenu;

    }

    public GamePanel getGamePanel() {
	return parent;
    }

    public int getWidth() {
	return parent.getPreferredSize().width;
    }

    public int getHeight() {
	return parent.getPreferredSize().height;
    }

    @Override
    public void draw(Graphics2D g) {
	currentMenu.draw(g);
    }

    @Override
    public KeyListener getKeyHandler() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void doLogic(double delta) {
	// TODO Auto-generated method stub

    }

    @Override
    public void destroyScreen() {
	// TODO Auto-generated method stub

    }

    @Override
    public void clearScreen() {
	// TODO Auto-generated method stub

    }

    public MouseListener getMouseListener() {
	return new MouseListener() {

	    @Override
	    public void mouseReleased(MouseEvent e) {
		for (MenuButton b : currentMenu.getButtons()) {
		    if (b.contains(e.getX(), e.getY())) {
			b.updateState(ButtonState.HOVER);
		    } else {
			b.updateState(ButtonState.DEFAULT);
		    }
		}

	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
		for (MenuButton b : currentMenu.getButtons()) {
		    if (b.contains(e.getX(), e.getY())) {
			b.updateState(ButtonState.CLICKED);
			b.clicked();
		    } else {
			b.updateState(ButtonState.DEFAULT);
		    }
		}

	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }

    public MouseMotionListener getMouseMotionListener() {
	return new MouseMotionListener() {

	    @Override
	    public void mouseMoved(MouseEvent e) {
		for (MenuButton b : currentMenu.getButtons()) {
		    if (b.contains(e.getX(), e.getY())) {
			b.updateState(ButtonState.HOVER);
		    } else {
			b.updateState(ButtonState.DEFAULT);
		    }
		}

	    }

	    @Override
	    public void mouseDragged(MouseEvent e) {
		// +2line of emptiness
	    }
	};
    }
}
