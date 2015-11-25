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

	/**
	 * Constructor :
	 * 
	 * @param mm
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param positionX
	 *            : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY
	 *            : y field position (can be defined by ExplorationXYPositions)
	 */
	public SegAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters,
			double positionX, double positionY) {
		super(mm, mmc, parameters, positionX, positionY);
	}

	public ImagePlus acquire(String channelName) {
		return super.acquire(0, channelName);
	}
}
