package maars.headless.batchSegmentation;

import ij.ImagePlus;
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
   
   @Parameter
   private boolean splitted;

   @Override
   public void run(){
      String imgPath = null;
      for (String d : dirs) {
         logger.info(d);
         MaarsParameters parameter = MaarsParameters.fromFile(d + File.separator  + configName);
         parameter.setSavingPath(d);
         parameter.save(d);
         String segDir = d + File.separator + parameter.getSegmentationParameter(MaarsParameters.SEG_PREFIX);
         File[] allFiles = Objects.requireNonNull(new File(segDir).listFiles(
               (FilenameFilter) new WildcardFileFilter("*." + suffix)));
         Map<Integer, String> serieNbPos;
         if (splitted){
            serieNbPos = ImgUtils.populateSeriesImgNames(allFiles);
         }else{
            imgPath = allFiles[0].getAbsolutePath();
            serieNbPos = ImgUtils.populateSeriesImgNames(imgPath);
         }
         for (int serie: serieNbPos.keySet()){
            ImagePlus currentImp = null;
            String posName;
            if (splitted){
               imgPath = serieNbPos.get(serie);
               logger.info(imgPath);
               try {
                  currentImp = ImgUtils.lociImport(imgPath);
               } catch (IOException | FormatException e) {
                  e.printStackTrace();
               }
               posName = ImgUtils.getPosNameFromFileName(imgPath);
            }else{
               posName = serieNbPos.get(serie);
               logger.info(posName);
               assert imgPath!= null;
               try {
                  currentImp = ImgUtils.lociImport(imgPath, serie, false);
               } catch (IOException | FormatException e) {
                  e.printStackTrace();
               }
            }
//            currentImp.show();
            assert currentImp != null;
            Thread th = new Thread(new MaarsSegmentation(parameter, currentImp, posName));
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
