package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Arrays;

public class OffsetCounter {
	//TODO THA remove of not needed
	
	static int offsetToSkip = 0;
	static int[] offsetsPerLine;
	
	public static int getOffsetToSkip() {
		return offsetToSkip;
	}
	
	public static int setOffsetToSkip(int newValue) {
		return offsetToSkip += newValue;
	}
	public static void clearOffset() {
		offsetToSkip = 0;
	}
	public static void initializeOffsetArray(int size) {
		offsetsPerLine = new int[size];
		Arrays.fill(offsetsPerLine, 0);
	}
	public static int[] getOffsetArray() {
		return offsetsPerLine;
	}
	
	public static void addOffsetForLine(int line, int offset) {
		offsetsPerLine[line] += offset;
	}
	
	public int getDiffBeforeLine(int line) {
		int diff = 0;
		for(int i = 0; i<offsetsPerLine.length; i++) {
			if(i<= line) {
				diff += offsetsPerLine[i];
			}else{
				return diff;
			}
		}
		
		return diff;
	}

}
