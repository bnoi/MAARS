package org.micromanager.maarslib;

import org.micromanager.utils.ImgUtils;

import ij.ImagePlus;
import net.imglib2.util.ImgUtil;

/**
 *@author Tong LI, mail:tongli.bioinfo@gmail.com
 *@version Nov 19, 2015
 */
public class FluoAnalyzer extends Thread {

	private MaarsFluoAnalysis mfa;
	private String channel;
	private int frame;
	private ImagePlus fluoImage;
	
	public FluoAnalyzer(MaarsFluoAnalysis mfa, ImagePlus fluoImage, String channel, int frame){
		this.mfa = mfa;
		this.fluoImage = fluoImage;
		this.channel = channel;
		this.frame = frame;
	}
	
	public void run(){
		mfa.setFluoImage(ImgUtils.zProject(fluoImage));
		mfa.createCellChannelFactory(channel);
		mfa.setCurrentFrame(frame);
		mfa.cropAllCells();
		mfa.analyzeEachCell();
	}
}
