package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.utils.ImgUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import mmcorej.CMMCore;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.internal.DefaultCoords;
import org.micromanager.internal.MMStudio;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */

public class AcqLauncher {

    private MMStudio mm;
    private CMMCore mmc;

    /**
     * Constructor :
     *
     * @param mm  : graphical user interface of Micro-Manager
     * @param mmc : Core object of Micro-Manager
     */
    public AcqLauncher(MMStudio mm, CMMCore mmc) {
        this.mm = mm;
        this.mmc = mmc;
    }

	public static ArrayList<Double> computZSlices(double zRange, double zStep, double zFocus){
        double z = zFocus - (zRange / 2);
        int sliceNumber = (int) Math.round(zRange / zStep);
        ArrayList<Double> slices = new ArrayList<Double>();
        for (int k = 0; k <= sliceNumber; k++) {
            slices.add(z);
            z = z + zStep;
        }
        return slices;
    }


    /**
     * @param acqSettings
     * @return a duplicate of acquired images.
     */
    public static ImagePlus acquire(SequenceSettings acqSettings) {
        MAARS_mda mda = new MAARS_mda(MMStudio.getInstance());
        Datastore ds = mda.acquire(acqSettings);
        List<Image> imageList = new ArrayList<Image>();
        //TODO unordered
        for (Coords coords : ds.getUnorderedImageCoords()){
            imageList.add(ds.getImage(coords));
        }
        return ImgUtils.convertImages2Imp(imageList,
                acqSettings.channels.get(0).config);
    }
}
