package maars.acquisition;

import ij.IJ;
import maars.agents.SetOfCells;
import maars.io.IOUtils;
import maars.main.MaarsParameters;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tong on 23/06/17.
 */
public class TmSpotDetecter implements Runnable {
   SetOfCells soc_;
   public TmSpotDetecter(SetOfCells soc){
      soc_ = soc;
   }
   @Override
   public void run() {
      IJ.log("123123");
//      List<Image> lImages = MMStudio.getInstance().displays().getCurrentWindow().getDisplayedImages();
//      lImages.subList(Math.max(lImages.size()-7,0),lImages.size());
   }
}
