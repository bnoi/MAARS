package org.micromanager.maars;

import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.maarslib.ExplorationXYPositions;
import org.micromanager.maarslib.FluoAnalyzer;
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
	public MAARSNoAcq(CMMCore mmc, MaarsParameters parameters) {
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
			try {
				segImg = IJ.openImage(pathToSegMovie);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Invalid path");
			}
			// --------------------------segmentation-----------------------------//
			MaarsSegmentation ms = new MaarsSegmentation(parameters, xPos, yPos);
			ms.segmentation(segImg);
			if (ms.roiDetected()) {
				SetOfCells soc = new SetOfCells(ms.getSegPombeParam());
				// ----------------if got ROI, start analysis --------//
				System.out.println("Initialize fluo analysis...");
				// MaarsFluoAnalysis mfa = new MaarsFluoAnalysis(parameters,
				// ms.getSegPombeParam(), xPos, yPos);
				int frame = 0;
				while (frame < 1) {
					String channels = parameters.getUsingChannels();
					String[] arrayChannels = channels.split(",", -1);
					for (String channel : arrayChannels) {
						String pathToFluoMovie = parameters.getSavingPath() + "/movie_X" + Math.round(xPos) + "_Y"
								+ Math.round(yPos) + "_FLUO/" + frame + "_" + channel + "/MMStack.ome.tif";
						ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
						System.out.println(pathToFluoMovie);
						new FluoAnalyzer(parameters, ms.getSegPombeParam(), fluoImage, segImg, soc, channel, frame,
								xPos, yPos).start();
					}
					frame++;
				}
			}
		}
		System.out.println("it took " + (System.currentTimeMillis() - start));
		System.out.println("DONE.");

	}
}
