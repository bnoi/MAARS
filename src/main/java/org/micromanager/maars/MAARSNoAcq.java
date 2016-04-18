package org.micromanager.maars;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.cellstateanalysis.singleCellAnalysisFactory.AnalysisFactory;
import org.micromanager.utils.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
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
	private String pathToSegDir;
	private String pathToFluoDir;
	private ArrayList<String> arrayChannels = new ArrayList<String>();
	private AnalysisFactory factory;

	public MAARSNoAcq(CMMCore mmc, MaarsParameters parameters) {
		this.mmc = mmc;
		this.parameters = parameters;
		this.soc = new SetOfCells();
		factory = new AnalysisFactory(parameters.getAnalaysisOptions());
	}

	@Override
	public void run() {
		ExecutorService es = null;
		// Start time
		long start = System.currentTimeMillis();

		// Acquisition path arrangement
		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);

		ImagePlus mergedImg = new ImagePlus();
		for (int i = 0; i < explo.length(); i++) {
			IJ.log("x : " + explo.getX(i) + " y : " + explo.getY(i));
			String xPos = String.valueOf(Math.round(explo.getX(i)));
			String yPos = String.valueOf(Math.round(explo.getY(i)));
			this.pathToSegDir = FileUtils.convertPath(parameters.getSavingPath() + "/movie_X" + xPos + "_Y" + yPos);
			pathToFluoDir = pathToSegDir + "_FLUO/";
			String pathToSegMovie = FileUtils.convertPath(pathToSegDir + "/MMStack.ome.tif");
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
				soc.reset();
				// from Roi.zip initialize a set of cell
				soc.loadCells(pathToSegDir);
				// Get the focus slice of BF image
				soc.setRoiMeasurementIntoCells(ms.getRoiMeasurements());
				Calibration bfImgCal = segImg.getCalibration();
				// ----------------start acquisition and analysis --------//
				try {
					PrintStream ps = new PrintStream(pathToSegDir + "/CellStateAnalysis.LOG");
					curr_err = System.err;
					curr_out = System.err;
					System.setOut(ps);
					System.setErr(ps);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				String[] listAcqNames = new File(pathToFluoDir).list();
				String pattern = "(\\d+)(_)(\\w+)";
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
				Collections.sort(arrayImgFrames);
				int nThread = Runtime.getRuntime().availableProcessors();
				es = Executors.newFixedThreadPool(nThread);
				Future<FloatProcessor> future = null;
				ArrayList<Map<String, Future<FloatProcessor>>> futureSet = new ArrayList<Map<String, Future<FloatProcessor>>>();
				for (int frameInd = 0; frameInd < arrayImgFrames.size(); frameInd++) {
					Map<String, Future<FloatProcessor>> channelsInFrame = new HashMap<String, Future<FloatProcessor>>();
					for (String channel : arrayChannels) {
						factory.addChannel(channel);
						int current_frame = arrayImgFrames.get(frameInd);
						IJ.log("Analysing channel " + channel + "_" + current_frame);
						String pathToFluoMovie = pathToFluoDir + current_frame + "_" + channel + "/MMStack.ome.tif";
						ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
						future = es.submit(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
								Integer.parseInt(parameters.getChMaxNbSpot(channel)),
								Double.parseDouble(parameters.getChSpotRaius(channel)),
								Double.parseDouble(parameters.getChQuality(channel)), Integer.valueOf(current_frame),
								factory));
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

		if (soc.size() != 0) {
			long startWriting = System.currentTimeMillis();
			MAARS.saveAll(soc.getCellArray(), parameters, mergedImg, pathToFluoDir, arrayChannels);
			// MAARS.mailNotify();
			IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
					+ " sec for writing results");
		}
		IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing");
	}
}
