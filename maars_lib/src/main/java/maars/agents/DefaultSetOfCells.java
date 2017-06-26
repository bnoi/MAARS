package maars.agents;

import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main object of MAARS, you got information about each cell (ROI measurement,
 * analysis related result)
 *
 * @author Tong LI
 */
public class DefaultSetOfCells implements Iterable<Cell>, Iterator<Cell>, Serializable, SetOfCells {
   private int iteratorCount = 0;
   private ArrayList<Cell> cellArray;
   private CopyOnWriteArrayList<Integer> cellsWithAtLeast1Spot_ = new CopyOnWriteArrayList<>();
   private String position_;

   public DefaultSetOfCells(String positionNb) {
      position_ = positionNb;
   }

   /**
    * @param pathToZip path to segmentation directory
    */
   public void loadCells(String pathToZip) {
      Roi[] roiArray = getRoisAsArray(pathToZip);
      cellArray = new ArrayList<>();
      for (int i = 1; i <= roiArray.length; i++) {
         cellArray.add(i - 1, new Cell(roiArray[i - 1], i));
      }
   }

   /**
    * Method to open ROI file and get them as ROI array
    *
    * @return array of ROI
    */
   private Roi[] getRoisAsArray(String pathToRois) {
      RoiManager roiManager = RoiManager.getInstance();
      if (roiManager == null) {
         roiManager = new RoiManager();
      }
      if (roiManager.getCount() == 0) {
         roiManager.runCommand("Open", pathToRois);
      }
      Roi[] rois = roiManager.getRoisAsArray();
      roiManager.reset();
      roiManager.close();
      return rois;
   }

   /**
    * Method to get Cell corresponding to index
    *
    * @param index cell number
    * @return Cell corresponding to index
    */
   public Cell getCell(int index) {
      return this.cellArray.get(index - 1);
   }

   /**
    * total number of cell
    *
    * @return the size of cell array
    */
   public int size() {
      return cellArray.size();
   }

   /**
    * @param rt rt from particle analyzer
    */
   public void addRoiMeasurementIntoCells(ResultsTable rt) {
      for (Cell c : cellArray) {
         c.setRoiMeasurement(rt.getRowAsString(c.getCellNumber() - 1));
      }
   }

   @Override
   public String getPosLabel() {
      return position_;
   }

   public void reset() {
      iteratorCount = 0;
      cellArray = null;
      cellArray = new ArrayList<>();
      cellsWithAtLeast1Spot_ = null;
      cellsWithAtLeast1Spot_ = new CopyOnWriteArrayList<>();
   }

   public void addPotentialMitosisCell(Integer cellNb) {
      if (!cellsWithAtLeast1Spot_.contains(cellNb)) {
         cellsWithAtLeast1Spot_.add(cellNb);
      }
   }

   public CopyOnWriteArrayList<Integer> getPotentialMitosisCell() {
      return cellsWithAtLeast1Spot_;
   }

   // iterator related
   @Override
   public Iterator<Cell> iterator() {
      resetCount();
      return this;
   }

   @Override
   public boolean hasNext() {
      return iteratorCount < cellArray.size();
   }

   @Override
   public Cell next() {
      if (iteratorCount >= cellArray.size())
         throw new NoSuchElementException();
      iteratorCount++;
      return cellArray.get(iteratorCount - 1);
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException();
   }

   private void resetCount() {
      this.iteratorCount = 0;
   }
}
