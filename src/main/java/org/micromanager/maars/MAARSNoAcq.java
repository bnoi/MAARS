package org.micromanager.maars;

import org.micromanager.AutofocusPlugin;
import org.micromanager.acquisition.FluoAcquisition;
import org.micromanager.acquisition.SegAcquisition;
import org.micromanager.internal.MMStudio;
import org.micromanager.maarslib.ExplorationXYPositions;
import org.micromanager.maarslib.FluoAnalyzer;
import org.micromanager.maarslib.MaarsFluoAnalysis;
import org.micromanager.maarslib.MaarsSegmentation;
import org.micromanager.utils.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import mmcorej.CMMCore;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq {
	public MAARSNoAcq(MMStudio mm, CMMCore mmc, MaarsParameters parameters) {
		// Start time
		long start = System.currentTimeMillis();

		// Acquisition path arrangement
		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);

		for (int i = 0; i < explo.length(); i++) {
			System.out.println("x : " + explo.getX(i) + " y : " + explo.getY(i));
			double xPos = explo.getX(i);
			double yPos = explo.getY(i);

			String pathToSegDir = FileUtils.convertPath(
					parameters.getSavingPath() + "/movie_X" + Math.round(xPos) + "_Y" + Math.round(yPos) + "/");
			String pathToSegMovie = FileUtils.convertPath(pathToSegDir + "MMStack.ome.tif");
			ImagePlus segImg = null;
			try{
				segImg = IJ.openImage(pathToSegMovie);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Invalid path");
			}
			// --------------------------segmentation-----------------------------//
			MaarsSegmentation ms = new MaarsSegmentation(parameters, xPos, yPos);
			ms.segmentation(segImg);
			if (ms.roiDetected()) {
				// ----------------if got ROI, start fluo-acquisition --------//
				MaarsFluoAnalysis mfa = new MaarsFluoAnalysis(parameters, ms.getSegPombeParam(), xPos, yPos);
				////////////////////////// multiple snapshot per
				////////////////////////// field//////////////////////////////
				if (parameters.useDynamic()) {
					double timeInterval = Double
							.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
					double startTime = System.currentTimeMillis();
					int frame = 0;
					double timeLimit = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT)) * 60
							* 1000;
					while (System.currentTimeMillis() - startTime <= timeLimit) {
						double beginAcq = System.currentTimeMillis();

						String channels = parameters.getUsingChannels();
						String[] arrayChannels = channels.split(",", -1);
						for (String channel : arrayChannels) {
							String pathToFluoMovie = parameters.getSavingPath() + "movie_X" + Math.round(xPos) + "_Y"
									+ Math.round(yPos) + "_FLUO/" + frame + "_" + channel;
							ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
							new FluoAnalyzer(mfa, fluoImage, channel, frame).start();
						}
						mfa.getSetOfCells().closeRoiManager();
						frame++;
						double acqTook = System.currentTimeMillis() - beginAcq;
						try {
							Thread.sleep((long) (timeInterval - acqTook));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//////////////////////////// one snapshot per
					//////////////////////////// field/////////////////////////////////
				} else {
					int frame = 0;
					String channels = parameters.getUsingChannels();
					String[] arrayChannels = channels.split(",", -1);
					for (String channel : arrayChannels) {
						String pathToFluoMovie = parameters.getSavingPath() + "movie_X" + Math.round(xPos) + "_Y"
								+ Math.round(yPos) + "_FLUO/" + frame + "_" + channel;
						ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
						new FluoAnalyzer(mfa, fluoImage, channel, frame).start();
					}
					mfa.getSetOfCells().closeRoiManager();
				}
				/////////////////////////// save cropped
				/////////////////////////// images//////////////////////////////////////
				if (Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES))) {
					mfa.saveCroppedImgs();
				}
				// close roi manager
				// mfa.getSetOfCells().closeRoiManager();
			}
		}
		mmc.setAutoShutter(true);
		try {
			mmc.waitForDevice(mmc.getShutterDevice());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("it took " + (System.currentTimeMillis() - start));
		System.out.println("DONE.");

	}
}
