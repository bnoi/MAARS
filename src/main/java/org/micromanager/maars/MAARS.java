package org.micromanager.maars;

import mmcorej.CMMCore;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.micromanager.AutofocusPlugin;
import org.micromanager.acquisition.FluoAcquisition;
import org.micromanager.acquisition.SegAcquisition;
import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.MMException;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;

/**
 * 
 * Main MAARS program
 * 
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
public class MAARS {
	private PrintStream curr_err;
	private PrintStream curr_out;

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
		mmc.setAutoShutter(false);
		// Get autofocus manager
		System.out.println("Autofocusing...");
		AutofocusPlugin autofocus = mm.getAutofocus();

		// Set XY stage device
		try {
			mmc.setOriginXY(mmc.getXYStageDevice());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Acquisition path arrangement
		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);

		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int i = 0; i < explo.length(); i++) {
			try {
				mm.core().setXYPosition(explo.getX(i), explo.getY(i));
				mmc.waitForDevice(mmc.getXYStageDevice());
			} catch (Exception e) {
				System.out.println("Can't set XY stage devie");
				e.printStackTrace();
			}
			String xPos = String.valueOf(Math.round(explo.getX(i)));
			String yPos = String.valueOf(Math.round(explo.getY(i)));
			System.out.println("x : " + xPos + " y : " + yPos);
			try {
				try {
					mmc.setShutterDevice(
							parameters.getChShutter(parameters.getSegmentationParameter(MaarsParameters.CHANNEL)));
				} catch (Exception e) {
					System.out.println("Can't set BF channel for autofocusing");
					e.printStackTrace();
				}
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
				Thread t1 = new Thread(new ImgWriter(soc));
				Thread t2 = new Thread(new spotsWriter(soc));
				Thread t3 = new Thread(new featuresWriter(soc));
				// from Roi initialize a set of cell
				try {
					t1.join();
					t2.join();
					t3.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				soc.reset();
				soc.loadCells(xPos, yPos);
				soc.setRoiMeasurementIntoCells(ms.getRoiMeasurements());
				// Get the focus slice of BF image
				Calibration bfImgCal = segImg.getCalibration();
				ImagePlus focusImage = new ImagePlus(segImg.getShortTitle(),
						segImg.getStack().getProcessor(ms.getSegPombeParam().getFocusSlide()));
				focusImage.setCalibration(bfImgCal);
				// ----------------start acquisition and analysis --------//
				FluoAcquisition fluoAcq = new FluoAcquisition(mm, mmc, parameters, xPos, yPos);
				try {
					PrintStream ps = new PrintStream(ms.getPathToSegDir() + "CellStateAnalysis.LOG");
					curr_err = System.err;
					curr_out = System.err;
					System.setOut(ps);
					System.setErr(ps);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				int frame = 0;
				if (parameters.useDynamic()) {
					double timeInterval = Double
							.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
					double startTime = System.currentTimeMillis();
					double timeLimit = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT)) * 60
							* 1000;
					while (System.currentTimeMillis() - startTime <= timeLimit) {
						double beginAcq = System.currentTimeMillis();
						String channels = parameters.getUsingChannels();
						String[] arrayChannels = channels.split(",", -1);
						for (String channel : arrayChannels) {
							String[] id = new String[] { xPos, yPos, String.valueOf(frame), channel };
							soc.addAcqID(id);
							ImagePlus fluoImage = fluoAcq.acquire(frame, channel);
							es.execute(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
									Integer.parseInt(parameters.getChMaxNbSpot(channel)),
									Double.parseDouble(parameters.getChSpotRaius(channel)), frame));
						}
						frame++;
						double acqTook = System.currentTimeMillis() - beginAcq;
						if (timeInterval > acqTook) {
							try {
								Thread.sleep((long) (timeInterval - acqTook));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} else {
					String channels = parameters.getUsingChannels();
					String[] arrayChannels = channels.split(",", -1);
					for (String channel : arrayChannels) {
						String[] id = new String[] { xPos, yPos, String.valueOf(frame), channel };
						soc.addAcqID(id);
						ImagePlus fluoImage = fluoAcq.acquire(frame, channel);
						es.execute(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
								Integer.parseInt(parameters.getChMaxNbSpot(channel)),
								Double.parseDouble(parameters.getChSpotRaius(channel)), frame));
					}
				}
				RoiManager.getInstance().reset();
				RoiManager.getInstance().close();
				if (soc.size() != 0) {
					t1.start();
					t2.start();
					t3.start();
				}
			}
		}
		mmc.setAutoShutter(true);
		es.shutdown();
		try {
			es.awaitTermination(300, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.setErr(curr_err);
		System.setOut(curr_out);
		System.out.println("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing");
		System.out.println("DONE.");
	}

	// IN-class classes for result writing
	class ImgWriter implements Runnable {

		private SetOfCells soc;

		public ImgWriter(SetOfCells soc) {
			this.soc = soc;
		}

		@Override
		public void run() {
			soc.saveCroppedImgs();
		}

	}

	class spotsWriter implements Runnable {

		private SetOfCells soc;

		public spotsWriter(SetOfCells soc) {
			this.soc = soc;
		}

		@Override
		public void run() {
			soc.saveSpots();
		}

	}

	class featuresWriter implements Runnable {

		private SetOfCells soc;

		public featuresWriter(SetOfCells soc) {
			this.soc = soc;
		}

		@Override
		public void run() {
			soc.saveFeatures();
		}

	}
}
