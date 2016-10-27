package edu.univ_tlse3.cellstateanalysis;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SpotsContainer {
   private HashMap<String, SpotCollection> spotsInCell;
   private Model trackmateModel;

   SpotsContainer() {
   }

   /**
    * Get the lowest qualit spot in the frame
    *
    * @return the spot with worst quality
    */
   static Spot findLowestQualitySpot(Iterable<Spot> spots) {
      double min = Double.POSITIVE_INFINITY;
      Spot lowestQualitySpot = null;
      for (Spot s : spots) {
         if (s.getFeature(Spot.QUALITY) < min) {
            min = s.getFeature(Spot.QUALITY);
            lowestQualitySpot = s;
         }
      }
      return lowestQualitySpot;
   }

   static SpotCollection getNBestqualitySpots(SpotCollection spots, int setSize, int maxNbSpot) {
      SpotCollection newSet = new SpotCollection();
      Iterator<Spot> it = spots.iterator(false);
      while (it.hasNext()) {
         Spot s = it.next();
         newSet.add(s, 0);
         if (newSet.getNSpots(0, false) > setSize * maxNbSpot) {
            newSet.remove(SpotsContainer.findLowestQualitySpot(newSet.iterable(0, false)), 0);
         }
      }
      return newSet;
   }

   /**
    * add channel
    *
    * @param channel channel name
    */
   void addChannel(String channel) {
      if (this.spotsInCell == null) {
         this.spotsInCell = new HashMap<String, SpotCollection>();
      }
      if (!this.spotsInCell.containsKey(channel)) {
         this.spotsInCell.put(channel, new SpotCollection());
      }
   }

   /**
    * put spot in specified channel / cell / frame
    *
    * @param channel channel name
    * @param frame   frame of acquisition
    * @param spot    spot
    */
   void putSpot(String channel, int frame, Spot spot) {
      this.spotsInCell.get(channel).add(spot, frame);
   }

   /**
    * Get the spot collection of cell
    *
    * @param channel fluo channel
    * @return SpotCollection of Trackmate
    */
   public SpotCollection getSpots(String channel) {
      return this.spotsInCell.get(channel);
   }

   /**
    * Get the set of cell in spot collection of frame...
    *
    * @param channel fluo channel
    * @param frame   frame of acquisition
    * @return mirror object iterable of spotCollection
    */
   Iterable<Spot> getSpotsInFrame(String channel, int frame) {
      return getSpots(channel).iterable(frame, false);
   }

   /**
    * remove cell from specified channel / cell / frame
    *
    * @param channel    fluo channel
    * @param frame      frame of acquisition
    * @param spToRemove the spot to be removed
    */
   void removeSpot(String channel, int frame, Spot spToRemove) {
      this.spotsInCell.get(channel).remove(spToRemove, frame);
   }

   /**
    * Get the number of spot in spotCollection of frame ...
    *
    * @param channel fluo channel
    * @param frame   frame of acquisition
    * @return number of spot in collection
    */
   int getNbOfSpot(String channel, int frame) {
      return getSpots(channel).getNSpots(frame, false);
   }

   public Model getTrackmateModel() {
      return trackmateModel;
   }

   /**
    * Save header of Trackmate output and spot collection
    *
    * @param model model from Trackmate
    */
   void setTrackmateModel(Model model) {
      if (this.trackmateModel == null)
         this.trackmateModel = model;
   }

   public Set<String> getUsingChannels() {
      return this.spotsInCell.keySet();
   }
}
