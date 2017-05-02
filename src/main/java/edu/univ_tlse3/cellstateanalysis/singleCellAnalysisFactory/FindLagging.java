package edu.univ_tlse3.cellstateanalysis.singleCellAnalysisFactory;

import com.google.common.collect.Iterables;
import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.SpotSetAnalyzor;
import fiji.plugin.trackmate.Spot;
import ij.gui.Line;
import ij.measure.Calibration;
import org.apache.commons.math4.util.FastMath;

import java.util.ArrayList;

public class FindLagging {

    public FindLagging(Cell cell, Iterable<Spot> spotSet, Calibration fluoImgCal,
                       ArrayList<Spot> poles, double radius, int frame) {
        // spbs for exemple
        double discardLaggingSpotRange = 1;
        int setSize = Iterables.size(spotSet);
        if (setSize > 2) {
            Line spLine = new Line(
                    (int) FastMath.round(poles.get(0).getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
                    (int) FastMath.round(poles.get(0).getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight),
                    (int) FastMath.round(poles.get(1).getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
                    (int) FastMath.round(poles.get(1).getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight));
            double spotDiameter = 2 * radius / fluoImgCal.pixelWidth;
            Line.setWidth((int) FastMath.round(spotDiameter));
            for (Spot s : spotSet) {
                if (!s.equals(poles.get(0)) && !s.equals(poles.get(1))) {
                    if (spLine.contains((int) FastMath.round(s.getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
                            (int) FastMath.round(s.getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight))) {
                        if (SpotSetAnalyzor.distance(s, poles.get(0)) > discardLaggingSpotRange * spotDiameter || SpotSetAnalyzor.distance(s, poles.get(0)) > discardLaggingSpotRange * spotDiameter) {
                            // lagging
                            cell.addSpotInBtwnFrame(frame);
                        }
                    } else {
                        //not on the line between the poles (maybe unattached)
                        cell.addFrameWithUnalignedSpot(frame);
                    }
                }
            }
        }
    }
}
