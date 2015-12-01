package org.micromanager.acquisition;

import mmcorej.CMMCore;

import org.micromanager.internal.MMStudio;
import org.micromanager.maars.MaarsParameters;

import ij.ImagePlus;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class SegAcquisition extends SuperClassAcquisition {

	public SegAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters, double positionX, double positionY) {
		super(mm, mmc, parameters, positionX, positionY);
	}

	public ImagePlus acquire(String channelName) {
		return super.acquire(0, channelName);
	}
}
