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

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.FluoAnalyzer;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.utils.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq implements Runnable {
	private PrintStream curr_err;
	private PrintStream curr_out;
	private MaarsParameters parameters;
	private SetOfCells soc;
	private String rootDir;
	private String pathToSegDir;
	private String pathToFluoDir;
	private ArrayList<String> arrayChannels = new ArrayList<String>();

	public MAARSNoAcq(MaarsParameters parameters) {
		this.parameters = parameters;
		rootDir = parameters.getSavingPath();
		this.soc = new SetOfCells();
	}

	private ArrayList<String[]> getAcqPositions() {
		ArrayList<String[]> acqPos = new ArrayList<String[]>();
		String[] listAcqNames = new File(rootDir).list();
		String pattern = "(X)(\\d+)(_)(Y)(\\d+)(_FLUO)";
		for (String acqName : listAcqNames) {
			if (Pattern.matches(pattern, acqName)) {
				acqPos.add(new String[] { acqName.split("_", -1)[0].substring(1),
						acqName.split("_", -1)[1].substring(1) });
			}
		}
		return acqPos;
	}

	@Override
	public void run() {
		ExecutorService es = null;
		// Start time
		long start = System.currentTimeMillis();
		ImagePlus mergedImg = new ImagePlus();
		for (String[] pos : getAcqPositions()) {
			String xPos = pos[0];
			String yPos = pos[1];
			IJ.log("x : " + xPos + " y : " + yPos);
			this.pathToSegDir = FileUtils.convertPath(rootDir + "/X" + xPos + "_Y" + yPos);
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
			ms.segmentation(segImg, this.pathToSegDir);
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
						int current_frame = arrayImgFrames.get(frameInd);
						IJ.log("Analysing channel " + channel + "_" + current_frame);
						String pathToFluoMovie = pathToFluoDir + current_frame + "_" + channel + "/MMStack.ome.tif";
						ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
						future = es.submit(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
								Integer.parseInt(parameters.getChMaxNbSpot(channel)),
								Double.parseDouble(parameters.getChSpotRaius(channel)),
								Double.parseDouble(parameters.getChQuality(channel)), Integer.valueOf(current_frame)));
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
				RoiManager.getInstance().reset();
				RoiManager.getInstance().close();
				if (soc.size() != 0) {
					long startWriting = System.currentTimeMillis();
					Boolean splitChannel = true;
					MAARS.saveAll(soc, mergedImg, pathToFluoDir, arrayChannels, splitChannel);
					MAARS.analyzeMitosisDynamic(soc, parameters, splitChannel, pathToSegDir, true);
					IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
							+ " sec for writing results");
				}
			}
		}
		try {
			es.shutdown();
			es.awaitTermination(120, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.setErr(curr_err);
		System.setOut(curr_out);
		IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing");
	}
}
