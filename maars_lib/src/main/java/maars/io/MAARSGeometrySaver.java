package maars.io;

import maars.agents.Cell;
import maars.utils.FileUtils;
import util.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MAARSGeometrySaver {
   private String geometryCSVDir;

   public MAARSGeometrySaver(String pathToFluoDir) {
      this.geometryCSVDir = pathToFluoDir + File.separator + "features" + File.separator;
      FileUtils.createFolder(geometryCSVDir);
   }

   private void saveGeometries(String channel, Cell cell) {
      HashMap<Integer, HashMap<String, Object>> geosInFrams = cell.getGeometryContainer().getGeosInChannel(channel);
      int cellNb = cell.getCellNumber();
      ArrayList<String[]> outLines = new ArrayList<>();
      if (geosInFrams.keySet().size() > 0) {
         int firstFrame = (Integer) geosInFrams.keySet().toArray()[0];
         ArrayList headerList = new ArrayList(geosInFrams.get(firstFrame).keySet());
         Collections.sort(headerList);
         String[] header = new String[headerList.size() + 1];
         header[0] = "Frame";
         for (int i = 1; i <= headerList.size(); i++) {
            header[i] = (String) headerList.get(i - 1);
         }
         FileWriter cellGeoWriter = null;
         String[] geoOfFrame;
         try {
            cellGeoWriter = new FileWriter(geometryCSVDir + String.valueOf(cellNb) + "_" + channel + ".csv");
         } catch (IOException e) {
            IOUtils.printErrorToIJLog(e);
         }
         for (int frame : geosInFrams.keySet()) {
            geoOfFrame = new String[headerList.size() + 1];
            geoOfFrame[0] = String.valueOf(frame);
            for (int i = 1; i <= headerList.size(); i++) {
               geoOfFrame[i] = String.valueOf(geosInFrams.get(frame).get(headerList.get(i - 1)));
            }
            outLines.add(geoOfFrame);
         }
         outLines.sort(Comparator.comparing(o -> Integer.valueOf(o[0])));
         outLines.add(0, header);
         assert cellGeoWriter != null;
         CSVWriter writer = new CSVWriter(cellGeoWriter, ',', CSVWriter.NO_QUOTE_CHARACTER);
         writer.writeAll(outLines);
         try {
            writer.close();
         } catch (IOException e) {
            IOUtils.printErrorToIJLog(e);
         }
      }
   }

   public void save(Cell cell) {
      for (String channel : cell.getGeometryContainer().getUsingChannels()) {
         saveGeometries(channel, cell);
      }
   }
}
