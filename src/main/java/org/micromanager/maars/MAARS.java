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
import org.micromanager.internal.utils.ReportingUtils;

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
public class MAARS implements Runnable {
	private PrintStream curr_err;
	private PrintStream curr_out;
	private MMStudio mm;
	private CMMCore mmc;
	private MaarsParameters parameters;
	private SetOfCells soc;

	/**
	 * Constructor
	 * 
	 * @param mm
	 * @param mmc
	 * @param parameters
	 */
	public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters, SetOfCells soc) {
		this.mmc = mmc;
		this.parameters = parameters;
		this.soc = soc;
		this.mm = mm;
	}

	public void runAnalysis() {
		// Start time
		long start = System.currentTimeMillis();
		mmc.setAutoShutter(false);

		// Set XY stage device
		try {
			mmc.setOriginXY(mmc.getXYStageDevice());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Acquisition path arrangement
		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);
		int nThread = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(nThread);
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
				mmc.setShutterDevice(
						parameters.getChShutter(parameters.getSegmentationParameter(MaarsParameters.CHANNEL)));
			} catch (Exception e2) {
				System.out.println("Can't set BF channel for autofocusing");
				e2.printStackTrace();
			}
			autofocus(mm, mmc);
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
									Double.parseDouble(parameters.getChSpotRaius(channel)), frame, timeInterval));
						}
						frame++;
						double acqTook = System.currentTimeMillis() - beginAcq;
						ReportingUtils.logMessage(String.valueOf(acqTook));
						if (timeInterval > acqTook) {
							try {
								Thread.sleep((long) (timeInterval - acqTook));
							} catch (InterruptedException e) {
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
								Double.parseDouble(parameters.getChSpotRaius(channel)), frame, 0));
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

	/**
	 * A MAARS need specific autofocus process based on JAF(H&P) sharpness
	 * autofocus
	 * 
	 * @param mm
	 * @param mmc
	 */
	public void autofocus(MMStudio mm, CMMCore mmc) {
		double initialPosition = 0;
		String focusDevice = mmc.getFocusDevice();
		try {
			initialPosition = mmc.getPosition();
		} catch (Exception e) {
			System.out.println("Can't get current z level");
			e.printStackTrace();
		}

		// Get autofocus manager
		System.out.println("First autofocus");
		AutofocusPlugin autofocus = mm.getAutofocus();
		try {
			mmc.setShutterOpen(true);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		try {
			autofocus.fullFocus();
		} catch (MMException e1) {
			e1.printStackTrace();
		}
		double firstPosition = 0;
		try {
			mmc.waitForDevice(focusDevice);
			firstPosition = mmc.getPosition(mmc.getFocusDevice());
		} catch (Exception e) {
			System.out.println("Can't get current z level");
			e.printStackTrace();
		}

		try {
			mmc.waitForDevice(focusDevice);
			mmc.setPosition(focusDevice, 2 * initialPosition - firstPosition);
		} catch (Exception e) {
			System.out.println("Can't set z position");
			e.printStackTrace();
		}

		System.out.println("Seconde autofocus");
		try {
			autofocus.fullFocus();
		} catch (MMException e1) {
			e1.printStackTrace();
		}

		double secondPosition = 0;
		try {
			mmc.waitForDevice(focusDevice);
			secondPosition = mmc.getPosition(mmc.getFocusDevice());
		} catch (Exception e) {
			System.out.println("Can't get current z level");
			e.printStackTrace();
		}

		try {
			mmc.waitForDevice(focusDevice);
			mmc.setPosition(focusDevice, (secondPosition + firstPosition) / 2);
		} catch (Exception e) {
			System.out.println("Can't set z position");
			e.printStackTrace();
		}
		try {
			mmc.setShutterOpen(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			soc.saveGeometries();
		}

	}

	@Override
	public void run() {
		this.runAnalysis();
	}
}
