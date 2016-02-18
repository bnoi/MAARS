package org.micromanager.maars;

import mmcorej.CMMCore;

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.micromanager.AutofocusPlugin;
import org.micromanager.acquisition.FluoAcquisition;
import org.micromanager.acquisition.SegAcquisition;
import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.MMException;
import org.micromanager.internal.utils.ReportingUtils;

import ij.IJ;
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
	private ConcurrentHashMap<Integer, Integer> merotelyCandidates;

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
		this.merotelyCandidates = new ConcurrentHashMap<Integer, Integer>();
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
				IJ.error("Can't set XY stage devie");
				e.printStackTrace();
			}
			String xPos = String.valueOf(Math.round(explo.getX(i)));
			String yPos = String.valueOf(Math.round(explo.getY(i)));
			IJ.log("Current position : x_" + xPos + " y_" + yPos);
//			autofocus(mm, mmc);
			double zFocus = 0;
			String focusDevice = mmc.getFocusDevice();
			try {
				zFocus = mmc.getPosition(focusDevice);
				mmc.waitForDevice(focusDevice);
			} catch (Exception e) {
				ReportingUtils.logMessage("could not get z current position");
				e.printStackTrace();
			}
			SegAcquisition segAcq = new SegAcquisition(mm, mmc, parameters, xPos, yPos);
			IJ.log("Acquire bright field image...");
			ImagePlus segImg = segAcq.acquire(parameters.getSegmentationParameter(MaarsParameters.CHANNEL), zFocus);
			// --------------------------segmentation-----------------------------//
			MaarsSegmentation ms = new MaarsSegmentation(parameters, xPos, yPos);
			ms.segmentation(segImg);
			if (ms.roiDetected()) {
				// from Roi initialize a set of cell
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
							ImagePlus fluoImage = fluoAcq.acquire(frame, channel, zFocus);
							es.execute(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
									Integer.parseInt(parameters.getChMaxNbSpot(channel)),
									Double.parseDouble(parameters.getChSpotRaius(channel)), frame, timeInterval,
									merotelyCandidates));
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
						} else {
							ReportingUtils.logMessage(
									"Attention : acquisition before took longer than " + timeInterval + " ms.");
						}
					}
					// main acquisition finished, find whether there is merotely
					// cells.

				} else {
					String channels = parameters.getUsingChannels();
					String[] arrayChannels = channels.split(",", -1);
					for (String channel : arrayChannels) {
						String[] id = new String[] { xPos, yPos, String.valueOf(frame), channel };
						soc.addAcqID(id);
						ImagePlus fluoImage = fluoAcq.acquire(frame, channel, zFocus);
						es.execute(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
								Integer.parseInt(parameters.getChMaxNbSpot(channel)),
								Double.parseDouble(parameters.getChSpotRaius(channel)), frame, 0, merotelyCandidates));
					}
				}
				RoiManager.getInstance().reset();
				RoiManager.getInstance().close();
				if (soc.size() != 0) {
					long startWriting = System.currentTimeMillis();
					soc.saveSpots();
					soc.saveGeometries();
					String croppedImgDir = soc.saveCroppedImgs();
					for (int nb : merotelyCandidates.keySet()) {
						if (this.merotelyCandidates.get(nb) > 5) {
							String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss")
									.format(Calendar.getInstance().getTime());
							IJ.log(timeStamp + " : " + nb);
							IJ.openImage(croppedImgDir + nb + "_GFP.tif").show();
							Toolkit.getDefaultToolkit().beep();
						}
					}
					mailNotify();
					ReportingUtils.logMessage("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
							+ " sec for writing results");
				}

			}
			this.merotelyCandidates.clear();
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
		IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing");
		IJ.log("DONE.");
	}

	/**
	 * A MAARS need specific autofocus process based on JAF(H&P) sharpness
	 * autofocus
	 * 
	 * @param mm
	 * @param mmc
	 */
	public void autofocus(MMStudio mm, CMMCore mmc) {
		try {
			mmc.setShutterDevice(parameters.getChShutter(parameters.getSegmentationParameter(MaarsParameters.CHANNEL)));
		} catch (Exception e2) {
			IJ.error("Can't set BF channel for autofocusing");
			e2.printStackTrace();
		}
		double initialPosition = 0;
		String focusDevice = mmc.getFocusDevice();
		try {
			initialPosition = mmc.getPosition();
		} catch (Exception e) {
			IJ.error("Can't get current z level");
			e.printStackTrace();
		}

		// Get autofocus manager
		IJ.log("First autofocus");
		AutofocusPlugin autofocus = mm.getAutofocus();
		double firstPosition = 0;
		try {
			mmc.setShutterOpen(true);
			autofocus.fullFocus();
			mmc.waitForDevice(focusDevice);
			firstPosition = mmc.getPosition(mmc.getFocusDevice());
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			mmc.waitForDevice(focusDevice);
			mmc.setPosition(focusDevice, 2 * initialPosition - firstPosition);
		} catch (Exception e) {
			IJ.error("Can't set z position");
			e.printStackTrace();
		}

		IJ.log("Seconde autofocus");
		double secondPosition = 0;
		try {
			autofocus.fullFocus();
			mmc.waitForDevice(focusDevice);
			secondPosition = mmc.getPosition(mmc.getFocusDevice());
		} catch (MMException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			mmc.waitForDevice(focusDevice);
			mmc.setPosition(focusDevice, (secondPosition + firstPosition) / 2);
		} catch (Exception e) {
			IJ.error("Can't set z position");
			e.printStackTrace();
		}
		try {
			mmc.setShutterOpen(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void mailNotify() {
		String to = "tongli.bioinfo@gmail.com";

		// Sender's email ID needs to be mentioned
		String from = "MAARS@univ-tlse3.fr";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", "smtps.univ-tlse3.fr");

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject: header field
			message.setSubject("Analysis done!");

			// Now set the actual message
			message.setText("");

			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.runAnalysis();
	}
}
