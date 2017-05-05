package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.utils.ImgUtils;
import ij.ImagePlus;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Only called with On-the-Fly analysis of MAARS.
 * Created by Tong LI on 24/06/2016.
 */
public class MAARS_mda {
    /**
     * run a MDA acquisition with setting
     * @param mm                MMStudio object
     * @param pathToAcqSetting  path to acq setting txt
     * @param savingPath        saving path of images
     * @param channelName       name of acquisition
     * @return  imageplus convected from datastore object of mm2
     */
    public static ImagePlus[] acquireImagePlus(MMStudio mm, String pathToAcqSetting, String savingPath, String channelName) {
        Datastore ds = null;
        try {
            mm.acquisitions().loadAcquisition(pathToAcqSetting);
            ds = mm.acquisitions().runAcquisition(channelName, savingPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Coords> sortedCoords = ImgUtils.getSortedCoords(ds);
        ArrayList<Image> imageList = new ArrayList<>();
        for (Coords coords : sortedCoords) {
          imageList.add(ds.getImage(coords));
       }
        return ImgUtils.convertImages2Imp(imageList, ds.getSummaryMetadata(), mm.getCore().getPixelSizeUm());
    }

   /**
    * run a MDA acquisition with setting
    * @param mm                MMStudio object
    * @param pathToAcqSetting  path to acq setting txt
    * @return  imageplus convected from datastore object of mm2
    */
   public static ImagePlus[] acquireImagePlus(MMStudio mm, String pathToAcqSetting) {
      Datastore ds = null;
      try {
         mm.acquisitions().loadAcquisition(pathToAcqSetting);
         ds = mm.acquisitions().runAcquisition();
      } catch (IOException e) {
         e.printStackTrace();
      }
      ArrayList<Coords> sortedCoords = ImgUtils.getSortedCoords(ds);
      ArrayList<Image> imageList = new ArrayList<>();
      for (Coords coords : sortedCoords) {
         imageList.add(ds.getImage(coords));
      }
      return ImgUtils.convertImages2Imp(imageList, ds.getSummaryMetadata(), mm.getCore().getPixelSizeUm());
   }
}
