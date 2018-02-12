package maars.headless.batchSegmentation;

import ij.IJ;
import ij.ImagePlus;
import maars.headless.ImgLoader;
import maars.headless.batchFluoAnalysis.MaarsFluoAnalysis;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.utils.FileUtils;
import net.imagej.ops.AbstractOp;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Pattern;
/**
 * Created by tongli on 13/06/2017.
 */
@Plugin(type=BatchSegmentation.class, name = BatchSegmentation.NAME,
      attrs = { @Attr(name = "aliases", value = BatchSegmentation.ALIASES) })
public class DefaultBatchSegmentation extends AbstractOp implements BatchSegmentation {
   private static Logger logger = LoggerFactory.getLogger(DefaultBatchSegmentation.class);
   @Parameter
   private String[] dirs;

   @Parameter
   private String configName;
   @Override
   public void run(){
      launchSeg(dirs, configName);}

   private void launchSeg(String[] dirs, String configName) {
      for (String d : dirs) {
         logger.info(d);
         MaarsParameters parameter = MaarsParameters.fromFile(d + File.separator  + configName);
         parameter.setSavingPath(d);
         parameter.save(d);
         String segPath = d + File.separator + parameter.getSegmentationParameter(MaarsParameters.SEG_PREFIX);
         String[] posNbs = ImgLoader.getPositionSuffix(segPath, MaarsFluoAnalysis.MMSIGNATURE);
         for (String pos: posNbs){
            logger.info(pos);
            for (String f : FileUtils.getTiffWithPattern(segPath, ".*.tif")){
               if (Pattern.matches(".*MMStack_" + pos+"\\.ome\\.tif", f)){
                  ImagePlus img = IJ.openImage(segPath + File.separator + f);
                  Thread th = new Thread(new MaarsSegmentation(parameter, img, pos));
                  th.start();
                  try {
                     th.join();
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
               }
            }
         }
      }
   }
}
