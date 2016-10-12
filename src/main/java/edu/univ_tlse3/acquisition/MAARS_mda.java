package edu.univ_tlse3.acquisition;
import edu.univ_tlse3.utils.FileUtils;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Datastore;
import org.micromanager.internal.MMStudio;

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
        //TODO there is a bug here, the acquisition manager do not read correctly my sequence setting
        mm_.getAcquisitionManager().setAcquisitionSettings(acqSettings);
        Datastore ds = mm_.getAcquisitionManager().runAcquisition(acqSettings.prefix, acqSettings.root);
        while (mm_.getAcquisitionEngine().isAcquisitionRunning()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ds;
    }
}
