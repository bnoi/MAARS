package edu.univ_tlse3.acquisition;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import mmcorej.CMMCore;
import org.micromanager.SequenceSettings;
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

public class SuperClassAcquisition {

    private MMStudio mm;
    private CMMCore mmc;

    /**
     * Constructor :
     *
     * @param mm  : graphical user interface of Micro-Manager
     * @param mmc : Core object of Micro-Manager
     */
    public SuperClassAcquisition(MMStudio mm, CMMCore mmc) {
        this.mm = mm;
        this.mmc = mmc;
    }

    public ImagePlus convert2Imp(List<Image> listImg, String channelName) {
        ImageStack imageStack = new ImageStack((int) mmc.getImageWidth(), (int) mmc.getImageHeight());
        for (Image img : listImg) {
            // Prepare a imagePlus (for analysis)
            ImageProcessor imgProcessor = mm.getDataManager().getImageJConverter().createProcessor(img);
            imageStack.addSlice(imgProcessor.convertToShortProcessor());
        }
        // ImagePlus for further analysis
        ImagePlus imagePlus = new ImagePlus(channelName, imageStack);
        Calibration cal = new Calibration();
        cal.setUnit("micron");
        cal.pixelWidth = mmc.getPixelSizeUm();
        cal.pixelHeight = mmc.getPixelSizeUm();
        imagePlus.setCalibration(cal);
        return imagePlus;
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
    public List<Image> acquire(SequenceSettings acqSettings, String channelGroup) {
        MAARS_mda mda = new MAARS_mda(mm, acqSettings, channelGroup);
        Datastore ds = mda.acquire();

        List<Image> listImg = new ArrayList<Image>();
        for (Coords coords : ds.getUnorderedImageCoords()) {
            IJ.log(coords.getChannel() + "-" + coords.getTime() + "-" + coords.getZ());
            listImg.add(ds.getImage(coords));
        }
        return listImg;
    }
}
