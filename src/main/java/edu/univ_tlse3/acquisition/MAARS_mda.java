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
 * Created by Tong LI on 24/06/2016.
 */
public class MAARS_mda {
    public static ImagePlus acquireImagePlus(MMStudio mm, String pathToAcqSetting, String savingPath, String channelName) {
        Datastore ds = null;
        try {
            mm.acquisitions().loadAcquisition(pathToAcqSetting);
            ds = mm.acquisitions().runAcquisition(channelName, savingPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Image> imageList = new ArrayList<>();
        for (Coords coords : ds.getUnorderedImageCoords()) {
            imageList.add(ds.getImage(coords));
        }
        imageList.sort(Comparator.comparingInt(o -> o.getCoords().getZ()));
        return ImgUtils.convertImages2Imp(imageList, channelName);
    }
}
