package org.micromanager.plugins.maars.fluoanalysis;

import maars.main.MaarsParameters;
import org.micromanager.Studio;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

/**
 * Created by tongli on 27/06/2017.
 */
public class MAARSFluoAnalysisFactory implements ProcessorFactory {
   private Studio studio_;
   private MaarsParameters parameters_;
   MAARSFluoAnalysisFactory(Studio studio, MaarsParameters parameters){
      studio_ = studio;
      parameters_ = parameters;
   }
   @Override
   public Processor createProcessor() {
      parameters_.setSavingPath(studio_.acquisitions().getAcquisitionSettings().root);
      return new MAARSFluoAnalysis(parameters_);
   }
}
