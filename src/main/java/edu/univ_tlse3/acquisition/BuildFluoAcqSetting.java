package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.maars.MaarsParameters;
import mmcorej.CMMCore;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.internal.MMStudio;

import ij.ImagePlus;
import org.micromanager.acquisition.ChannelSpec;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class BuildFluoAcqSetting extends SuperClassAcquisition {
    private String channelGroup;
    private double zRange;
    private double zStep;
    private String savingRoot;

	public BuildFluoAcqSetting(MMStudio mm, CMMCore mmc, MaarsParameters parameters) {
		super(mm, mmc);
        this.channelGroup = parameters.getChannelGroup();
        this.zRange = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
        this.zStep = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
        this.savingRoot = parameters.getSavingPath();
	}

    public ArrayList<Double> computZSlices(double zFocus){
        return SuperClassAcquisition.computZSlices(zRange,zStep,zFocus);
    }

	public SequenceSettings buildSeqSetting(MaarsParameters parameters, ArrayList<Double> slices){
        ArrayList<String> arrayChannels = new ArrayList<String>();
        Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));

        ArrayList<ChannelSpec> channelSetting = new ArrayList<ChannelSpec>();
        Color chColor;
        double chExpose;
        for (String ch : arrayChannels){
            chColor = MaarsParameters.getColor(parameters.getChColor(ch));
            chExpose = Double.parseDouble(parameters.getChExposure(ch));
            ChannelSpec channel_spec = new ChannelSpec();
            channel_spec.config = ch;
            channel_spec.color = chColor;
            channel_spec.exposure = chExpose;
            channelSetting.add(channel_spec);
        }

        SequenceSettings fluoAcqSetting = new SequenceSettings();
        Double interval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
        fluoAcqSetting.save = Boolean
                .parseBoolean(parameters.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES));
        fluoAcqSetting.prefix = "";
        fluoAcqSetting.root = this.savingRoot;
        fluoAcqSetting.slices = slices;
        fluoAcqSetting.channels = channelSetting;
        fluoAcqSetting.shouldDisplayImages= true;
        fluoAcqSetting.keepShutterOpenSlices = true;
        fluoAcqSetting.keepShutterOpenChannels = false;
        fluoAcqSetting.channelGroup = channelGroup;
        fluoAcqSetting.slicesFirst = true;
        fluoAcqSetting.intervalMs = interval;
        fluoAcqSetting.numFrames = 90;
        return fluoAcqSetting;
    }

    public void acquireToImp(SequenceSettings acqSettings, MaarsParameters parameters) {
        //prepare executor for image analysis
        int nThread = Runtime.getRuntime().availableProcessors();
        ExecutorService es = Executors.newFixedThreadPool(nThread);
        ImagePlus fluoImage = super.acquire(acqSettings, parameters);
//        if (Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.DO_ANALYSIS))) {
//            es.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc, channel,
//                    Integer.parseInt(parameters.getChMaxNbSpot(channel)),
//                    Double.parseDouble(parameters.getChSpotRaius(channel)),
//                    Double.parseDouble(parameters.getChQuality(channel)), frame));
//        }
        es.shutdown();
        try {
            es.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
