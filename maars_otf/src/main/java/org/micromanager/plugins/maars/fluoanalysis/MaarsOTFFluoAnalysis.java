package org.micromanager.plugins.maars.fluoanalysis;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import maars.agents.DefaultSetOfCells;
import maars.cellAnalysis.FluoAnalyzer;
import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import maars.mmUtils.ImgMMUtils;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;
import org.micromanager.internal.MMStudio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tongli on 27/06/2017.
 */
public class MaarsOTFFluoAnalysis extends Processor {
   private MaarsParameters parameters_;
   private MMStudio mm_ = MMStudio.getInstance();
   private Calibration cal_ = new Calibration();
   private HashMap<String, ArrayList<Image>> chImages= new HashMap<>();
   private HashMap<String, DefaultSetOfCells> posSoc_ = new HashMap<>();
   private ExecutorService es_ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
   MaarsOTFFluoAnalysis(MaarsParameters parameters){
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
      String currentPos = mm_.getPositionList().getPosition(image.getCoords().getStagePosition()).getLabel();
      try {
         currentCh = mm_.getCMMCore().getCurrentConfig("Channel");
      } catch (Exception e) {
         IJ.error("Your group name of channels is unknown.");
      }
      if (!posSoc_.containsKey(currentPos)){
         DefaultSetOfCells defaultSetOfCells = new DefaultSetOfCells(currentPos);
         String segAnaDir = FileUtils.convertPath(parameters_.getSavingPath()) + File.separator +
               parameters_.getSegmentationParameter(MaarsParameters.SEG_PREFIX) + Maars_Interface.SEGANALYSIS_SUFFIX;
         String currentPosPrefix = segAnaDir + currentPos + File.separator;
         String currentZipPath = currentPosPrefix + "ROI.zip";
         defaultSetOfCells.loadCells(currentZipPath);
         posSoc_.put(currentPos, defaultSetOfCells);
      }
      ArrayList<Image> currentChImgs = chImages.get(currentCh);
      currentChImgs.add(image);
      processorContext.outputImage(image);

      if (currentChImgs.size() == mm_.acquisitions().getAcquisitionSettings().slices.size() && isOk){
         chImages.get(currentCh).clear();
         ImagePlus imp = ImgMMUtils.convertWithMetadata(currentChImgs, mm_.getCachedPixelSizeUm());
         ImagePlus zProjectedFluoImg = ImgUtils.zProject(imp, cal_);
         try {
            es_.submit(new FluoAnalyzer(zProjectedFluoImg, cal_,
                  posSoc_.get(currentPos), currentCh, Integer.parseInt(parameters_.getChMaxNbSpot(currentCh)),
                  Double.parseDouble(parameters_.getChSpotRaius(currentCh)),
                  Double.parseDouble(parameters_.getChQuality(currentCh)), image.getCoords().getTime(),
                  null, parameters_.useDynamic())).get();
         } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
         }
         es_.shutdown();
      }
   }
}
