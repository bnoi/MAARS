package org.micromanager.cellstateanalysis;

import java.util.HashMap;

import ij.gui.Line;

public class SpindleContainer {
	HashMap<Integer, Line> container;
	public SpindleContainer(){
		
	}
	
	public void addSpLine(int frame, Line spLine){
		if (container == null){
			container = new HashMap<Integer, Line>();
		}
		container.put(frame, spLine);
	}
}
