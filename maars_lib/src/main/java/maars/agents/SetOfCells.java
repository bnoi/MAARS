package maars.agents;

import ij.measure.ResultsTable;

/**
 * Created by tong on 26/06/17.
 */
public interface SetOfCells {
   Cell getCell(int index);

   int size();

   void loadCells(String pathToROIZip);

   void reset();

   void addRoiMeasurementIntoCells(ResultsTable rt);

   String getPosLabel();
}
