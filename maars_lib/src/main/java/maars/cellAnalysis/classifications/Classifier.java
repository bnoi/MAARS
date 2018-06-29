package maars.cellAnalysis.classifications;

import maars.agents.Cell;

import java.io.IOException;
import java.io.Serializable;

public interface Classifier extends Serializable {

   String[] phaseLabel = {"Interphase", "Mitosis"};
   String[] defectsLabel = {"Lagging", "MisAligned"};

   double[] infer(Cell cell);

   void serialize(String savePath) throws IOException;

   void deserialize(String loadPath) throws IOException;

}
