package de.tong.gui.screen;

import de.tong.block.BlockType;
import de.tong.field.TetrisField;
import de.tong.graphics.Ball;
import de.tong.graphics.Block;
import de.tong.gui.GamePanel;

public abstract class GameScreen extends Screen {

    public GameScreen(GamePanel parent) {
	super(parent);

    }

    public abstract void updateBlock(Block b, BlockType preview, TetrisField trigger);

    public abstract int getBlockSpeed();

    public abstract void newRound();

    public abstract Ball getBall();

}
