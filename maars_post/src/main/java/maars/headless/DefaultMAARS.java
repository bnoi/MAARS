package maars.headless;

import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpEnvironment;
import org.scijava.ItemIO;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Created by tongli on 10/06/2017.
 */
@Plugin(type=MAARS_headless.class, name = MAARS_headless.NAME,
      attrs = { @Attr(name = "aliases", value = MAARS_headless.ALIASES) })
public class DefaultMAARS extends AbstractOp implements MAARS_headless{
   @Parameter
   private String path_;

   @Parameter
   private Boolean batch_mode_;

   @Parameter (type= ItemIO.OUTPUT)
   private boolean res;

   @Override
   public void run() {
      Maars_Interface.copyDeps();
      String configFileName = "maars_config.xml";
      MaarsParameters parameters = GuiFreeRun.loadMaarsParameters(configFileName, path_);
      parameters.setSavingPath(path_);
   }

   @Override
   public OpEnvironment ops() {
      return null;
   }

   @Override
   public void setEnvironment(OpEnvironment opEnvironment) {

   }
}
