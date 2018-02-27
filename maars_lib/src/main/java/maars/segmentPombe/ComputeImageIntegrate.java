package maars.segmentPombe;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.util.concurrent.Callable;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 13, 2015
 */
class ComputeImageIntegrate implements Callable<FloatProcessor> {
   private ImagePlus img;
   private ComputeIntegration computeIntegratedImage_;

   ComputeImageIntegrate(ImagePlus img, float zFocus, float sigma, int direction) {
      this.img = img;
      // compute Integrated value
      computeIntegratedImage_ = new ComputeIntegration(zFocus, sigma, direction);
      computeIntegratedImage_.preCalculateParameters(0, img.getNSlices() - 1);
   }

   /**
    * calculate Integrated score for one given img, img here should normally be
    * a crop of the original BF image.
    */
   @Override
   public FloatProcessor call() {
      // initiate variable to compute Integrated value
      float[] iz = new float[img.getNSlices()];
      FloatProcessor IntegratedImage = new FloatProcessor(img.getWidth(), img.getHeight());
      for (int x = 0; x < img.getWidth(); x++) {
         for (int y = 0; y < img.getHeight(); y++) {
            for (int z = 0; z < img.getNSlices(); z++) {
               img.setZ(z);
               iz[z] = img.getPixel(x, y)[0];
               // the first element returned by the getPixel function is
               // the grayscale values
            }
            double IntegratedPixelValue = computeIntegratedImage_.integrate(iz);
            IntegratedImage.putPixelValue(x, y, IntegratedPixelValue);
         }
      }
      return IntegratedImage;
   }
}
