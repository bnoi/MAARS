package org.micromanager.maars;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.maarslib.ExplorationXYPositions;
import org.micromanager.maarslib.MaarsSegmentation;
import org.micromanager.utils.FileUtils;
import org.micromanager.utils.ImgUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import mmcorej.CMMCore;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq {
	public MAARSNoAcq(CMMCore mmc, MaarsParameters parameters, SetOfCells soc) {
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
				// from Roi initialize a set of cell
				soc.loadCells(ms.getSegPombeParam());
				// Get the focus slice of BF image
				Calibration bfImgCal = segImg.getCalibration();
				ImagePlus focusImage = new ImagePlus(segImg.getShortTitle(),
						segImg.getStack().getProcessor(ms.getSegPombeParam().getFocusSlide()));
				focusImage.setCalibration(bfImgCal);
				// measure parameters of ROI
				for (Cell cell : soc) {
					cell.setFocusImage(ImgUtils.cropImgWithRoi(focusImage, cell.getCellShapeRoi()));
					cell.measureBfRoi();
				}
				// ----------------start acquisition and analysis --------//
				try {
					PrintStream ps = new PrintStream(parameters.getSavingPath() + "CellStateAnalysis.LOG");
					System.setOut(ps);
					System.setErr(ps);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				int frame = 0;
				while (frame < 1) {
					String channels = parameters.getUsingChannels();
					String[] arrayChannels = channels.split(",", -1);
					for (String channel : arrayChannels) {
						String pathToFluoMovie = parameters.getSavingPath() + "/movie_X" + Math.round(xPos) + "_Y"
								+ Math.round(yPos) + "_FLUO/" + frame + "_" + channel + "/MMStack.ome.tif";
						ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
						System.out.println(pathToFluoMovie);
						new FluoAnalyzer(parameters, fluoImage, bfImgCal, soc, channel, frame, xPos, yPos).start();
					}
					frame++;
				}
			}
		}
		System.out.println("it took " + (System.currentTimeMillis() - start));
		System.out.println("DONE.");

	}
}
