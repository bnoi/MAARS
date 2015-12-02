package org.micromanager.maars;

import mmcorej.CMMCore;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.micromanager.AutofocusPlugin;
import org.micromanager.acquisition.FluoAcquisition;
import org.micromanager.acquisition.SegAcquisition;
import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.MMException;
import org.micromanager.maarslib.ExplorationXYPositions;
import org.micromanager.maarslib.MaarsSegmentation;
import org.micromanager.utils.ImgUtils;

import ij.ImagePlus;
import ij.measure.Calibration;

/**
 * 
 * Main MAARS process
 * 
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
public class MAARS {

	/**
	 * Constructor and run the program
	 * 
	 * @param mm
	 * @param mmc
	 * @param parameters
	 */
	public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters, SetOfCells soc) {
		// Start time
		long start = System.currentTimeMillis();

		// Get autofocus manager
		System.out.println("Autofocusing...");
		AutofocusPlugin autofocus = mm.getAutofocus();

		// Set XY stage device
		try {
			mmc.setOriginXY(mmc.getXYStageDevice());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Acquisition path arrangement
		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);

		for (int i = 0; i < explo.length(); i++) {
			System.out.println("x : " + explo.getX(i) + " y : " + explo.getY(i));
			double xPos = explo.getX(i);
			double yPos = explo.getY(i);

			try {
				mm.core().setXYPosition(xPos, yPos);
				mmc.waitForDevice(mmc.getXYStageDevice());
			} catch (Exception e) {
				System.out.println("Can't set XY stage devie");
				e.printStackTrace();
			}
			try {
				autofocus.fullFocus();
			} catch (MMException e1) {
				System.out.println("Can't do autofocus");
				e1.printStackTrace();
			}

			SegAcquisition segAcq = new SegAcquisition(mm, mmc, parameters, xPos, yPos);
			System.out.println("Acquire bright field image...");
			ImagePlus segImg = segAcq.acquire(parameters.getSegmentationParameter(MaarsParameters.CHANNEL));
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
				FluoAcquisition fluoAcq = new FluoAcquisition(mm, mmc, parameters, xPos, yPos);
				try {
					PrintStream ps = new PrintStream(parameters.getSavingPath() + "CellStateAnalysis.LOG");
					System.setOut(ps);
					System.setErr(ps);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
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
							ImagePlus fluoImage = fluoAcq.acquire(frame, channel);
							new FluoAnalyzer(parameters, fluoImage, bfImgCal, soc, channel, frame, xPos, yPos).start();
						}
						frame++;
						double acqTook = System.currentTimeMillis() - beginAcq;
						try {
							Thread.sleep((long) (timeInterval - acqTook));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//
				} else {
					int frame = 0;
					String channels = parameters.getUsingChannels();
					String[] arrayChannels = channels.split(",", -1);
					for (String channel : arrayChannels) {
						ImagePlus fluoImage = fluoAcq.acquire(frame, channel);
						new FluoAnalyzer(parameters, fluoImage, bfImgCal, soc, channel, frame, xPos, yPos).start();
					}
				}
			}
		}
		mmc.setAutoShutter(true);
		System.out.println("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec");
		System.out.println("DONE.");
	}
}
