package de.tong.block;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to maintain the different block colors the game will use. These will
 * get more customizable in the future.
 * 
 * @author Wolfgang Mueller
 * 
 */
public class BlockColor {

    private static Map<BlockType, Color> colorMap = new HashMap<BlockType, Color>();
    private static boolean init = false;

    /**
     * 
     * Returns the {@link Color} for a specific {@link BlockType}. This method
     * will also initialize the HashMap if the 'init-state' is false;
     * 
     * @param t
     *            the {@link BlockType}
     * @return The corresponding {@link Color}
     */
    public static Color getColorFor(BlockType t) {
	if (!init) {
	    initialize();
	}
	return colorMap.get(t);
    }

    /**
     * Initializing every {@link Color} and adding these to the {@link HashMap}
     * This method will also set the 'init-state' to true.
     * 
     * These values were taken from
     * http://en.wikipedia.org/wiki/Tetris#Colors_of_tetrominoes
     * 
     * epic color : new Color(30, 50, 90)
     * 
     */
    public static void initialize() {
	init = true;
	colorMap.put(BlockType.I_BLOCK, Color.CYAN);
	colorMap.put(BlockType.J_BLOCK, Color.BLUE);
	colorMap.put(BlockType.L_BLOCK, Color.ORANGE);
	colorMap.put(BlockType.O_BLOCK, Color.YELLOW);
	colorMap.put(BlockType.S_BLOCK, Color.GREEN);
	colorMap.put(BlockType.T_BLOCK, new Color(128, 0, 128));
	colorMap.put(BlockType.Z_BLOCK, Color.RED);
    }
}
