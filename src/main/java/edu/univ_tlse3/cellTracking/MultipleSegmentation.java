package edu.univ_tlse3.cellTracking;

import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.maars.MaarsSegmentation;
import edu.univ_tlse3.utils.IOUtils;
import ij.IJ;
import ij.ImagePlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by tong on 14/04/17.
 */
public class MultipleSegmentation {
   public static void main(String[] args) {
      ExecutorService es_ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      String configFileName = "/home/tong/Desktop/test_pr_tong/test_01_3/maars_config.xml";
      InputStream inStream = null;
      try {
         inStream = new FileInputStream(configFileName);
      } catch (FileNotFoundException e) {
         IOUtils.printErrorToIJLog(e);
      }
      MaarsParameters parameters = new MaarsParameters(inStream);
      ImagePlus segImg = IJ.openImage(parameters.getSavingPath() + File.separator +"1.tif");
      segImg.show();
      MaarsSegmentation ms = new MaarsSegmentation(parameters, segImg);
      Future future;
      future = es_.submit(ms);
      try {
         future.get();
      } catch (InterruptedException | ExecutionException e) {
         IOUtils.printErrorToIJLog(e);
      }
   }
}
