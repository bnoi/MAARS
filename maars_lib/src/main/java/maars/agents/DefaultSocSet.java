package maars.agents;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by tong on 26/06/17.
 */
public class DefaultSocSet implements SocSet{
   private HashMap<String, SetOfCells> socSet_;
   public DefaultSocSet(){
      socSet_ = new HashMap<>();
   }

   @Override
   public SetOfCells getSoc(String positionName) {
      return socSet_.get(positionName);
   }

   @Override
   public int size() {
      return socSet_.size();
   }

   @Override
   public void put(String positionNames, SetOfCells soc) {
     socSet_.put(positionNames,soc);
   }

   @Override
   public String[] getPositionNames() {
      String[] s = socSet_.keySet().toArray(new String[socSet_.size()]);
      Arrays.sort(s);
      return s;
   }
}
