package org.micromanager.plugins.maars.segmentation;

import maars.gui.MaarsSegmentationDialog;
import maars.main.MaarsParameters;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;

/**
 * Created by tong on 26/06/17.
 */
public class MaarsOTFSegConfigurator implements ProcessorConfigurator {
   MaarsSegmentationDialog dialog;
   Studio studio_;
   MaarsParameters parameters_;

   public MaarsOTFSegConfigurator(Studio studio, MaarsParameters parameters){
      studio_ = studio;
      parameters_ = parameters;
   }

   @Override
   public void showGUI() {
      dialog = new MaarsSegmentationDialog(parameters_, null);
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
