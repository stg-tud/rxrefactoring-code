package de.tong.graphics.buttons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.tong.graphics.Drawable;
import de.tong.util.FontManager;

public class MenuButton extends Rectangle2D.Double implements Drawable {

    private ButtonState state = ButtonState.DEFAULT;
    private String text;
    private static FontMetrics textMetrics;
    private List<ActionListener> listeners = new ArrayList<ActionListener>();
    private BasicStroke stroke = new BasicStroke(4.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f);

    public MenuButton(double x, double y, double w, double h, String text) {
	super(x, y, w, h);
	this.text = text;
    }

    public void setState(ButtonState state) {
	this.state = state;

    }

    @Override
    public void draw(Graphics2D g) {
	switch (state) {

	    case DEFAULT:
		g.setColor(new Color(110, 130, 150));
		g.setPaint(new GradientPaint((float) x, (float) y, new Color(0, 255, 0, 170), (float) (x + 350), (float) y, new Color(0, 0, 0, 0)));
		break;
	    case HOVER:
		g.setColor(Color.ORANGE);
		g.setPaint(new GradientPaint((float) x, (float) y, new Color(255, 180, 0, 255), (float) (x + 400), (float) y, new Color(0, 0, 0, 0)));
		break;
	    case CLICKED:
		g.setColor(new Color(255, 220, 70));
		break;
	    default:
		g.setColor(Color.BLACK);

	}
	g.setStroke(stroke);

	// g.fill(new Rectangle2D.Double(x, y, 20, height));
	g.draw(this);

	textMetrics = g.getFontMetrics(FontManager.getFont("buttonFont"));
	// preserve font, reset later.
	Font f = g.getFont();
	g.setFont(FontManager.getFont("buttonFont"));

	double centeredX = x + ((this.getWidth() / 2) - (textMetrics.stringWidth(text) / 2));
	double centeredY = y - 4 + (this.getHeight() / 2) + textMetrics.getHeight() / 2.5;
	g.setColor(Color.WHITE);
	g.drawString(text, (int) centeredX, (int) centeredY);
	// reset font
	g.setFont(f);

    }

    public void addActionListener(ActionListener l) {
	listeners.add(l);
    }

    public void clicked() {
	for (ActionListener l : listeners) {
	    l.actionPerformed(new ActionEvent(this, 0, "clicked"));
	}
    }

    public void updateState(ButtonState b) {
	setState(b);
    }

}
