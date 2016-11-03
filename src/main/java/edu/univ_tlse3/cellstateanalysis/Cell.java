package edu.univ_tlse3.cellstateanalysis;

import edu.univ_tlse3.utils.ImgUtils;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import ij.gui.Roi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Tong LI
 */
public class Cell implements Serializable{

   static final int AREA = 1, MEAN = 2, STD_DEV = 3, MIN = 4, MAX = 5, X_CENTROID = 6, Y_CENTROID = 7,
           PERIMETER = 8, MAJOR = 9, MINOR = 10, ANGLE = 11, CIRCULARITY = 12, ASPECT_RATIO = 13, ROUNDNESS = 14,
           SOLIDITY = 15;
   private int cellNumber;
   private Roi cellShapeRoi;
   private String[] measurements;
   private SpotsContainer spotContainer;
   private GeometryContainer geoContainer;
   private ArrayList<Integer> spotInBetweenFrames = new ArrayList<Integer>();
   private int anaBOnsetFrame_;

   /**
    * @param roiCellShape : ROI that correspond to segmented cell
    * @param cellNb       ：cell instance index in array
    */
   public Cell(Roi roiCellShape, int cellNb) {
      this.cellShapeRoi = roiCellShape;
      this.cellNumber = cellNb;
      this.spotContainer = new SpotsContainer();
      this.geoContainer = new GeometryContainer();
   }

   /**
    * @return ROI corresponding to segmented cell
    */
   public Roi getCellShapeRoi() {
      return this.cellShapeRoi;
   }

   Roi rescaleCellShapeRoi(double[] factors) {
      return ImgUtils.rescaleRoi(this.cellShapeRoi, factors);
   }

   public int getCellNumber() {
      return this.cellNumber;
   }

   void setRoiMeasurement(String measurements) {
      this.measurements = measurements.split("\t", -1);
   }

   public double get(int headerIndex) {
      return Double.parseDouble(measurements[headerIndex]);
   }

   void addChannel(String channel) {
      this.spotContainer.addChannel(channel);
      this.geoContainer.addChannel(channel);
   }

   void putSpot(String channel, int frame, Spot s) {
      this.spotContainer.putSpot(channel, frame, s);
   }

   int getNbOfSpots(String channel, int frame) {
      return this.spotContainer.getNbOfSpot(channel, frame);
   }

   Iterable<Spot> getSpotsInFrame(String channel, int frame) {
      return this.spotContainer.getSpotsInFrame(channel, frame);
   }

   void removeSpot(String channel, int frame, Spot s) {
      this.spotContainer.removeSpot(channel, frame, s);
   }

   void setTrackmateModel(Model model) {
      this.spotContainer.setTrackmateModel(model);
   }

   public SpotsContainer getSpotContainer() {
      return this.spotContainer;
   }

   void putGeometry(String channel, int frame, HashMap<String, Object> geometries) {
      this.geoContainer.putGeometry(channel, frame, geometries);
   }

   public GeometryContainer getGeometryContainer() {
      return this.geoContainer;
   }

   public void addSpotInBtwnFrame(Integer frame) {
      this.spotInBetweenFrames.add(frame);
   }

   public ArrayList<Integer> getSpotInBtwnFrames() {
      return this.spotInBetweenFrames;
   }

   public void setAnaBOnsetFrame(int anaBOnsetFrame) {
      anaBOnsetFrame_ = anaBOnsetFrame;
   }
}
