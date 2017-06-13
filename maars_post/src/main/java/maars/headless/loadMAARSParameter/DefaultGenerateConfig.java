package maars.headless.loadMAARSParameter;

import maars.gui.MaarsFluoAnalysisDialog;
import maars.gui.MaarsSegmentationDialog;
import maars.main.MaarsParameters;
import maars.utils.FileUtils;
import net.imagej.ops.AbstractOp;
import org.scijava.ItemIO;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.InputStream;

/**
 * Created by tongli on 13/06/2017.
 */
@Plugin(type=GenerateConfigFile.class, name = GenerateConfigFile.NAME,
      attrs = { @Attr(name = "aliases", value = GenerateConfigFile.ALIASES) })
public class DefaultGenerateConfig extends AbstractOp implements GenerateConfigFile {
   @Parameter
   private String dirContainsConfig;

   @Parameter (type = ItemIO.OUTPUT)
   private MaarsParameters parameters_;

   @Override
   public void run() {
      parameters_ = loadMaarsParameters(dirContainsConfig);
   }

   public static MaarsParameters loadMaarsParameters(String rootDir) {
      InputStream inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
      MaarsParameters parameters = new MaarsParameters(inStream);
      parameters.setSavingPath(rootDir);
      new MaarsSegmentationDialog(parameters, null);
      MaarsFluoAnalysisDialog fluoAnalysisDialog = new MaarsFluoAnalysisDialog(parameters);
      return fluoAnalysisDialog.getParameters();
   }
}
