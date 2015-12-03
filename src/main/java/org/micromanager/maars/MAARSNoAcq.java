package org.micromanager.maars;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.SetOfCells;
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
	private PrintStream curr_err;
	private PrintStream curr_out;

	public MAARSNoAcq(CMMCore mmc, MaarsParameters parameters, SetOfCells soc) {
		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		// Start time
		long start = System.currentTimeMillis();

		// Acquisition path arrangement
		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);

		for (int i = 0; i < explo.length(); i++) {
			System.out.println("x : " + explo.getX(i) + " y : " + explo.getY(i));
			String xPos = String.valueOf(Math.round(explo.getX(i)));
			String yPos = String.valueOf(Math.round(explo.getY(i)));

			ConcurrentHashMap<String, Object> acquisitionMeta = new ConcurrentHashMap<String, Object>();
			acquisitionMeta.put(MaarsParameters.X_POS, xPos);
			acquisitionMeta.put(MaarsParameters.Y_POS, yPos);

			String pathToSegDir = FileUtils
					.convertPath(parameters.getSavingPath() + "/movie_X" + xPos + "_Y" + yPos + "/");
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
				soc.setAcquisitionMeta(acquisitionMeta);
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
					curr_err = System.err;
					curr_out = System.err;
					System.setOut(ps);
					System.setErr(ps);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				int frame = 0;
				while (frame < 1) {
					acquisitionMeta.put(MaarsParameters.FRAME, frame);
					String channels = parameters.getUsingChannels();
					String[] arrayChannels = channels.split(",", -1);
					for (String channel : arrayChannels) {
						acquisitionMeta.put(MaarsParameters.CUR_CHANNEL, channel);
						acquisitionMeta.put(MaarsParameters.CUR_MAX_NB_SPOT,
								Integer.parseInt(parameters.getChMaxNbSpot(channel)));
						acquisitionMeta.put(MaarsParameters.CUR_SPOT_RADIUS,
								Double.parseDouble(parameters.getChSpotRaius(channel)));
						String pathToFluoMovie = parameters.getSavingPath() + "/movie_X"
								+ acquisitionMeta.get(MaarsParameters.X_POS) + "_Y"
								+ acquisitionMeta.get(MaarsParameters.Y_POS) + "_FLUO/"
								+ acquisitionMeta.get(MaarsParameters.FRAME) + "_"
								+ acquisitionMeta.get(MaarsParameters.CUR_CHANNEL) + "/MMStack.ome.tif";
						ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
						System.out.println(pathToFluoMovie);
						es.execute(new FluoAnalyzer(parameters, fluoImage, bfImgCal, soc, acquisitionMeta));
					}
					frame++;
				}
			}
		}
		es.shutdown();
		while (!es.isTerminated()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.setErr(curr_err);
		System.setOut(curr_out);
		System.out.println("it took " + (System.currentTimeMillis() - start) + " sec for analysing");
		System.out.println("DONE.");

	}
}
