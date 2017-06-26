package org.micromanager.plugins.maars;

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
public class MAARSSegPlugin implements ProcessorPlugin, SciJavaPlugin{
   Studio studio_;

   @Override
   public ProcessorConfigurator createConfigurator(PropertyMap propertyMap) {
      return new MAARSSegConfigurator(studio_);
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap propertyMap) {
      return new MAARSSegFactory(studio_);
   }

   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public String getName() {
      return "MAARS Seg";
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
