package org.micromanager.maarslib;

import ij.ImagePlus;

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
		mfa.setFluoImage(fluoImage);
		mfa.createCellChannelFactory(channel);
		mfa.setCurrentFrame(frame);
		mfa.zProject();
		mfa.cropAllCells();
		mfa.analyzeEachCell();
	}
}
