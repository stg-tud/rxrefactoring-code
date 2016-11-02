package de.tong.field;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import de.tong.block.BlockFactory;
import de.tong.block.BlockType;
import de.tong.graphics.Block;
import de.tong.gui.screen.GameScreen;
import de.tong.player.Player;
import de.tong.util.FontManager;

public class StatusField extends Field {

    private Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f);
    private BlockType preview;
    private Block previewBlock;
    private int xOffset = 0;
    private int yOffset = 19;

    public StatusField(double x, double y, double width, double height, boolean border, boolean visible, Player owner, GameScreen parent) {
	super(x, y, 25 * width, 25 * height, border, visible, owner, parent);

    }

    @Override
    public void draw(Graphics2D g) {
	super.draw(g);

	previewBlock.draw(g);
	g.setColor(Color.WHITE);
	g.setFont(FontManager.getFont("pointsFont"));

	switch (owner.getOptions().getCurrentMode()) {

	    case SINGLEPLAYER:
		g.drawString(Integer.toString(owner.getPoints()), (int) (x + 20), 80);
		break;

	    case LIFEMODE:
		g.drawString(Integer.toString(owner.getLifes()), (int) (x + 20), 80);
		break;

	    case REGRESSIONMODE:
		g.drawString(Integer.toString(owner.getPointsLeft()), (int) (x + 20), 80);
		break;

	    case ROUNDMODE:
		g.drawString(Integer.toString(owner.getRoundsWon()), (int) (x + 20), 80);
		break;

	    default:
		g.drawString(Integer.toString(owner.getPoints()), (int) (x + 20), 80);
		break;
	}

	g.setFont(FontManager.getFont("headerFont"));
	g.drawString(owner.getName() + "", (int) (x + 10), 40);
	g.drawString("next block", (int) (x + 10), 19 * 25);

    }

    @Override
    public void drawBorder(Graphics2D g) {

	g.setColor(Color.CYAN);
	g.setStroke(stroke);
	g.draw(this);

    }

    public void setPreviewBlockType(BlockType preview) {
	this.preview = preview;
	previewBlock = BlockFactory.createPreviewBlock(preview, this, xOffset, yOffset);

    }
}
