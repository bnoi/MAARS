package maars.segmentPombe;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.util.concurrent.Callable;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 13, 2015
 */
class ComputeImageCorrelation implements Callable<FloatProcessor> {
   private ImagePlus img;
   private ComputeCorrelation computeCorrelationImage_;

   ComputeImageCorrelation(ImagePlus img, float zFocus, float sigma, int direction) {
      this.img = img;
      // compute correlation value
      computeCorrelationImage_ = new ComputeCorrelation(zFocus, sigma, direction);
      computeCorrelationImage_.preCalculateParameters(0, img.getNSlices() - 1);
   }

   /**
    * calculate correlation score for one given img, img here should normally be
    * a crop of the original BF image.
    */
   @Override
   public FloatProcessor call() {
      // initiate variable to compute correlation value
      float[] iz = new float[img.getNSlices()];
      FloatProcessor correlationImage = new FloatProcessor(img.getWidth(), img.getHeight());
      for (int x = 0; x < img.getWidth(); x++) {
         for (int y = 0; y < img.getHeight(); y++) {
            for (int z = 0; z < img.getNSlices(); z++) {
               img.setZ(z);
               iz[z] = img.getPixel(x, y)[0];
               // the first element returned by the getPixel function is
               // the grayscale values
            }
            double correlationPixelValue = computeCorrelationImage_.integrate(iz);
            correlationImage.putPixelValue(x, y, correlationPixelValue);
         }
      }
      return correlationImage;
   }
}
