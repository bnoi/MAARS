package org.micromanager.plugins.maars;

import ij.IJ;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;

/**
 * Created by tong on 26/06/17.
 */
public class MAARSSeg extends Processor{
   private int counter = 0;

   @Override
   public void processImage(Image image, ProcessorContext processorContext) {
      IJ.log(image.getCoords() + "");
      IJ.log(counter++ + "");
      processorContext.outputImage(image);
   }
}
