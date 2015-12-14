package org.micromanager.cellstateanalysis;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.micromanager.utils.ImgUtils;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAnalyzer implements Runnable {

	private ImagePlus fluoImage;
	private SetOfCells soc;
	private double[] factors;
	private Calibration bfImgCal;
	private Calibration fluoImgCal;
	private SpotCollection spots;
	private String channel;
	private int maxNbSpot;
	private double radius;
	private int frame;

	/**
	 * Analyze the set of cell in given the fluo image
	 * 
	 * @param fluoImage
	 *            image to analyze
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
	 */
	public FluoAnalyzer(ImagePlus fluoImage, Calibration bfImgCal, SetOfCells soc, String channel, int maxNbSpot,
			double radius, int frame) {
		this.fluoImage = fluoImage;
		this.fluoImgCal = fluoImage.getCalibration();
		this.soc = soc;
		this.bfImgCal = bfImgCal;
		this.channel = channel;
		this.maxNbSpot = maxNbSpot;
		this.radius = radius;
		this.frame = frame;
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
		zProjectedFluoImg = ImgUtils.unitCmToMicron(zProjectedFluoImg);

		// Call trackmate to detect spots
		SpotsDetector detector = new SpotsDetector(zProjectedFluoImg, radius);
		Model model = detector.doDetection();
		soc.setTrackmateModel(model);
		spots = model.getSpots();

		this.factors = ImgUtils.getRescaleFactor(bfImgCal, fluoImgCal);

		int nThread = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(nThread);
		int nbCell = soc.size();
		final int[] nbOfCellEachThread = new int[2];
		nbOfCellEachThread[0] = (int) nbCell / nThread;
		nbOfCellEachThread[1] = (int) nbOfCellEachThread[0] + nbCell % nThread;
		Future<?> future = null;
		for (int i = 0; i < nThread; i++) {
			// analyze every subset of cell
			future = es.submit(new AnalyseBlockCells(i, nbOfCellEachThread));
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
			es.awaitTermination(90, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * analyzer of subset
	 */
	private class AnalyseBlockCells implements Runnable {
		final int index;
		final int[] deltas;

		public AnalyseBlockCells(int index, final int[] deltas) {
			this.index = index;
			this.deltas = deltas;
		}

		@Override
		public void run() {
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
			for (int j = begin; j < end; j++) {
				Cell cell = soc.getCell(j);
				int cellNb = cell.getCellNumber();
				Roi tmpRoi = null;
				if (factors[0] != 1 || factors[1] != 1) {
					tmpRoi = cell.rescaleCellShapeRoi(factors);
				} else {
					tmpRoi = cell.getCellShapeRoi();
				}
				for (Spot s : spots.iterable(false)) {
					if (tmpRoi.contains((int) Math.round(s.getFeature(Spot.POSITION_X) / fluoImgCal.pixelWidth),
							(int) Math.round(s.getFeature(Spot.POSITION_Y) / fluoImgCal.pixelHeight))) {
						soc.putSpot(channel, cellNb, frame, s);
						if (soc.getNbOfSpot(channel, cellNb, frame) > maxNbSpot) {
							Spot lowesetQulitySpot = soc.findLowestQualitySpot(channel, cellNb, frame);
							soc.getSpots(channel, cellNb).remove(lowesetQulitySpot, frame);
						}
					}
				}
				Iterable<Spot> spotSet = soc.getSpotsInFrame(channel, cellNb, frame);
				ComputeFeatures cptgeometry = new ComputeFeatures(spotSet,
						cell.get(Cell.X_CENTROID) * fluoImgCal.pixelWidth,
						cell.get(Cell.Y_CENTROID) * fluoImgCal.pixelHeight, cell.get(Cell.MAJOR), cell.get(Cell.ANGLE),
						tmpRoi.getXBase() * fluoImgCal.pixelWidth, tmpRoi.getYBase() * fluoImgCal.pixelHeight);
				soc.putFeature(channel, cellNb, frame, cptgeometry.getFeatures());
			}
		}
	}
}
