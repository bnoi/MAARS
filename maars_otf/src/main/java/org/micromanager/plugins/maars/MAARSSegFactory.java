package org.micromanager.plugins.maars;

import org.micromanager.Studio;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

/**
 * Created by tong on 26/06/17.
 */
public class MAARSSegFactory implements ProcessorFactory{
   Studio studio_;
   public MAARSSegFactory(Studio studio){
      studio_ = studio;
   }
   @Override
   public Processor createProcessor() {
      return new MAARSSeg();
   }
}
