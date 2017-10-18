package maars.headless.batchFluoAnalysis;

import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import net.imagej.ops.AbstractOp;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type=BatchFluoAnalysis.class, name = BatchFluoAnalysis.NAME,
      attrs = { @Attr(name = "aliases", value = BatchFluoAnalysis.ALIASES) })
public class DefaultBatchFluoAnalysis extends AbstractOp implements BatchFluoAnalysis{

   @Parameter
   private String[] dirs;

   @Parameter
   private String configName;

   @Override
   public void run() {
      fluoAnalysis(dirs,configName);
   }

   private void fluoAnalysis(String[] dirs, String configName){
      Maars_Interface.copyDeps();
      for (String d : dirs) {
         System.out.println(d);
         MaarsParameters parameter = MaarsParameters.fromFile(d + File.separator  + configName);
         parameter.setSavingPath(d);
         parameter.save(d);
         Thread th = new Thread(new MaarsFluoAnalysis(parameter));
         th.start();
         try {
            th.join();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }
}
