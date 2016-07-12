package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.maars.MaarsParameters;
import mmcorej.CMMCore;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.micromanager.SequenceSettings;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import ij.ImagePlus;
import org.micromanager.internal.utils.ChannelSpec;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAcquisition extends SuperClassAcquisition {
    private String channelGroup;
    private String ch;
    private double zRange;
    private double zStep;
    private Color chColor;
    private double chExpose;
    private String savingRoot;

	public FluoAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters) {
		super(mm, mmc);
        this.channelGroup = parameters.getChannelGroup();
        this.zRange = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
        this.zStep = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
        this.savingRoot = parameters.getSavingPath() + "_FLUO";
	}

    public ArrayList<Double> computZSlices(double zFocus){
        return SuperClassAcquisition.computZSlices(zRange,zStep,zFocus);
    }

	public SequenceSettings buildSeqSetting(String frame,  String channel, MaarsParameters parameters, ArrayList<Double> slices, boolean save){
        this.ch = channel;
        this.chColor = MaarsParameters.getColor(parameters.getChColor(ch));
        this.chExpose = Double.parseDouble(parameters.getChExposure(ch));

        ArrayList<ChannelSpec> channelSetting = new ArrayList<ChannelSpec>();
        ChannelSpec channel_spec = new ChannelSpec();
        channel_spec.config = ch;
        channel_spec.color = chColor;
        channel_spec.exposure = chExpose;
        channelSetting.add(channel_spec);

        SequenceSettings fluoAcqSetting = new SequenceSettings();
        fluoAcqSetting.save = save;
        fluoAcqSetting.prefix = frame;
        fluoAcqSetting.root = this.savingRoot;
        fluoAcqSetting.slices = slices;
        fluoAcqSetting.channels = channelSetting;
        return fluoAcqSetting;
    }


    public ImagePlus acquire(SequenceSettings acqSettings) {
        List<Image> listImg = super.acquire(acqSettings, channelGroup);
        return super.convert2Imp(listImg, this.ch);
    }
}
