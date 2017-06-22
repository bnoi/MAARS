package maars.headless;

import ij.IJ;
import maars.agents.SetOfCells;
import maars.main.MaarsParameters;
import maars.main.Maars_Interface;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq implements Runnable {
   private MaarsParameters parameters;
   private String rootDir;

   MAARSNoAcq(MaarsParameters parameters) {
      this.parameters = parameters;
      rootDir = parameters.getSavingPath();
   }

   private ArrayList<String[]> getAcqPositions() {
      ArrayList<String[]> acqPos = new ArrayList<>();
      String[] listAcqNames = new File(rootDir).list();
      String pattern = "(X)(\\d+)(_)(Y)(\\d+)(_FLUO)";
      assert listAcqNames != null;
      for (String acqName : listAcqNames) {
         if (Pattern.matches(pattern, acqName)) {
            acqPos.add(new String[]{acqName.split("_", -1)[0].substring(1),
                  acqName.split("_", -1)[1].substring(1)});
         }
      }
      return acqPos;
   }

   @Override
   public void run() {
      long start = System.currentTimeMillis();
//      String[] posNbs = Maars_Interface.post_segmentation(parameters);
//      Maars_Interface.post_fluoAnalysis(posNbs, rootDir, parameters);
      IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
   }
}