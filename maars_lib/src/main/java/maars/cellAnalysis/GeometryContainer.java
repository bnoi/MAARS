package maars.cellAnalysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class GeometryContainer <T extends Number> implements Serializable {
   private HashMap<String, HashMap<Integer, HashMap<String, T>>> geosOfCells;

   public GeometryContainer() {
   }

   /**
    * @param channel channel name
    */
   public void addChannel(String channel) {
      if (this.geosOfCells == null) {
         this.geosOfCells = new HashMap<>();
      }
      if (!this.geosOfCells.containsKey(channel)) {
         this.geosOfCells.put(channel, new HashMap<>());
      }
   }

   /**
    * @param channel channel name
    * @param frame   frame name
    */
   private boolean geoFrameExists(String channel, int frame) {
      return getGeosInChannel(channel).containsKey(frame);
   }

   /**
    * @param channel    channel name
    * @param frame      frame name
    * @param geometries geometry of spindle
    */
   public void putGeometry(String channel, Integer frame, HashMap<String, T> geometries) {
      if (!geoFrameExists(channel, frame)) {
         getGeosInChannel(channel).put(frame, new HashMap<>());
      }
      getGeosInChannel(channel).put(frame, geometries);
   }

   public String[] getUsingChannels() {
      return this.geosOfCells.keySet().toArray(new String[]{});
   }

   public HashMap<Integer, HashMap<String, T>> getGeosInChannel(String channel) {
      return this.geosOfCells.get(channel);
   }

}
