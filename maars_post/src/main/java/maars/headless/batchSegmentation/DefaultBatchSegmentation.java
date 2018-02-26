package maars.headless.batchSegmentation;

import loci.formats.FormatException;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.utils.ImgUtils;
import net.imagej.ops.AbstractOp;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
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

   @Parameter
   private String suffix;

   @Override
   public void run(){
      String imgPath;
      for (String d : dirs) {
         logger.info(d);
         MaarsParameters parameter = MaarsParameters.fromFile(d + File.separator  + configName);
         parameter.setSavingPath(d);
         parameter.save(d);
         String segDir = d + File.separator + parameter.getSegmentationParameter(MaarsParameters.SEG_PREFIX);
         imgPath = Objects.requireNonNull(new File(segDir).listFiles(
               (FilenameFilter) new WildcardFileFilter("*." + suffix)))[0].getAbsolutePath();
         Map<Integer, String> serieNbPos = ImgUtils.populateSeriesImgNames(imgPath);

         for (int serie: serieNbPos.keySet()){
            String posName =serieNbPos.get(serie);
            logger.info(posName);
            try {
               Thread th = new Thread(new MaarsSegmentation(parameter, ImgUtils.lociImport(imgPath, serie, false), posName));
               th.start();
               try {
                  th.join();
               } catch (InterruptedException e) {
                  e.printStackTrace();
               }
            } catch (IOException | FormatException e) {
               e.printStackTrace();
            }
         }
      }
   }
}
