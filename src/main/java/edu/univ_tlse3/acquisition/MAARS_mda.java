package edu.univ_tlse3.acquisition;
import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.FileUtils;
import org.micromanager.acquisition.SequenceSettings;
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
    public Datastore acquire(SequenceSettings acqSettings, MaarsParameters parameters) {
        FileUtils.createFolder(acqSettings.root);
        mm_.getAcquisitionEngine().setSequenceSettings(acqSettings);
        mm_.getAcquisitionEngine().setChannelGroup(acqSettings.channelGroup);
        Datastore ds = null;
        try {
            ds = mm_.getAcquisitionEngine().acquire();
        } catch (MMException e) {
            e.printStackTrace();
        }
        mm_.getAcquisitionEngine().shutdown();
        return ds;
    }
}
