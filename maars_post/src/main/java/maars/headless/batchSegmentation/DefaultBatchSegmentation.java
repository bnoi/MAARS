package maars.headless.batchSegmentation;

import ij.IJ;
import ij.ImagePlus;
import maars.gui.MaarsFluoAnalysisDialog;
import maars.gui.MaarsSegmentationDialog;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.utils.FileUtils;
import net.imagej.ops.AbstractOp;
import org.scijava.ItemIO;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
   public void run(){loadMaarsParameters(dirs, configName);}

   public static void loadMaarsParameters(String[] dirs, String configName) {
      for (String d : dirs) {
         MaarsParameters parameter;
         InputStream inStream = null;
         if (FileUtils.exists(d + File.separator + configName)) {
            try {
               inStream = new FileInputStream(d + File.separator + configName);
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            }
         } else {
            inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
         }
         parameter = new MaarsParameters(inStream);
         parameter.setSavingPath(d);
         for (String f : FileUtils.getTiffWithPattern(d,".*.tif")){
            System.out.println(d + File.separator + f);
            ImagePlus img = IJ.openImage(d + File.separator + f);
            Thread th = new Thread(new MaarsSegmentation(parameter, img, f));
            th.start();
            try {
               th.join();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
         parameter.save(d);
      }
   }
}
