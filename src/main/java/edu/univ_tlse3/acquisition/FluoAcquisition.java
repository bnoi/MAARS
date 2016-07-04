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
	public FluoAcquisition(MMStudio mm, CMMCore mmc) {
		super(mm, mmc);
	}

	public ImagePlus acquire(String channelName, double zFocus) {
		List<Image> listImg = super.acquire(channelName, zFocus);
		return super.convert2Imp(listImg, channelName, step); 
	}
}
