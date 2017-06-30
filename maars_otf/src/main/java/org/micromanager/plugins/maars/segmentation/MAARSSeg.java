package org.micromanager.plugins.maars.segmentation;

import ij.ImagePlus;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.mmUtils.ImgUtils;
import org.json.JSONException;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;
import org.micromanager.internal.MMStudio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tong on 26/06/17.
 */
public class MAARSSeg extends Processor{
   private MaarsParameters parameters_;
   private MMStudio mm_ = MMStudio.getInstance();
   private List<Image> zstack_ = new ArrayList<>();
   private int counter_ = 0;
   private String pos_ = "Pos0";

   public MAARSSeg(MaarsParameters parameters){
      parameters_ = parameters;
   }

   @Override
   public void processImage(Image image, ProcessorContext processorContext) {
      boolean isOk = IsOkToSegmentCells();
      counter_++;
      zstack_.add(image);
      processorContext.outputImage(image);
      if (counter_ == mm_.acquisitions().getAcquisitionSettings().slices.size() && isOk){
         String prefix = "";
         try {
            prefix = mm_.getAcquisitionEngine2010().getSummaryMetadata().getString("Prefix");
         } catch (JSONException e) {
            e.printStackTrace();
         }
         parameters_.setSegmentationParameter(MaarsParameters.SEG_PREFIX,  prefix);
         ImagePlus imp = ImgUtils.convertWithMetadata(zstack_, mm_.getCachedPixelSizeUm());
         ExecutorService es = Executors.newSingleThreadExecutor();
         if (mm_.positions().getPositionList().getPositions().length>0){
            pos_= mm_.positions().getPositionList().getPosition(image.getCoords().getStagePosition()).getLabel();
         }
         try {
            es.submit(new MaarsSegmentation(parameters_, imp, pos_)).get();
         } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
         }
         es.shutdown();
         counter_ = 0;
         zstack_ = new ArrayList<>();
         parameters_.save(parameters_.getSavingPath());
      }
   }

   public Boolean IsOkToSegmentCells() {
      boolean isOk = true;
      if (mm_.live().getIsLiveModeOn()){
         mm_.logs().showError("MAARS segmentation is not designed for live streaming, please launch MDA acquisition.");
         mm_.live().setLiveMode(false);
         isOk = false;
      }else if (!mm_.acquisitions().getAcquisitionSettings().save){
         mm_.logs().showError("You need to save your images to perform segmentation.");
         mm_.acquisitions().setPause(true);
         mm_.acquisitions().haltAcquisition();
         isOk = false;
      }
      return isOk;
   }
}
