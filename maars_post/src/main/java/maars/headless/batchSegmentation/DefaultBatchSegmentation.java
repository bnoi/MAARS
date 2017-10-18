package maars.headless.batchSegmentation;

import ij.IJ;
import ij.ImagePlus;
import maars.headless.batchFluoAnalysis.MaarsFluoAnalysis;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.utils.FileUtils;
import net.imagej.ops.AbstractOp;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.regex.Pattern;
import fiji.Debug;
/**
 * Created by tongli on 13/06/2017.
 */
@Plugin(type=BatchSegmentation.class, name = BatchSegmentation.NAME,
      attrs = { @Attr(name = "aliases", value = BatchSegmentation.ALIASES) })
public class DefaultBatchSegmentation extends AbstractOp implements BatchSegmentation {
   @Parameter
   private String[] dirs;

   @Parameter
   private String configName;
   @Override
   public void run(){
      launchSeg(dirs, configName);}

   private void launchSeg(String[] dirs, String configName) {
      for (String d : dirs) {
         System.out.println(d);
         MaarsParameters parameter = MaarsParameters.fromFile(d + File.separator  + configName);
         parameter.setSavingPath(d);
         parameter.save(d);
         String segPath = d + File.separator + parameter.getSegmentationParameter(MaarsParameters.SEG_PREFIX);
         String[] posNbs = MaarsFluoAnalysis.getPositionSuffix(segPath);
         for (String pos: posNbs){
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
   public static void main(String[] args){
      Debug.run("","");
   }
}
