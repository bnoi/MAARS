package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.ImgUtils;

import ij.ImagePlus;
import mmcorej.CMMCore;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.internal.DefaultCoords;
import org.micromanager.internal.MMStudio;

import java.util.ArrayList;

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
    public ImagePlus acquire(SequenceSettings acqSettings, MaarsParameters parameters) {
        MAARS_mda mda = new MAARS_mda(mm);
        Datastore ds = mda.acquire(acqSettings, parameters);
        Coords.CoordsBuilder coordsBuilder = new DefaultCoords.Builder();
        coordsBuilder.time(ds.getMaxIndex(Coords.TIME));
        return ImgUtils.convertImages2Imp(ds.getImagesMatching(coordsBuilder.build()),
                acqSettings.channels.get(0).config, mm, mmc);
    }
}
