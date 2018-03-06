package maars.main;

import ij.IJ;
import ij.ImagePlus;
import maars.agents.Cell;
import maars.agents.DefaultSetOfCells;
import maars.cellAnalysis.PythonPipeline;
import maars.io.IOUtils;
import maars.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tongli on 09/06/2017.
 */
public class Maars_Interface {
   public final static String SEGANALYSIS_SUFFIX = "_SegAnalysis" + File.separator;
   public final static String FLUOANALYSIS_SUFFIX = "_FluoAnalysis" + File.separator;
   /**
    * @param tasksSet tasks to be terminated
    */
   public static void waitAllTaskToFinish(CopyOnWriteArrayList<Map<String, Future>> tasksSet) {
      for (Map<String, Future> aFutureSet : tasksSet) {
         for (String channel : aFutureSet.keySet()) {
            try {
               aFutureSet.get(channel).get();
            } catch (InterruptedException | ExecutionException e) {
               IOUtils.printErrorToIJLog(e);
            }
            IJ.showStatus("Terminating analysis...");
         }
      }
      IJ.log("Spot detection finished! Proceed to saving and analysis...");
   }

   public static void copyDeps(){
      FileUtils.createFolder(MaarsParameters.DEPS_DIR);
      FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.ANALYSING_SCRIPT_NAME);
   }

   public static MaarsParameters loadParameters(){
      String configFileName = "maars_config.xml";
      InputStream inStream = null;
      if (FileUtils.exists(configFileName)) {
         try {
            inStream = new FileInputStream(configFileName);
         } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
         }
      } else {
         inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
      }
      return new MaarsParameters(inStream);
   }
}
