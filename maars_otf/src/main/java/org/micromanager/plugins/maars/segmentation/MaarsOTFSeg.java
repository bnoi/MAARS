package org.micromanager.plugins.maars.segmentation;

import ij.ImagePlus;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.mmUtils.ImgMMUtils;
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
public class MaarsOTFSeg extends Processor{
   private MaarsParameters parameters_;
   private MMStudio mm_ = MMStudio.getInstance();
   private List<Image> zstack_ = new ArrayList<>();
   private int counter_ = 0;
   private String pos_ = "Pos0";

   MaarsOTFSeg(MaarsParameters parameters){
      parameters_ = parameters;
   }

   @Override
   public void processImage(Image image, ProcessorContext processorContext) {
      boolean isOk = ImgMMUtils.IsOkToProceed();
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
         ImagePlus imp = ImgMMUtils.convertWithMetadata(zstack_, mm_.getCachedPixelSizeUm());
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
}
