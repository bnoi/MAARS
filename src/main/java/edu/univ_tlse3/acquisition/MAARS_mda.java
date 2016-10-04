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
    SequenceSettings acqSettings_;
    public MAARS_mda(MMStudio mm, SequenceSettings acqSettings) {
        mm_ = mm;
        acqSettings_ = acqSettings;
    }
    public Datastore acquire() {
        FileUtils.createFolder(acqSettings_.root);
        mm_.getAcquisitionManager().runAcquisitionWithSettings(acqSettings_, true);
        Datastore ds = mm_.getAcquisitionManager().runAcquisition();
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
