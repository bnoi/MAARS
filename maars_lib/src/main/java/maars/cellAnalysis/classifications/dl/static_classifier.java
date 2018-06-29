package maars.cellAnalysis.classifications.dl;

import maars.agents.Cell;
import maars.cellAnalysis.classifications.Classifier;

import java.io.IOException;

public class static_classifier implements Classifier {
   @Override
   public double[] infer(Cell cell) {
      return new double[0];
   }

   @Override
   public void serialize(String savePath) throws IOException {
      throw new IOException("not supported yet");
   }

   @Override
   public void deserialize(String loadPath) throws IOException {
      throw new IOException("not supported yet");
   }
}
