package de.tong.graphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Timer;

import de.tong.gui.GamePanel;
import de.tong.util.FontManager;

public class TimerOverlay extends RoundRectangle2D.Double implements Drawable, ActionListener {

    private GamePanel parent;
    private boolean visible;
    private byte seconds = 3;
    private Timer t;
    private FontMetrics metrics;

    public TimerOverlay(GamePanel parent) {
	this.parent = parent;

	t = new Timer(1000, this);

    }

    public void startTimer() {
	t.start();
    }

    public void showOverlay() {
	visible = true;
    }

    public void hideOverlay() {
	visible = false;
    }

    @Override
    public void draw(Graphics2D g) {
	if (visible) {
	    metrics = g.getFontMetrics(FontManager.getFont("timerFont"));
	    g.setFont(FontManager.getFont("timerFont"));

	    String s = "Start in";
	    int len = metrics.stringWidth(s);

	    x = parent.getWidth() / 2 - (len + 20) / 2;
	    y = 240;
	    width = len + 20;
	    height = 120;

	    g.setColor(new Color(0, 0, 0));
	    g.fill(this);

	    g.setColor(Color.WHITE);
	    g.drawString(s, parent.getWidth() / 2 - len / 2, 300);

	    s = Byte.toString(seconds);
	    len = metrics.stringWidth(s);

	    g.drawString(s, parent.getWidth() / 2 - len / 2, 350);

	}

    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (seconds > 1) {
	    seconds--;
	} else {
	    t.stop();
	    seconds = 3;
	    hideOverlay();
	    System.out.println("Timer stopped, starting game");
	    parent.startGame();

	}

    }
}
