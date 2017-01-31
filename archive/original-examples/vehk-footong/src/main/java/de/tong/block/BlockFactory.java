package de.tong.block;

import java.util.Random;

import de.tong.field.StatusField;
import de.tong.field.TetrisField;
import de.tong.graphics.Block;

/**
 * This is a utility-class which creates Blocks of a specific {@link BlockType}.
 * 
 * 
 * @author Wolfgang Mueller
 * 
 */
public class BlockFactory {

    private static final BlockMap T_BLOCK = new BlockMap();
    private static final BlockMap I_BLOCK = new BlockMap();
    private static final BlockMap J_BLOCK = new BlockMap();
    private static final BlockMap O_BLOCK = new BlockMap();
    private static final BlockMap L_BLOCK = new BlockMap();
    private static final BlockMap S_BLOCK = new BlockMap();
    private static final BlockMap Z_BLOCK = new BlockMap();
    private static boolean init = false;
    private static Random rand = new Random();

    /**
     * This method creates a block of the given {@link BlockType}
     * 
     * @param t
     *            the {@link BlockType}
     * @return an object of the new Block
     */
    public static Block createBlock(BlockType t, TetrisField field) {
	if (!init) {
	    initialize();
	}

	switch (t) {
	    case T_BLOCK:
		return new Block(T_BLOCK, BlockColor.getColorFor(BlockType.T_BLOCK), t, field);
	    case I_BLOCK:
		return new Block(I_BLOCK, BlockColor.getColorFor(BlockType.I_BLOCK), t, field);
	    case J_BLOCK:
		return new Block(J_BLOCK, BlockColor.getColorFor(BlockType.J_BLOCK), t, field);
	    case L_BLOCK:
		return new Block(L_BLOCK, BlockColor.getColorFor(BlockType.L_BLOCK), t, field);
	    case O_BLOCK:
		return new Block(O_BLOCK, BlockColor.getColorFor(BlockType.O_BLOCK), t, field);
	    case S_BLOCK:
		return new Block(S_BLOCK, BlockColor.getColorFor(BlockType.S_BLOCK), t, field);
	    case Z_BLOCK:
		return new Block(Z_BLOCK, BlockColor.getColorFor(BlockType.Z_BLOCK), t, field);
	    default:
		return null;
	}

    }

    public static Block createPreviewBlock(BlockType t, StatusField field, int xOffset, int yOffset) {
	if (!init) {
	    initialize();
	}

	switch (t) {
	    case T_BLOCK:
		return new Block(T_BLOCK, BlockColor.getColorFor(BlockType.T_BLOCK), t, field, xOffset, yOffset);
	    case I_BLOCK:
		return new Block(I_BLOCK, BlockColor.getColorFor(BlockType.I_BLOCK), t, field, xOffset, yOffset);
	    case J_BLOCK:
		return new Block(J_BLOCK, BlockColor.getColorFor(BlockType.J_BLOCK), t, field, xOffset, yOffset);
	    case L_BLOCK:
		return new Block(L_BLOCK, BlockColor.getColorFor(BlockType.L_BLOCK), t, field, xOffset, yOffset);
	    case O_BLOCK:
		return new Block(O_BLOCK, BlockColor.getColorFor(BlockType.O_BLOCK), t, field, xOffset, yOffset);
	    case S_BLOCK:
		return new Block(S_BLOCK, BlockColor.getColorFor(BlockType.S_BLOCK), t, field, xOffset, yOffset);
	    case Z_BLOCK:
		return new Block(Z_BLOCK, BlockColor.getColorFor(BlockType.Z_BLOCK), t, field, xOffset, yOffset);
	    default:
		return null;
	}

    }

    public static Block createRandomBlock(TetrisField field) {
	return createBlock(getRandomBlockType(), field);
    }

    public static BlockType getRandomBlockType() {
	return BlockType.values()[rand.nextInt(BlockType.values().length)];
    }

    /**
     * Initializes every {@link BlockMap} in the game.
     * 
     */
    private static void initialize() {
	init = true;

	// These arrays are VERY confusing, so please DON'T MAKE ANY CHANGES
	// (unless you REALLY know what you're doing).
	// [Don't even look here. Just look away. Or do some work.]
	// @formatter:off

	byte[][][] tb = { { { 0, 0, 0 }, { 1, 1, 1 }, { 0, 1, 0 } },
	                  { { 0, 1, 0 }, { 1, 1, 0 }, { 0, 1, 0 } },
			  { { 0, 1, 0 }, { 1, 1, 1,}, { 0, 0, 0 } },
			  { { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 0 } } };

	

	byte[][][] ib = { { { 0, 1, 0, 0 },
	    	            { 0, 1, 0, 0 }, 
	    	            { 0, 1, 0, 0 },
	    	            { 0, 1, 0, 0 } },
			  { { 1, 1, 1, 1 } } };

	byte[][][] jb = { { {0, 1, 0}, {0, 1, 0} , {1, 1, 0} },
	                  { {1, 0, 0}, {1, 1, 1} , {0, 0, 0} },
	                  { {0, 1, 1}, {0, 1, 0} , {0, 1, 0} },
	                  { {0, 0, 0}, {1, 1, 1} , {0, 0, 1} } };

	byte[][][] lb = { { { 0, 1, 0 }, { 0, 1, 0}, { 0, 1, 1} },
			  { { 0, 0, 0}, { 1, 1, 1}, { 1, 0, 0} },
			  { { 1, 1, 0}, { 0, 1, 0}, { 0, 1, 0} },
			  { { 0, 0, 1}, { 1, 1, 1}, { 0, 0, 0} } };

	byte[][][] ob = { { {  1, 1 }, {  1, 1  } } };

	byte[][][] sb = { { {0, 1, 1}, {1, 1, 0} , {0, 0, 0}  },
	                  { {1, 0, 0}, {1, 1, 0} , {0, 1, 0} }};

	byte[][][] zb = { { {1, 1, 0}, {0, 1, 1} , {0, 0, 0}  },
	                  { {0, 1, 0}, {1, 1, 0} , {1, 0, 0} }};

	// @formatter:on
	for (int i = 0; i < tb.length; i++) {
	    T_BLOCK.add(tb[i]);
	}

	for (int i = 0; i < ib.length; i++) {
	    I_BLOCK.add(ib[i]);
	}

	for (int i = 0; i < jb.length; i++) {
	    J_BLOCK.add(jb[i]);
	}

	for (int i = 0; i < lb.length; i++) {
	    L_BLOCK.add(lb[i]);
	}

	O_BLOCK.add(ob[0]);

	for (int i = 0; i < sb.length; i++) {
	    S_BLOCK.add(sb[i]);
	}

	for (int i = 0; i < zb.length; i++) {
	    Z_BLOCK.add(zb[i]);
	}

    }
}
