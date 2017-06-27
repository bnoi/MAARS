package org.micromanager.plugins.maars;

import ij.IJ;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tong on 26/06/17.
 */
public class MAARSSeg extends Processor{
   private int counter = 0;
   private MaarsParameters parameters_;

   public MAARSSeg(MaarsParameters parameters){
      parameters_ = parameters;
   }

   @Override
   public void processImage(Image image, ProcessorContext processorContext) {
      ExecutorService es = Executors.newSingleThreadExecutor();
      IJ.log(image.getCoords() + "");
      IJ.log(image.getMetadata().getPositionName());
      if (counter != 0 && counter%33 == 0){
         IJ.log(123+"");
//         es.submit(new MaarsSegmentation(parameters_, segImg, posNb)).get();

      }
      counter++;
      processorContext.outputImage(image);
   }
}
