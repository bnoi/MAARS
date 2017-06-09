package maars.mmUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.SummaryMetadata;
import org.micromanager.internal.MMStudio;
import ij.process.ImageProcessor;

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
   public static HashMap<Integer, ImagePlus[]> convertImages2Imp(List<Image> listImg, SummaryMetadata summaryMetadata,
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
      HashMap<Integer, ImagePlus[]> reorderedImps = new HashMap<>();
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
         reorderedImps.put(i, channels);
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
}
