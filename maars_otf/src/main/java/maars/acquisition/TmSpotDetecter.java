package maars.acquisition;

import ij.IJ;
import maars.agents.SocSet;
import org.micromanager.internal.MMStudio;

/**
 * Created by tong on 23/06/17.
 */
public class TmSpotDetecter implements Runnable {
   SocSet socSet_;
   int counter = 0;
   public TmSpotDetecter(SocSet socSet){
      socSet_ = socSet;
   }
   @Override
   public void run() {
      MMStudio mm = MMStudio.getInstance();
      IJ.log(mm.getPositionList().getPosition(counter).getLabel() +" ");
      IJ.log(socSet_.getSoc(mm.getPositionList().getPosition(counter).getLabel()).getPosLabel() + "");
      counter++;
//      List<Image> lImages = mm.displays().getCurrentWindow().getDisplayedImages();
//      lImages.subList(Math.max(lImages.size()-7,0),lImages.size());
   }
}
