package edu.univ_tlse3.cellstateanalysis;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import edu.univ_tlse3.cellstateanalysis.singleCellAnalysisFactory.FindLagging;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.utils.ImgUtils;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAnalyzer implements Runnable {

   private ImagePlus fluoImage;
   private SetOfCells soc;
   private Calibration bfImgCal;
   private Calibration fluoImgCal;
   private String channel;
   private int maxNbSpot;
   private double radius;
   private double quality;
   private int frame;
   private SpotCollection collection;
   private Model model;
   private SOCVisualizer socVisualizer_;
   private Boolean useDynamic_;
   private ExecutorService es_;

   /**
    * @param fluoImage     image to analyze zProjectedFluoImg
    * @param bfImgCal      bright field image calibration, need it to decide whether or
    *                      not rescale ROI
    * @param soc           the set of cell to analyze
    * @param channel       fluo image channel
    * @param maxNbSpot     max number of spot in corresponding channel
    * @param radius        radius of spot in corresponding channel
    * @param frame         time point
    * @param quality       user predefined quality threshold for spot selection
    * @param socVisualizer a JFreeChart based display to show cell params
    * @param useDynamic    perform dynamic analysis
    * @param es            ExecutorService
    */
   public FluoAnalyzer(ImagePlus fluoImage, Calibration bfImgCal, SetOfCells soc, String channel, int maxNbSpot,
                       double radius, double quality, int frame, SOCVisualizer socVisualizer, Boolean useDynamic,
                       ExecutorService es) {
      this.fluoImage = fluoImage;
      this.fluoImgCal = fluoImage.getCalibration();
      this.soc = soc;
      this.bfImgCal = bfImgCal;
      this.channel = channel;
      this.maxNbSpot = maxNbSpot;
      this.radius = radius;
      this.quality = quality;
      this.frame = frame;
      socVisualizer_ = socVisualizer;
      useDynamic_ = useDynamic;
      es_ = es;
   }

   /**
    * the main, use one new thread just in order to free acquisition thread to
    * acquire images as soon as possible
    */
   @Override
   public void run() {
      if (fluoImage.getCalibration().getUnit().equals("cm")) {
         fluoImage = ImgUtils.unitCmToMicron(fluoImage);
      }
      // TODO projection or not
      ImagePlus zProjectedFluoImg;
      if (fluoImage.getImageStackSize() == 1) {
         zProjectedFluoImg = fluoImage;
      } else {
         zProjectedFluoImg = ImgUtils.zProject(fluoImage);
         zProjectedFluoImg.setTitle(fluoImage.getTitle() + "_" + channel + "_projected");
         zProjectedFluoImg.setCalibration(fluoImage.getCalibration());
      }
      MaarsTrackmate trackmate = new MaarsTrackmate(zProjectedFluoImg, radius, quality);
      this.model = trackmate.doDetection(true);
      int nbCell = soc.size();
      collection = SpotsContainer.getNBestqualitySpots(model.getSpots(), nbCell, maxNbSpot);
      double[] factors = ImgUtils.getRescaleFactor(bfImgCal, fluoImgCal);

      int nThread = Runtime.getRuntime().availableProcessors();
      final int[] nbOfCellEachThread = new int[2];
      nbOfCellEachThread[0] = nbCell / nThread;
      nbOfCellEachThread[1] = nbOfCellEachThread[0] + nbCell % nThread;
      List<Future> jobs = new ArrayList<>();
      for (int i = 0; i < nThread; i++) {
         // analyze every subset of cell
         jobs.add(es_.submit(new AnalyseBlockCells(i, nbOfCellEachThread, factors)));
      }
      for (Future f : jobs) {
         try {
            f.get();
         } catch (InterruptedException | ExecutionException e) {
            IJ.error(e.toString());
         }
      }
      if (useDynamic_){
         socVisualizer_.updateCellsDisplay(soc);
      }
   }

   //private class for analysing cells
   //--------------------------------------------------------------------------------------//

   /**
    * analyzer of subset
    */
   private class AnalyseBlockCells implements Runnable {
      private final int index;
      private final int[] deltas;
      private double[] factors;

      AnalyseBlockCells(int index, final int[] deltas, double[] factors) {
         this.index = index;
         this.deltas = deltas;
         this.factors = factors;
      }

      @Override
      public void run() {
         // distribute number of cells for each thread
         int begin = 0;
         int end;
         if (index == 0) {
            if (deltas[0] != deltas[1]) {
               end = deltas[1];
            } else {
               end = deltas[0];
            }
         } else {
            begin = index * deltas[0] + (deltas[1] - deltas[0]);
            end = begin + deltas[0];
         }
         // need to be false because all spots are not visible
         ArrayList<Spot> currentThreadSpots = Lists.newArrayList(collection.iterable(false));
         for (int j = begin + 1; j <= end; j++) {
            Cell cell = soc.getCell(j);
            cell.addChannel(channel);
            cell.setTrackmateModel(model);
            Roi tmpRoi;
            if (factors[0] != 1 || factors[1] != 1) {
               tmpRoi = cell.rescaleCellShapeRoi(factors);
            } else {
               tmpRoi = cell.getCellShapeRoi();
            }
            double calibratedXBase = tmpRoi.getXBase() * fluoImgCal.pixelWidth;
            double calibratedYBase = tmpRoi.getYBase() * fluoImgCal.pixelHeight;
            ArrayList<Spot> spotsToDel = new ArrayList<>();
            for (Spot s : currentThreadSpots) {
               if (tmpRoi.contains((int) Math.round(s.getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
                       (int) Math.round(s.getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight))) {
                  cell.putSpot(channel, frame, s);
                  spotsToDel.add(s);
                  if (cell.getNbOfSpots(channel, frame) > maxNbSpot) {
                     Spot lowesetQulitySpot = SpotsContainer
                             .findLowestQualitySpot(cell.getSpotsInFrame(channel, frame));
                     cell.removeSpot(channel, frame, lowesetQulitySpot);
                  }
               }
            }
            // remove spots found in current cell in order to accelerate
            // iteration
            for (Spot s2del : spotsToDel) {
               currentThreadSpots.remove(s2del);
            }
            SpotSetAnalyzor spotSetAnalyzor = new SpotSetAnalyzor(cell.get(Cell.X_CENTROID) * fluoImgCal.pixelWidth,
                    cell.get(Cell.Y_CENTROID) * fluoImgCal.pixelHeight,
                    cell.get(Cell.MAJOR) * fluoImgCal.pixelWidth, cell.get(Cell.ANGLE), calibratedXBase,
                    calibratedYBase);

            Iterable<Spot> spotSet = cell.getSpotsInFrame(channel, frame);
            if (spotSet != null) {
               HashMap<String, Object> geometry = new HashMap<>();
               spotSetAnalyzor.compute(geometry, spotSet);
               ArrayList<Spot> poles = spotSetAnalyzor.getPoles();
               new FindLagging(cell, spotSet, fluoImgCal, poles, radius, frame);
               cell.putGeometry(channel, frame, geometry);
               if ((Integer)geometry.get(SpotSetAnalyzor.NbOfSpotDetected) > 1){
                  soc.addPotentialMitosisCell(cell.getCellNumber());
               }
            }
         }
      }
   }
}
