package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.utils.ImgUtils;
import ij.ImagePlus;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Only called with On-the-Fly analysis of MAARS.
 * Created by Tong LI on 24/06/2016.
 */
public class MAARS_mda implements Callable<ImagePlus[]>{
    private String pathToAcqSetting_;
    private String savingPath_;
    private String channelName_;
    /**
     * run a MDA acquisition with setting
     * @param pathToAcqSetting  path to acq setting txt
     * @param savingPath        saving path of images
     * @param channelName       name of acquisition
     */
    public MAARS_mda(String pathToAcqSetting, String savingPath, String channelName) {
        pathToAcqSetting_ = pathToAcqSetting;
        savingPath_ = savingPath;
        channelName_ = channelName;
    }

    @Override
    public ImagePlus[] call() throws Exception {
        Datastore ds;
        MMStudio mm = MMStudio.getInstance();
        try {
            mm.acquisitions().loadAcquisition(pathToAcqSetting_);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ds = mm.acquisitions().runAcquisition(channelName_, savingPath_);
        ArrayList<Coords> sortedCoords = ImgUtils.getSortedCoords(ds);
        ArrayList<Image> imageList = new ArrayList<>();
        for (Coords coords : sortedCoords) {
            imageList.add(ds.getImage(coords));
        }
        return ImgUtils.convertImages2Imp(imageList, ds.getSummaryMetadata(), mm.getCore().getPixelSizeUm());
    }
}
