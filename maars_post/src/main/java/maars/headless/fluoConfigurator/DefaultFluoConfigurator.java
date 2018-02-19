package maars.headless.fluoConfigurator;

import maars.gui.MaarsFluoAnalysisDialog;
import maars.main.MaarsParameters;
import net.imagej.ops.AbstractOp;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

/**
 * Created by tongli on 13/06/2017.
 */
@Plugin(type= FluoConfigurator.class, name = FluoConfigurator.NAME,
      attrs = { @Attr(name = "aliases", value = FluoConfigurator.ALIASES) })
public class DefaultFluoConfigurator extends AbstractOp implements FluoConfigurator {
   @Parameter
   private String[] dirs;

   @Parameter
   private String configName;

   @Override
   public void run(){
      for (String d : dirs) {
         System.out.println(d);
         MaarsParameters parameter = MaarsParameters.fromFile(d + File.separator + configName);
         new MaarsFluoAnalysisDialog(parameter);
      }
   }
}
