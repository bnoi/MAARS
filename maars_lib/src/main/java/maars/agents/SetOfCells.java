package maars.agents;

import ij.measure.ResultsTable;

/**
 * Created by tong on 26/06/17.
 */
public interface SetOfCells {
   public Cell getCell(int index);

   public int size();

   public void loadCells(String pathToROIZip);

   public void reset();

   public void addRoiMeasurementIntoCells(ResultsTable rt);

   public String getPosLabel();
}
