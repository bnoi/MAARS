package org.micromanager.plugins.maars.fluoanalysis;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import maars.cellAnalysis.FluoAnalyzer;
import maars.main.MaarsParameters;
import maars.mmUtils.ImgMMUtils;
import maars.utils.ImgUtils;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;
import org.micromanager.internal.MMStudio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tongli on 27/06/2017.
 */
public class MAARSFluoAnalysis extends Processor {
   private MaarsParameters parameters_;
   private MMStudio mm_ = MMStudio.getInstance();
   private Calibration cal_ = new Calibration();
   private String pos_ = "Pos0";
   private HashMap<String, ArrayList<Image>> chImages= new HashMap<>();
   MAARSFluoAnalysis(MaarsParameters parameters){
      parameters_ = parameters;
      SequenceSettings seqSetting = mm_.getAcquisitionManager().getAcquisitionSettings();
      for (ChannelSpec chspc : seqSetting.channels){
         if (chspc.useChannel){
            chImages.put(chspc.config, new ArrayList<>());
         }
      }
      cal_.pixelDepth = Math.abs(seqSetting.slices.get(0) - seqSetting.slices.get(1));
      cal_.pixelWidth = mm_.getCore().getPixelSizeUm();
      cal_.pixelHeight = mm_.getCore().getPixelSizeUm();
      cal_.frameInterval = seqSetting.intervalMs;
   }
   @Override
   public void processImage(Image image, ProcessorContext processorContext) {
      boolean isOk = ImgMMUtils.IsOkToProceed();
      String currentCh = null;
      try {
         currentCh = mm_.getCMMCore().getCurrentConfig("Channel");
      } catch (Exception e) {
         IJ.error("Your group name of channels is unknown.");
      }

      ArrayList<Image> currentChImgs = chImages.get(currentCh);
      currentChImgs.add(image);
      processorContext.outputImage(image);
      ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      if (currentChImgs.size() == mm_.acquisitions().getAcquisitionSettings().slices.size() && isOk){
         chImages.get(currentCh).clear();
         ImagePlus imp = ImgMMUtils.convertWithMetadata(currentChImgs, mm_.getCachedPixelSizeUm());
         ImagePlus zProjectedFluoImg = ImgUtils.zProject(imp, cal_);
         es.submit(new FluoAnalyzer(zProjectedFluoImg, cal_,
               soc, currentCh, Integer.parseInt(parameters_.getChMaxNbSpot(currentCh)),
               Double.parseDouble(parameters_.getChSpotRaius(currentCh)),
               Double.parseDouble(parameters_.getChQuality(currentCh)), image.getCoords().getTime(), socVisualizer,
               parameters_.useDynamic())).get();
         //         String prefix = "";
//         prefix = mm_.getAcquisitionEngine2010().getSummaryMetadata().getString("Prefix");
//         parameters_.setSegmentationParameter(MaarsParameters.SEG_PREFIX,  prefix);
//         ImagePlus imp = ImgMMUtils.convertWithMetadata(zstack_, mm_.getCachedPixelSizeUm());
//         ExecutorService es = Executors.newSingleThreadExecutor();
//         if (mm_.positions().getPositionList().getPositions().length>0){
//            pos_= mm_.positions().getPositionList().getPosition(image.getCoords().getStagePosition()).getLabel();
//         }
//         es.submit(new MaarsSegmentation(parameters_, imp, pos_)).get();
         es.shutdown();
//         parameters_.save(parameters_.getSavingPath());
      }
   }
}
