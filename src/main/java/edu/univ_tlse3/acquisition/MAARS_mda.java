package edu.univ_tlse3.acquisition;
import edu.univ_tlse3.utils.FileUtils;
import ij.IJ;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.Datastore;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.MMException;

/**
 * Created by Tong LI on 24/06/2016.
 */
public class MAARS_mda{
    MMStudio mm_;
    public MAARS_mda(MMStudio mm) {
        mm_ = mm;
    }
    public Datastore acquire(SequenceSettings acqSettings) {
        FileUtils.createFolder(acqSettings.root);
//        mm_.getAcquisitionManager().setAcquisitionSettings(acqSettings);
        AcquisitionWrapperEngine acqEng = new AcquisitionWrapperEngine();
        acqEng.setSequenceSettings(acqSettings);
        mm_.setAcquisitionEngine(acqEng);
        Datastore ds = null;
        try {
            ds = mm_.getAcquisitionEngine().acquire();
        } catch (MMException e) {
            e.printStackTrace();
        }
        return ds;
    }
}
