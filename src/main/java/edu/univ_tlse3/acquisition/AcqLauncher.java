package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.utils.ImgUtils;
import ij.ImagePlus;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */

public class AcqLauncher {

    /**
     * Constructor :
     */
    public AcqLauncher() {
    }

    /**
     * @param acqEng customized acquisition engine
     * @return a duplicate of acquired images.
     */
    public static ImagePlus acquire(AcquisitionWrapperEngine acqEng) {
        MAARS_mda mda = new MAARS_mda(MMStudio.getInstance());
        Datastore ds = mda.acquire(acqEng);
        List<Image> imageList = new ArrayList<Image>();
        //TODO unordered
        for (Coords coords : ds.getUnorderedImageCoords()){
            imageList.add(ds.getImage(coords));
        }
        return ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);
    }
}
