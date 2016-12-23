package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.maars.MaarsParameters;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.internal.MMStudio;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class SegAcqSetting {
   private String channelGroup;
   private String ch;
   private double zRange;
   private double zStep;
   private Color chColor;
   private double chExpose;
   private String savingRoot;

   public SegAcqSetting(MaarsParameters parameters) {
      this.ch = parameters.getSegmentationParameter(MaarsParameters.CHANNEL);
      this.channelGroup = parameters.getChannelGroup();
      this.zRange = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
      this.zStep = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
      this.chColor = MaarsParameters.getColor(parameters.getChColor(ch));
      this.chExpose = Double.parseDouble(parameters.getChExposure(ch));
      this.savingRoot = parameters.getSavingPath();
   }

   public ArrayList<ChannelSpec> configChannels() {
      ArrayList<ChannelSpec> channelSetting = new ArrayList<>();
      ChannelSpec bf_spec = new ChannelSpec();
      bf_spec.config = ch;
      bf_spec.color = chColor;
      bf_spec.exposure = chExpose;
      bf_spec.doZStack = true;
      bf_spec.useChannel = true;
      channelSetting.add(bf_spec);
      return channelSetting;
   }

   public SequenceSettings configAcqSettings(ArrayList<ChannelSpec> channelSetting) {
      SequenceSettings segAcqSettings = new SequenceSettings();
      segAcqSettings.save = true;
      segAcqSettings.numFrames = 1;
      segAcqSettings.usePositionList = false;
      segAcqSettings.useAutofocus = false;
      segAcqSettings.prefix = "";
      segAcqSettings.root = this.savingRoot;
      segAcqSettings.keepShutterOpenSlices = true;
      segAcqSettings.keepShutterOpenChannels = true;
      segAcqSettings.channels = channelSetting;
      segAcqSettings.shouldDisplayImages = true;
      segAcqSettings.channelGroup = channelGroup;
      return segAcqSettings;
   }

   public AcquisitionWrapperEngine buildSegAcqEngine(SequenceSettings segAcqSettings, MMStudio mm) {
      AcquisitionWrapperEngine acqEng = mm.getAcquisitionEngine();
      acqEng.setSequenceSettings(segAcqSettings);
      acqEng.enableZSliceSetting(true);
      acqEng.setSlices(-zRange / 2, zRange / 2, zStep, false);
      return acqEng;
   }
}
