package org.micromanager.plugins.maars.fluoanalysis;

import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.plugins.maars.segmentation.MaarsOTFSegPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Created by tongli on 27/06/2017.
 */
@Plugin(type = ProcessorPlugin.class)
public class MaarsOTFFluoAnalysisPlugin implements ProcessorPlugin, SciJavaPlugin {
   Studio studio_;
   MaarsParameters parameters_ = Maars_Interface.loadParameters();
   static final String pluginName = "MAARS Fluo";
   @Override
   public ProcessorConfigurator createConfigurator(PropertyMap propertyMap) {
      PropertyMap.PropertyMapBuilder maarsBuilder = studio_.data().getPropertyMapBuilder();
//      maarsBuilder = maarsBuilder.putDouble(MaarsParameters.MAXIMUM_CELL_AREA,
//            Double.parseDouble(parameters_.getSegmentationParameter(MaarsParameters.MAXIMUM_CELL_AREA)));
//      maarsBuilder = maarsBuilder.putDouble(MaarsParameters.MINIMUM_CELL_AREA,
//            );
//      maarsBuilder = maarsBuilder.putBoolean(MaarsParameters.FILTER_MEAN_GREY_VALUE,
//            );
//      maarsBuilder = maarsBuilder.putInt(MaarsParameters.MEAN_GREY_VALUE,
//            );
//      maarsBuilder = maarsBuilder.putBoolean(MaarsParameters.FILTER_SOLIDITY,
//            );
//      maarsBuilder = maarsBuilder.putInt(MaarsParameters.SOLIDITY,
//            );
      studio_.profile().insertProperties(MaarsOTFSegPlugin.class, maarsBuilder.build());
      return new MaarsOTFFluoAnalysisConfigurator(studio_, parameters_);
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap propertyMap) {
      return new MaarsOTFFluoAnalysisFactory(studio_, parameters_);
   }

   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public String getName() {
      return "MAARS FluoAnalysis";
   }

   @Override
   public String getHelpText() {
      return "Fluo-analysis plugin of MAARS";
   }

   @Override
   public String getVersion() {
      return null;
   }

   @Override
   public String getCopyright() {
      return "Tong LI";
   }
}
