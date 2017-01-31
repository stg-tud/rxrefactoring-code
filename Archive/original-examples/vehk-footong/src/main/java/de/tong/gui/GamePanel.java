package de.tong.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.tong.controls.GameEvent;
import de.tong.graphics.TimerOverlay;
import de.tong.gui.screen.MenuScreen;
import de.tong.gui.screen.Screen;
import de.tong.player.Achievements;
import de.tong.player.Player;
import de.tong.player.Profile;

/**
 * 
 * The panel on which the whole game will be drawn.
 * 
 * @author Wolfgang Mueller
 * 
 */
public class GamePanel extends JPanel implements Runnable {

    // The main thread for redrawing all the components
    public Player pl1;
    public Player pl2;
    private Thread t;

    private double delta;
    private long last = System.nanoTime();
    private int fps;
    private JFrame parent;
    private boolean gameRunning;

    private TimerOverlay over = new TimerOverlay(this);

    private Screen currentScreen;

    public GamePanel(JFrame parent) {

	// set window size
	this.setPreferredSize(new Dimension(860, 650));
	this.parent = parent;

	GameEvent.setPanel(this);
	t = new Thread(this);

	pl1 = new Player("Player 1");
	Profile.createProfile(pl1.getName());
	pl2 = new Player("Player 2");
	Profile.createProfile(pl2.getName());

	Achievements.initialize();

	setBackground(Color.BLACK);

	setScreen(new MenuScreen(this));
	// setScreen(new SingleplayerScreen(this, pl1));
	// queueGameStart();
	// setScreen(new MenuScreen(this));

	t.start();

    }

    public Player getPl1() {
	return pl1;
    }

    public Player getPl2() {
	return pl2;
    }

    public void queueGameStart() {
	// stop game if it's already running
	gameRunning = false;
	over.startTimer();
	over.showOverlay();

    }

    public void startGame() {
	gameRunning = true;
    }

    public void endGame() {
	gameRunning = false;
    }

    public void setScreen(Screen newScreen) {
	if (currentScreen != null) {
	    currentScreen.destroyScreen();
	}
	this.currentScreen = newScreen;
	if (parent.getKeyListeners().length > 0) {
	    KeyListener former = parent.getKeyListeners()[0];
	    System.out.println("Removing former listener");
	    parent.removeKeyListener(former);
	}
	if (this.getMouseListeners().length > 0) {
	    this.removeMouseListener(this.getMouseListeners()[0]);
	    this.removeMouseMotionListener(this.getMouseMotionListeners()[0]);
	    System.out.println("Removing Mouse(Motion)Listener");
	}
	System.out.println("Screen was changed to '" + newScreen.getClass().getSimpleName() + "' ");
	parent.addKeyListener(currentScreen.getKeyHandler());
	if (newScreen instanceof MenuScreen) {
	    System.out.println("MenuScreen detected, adding MouseListener");
	    this.addMouseListener(((MenuScreen) newScreen).getMouseListener());
	    this.addMouseMotionListener(((MenuScreen) newScreen).getMouseMotionListener());
	}
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);

	// casting g to Graphics2D -> more functions (e.g 'draw(Shape s)' )
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	g.setColor(Color.WHITE);
	g2.drawString(String.valueOf(fps), 2, 10);
	currentScreen.draw(g2);

	over.draw(g2);

    }

    private void computeDelta() {
	delta = System.nanoTime() - last;
	last = System.nanoTime();
	fps = (int) (1e9 / delta);
    }

    @Override
    public void run() {

	//
	while (true) {

	    try {
		repaint();
		// update game objects every 10 milliseconds
		computeDelta();

		if (gameRunning) {
		    currentScreen.doLogic(delta);

		}

		// Put the thread to sleep for 10 milliseconds
		Thread.sleep(10);

	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }

	}

    }

    public Screen getCurrentScreen() {
	return currentScreen;
    }

    public boolean isGameRunning() {
	return gameRunning;
    }

}
