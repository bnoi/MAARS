package org.micromanager.plugins.maars;

import maars.gui.MaarsSegmentationDialog;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;

/**
 * Created by tong on 26/06/17.
 */
public class MAARSSegConfigurator implements ProcessorConfigurator {
   MaarsSegmentationDialog dialog;
   Studio studio_;

   public MAARSSegConfigurator(Studio studio){
      studio_ = studio;
   }

   @Override
   public void showGUI() {
      dialog = new MaarsSegmentationDialog();
   }

   @Override
   public void cleanup() {
      dialog.setVisible(false);
   }

   @Override
   public PropertyMap getSettings() {
      return null;
   }
}
