package edu.univ_tlse3.acquisition;

import edu.univ_tlse3.maars.MaarsParameters;
import mmcorej.CMMCore;

import java.util.List;

import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import ij.ImagePlus;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAcquisition extends SuperClassAcquisition {
	private double step;
	public FluoAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters) {
		super(mm, mmc, parameters);
		this.step = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
	}

	public ImagePlus acquire(int frame, String channelName, Boolean save) {
		List<Image> listImg = super.acquire(channelName);
		if (save){
			
			super.save(listImg, frame, channelName, step);
		}
		return super.convert2Imp(listImg, channelName, step); 
	}
}
