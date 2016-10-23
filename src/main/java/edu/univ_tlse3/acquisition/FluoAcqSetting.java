package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.maars.MaarsParameters;

import java.awt.*;
import java.util.ArrayList;

import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.internal.MMStudio;

import org.micromanager.acquisition.ChannelSpec;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAcqSetting {
    private String channelGroup_;
    private double zRange_;
    private double zStep_;
    private String savingRoot_;
    private MaarsParameters parameters_;

    public FluoAcqSetting(MaarsParameters parameters) {
        channelGroup_ = parameters.getChannelGroup();
        zRange_ = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
        zStep_ = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
        savingRoot_ = parameters.getSavingPath();
        parameters_ = parameters;
    }

    public ArrayList<ChannelSpec> configChannels(String channelName){
        ArrayList<ChannelSpec> channelSpecs = new ArrayList<ChannelSpec>();
        Color chColor = MaarsParameters.getColor(parameters_.getChColor(channelName));
        double chExpose = Double.parseDouble(parameters_.getChExposure(channelName));
        ChannelSpec channel_spec = new ChannelSpec();
        channel_spec.config = channelName;
        channel_spec.color = chColor;
        channel_spec.exposure = chExpose;
        channel_spec.doZStack = true;
        channelSpecs.add(channel_spec);
        return channelSpecs;
    }

    public SequenceSettings configAcqSettings(ArrayList<ChannelSpec> channelSpecs){
        SequenceSettings fluoAcqSetting = new SequenceSettings();
        Double interval = Double.parseDouble(parameters_.getFluoParameter(MaarsParameters.TIME_INTERVAL));
        Double duration = Double.parseDouble(parameters_.getFluoParameter(MaarsParameters.TIME_LIMIT));
        fluoAcqSetting.save = Boolean
                .parseBoolean(parameters_.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES));
        fluoAcqSetting.prefix = channelSpecs.get(0).config;
        fluoAcqSetting.root = savingRoot_;
        fluoAcqSetting.channels = channelSpecs;
        fluoAcqSetting.shouldDisplayImages= false;
        fluoAcqSetting.keepShutterOpenSlices = false;
        fluoAcqSetting.keepShutterOpenChannels = false;
        fluoAcqSetting.channelGroup = channelGroup_;
        fluoAcqSetting.slicesFirst = true;
        fluoAcqSetting.intervalMs = interval;
        fluoAcqSetting.numFrames = (int) Math.round(duration/interval);
        return fluoAcqSetting;
    }

    public AcquisitionWrapperEngine buildFluoAcqEngine(SequenceSettings fluoAcqSettings, MMStudio mm){
        AcquisitionWrapperEngine acqEng = mm.getAcquisitionEngine();
        acqEng.setSequenceSettings(fluoAcqSettings);
        acqEng.enableZSliceSetting(true);
        acqEng.setSlices(-zRange_/2,zRange_/2,zStep_,false);
        return acqEng;
    }
}
