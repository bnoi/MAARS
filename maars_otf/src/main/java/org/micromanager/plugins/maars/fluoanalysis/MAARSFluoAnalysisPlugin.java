package org.micromanager.plugins.maars.fluoanalysis;

import maars.main.MaarsParameters;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.data.ProcessorPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Created by tongli on 27/06/2017.
 */
@Plugin(type = ProcessorPlugin.class)
public class MAARSFluoAnalysisPlugin implements ProcessorPlugin, SciJavaPlugin {
   Studio studio_;
   MaarsParameters parameters_;
   @Override
   public ProcessorConfigurator createConfigurator(PropertyMap propertyMap) {
      return null;
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap propertyMap) {
      return null;
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
