package maars.segmentPombe;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;

import java.util.ArrayList;

/**
 * Created by tongli on 27/04/2017.
 */
class CellFilterFacotory {
   private ResultsTable rt_;
   private ImagePlus targetImg_;
   private RoiManager rm_ = RoiManager.getInstance();

   CellFilterFacotory(ResultsTable rt, ImagePlus targetImg) {
      rt_ = rt;
      targetImg_ = targetImg;
   }

   private void resFilter(int parameter, double min, double max) {
      ArrayList<Integer> rowTodelete = new ArrayList<>();
      rt_.reset();
      Roi[] roiArray = rm_.getRoisAsArray();
      rm_.runCommand("Select All");
      rm_.runCommand("Delete");
      Analyzer analyzer = new Analyzer(targetImg_,
            Measurements.AREA + Measurements.STD_DEV + Measurements.MIN_MAX +
                  Measurements.SHAPE_DESCRIPTORS + Measurements.MEAN + Measurements.CENTROID +
                  Measurements.PERIMETER + Measurements.ELLIPSE + Measurements.KURTOSIS + Measurements.SKEWNESS, rt_);
      System.out.println("- analyze each roi and add it to manager if it is wanted");
      for (Roi roi : roiArray) {
         roi.setImage(targetImg_);
         targetImg_.setRoi(roi);
         analyzer.measure();
      }
      targetImg_.deleteRoi();

      System.out.println("- delete from result table roi unwanted");
      int name = 1;
      for (int i = 0; i < rt_.getColumn(parameter).length; i++) {
         double value = rt_.getValueAsDouble(parameter, i);
         if (value <= max && value >= min) {
            roiArray[i].setName("" + name);
            rm_.addRoi(roiArray[i]);
            name++;
         } else {
            rowTodelete.add(i);
         }
      }
      deleteRowOfResultTable(rt_, rowTodelete);
      System.out.println("Filter done.");
   }

   void filterAll(ArrayList<CellFilter> filters) {
      for (CellFilter filter : filters) {
         resFilter(filter.parameter_, filter.min_, filter.max_);
      }
   }

   private void deleteRowOfResultTable(ResultsTable rt, ArrayList<Integer> rowToDelete) {
      for (int i = 0; i < rowToDelete.size(); i++) {
         int row = rowToDelete.get(i) - i;
         rt.deleteRow(row);
      }
   }
}
