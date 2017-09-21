package org.micromanager.plugins.maars.segmentation;

import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.data.ProcessorPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Created by tong on 26/06/17.
 */
@Plugin(type = ProcessorPlugin.class)
public class MaarsOTFSegPlugin implements ProcessorPlugin, SciJavaPlugin{
   Studio studio_;
   MaarsParameters parameters_ = Maars_Interface.loadParameters();
   static final String pluginName = "MAARS Seg";

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
      return new MaarsOTFSegConfigurator(studio_, parameters_);
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap propertyMap) {
      return new MaarsOTFSegFactory(studio_, parameters_);
   }

   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public String getName() {
      return pluginName;
   }

   @Override
   public String getHelpText() {
      return "Bright field segmentation of MAARS";
   }

   @Override
   public String getVersion() {
      return "0.0.1";
   }

   @Override
   public String getCopyright() {
      return "Tong LI";
   }
}
