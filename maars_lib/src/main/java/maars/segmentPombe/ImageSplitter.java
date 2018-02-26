package maars.segmentPombe;

import ij.ImagePlus;
import ij.ImageStack;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 13, 2015
 */
class ImageSplitter {
   private ImagePlus img;
   private int numberToSplit;

   /**
    * constructor
    *
    * @param img    image to analyse
    * @param number : typically is the available processor number
    */
   ImageSplitter(ImagePlus img, int number) {
      this.img = img;
      this.numberToSplit = number;
   }

   /**
    * @return double[]: if divided evenly, double[0] == double[1] else double[1]
    * take the rest of width
    */
   int[] getWidths() {
      int[] coord = new int[2];
      coord[0] = img.getWidth() / numberToSplit;
      coord[1] = coord[0] + img.getWidth() % numberToSplit;
      return coord;
   }

   /**
    * @param xBase : fist x coordinate
    * @param width : width double[0] or double[1]
    * @return cropped imagePlus
    */
   ImagePlus crop(int xBase, int width) {
      ImagePlus newImg = new ImagePlus();
      ImageStack stack = img.getStack();
      ImageStack newStack = stack.crop(xBase, 0, 0, width, img.getHeight(), img.getNSlices());
      newImg.setStack(newStack);
      return newImg;
   }
}
