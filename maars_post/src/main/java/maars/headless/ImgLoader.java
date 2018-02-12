package maars.headless;
import ij.IJ;
import ij.ImagePlus;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import maars.utils.ImgUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tong on 12/02/18.
 */
public class ImgLoader {
   public static HashMap<String, String> populateSeriesImgNames(String pathToTiffFile) {
      HashMap<String, String> seriesImgNames = new HashMap<>();
      int seriesCount = 0;
      try (ImageReader reader = new ImageReader()) {
         IMetadata omexmlMetadata = MetadataTools.createOMEXMLMetadata();
         reader.setMetadataStore(omexmlMetadata);
         try {
            reader.setId(pathToTiffFile);
         } catch (FormatException | IOException e) {
            e.printStackTrace();
         }
         seriesCount = reader.getSeriesCount();
         for (int i = 0; i < seriesCount; i++) {
            reader.setSeries(i);
            String name = omexmlMetadata.getImageName(i); // this is the image name stored in the file
            String label = "series_" + (i + 1);  // this is the label that you see in ImageJ
            seriesImgNames.put(name, label);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      assert seriesCount !=0 ;
      IJ.log(seriesCount + " series registered");
      return seriesImgNames;
   }

   public static ImagePlus loadImgOfPosition(String pathToFluoImgsDir, String pattern, String pos) {
      File[] listOfFiles = new File(pathToFluoImgsDir).listFiles();
      String fluoTiffName = null;
      for (File f:listOfFiles){
         if (Pattern.matches(".*" + pattern + pos+"\\..*", f.getName())){
            fluoTiffName = f.getName();
         }
      }
      assert fluoTiffName!= null;
      IJ.log(fluoTiffName);
      HashMap<String, String> map = ImgLoader.populateSeriesImgNames(pathToFluoImgsDir + File.separator + fluoTiffName);
      String serie_number;
      if (map.size() !=1){
         serie_number = map.get(fluoTiffName.split("\\.")[0]);
         IJ.log(serie_number + " selected");
      }else{
         serie_number = "";
      }
      ImagePlus im2 = ImgUtils.lociImport(pathToFluoImgsDir + File.separator + fluoTiffName, serie_number);
      return im2;
   }

   public static String[] getPositionSuffix(String path, String pattern){
      String tifName = null;
      File[] listOfFiles = new File(path).listFiles();
      for (File f:listOfFiles){
         if (f.getName().endsWith(".tif") || f.getName().endsWith(".tiff")){
            tifName = f.getName();
         }
      }
      HashMap<String, String> namesDic = ImgLoader.populateSeriesImgNames(path + File.separator + tifName);
      String[] names = new String[namesDic.size()];
      names = namesDic.keySet().toArray(names);
      Pattern p = Pattern.compile(".*" + pattern + "(.*)");
      String[] posList = new String[names.length];
      for (int i =0; i< names.length; i++){
         Matcher matcher = p.matcher(names[i]);
         while (matcher.find()) {
            posList[i] = matcher.group(1);
         }
      }
      return posList;
   }
}
