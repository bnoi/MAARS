package maars.cellAnalysis;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SpotsContainer implements Serializable {
   private HashMap<String, SpotCollection> spotsInCell_;
   private Model trackmateModel;

   public SpotsContainer() {
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
            newSet.remove(findLowestQualitySpot(newSet.iterable(0, false)), 0);
         }
      }
      return newSet;
   }

   /**
    * add channel
    *
    * @param channel channel name
    */
   public void addChannel(String channel) {
      if (this.spotsInCell_ == null) {
         this.spotsInCell_ = new HashMap<>();
      }
      if (!this.spotsInCell_.containsKey(channel)) {
         this.spotsInCell_.put(channel, new SpotCollection());
      }
   }

   /**
    * put spot in specified channel / cell / frame
    *
    * @param channel channel name
    * @param frame   frame of acquisition
    * @param spot    spot
    */
   public void putSpot(String channel, int frame, Spot spot) {
      //TODO @SpotCollection do not provide a method to test whether it contains a spot, this may add two times the same spot
      spotsInCell_.get(channel).add(spot, frame);
   }

   /**
    * Get the spot collection of cell
    *
    * @param channel fluo channel
    * @return SpotCollection of Trackmate
    */
   public SpotCollection getSpots(String channel) {
      return this.spotsInCell_.get(channel);
   }

   /**
    * Get the set of cell in spot collection of frame...
    *
    * @param channel fluo channel
    * @param frame   frame of acquisition
    * @return mirror object iterable of spotCollection
    */
   public Iterable<Spot> getSpotsInFrame(String channel, int frame) {
      return getSpots(channel).iterable(frame, false);
   }

   /**
    * remove cell from specified channel / cell / frame
    *
    * @param channel    fluo channel
    * @param frame      frame of acquisition
    * @param spToRemove the spot to be removed
    */
   public void removeSpot(String channel, int frame, Spot spToRemove) {
      this.spotsInCell_.get(channel).remove(spToRemove, frame);
   }

   /**
    * Get the number of spot in spotCollection of frame ...
    *
    * @param channel fluo channel
    * @param frame   frame of acquisition
    * @return number of spot in collection
    */
   public int getNbOfSpot(String channel, int frame) {
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
   public void setTrackmateModel(Model model) {
      if (this.trackmateModel == null)
         this.trackmateModel = model;
   }

   public Set<String> getUsingChannels() {
      return this.spotsInCell_.keySet();
   }
}
