package de.tong.block;

import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the byte-arrays which make up the block. The blockMap
 * list contains every single rotation step.
 * 
 * @author Wolfgang Mueller
 * 
 */
public class BlockMap {

    private List<byte[][]> blockMap = new ArrayList<byte[][]>();

    public BlockMap(List<byte[][]> list) {
	this.blockMap = list;

    }

    public BlockMap() {

    }

    public byte[][] getByteListAt(int i) {
	return blockMap.get(i);
    }

    public void add(byte[][] t) {
	blockMap.add(t);

    }

    public int getSize() {
	return blockMap.size();
    }
}
