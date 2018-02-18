package maars.io;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import maars.agents.Cell;
import maars.agents.DefaultSetOfCells;
import maars.main.Maars_Interface;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tongli on 27/12/2016.
 */
public class IOUtils {
   public static void printErrorToIJLog(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter ps = new PrintWriter(sw);
      exception.printStackTrace(ps);
      IJ.error(sw.toString());
      try {
         sw.close();
      } catch (IOException e1) {
         e1.printStackTrace();
      }
      ps.close();
   }

   public static void serializeSoc(String pathToFluoDir, DefaultSetOfCells soc) {
      File f = new File(pathToFluoDir + "SetOfCell.serialize");
      ObjectOutputStream objOut = null;
      try {
         objOut = new ObjectOutputStream(new BufferedOutputStream(
               new FileOutputStream(f)));
         objOut.writeObject(soc);
         objOut.flush();

         IJ.log("Set of cel object is serialized.");
      } catch (IOException i) {
         IJ.log((i.getMessage()));
      } finally {
         if (objOut != null) {
            try {
               objOut.close();
            } catch (IOException e) {
               IOUtils.printErrorToIJLog(e);
            }
         }
      }
   }

   public static void saveAll(DefaultSetOfCells soc, ImagePlus mergedImg, String pathToDir,
                              Boolean useDynamic, ArrayList<String> arrayChannels, String posNb, String prefix) {
      IJ.log("Saving information of each cell on disk");
      String dest = pathToDir + prefix + Maars_Interface.FLUOANALYSIS_SUFFIX + posNb + File.separator;
      FileUtils.createFolder(dest);
      MAARSSpotsSaver spotSaver = new MAARSSpotsSaver(dest);
      MAARSGeometrySaver geoSaver = new MAARSGeometrySaver(dest);
      MAARSImgSaver imgSaver = new MAARSImgSaver(dest);
//        TODO
      CopyOnWriteArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
      ImagePlus croppedImg;
      Duplicator duplicator = new Duplicator();
      for (int i : cellIndex) {
         Cell cell = soc.getCell(i);
//        for (Cell cell : soc){
         geoSaver.save(cell);
         spotSaver.save(cell);
         mergedImg.setRoi(cell.getCellShapeRoi());
         for (int j = 1; j <= mergedImg.getNChannels(); j++) {
            croppedImg = duplicator.run(mergedImg, j, j, 1, mergedImg.getNSlices(),
                  1, mergedImg.getNFrames());
            IJ.run(croppedImg, "Grays", "");
            croppedImg.setRoi(ImgUtils.centerCroppedRoi(cell.getCellShapeRoi()));
            imgSaver.saveImgs(croppedImg, i, arrayChannels.get(j - 1), false);
         }
      }
      if (useDynamic) {
         IOUtils.serializeSoc(dest, soc);
      }
      mergedImg.hide();
   }
}
