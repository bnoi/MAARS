//package maars.acquisition;
//
//import maars.io.IOUtils;
//import org.micromanager.data.Datastore;
//import org.micromanager.internal.MMStudio;
//
//import java.io.IOException;
//import java.util.concurrent.Callable;
//
///**
// * Only called with On-the-Fly analysis of MaarsOTFSeg.
// * Created by Tong LI on 24/06/2016.
// */
//public class MAARS_mda implements Callable<Datastore> {
//   private String pathToAcqSetting_;
//   /**
//    * run a MDA acquisition with setting
//    *
//    * @param pathToAcqSetting path to acq setting txt
//    */
//   public MAARS_mda(String pathToAcqSetting) {
//      pathToAcqSetting_ = pathToAcqSetting;
//   }
//
//   @Override
//   public Datastore call() throws Exception {
//      MMStudio mm = MMStudio.getInstance();
//      try {
//         return mm.acquisitions().runAcquisitionWithSettings(mm.acquisitions().loadSequenceSettings(pathToAcqSetting_),true);
//      } catch (IOException e) {
//         IOUtils.printErrorToIJLog(e);
//      }
//      return null;
//   }
//}
