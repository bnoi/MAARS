package maars.agents;

/**
 * Created by tong on 26/06/17.
 */
public interface SocSet {
   public SetOfCells getSoc(String positionName);

   public int size();

   public void put(String positionNames, SetOfCells soc);

   public String[] getPositionNames();
}
