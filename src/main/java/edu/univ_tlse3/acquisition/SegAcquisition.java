package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.maars.MaarsParameters;
import ij.ImagePlus;
import mmcorej.CMMCore;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import org.micromanager.acquisition.ChannelSpec;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class SegAcquisition extends SuperClassAcquisition {
    private String channelGroup;
    private String ch;
    private double zRange;
    private double zStep;
    private Color chColor;
    private double chExpose;
    private String savingRoot;
    private ArrayList<ChannelSpec> channelSetting;

	public SegAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters) {
		super(mm, mmc);
		this.ch = parameters.getSegmentationParameter(MaarsParameters.CHANNEL);
		this.channelGroup = parameters.getChannelGroup();
        this.zRange = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
        this.zStep = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
        this.chColor = MaarsParameters.getColor(parameters.getChColor(ch));
        this.chExpose = Double.parseDouble(parameters.getChExposure(ch));
        this.savingRoot = parameters.getSavingPath();

        channelSetting = new ArrayList<ChannelSpec>();
        ChannelSpec bf_spec = new ChannelSpec();
        bf_spec.config = ch;
        bf_spec.color = chColor;
        bf_spec.exposure = chExpose;
        channelSetting.add(bf_spec);
	}

    public ArrayList<Double> computZSlices(double zFocus){
        return SuperClassAcquisition.computZSlices(zRange,zStep,zFocus);
    }

    public SequenceSettings buildSeqSetting(ArrayList<Double> slices, boolean save) {
        SequenceSettings segAcqSettings = new SequenceSettings();
		segAcqSettings.save = save;
		segAcqSettings.prefix = "";
		segAcqSettings.root = this.savingRoot;
        segAcqSettings.keepShutterOpenSlices = true;
		segAcqSettings.slices = slices;
		segAcqSettings.channels = this.channelSetting;
        segAcqSettings.shouldDisplayImages= true;
        segAcqSettings.channelGroup = channelGroup;
        return segAcqSettings;
    }

	public ImagePlus acquireToImp(SequenceSettings acqSettings) {
		List<Image> listImg = super.acquire(acqSettings);
		return super.convert2Imp(listImg, this.ch);
	}
}
