package org.micromanager.acquisition;

import mmcorej.CMMCore;

import java.util.List;

import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;
import org.micromanager.maars.MaarsParameters;

import ij.ImagePlus;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAcquisition extends SuperClassAcquisition {
	private double step;
	public FluoAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters, String positionX, String positionY) {
		super(mm, mmc, parameters, positionX, positionY);
		this.step = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
	}

	public ImagePlus acquire(int frame, String channelName, double zFocus, String pathToFluoMovie, Boolean save) {
		List<Image> listImg = super.acquire(frame, channelName, zFocus);
		if (save){
			
			super.save(listImg, frame, channelName, step, pathToFluoMovie);
		}
		return super.convert2Imp(listImg, channelName, step); 
	}
}
