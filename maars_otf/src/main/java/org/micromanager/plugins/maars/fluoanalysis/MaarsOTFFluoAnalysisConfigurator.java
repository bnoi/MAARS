package org.micromanager.plugins.maars.fluoanalysis;

import maars.gui.MaarsFluoAnalysisDialog;
import maars.main.MaarsParameters;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;

/**
 * Created by tongli on 27/06/2017.
 */
public class MaarsOTFFluoAnalysisConfigurator implements ProcessorConfigurator {
   Studio studio_;
   MaarsParameters parameters_;
   MaarsFluoAnalysisDialog dialog;

   public MaarsOTFFluoAnalysisConfigurator(Studio studio, MaarsParameters parameters){
      studio_ = studio;
      parameters_ = parameters;
   }
   @Override
   public void showGUI() {
      dialog = new MaarsFluoAnalysisDialog(parameters_);
   }

   @Override
   public void cleanup() {
      if (dialog!=null && dialog.isVisible()) {
         dialog.setVisible(false);
      }
      dialog = null;
      parameters_ = null;
   }

   @Override
   public PropertyMap getSettings() {
      return null;
   }
}
