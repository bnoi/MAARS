package maars.mmUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.process.ImageProcessor;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.SummaryMetadata;
import org.micromanager.internal.MMStudio;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by tongli on 09/06/2017.
 */
public class ImgUtils {

   /**
    * convert a list of Image to an ImagePlus
    *
    * @param listImg         list of Images
    * @param summaryMetadata metadata from datastore of mm2
    * @param pixelSizeUm     image calib
    * @return imageplus
    */
   public static HashMap<String, ImagePlus[]> convertImages2Imp(List<Image> listImg, SummaryMetadata summaryMetadata,
                                                                 double pixelSizeUm) {
      ImageStack imageStack = new ImageStack(Integer.valueOf(summaryMetadata.getUserData().getString("Width")),
            Integer.valueOf(summaryMetadata.getUserData().getString("Height")));
      for (Image img : listImg) {
         imageStack.addSlice(img.getMetadata().getReceivedTime(), image2imp(img));
      }
      ImagePlus imagePlus = new ImagePlus("", imageStack);
      Calibration cal = new Calibration();
      cal.setUnit("micron");
      cal.pixelWidth = pixelSizeUm;
      cal.pixelHeight = pixelSizeUm;
      cal.pixelDepth = summaryMetadata.getZStepUm();
      imagePlus.setCalibration(cal);
//       String[] axisOrder = summaryMetadata.getAxisOrder();
//       System.out.println(""+ axisOrder);
      int positionNb = summaryMetadata.getIntendedDimensions().getStagePosition();
      int onePosStackSize = imagePlus.getStackSize() / positionNb;
      HashMap<String, ImagePlus[]> reorderedImps = new HashMap<>();
      ImagePlus reorderedOnePos;
      for (int i = 0; i < positionNb; i++) {
         ImagePlus onePos = new Duplicator().run(imagePlus, i * onePosStackSize + 1, (i + 1) * onePosStackSize);
         reorderedOnePos = HyperStackConverter.toHyperStack(onePos, summaryMetadata.getIntendedDimensions().getChannel(),
               summaryMetadata.getIntendedDimensions().getZ(), summaryMetadata.getIntendedDimensions().getTime(),
               "xytzc", "Grayscale");
         ImagePlus[] channels;
         if (summaryMetadata.getChannelNames().length > 1) {
            channels = ChannelSplitter.split(reorderedOnePos);
            for (int j = 0; j < channels.length; j++) {
               channels[j].setTitle(summaryMetadata.getChannelNames()[j]);
            }
         } else {
            channels = new ImagePlus[]{reorderedOnePos};
         }
         reorderedImps.put(summaryMetadata.getStagePositions()[i].getLabel(), channels);
      }
      return reorderedImps;
   }

   /**
    * @param image from micro-manager
    * @return ImageProcessor
    */
   private static ImageProcessor image2imp(Image image) {
      MMStudio mm = MMStudio.getInstance();
      return mm.getDataManager().getImageJConverter().createProcessor(image);
   }

   public static ArrayList<Coords> getSortedCoords(Datastore ds) {
      ArrayList<Coords> coordsList = new ArrayList<>();
      for (Coords coords : ds.getUnorderedImageCoords()) {
         coordsList.add(coords);
      }
      java.util.Collections.sort(coordsList, (a, b) -> {
         int p1 = a.getStagePosition();
         int p2 = b.getStagePosition();
         if (p1 != p2) {
            return p1 < p2 ? -1 : 1;
         }
         int t1 = a.getTime();
         int t2 = b.getTime();
         if (t1 != t2) {
            return t1 < t2 ? -1 : 1;
         }
         int z1 = a.getZ();
         int z2 = b.getZ();
         if (z1 != z2) {
            return z1 < z2 ? -1 : 1;
         }
         int c1 = a.getChannel();
         int c2 = b.getChannel();
         return c1 < c2 ? -1 : 1;
      });
      return coordsList;
   }

   public static ArrayList<Image> dsToSortedList(Datastore ds, int frame){
      ArrayList<Image> imgs = new ArrayList<>();
      ArrayList<Coords> sortedCoords = maars.mmUtils.ImgUtils.getSortedCoords(ds);
      for (Coords coords : sortedCoords) {
         Image newImg = ds.getImage(coords);
         Coords.CoordsBuilder builder = coords.copy();
         builder.time(frame);
         imgs.add(newImg.copyAtCoords(builder.build()));
      }
      return imgs;
   }

   /**
    * @param fluoDir fluo base dir where all full field images are stored
    * @return an ImagePlus with all channels stacked
    */
   @Deprecated
   public static ImagePlus loadFullFluoImgs(String fluoDir) {
      Concatenator concatenator = new Concatenator();
      ArrayList<String> listAcqNames = new ArrayList<>();
      String pattern = "(\\w+)(_)(\\d+)";
      for (String acqName : new File(fluoDir).list()) {
         if (Pattern.matches(pattern, acqName)) {
            listAcqNames.add(acqName);
         }
      }
      String[] listAcqNamesArray = listAcqNames.toArray(new String[listAcqNames.size()]);
      Arrays.sort(listAcqNamesArray,
            Comparator.comparing(o -> Integer.parseInt(o.split("_", -1)[1])));
      concatenator.setIm5D(true);
      ImagePlus concatenatedFluoImgs = null;
      for (String acqName : listAcqNamesArray) {
         ImagePlus newImg = IJ.openImage(fluoDir + File.separator + acqName + File.separator + acqName + "_MMStack_Pos0.ome.tif");
         ImageStack stack = newImg.getStack();
         for (int i = 1; i <= stack.getSize(); i++) {
            stack.setSliceLabel(acqName, i);
         }
         concatenatedFluoImgs = concatenatedFluoImgs == null ?
               newImg : concatenator.concatenate(concatenatedFluoImgs, newImg, false);
      }
      return concatenatedFluoImgs;
   }
}
