package org.micromanager.cellstateanalysis.singleCellAnalysisFactory;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.util.FastMath;
import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.SpotSetAnalyzor;

import com.google.common.collect.Iterables;

import fiji.plugin.trackmate.Spot;
import ij.gui.Line;
import ij.measure.Calibration;

public class FindMerotely {

	public FindMerotely(Cell cell, Iterable<Spot> spotSet, HashMap<String, Object> geometry, Calibration fluoImgCal,
			ArrayList<Spot> poles, double radius, int frame) {
		// TODO to specify in gui that GFP for Kt and cfp for
		// spbs for exemple
		int setSize = Iterables.size(spotSet);
		if (setSize > 2) {
			Line spLine = new Line(
					(int) FastMath.round(poles.get(0).getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
					(int) FastMath.round(poles.get(0).getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight),
					(int) FastMath.round(poles.get(1).getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
					(int) FastMath.round(poles.get(1).getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight));
			Line.setWidth(2 * (int) FastMath.round(radius / fluoImgCal.pixelWidth));
			for (Spot s : spotSet) {
				if (!s.equals(poles.get(0)) && !s.equals(poles.get(1))) {
					if (spLine.contains((int) FastMath.round(s.getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
							(int) FastMath.round(s.getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight))) {
						// merotely
						cell.addSpotInBtwnFrame(frame);
					}
				}
			}
		}
	}
}
