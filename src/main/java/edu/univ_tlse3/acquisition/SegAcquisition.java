package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.maars.MaarsParameters;
import mmcorej.CMMCore;

import java.util.ArrayList;
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
public class SegAcquisition extends SuperClassAcquisition {
    private String channelGroup;
    private String bf;
	public SegAcquisition(MMStudio mm, CMMCore mmc) {
		super(mm, mmc);
	}

    public SequenceSettings buildSeqSetting(MaarsParameters parameters, double zFocus) {
        this.bf = parameters.getSegmentationParameter(MaarsParameters.CHANNEL);
        this.channelGroup = parameters.getChannelGroup();
        double range = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
        double step = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
        double z = zFocus - (range / 2);

        int sliceNumber = (int) Math.round(range / step);
        ArrayList<Double> slices = new ArrayList<Double>();
        for (int k = 0; k <= sliceNumber; k++) {
            slices.add(z);
            z = z + step;
        }

        ArrayList<ChannelSpec> channels = new ArrayList<ChannelSpec>();
        ChannelSpec bf_spec = new ChannelSpec();
        bf_spec.config = bf;
        bf_spec.color = MaarsParameters.getColor(parameters.getChColor(bf));
        bf_spec.exposure = Double.parseDouble(parameters.getChExposure(bf));
        channels.add(bf_spec);

        SequenceSettings acqSettings = new SequenceSettings();
        acqSettings.save = true;
        acqSettings.prefix = "";
        acqSettings.root = parameters.getSavingPath();
        acqSettings.keepShutterOpenChannels = true;
        acqSettings.slices = slices;
        acqSettings.channels = channels;
        return acqSettings;
    }

	public ImagePlus acquire(SequenceSettings acqSettings) {
		List<Image> listImg = super.acquire(acqSettings, channelGroup);
		return super.convert2Imp(listImg, this.bf);
	}
}
