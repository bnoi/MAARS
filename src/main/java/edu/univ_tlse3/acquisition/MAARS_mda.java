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
        //acqSettings.usePositionList = true;
        mm_.getAcquisitionEngine().setSequenceSettings(acqSettings);
        mm_.getAcquisitionEngine().setChannelGroup(acqSettings.channelGroup);

//        if (do_analysis) {
//            es.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc, channel,
//                    Integer.parseInt(parameters.getChMaxNbSpot(channel)),
//                    Double.parseDouble(parameters.getChSpotRaius(channel)),
//                    Double.parseDouble(parameters.getChQuality(channel)), frame));
//        }
//        Datastore ds = mm_.getAcquisitionManager().runAcquisition(acqSettings.prefix, acqSettings.root);
        Datastore ds = null;
        try {
            ds = mm_.getAcquisitionEngine().acquire();
        } catch (MMException e) {
            e.printStackTrace();
        }
        while (mm_.getAcquisitionEngine().isAcquisitionRunning()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mm_.getAcquisitionEngine().shutdown();
        return ds;
    }
}
