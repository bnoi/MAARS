package org.micromanager.maars;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.GetMitosis;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.utils.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.ZProjector;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import mmcorej.CMMCore;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq implements Runnable {
	private PrintStream curr_err;
	private PrintStream curr_out;
	private CMMCore mmc;
	private MaarsParameters parameters;
	private SetOfCells soc;
	private ConcurrentHashMap<Integer, Integer> merotelyCandidates;

	public MAARSNoAcq(CMMCore mmc, MaarsParameters parameters, SetOfCells soc) {
		this.mmc = mmc;
		this.parameters = parameters;
		this.soc = soc;
		this.merotelyCandidates = new ConcurrentHashMap<Integer, Integer>();
		RoiManager manager = RoiManager.getInstance();
		if (manager != null) {
			manager.removeAll();
			manager.reset();
		}
	}

	@Override
	public void run() {
		ExecutorService es = null;
		// Start time
		long start = System.currentTimeMillis();

		// Acquisition path arrangement
		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);

		ImagePlus mergedImg = new ImagePlus();
		int frameCounter = 0;
		String pathToFluoDir = null;

		for (int i = 0; i < explo.length(); i++) {
			IJ.log("x : " + explo.getX(i) + " y : " + explo.getY(i));
			String xPos = String.valueOf(Math.round(explo.getX(i)));
			String yPos = String.valueOf(Math.round(explo.getY(i)));
			String pathToSegDir = FileUtils
					.convertPath(parameters.getSavingPath() + "/movie_X" + xPos + "_Y" + yPos + "/");
			String pathToSegMovie = FileUtils.convertPath(pathToSegDir + "MMStack.ome.tif");
			ImagePlus segImg = null;
			try {
				segImg = IJ.openImage(pathToSegMovie);
			} catch (Exception e) {
				e.printStackTrace();
				IJ.error("Invalid path");
			}
			// --------------------------segmentation-----------------------------//
			MaarsSegmentation ms = new MaarsSegmentation(parameters, xPos, yPos);
			ms.segmentation(segImg);
			if (ms.roiDetected()) {
				// from Roi.zip initialize a set of cell
				soc.loadCells(xPos, yPos);
				// Get the focus slice of BF image
				soc.setRoiMeasurementIntoCells(ms.getRoiMeasurements());
				Calibration bfImgCal = segImg.getCalibration();
				// ----------------start acquisition and analysis --------//
				try {
					PrintStream ps = new PrintStream(pathToSegDir + "CellStateAnalysis.LOG");
					curr_err = System.err;
					curr_out = System.err;
					System.setOut(ps);
					System.setErr(ps);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				pathToFluoDir = parameters.getSavingPath() + "/movie_X" + xPos + "_Y" + yPos + "_FLUO/";
				String[] listAcqNames = new File(pathToFluoDir).list();
				String pattern = "(\\d+)(_)(\\w+)";
				ArrayList<String> arrayChannels = new ArrayList<String>();
				ArrayList<Integer> arrayImgFrames = new ArrayList<Integer>();
				for (String acqName : listAcqNames) {
					if (Pattern.matches(pattern, acqName)) {
						String current_channel = acqName.split("_", -1)[1];
						String current_frame = acqName.split("_", -1)[0];
						if (!arrayChannels.contains(current_channel)) {
							arrayChannels.add(current_channel);
						}
						if (!arrayImgFrames.contains(Integer.parseInt(current_frame))) {
							arrayImgFrames.add(Integer.parseInt(current_frame));
						}
					}
				}
				frameCounter = arrayImgFrames.size();
				Collections.sort(arrayImgFrames);
				int nThread = Runtime.getRuntime().availableProcessors();
				es = Executors.newFixedThreadPool(nThread);
				Future<FloatProcessor> future = null;
				ArrayList<Map<String, Future<FloatProcessor>>> futureSet = new ArrayList<Map<String, Future<FloatProcessor>>>();
				for (int frameInd = 0; frameInd < arrayImgFrames.size(); frameInd++) {
					Map<String, Future<FloatProcessor>> channelsInFrame = new HashMap<String, Future<FloatProcessor>>();
					for (String channel : arrayChannels) {
						int current_frame = arrayImgFrames.get(frameInd);
						IJ.log("Analysing channel " + channel + "_" + current_frame);
						String[] id = new String[] { xPos, yPos, String.valueOf(current_frame), channel };
						soc.addAcqID(id);
						String pathToFluoMovie = pathToFluoDir + current_frame + "_" + channel + "/MMStack.ome.tif";
						ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
						future = es.submit(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
								Integer.parseInt(parameters.getChMaxNbSpot(channel)),
								Double.parseDouble(parameters.getChSpotRaius(channel)),
								Double.parseDouble(parameters.getChQuality(channel)), Integer.valueOf(current_frame),
								merotelyCandidates));
						channelsInFrame.put(channel, future);
					}
					futureSet.add(channelsInFrame);
				}
				ImageStack fluoStack = new ImageStack(segImg.getWidth(), segImg.getHeight());
				try {
					for (int frameInd = 0; frameInd < futureSet.size(); frameInd++) {
						for (String channel : futureSet.get(frameInd).keySet()) {
							fluoStack.addSlice(channel, futureSet.get(frameInd).get(channel).get());
						}
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
				mergedImg = new ImagePlus("merged", fluoStack);
				mergedImg.setCalibration(segImg.getCalibration());
				mergedImg.setZ(fluoStack.getSize());
			}
		}
		es.shutdown();
		try {
			es.awaitTermination(120, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.setErr(curr_err);
		System.setOut(curr_out);
		// TODO add a textfield in gui to specify this parameter
		double laggingThreshold = 120;
		double timeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
		if (soc.size() != 0) {
			long startWriting = System.currentTimeMillis();
			soc.saveSpots();
			soc.saveGeometries();
			Boolean splitChannel = true;
			HashMap<Integer, HashMap<String, ImagePlus>> croppedImgSet = soc.cropRois(mergedImg, splitChannel);
			String croppedImgDir = pathToFluoDir + "croppedImgs/";
			soc.saveCroppedImgs(croppedImgSet, pathToFluoDir + "croppedImgs/");
			// TODO a new static class to find lagging chromosomes
			for (int nb : merotelyCandidates.keySet()) {
				int abnormalStateTimes = this.merotelyCandidates.get(nb);
				if (abnormalStateTimes > (laggingThreshold / (timeInterval / 1000))) {
					String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					IJ.log(timeStamp + " : " + nb + "_" + frameCounter + "_"
							+ abnormalStateTimes * timeInterval / 1000);
					if (splitChannel) {
						IJ.openImage(croppedImgDir + nb + "_GFP.tif").show();
					} else {
						IJ.openImage(croppedImgDir + nb + "_merged.tif").show();
					}
				}
			}
			soc.exportChannelBtf(pathToFluoDir, mergedImg, splitChannel);
			GetMitosis.getMitosisWithPython(parameters.getSavingPath(), "CFP");
			// MAARS.mailNotify();
			IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
					+ " sec for writing results");
		}
		IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing");
	}
}
