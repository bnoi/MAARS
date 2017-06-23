package maars.acquisition;

import maars.agents.SetOfCells;
import maars.io.IOUtils;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Only called with On-the-Fly analysis of MAARS.
 * Created by Tong LI on 24/06/2016.
 */
public class MAARS_mda implements Callable<Datastore> {
   private String pathToAcqSetting_;
   private SetOfCells soc_=null;
   /**
    * run a MDA acquisition with setting
    *
    * @param pathToAcqSetting path to acq setting txt
    */
   public MAARS_mda(String pathToAcqSetting) {
      pathToAcqSetting_ = pathToAcqSetting;
   }

   public MAARS_mda(String pathToAcqSetting, SetOfCells soc){
      pathToAcqSetting_ = pathToAcqSetting;
      soc_ = soc;
   }

   @Override
   public Datastore call() throws Exception {
      MMStudio mm = MMStudio.getInstance();
      TmSpotDetecter detecter = null;
      if (soc_!=null) {
         detecter = new TmSpotDetecter(soc_);
      }
      try {
         if (detecter != null) {
            mm.acquisitions().attachRunnable(0, -1, 0, 0, detecter);
         }
         return mm.acquisitions().runAcquisitionWithSettings(mm.acquisitions().loadSequenceSettings(pathToAcqSetting_),true);
      } catch (IOException e) {
         IOUtils.printErrorToIJLog(e);
      }
      return null;
   }
}
