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
   private String d;

   @Parameter
   private String configName;

   @Parameter
   private String suffix;

   @Override
   public void run() {
      fluoAnalysis(d,configName);
   }

   private void fluoAnalysis(String d, String configName){
      Maars_Interface.copyDeps();
      System.out.println(d);
      MaarsParameters parameter = MaarsParameters.fromFile(d + File.separator  + configName);
      parameter.setSavingPath(d);
      parameter.save(d);
      Thread th = new Thread(new MaarsFluoAnalysis(parameter, suffix));
      th.start();
      try {
         th.join();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
