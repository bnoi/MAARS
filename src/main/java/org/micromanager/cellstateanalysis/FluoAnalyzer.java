package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.FastMath;
import org.micromanager.utils.ImgUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAnalyzer implements Runnable {

	private ImagePlus fluoImage;
	private SetOfCells soc;
	private Calibration bfImgCal;
	private Calibration fluoImgCal;
	private String channel;
	private int maxNbSpot;
	private double radius;
	private int frame;
	private SpotCollection collection;
	private ConcurrentHashMap<Integer, Integer> merotelyCandidates;

	/**
	 * @param fluoImage
	 *            image to analyze
	 * 
	 * @param bfImgCal
	 *            bright field image calibration, need it to decide whether or
	 *            not rescale ROI
	 * @param soc
	 *            the set of cell to analyze
	 * @param channel
	 *            fluo image channel
	 * @param maxNbSpot
	 *            max number of spot in corresponding channel
	 * @param radius
	 *            radius of spot in corresponding channel
	 * @param frame
	 *            time point
	 * @param timeInterval
	 *            interval between time points
	 * @param merotelyCandidates
	 *            hashmap<cell number, times that this cell has been found one
	 *            point on the "spindle line"
	 */

	public FluoAnalyzer(ImagePlus fluoImage, Calibration bfImgCal, SetOfCells soc, String channel, int maxNbSpot,
			double radius, int frame, ConcurrentHashMap<Integer, Integer> merotelyCandidates) {
		this.fluoImage = fluoImage;
		this.fluoImgCal = fluoImage.getCalibration();
		soc.setFluoImgCalib(fluoImgCal);
		this.soc = soc;
		this.bfImgCal = bfImgCal;
		this.channel = channel;
		this.maxNbSpot = maxNbSpot;
		this.radius = radius;
		this.frame = frame;
		this.merotelyCandidates = merotelyCandidates;
	}

	/**
	 * the main, use one new thread just in order to free acquisition thread to
	 * acquire images as soon as possible
	 */
	public void run() {
		soc.addSpotContainerOf(channel);
		soc.addFeatureContainerOf(channel);
		// TODO project or not. Do not project if do 3D detection
		ImagePlus zProjectedFluoImg = ImgUtils.zProject(fluoImage);
		zProjectedFluoImg.setCalibration(fluoImage.getCalibration());
		zProjectedFluoImg = ImgUtils.unitCmToMicron(zProjectedFluoImg);
		// Call trackmate to detect spots
		MaarsTrackmate trackmate = null;
		if (channel.equals("CFP")){
			trackmate = new MaarsTrackmate(zProjectedFluoImg, radius, 10);
		}else if (channel.equals("GFP")){
			trackmate = new MaarsTrackmate(zProjectedFluoImg, radius, 2);
		}
		Model model = trackmate.doDetection();

		if (frame == 0) {
			soc.setTrackmateModel(model);
			zProjectedFluoImg.show();
			SelectionModel selectionModel = new SelectionModel(model);
			HyperStackDisplayer displayer = new HyperStackDisplayer(model, selectionModel, zProjectedFluoImg);
			displayer.render();
			displayer.refresh();
		}

		collection = getNBestqualitySpots(model.getSpots());
		double[] factors = ImgUtils.getRescaleFactor(bfImgCal, fluoImgCal);

		int nThread = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(nThread);
		int nbCell = soc.size();
		final int[] nbOfCellEachThread = new int[2];
		nbOfCellEachThread[0] = (int) nbCell / nThread;
		nbOfCellEachThread[1] = (int) nbOfCellEachThread[0] + nbCell % nThread;
		Future<?> future = null;
		for (int i = 0; i < nThread; i++) {
			// analyze every subset of cell
			future = es.submit(new AnalyseBlockCells(i, nbOfCellEachThread, factors, merotelyCandidates));
		}
		es.shutdown();
		try {
			future.get();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
		try {
			es.awaitTermination(3, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private SpotCollection getNBestqualitySpots(SpotCollection spots) {
		SpotCollection newSet = new SpotCollection();
		Iterator<Spot> it = spots.iterator(false);
		while (it.hasNext()) {
			Spot s = it.next();
			newSet.add(s, 0);
			if (newSet.getNSpots(0, false) > soc.size() * maxNbSpot) {
				newSet.remove(findLowestQualitySpot(newSet.iterable(0, false)), 0);
			}
		}
		return newSet;
	}

	/**
	 * Get the lowest qualit spot in the frame
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 * @return
	 */
	public static Spot findLowestQualitySpot(Iterable<Spot> spots) {
		double min = Double.POSITIVE_INFINITY;
		Spot lowestQualitySpot = null;
		for (Spot s : spots) {
			if (s.getFeature(Spot.QUALITY) < min) {
				min = s.getFeature(Spot.QUALITY);
				lowestQualitySpot = s;
			}
		}
		return lowestQualitySpot;
	}

	/**
	 * analyzer of subset
	 */
	private class AnalyseBlockCells implements Runnable {
		private final int index;
		private final int[] deltas;
		private double[] factors;
		private ConcurrentHashMap<Integer, Integer> merotelyCandidates;

		public AnalyseBlockCells(int index, final int[] deltas, double[] factors,
				ConcurrentHashMap<Integer, Integer> merotelyCandidates) {
			this.index = index;
			this.deltas = deltas;
			this.merotelyCandidates = merotelyCandidates;
			this.factors = factors;
		}

		@Override
		public void run() {
			// distribute number of cells for each thread
			int begin = 0;
			int end = 0;
			if (index == 0) {
				if (deltas[0] != deltas[1]) {
					end = deltas[1];
				} else {
					end = deltas[0];
				}
			} else {
				begin = index * deltas[0] + (deltas[1] - deltas[0]);
				end = begin + deltas[0];
			}
			// need to be false because all spots are not visible
			ArrayList<Spot> currentThreadSpots = Lists.newArrayList(collection.iterable(false));
			for (int j = begin; j < end; j++) {
				Cell cell = soc.getCell(j);
				int cellNb = cell.getCellNumber();
				Roi tmpRoi = null;
				if (factors[0] != 1 || factors[1] != 1) {
					tmpRoi = cell.rescaleCellShapeRoi(factors);
				} else {
					tmpRoi = cell.getCellShapeRoi();
				}
				double calibratedXBase = tmpRoi.getXBase() * fluoImgCal.pixelWidth;
				double calibratedYBase = tmpRoi.getYBase() * fluoImgCal.pixelHeight;
				ArrayList<Spot> spotsToDel = new ArrayList<Spot>();
				for (Spot s : currentThreadSpots) {
					if (tmpRoi.contains((int) Math.round(s.getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
							(int) Math.round(s.getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight))) {
						soc.putSpot(channel, cellNb, frame, s);
						spotsToDel.add(s);
						if (soc.getNbOfSpot(channel, cellNb, frame) > maxNbSpot) {
							Spot lowesetQulitySpot = FluoAnalyzer
									.findLowestQualitySpot(soc.getSpotsInFrame(channel, cellNb, frame));
							soc.removeSpot(channel, cellNb, frame, lowesetQulitySpot);
						}
					}
				}
				// remove spots found in current cell in order to accelerate
				// iteration
				for (Spot s2del : spotsToDel) {
					currentThreadSpots.remove(s2del);
				}
				ComputeGeometry cptgeometry = new ComputeGeometry(cell.get(Cell.X_CENTROID) * fluoImgCal.pixelWidth,
						cell.get(Cell.Y_CENTROID) * fluoImgCal.pixelHeight,
						cell.get(Cell.MAJOR) * fluoImgCal.pixelWidth, cell.get(Cell.ANGLE), calibratedXBase,
						calibratedYBase);
				Iterable<Spot> spotSet = soc.getSpotsInFrame(channel, cellNb, frame);
				// testing whether using GFP, because usually we mark Kt with
				// GFP
				if (spotSet != null) {
					// this functions modify directly coordinates of spot in
					// soc, because it's back-up
//					cptgeometry.centerSpots(spotSet);
					int setSize = Iterables.size(spotSet);
					HashMap<String, Object> geometry = new HashMap<String, Object>();
					geometry.put(ComputeGeometry.NbOfSpotDetected, setSize);
					if (setSize == 1) {
						geometry.put(ComputeGeometry.PHASE, ComputeGeometry.INTERPHASE);
					} else {
						ArrayList<Spot> poles = cptgeometry.findMostDistant2Spots(spotSet);
						geometry.put(ComputeGeometry.PHASE, ComputeGeometry.MITOSIS);
						geometry = cptgeometry.compute(geometry, poles);
						if (setSize > 2) {
							if ((double) geometry.get(ComputeGeometry.SpLength) > 4) {
								Line spLine = new Line(
										(int) FastMath.round(
												poles.get(0).getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
										(int) FastMath.round(
												poles.get(0).getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight),
										(int) FastMath.round(
												poles.get(1).getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
										(int) FastMath.round(
												poles.get(1).getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight));
								Line.setWidth((int) FastMath.round(0.25 / fluoImgCal.pixelWidth));
								for (Spot s : spotSet) {
									if (!s.equals(poles.get(0)) && !s.equals(poles.get(1))) {
										// detect metaphase Kt oscillation or
										// merotely
										if (spLine.contains(
												(int) FastMath
														.round(s.getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
												(int) FastMath.round(
														s.getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight))) {
											if (merotelyCandidates.containsKey(cellNb)) {
												this.merotelyCandidates.replace(cellNb,
														this.merotelyCandidates.get(cellNb) + 1);
											} else {
												this.merotelyCandidates.put(cellNb, 1);
											}
										}
									}
								}
							}
						}
					}
					soc.putGeometry(channel, cellNb, frame, geometry);
				}
			}
		}
	}
}
