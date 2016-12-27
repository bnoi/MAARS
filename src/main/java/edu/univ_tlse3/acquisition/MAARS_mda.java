package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.utils.IOUtils;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.Datastore;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.MMException;

/**
 * Created by Tong LI on 24/06/2016.
 *
 */
class MAARS_mda {
   private MMStudio mm_;

   MAARS_mda(MMStudio mm) {
      mm_ = mm;
   }

   Datastore acquire(AcquisitionWrapperEngine acqEng) {
      mm_.setAcquisitionEngine(acqEng);
      Datastore ds = null;
      try {
         ds = mm_.getAcquisitionEngine().acquire();
      } catch (MMException e) {
         IOUtils.printErrorToIJLog(e);
      }
      while (mm_.getAcquisitionEngine().isAcquisitionRunning()) {
         mm_.getCore().sleep(1000);
      }
      return ds;
   }
}
