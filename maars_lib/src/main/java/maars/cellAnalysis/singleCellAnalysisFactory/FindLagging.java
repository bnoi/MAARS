package maars.cellAnalysis.singleCellAnalysisFactory;

import fiji.plugin.trackmate.Spot;
import ij.gui.Line;
import ij.measure.Calibration;
import maars.agents.Cell;
import maars.cellAnalysis.SpotSetAnalyzor;
import maars.utils.CollectionUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;

public class FindLagging {

   /**
    * lable cells with lagging or aligned Kts in SOC object
    *
    * @param cell       Cell object
    * @param spotSet    spotset in current cell
    * @param fluoImgCal calibration of fluo img
    * @param poles      the SPB (spots)
    * @param radius     raidus of spot
    * @param frame      current frame number
    */
   public FindLagging(Cell cell, Iterable<Spot> spotSet, Calibration fluoImgCal,
                      ArrayList<Spot> poles, double radius, int frame) {
      // spbs for exemple
      double discardLaggingSpotRange = 1;
      int setSize = CollectionUtils.size(spotSet);
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
                     // potential lagging, this lagging will be confirmed only if it's later than anaB onset
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
