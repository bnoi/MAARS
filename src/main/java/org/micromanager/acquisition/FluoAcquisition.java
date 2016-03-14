package org.micromanager.acquisition;

import mmcorej.CMMCore;

import org.micromanager.internal.MMStudio;
import org.micromanager.maars.MaarsParameters;

import ij.ImagePlus;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAcquisition extends SuperClassAcquisition {

	public FluoAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters, String positionX, String positionY) {
		super(mm, mmc, parameters, positionX, positionY);
	}

	public ImagePlus acquire(int frame, String channelName, double zFocus) {
		return super.acquire(frame, channelName, zFocus, true);
	}
}
