package org.micromanager.plugins.maars.segmentation;

import maars.main.MaarsParameters;
import org.micromanager.Studio;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

/**
 * Created by tong on 26/06/17.
 */
public class MaarsOTFSegFactory implements ProcessorFactory{
   private Studio studio_;
   private MaarsParameters parameters_;
   MaarsOTFSegFactory(Studio studio, MaarsParameters parameters){
      studio_ = studio;
      parameters_ = parameters;
   }
   @Override
   public Processor createProcessor() {
      parameters_.setSavingPath(studio_.acquisitions().getAcquisitionSettings().root);
      return new MaarsOTFSeg(parameters_);
   }
}
