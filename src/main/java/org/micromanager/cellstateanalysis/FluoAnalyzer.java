package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.maars.MaarsParameters;
import org.micromanager.utils.ImgUtils;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAnalyzer implements Runnable {

	private ImagePlus fluoImage;
	private SetOfCells soc;
	private double[] factors;
	private ConcurrentHashMap<String, Object> acquisitionMeta;
	private Calibration bfImgCal;
	private SpotCollection spots;

	public FluoAnalyzer(MaarsParameters parameters, ImagePlus fluoImage, Calibration bfImgCal, SetOfCells soc,
			ConcurrentHashMap<String, Object> acquisitionMeta) {
		this.acquisitionMeta = acquisitionMeta;
		this.fluoImage = fluoImage;
		this.soc = soc;
		this.bfImgCal = bfImgCal;
	}

	// /**
	// * if number of spot still higher than expected number of spot in the
	// * channel get the n highest quality spot(s)
	// *
	// * @param visibleOnly
	// */
	// public void findBestNSpotInCell(Boolean visibleOnly) {
	// Spot tmpSpot = null;
	// ArrayList<Integer> idToSkip = new ArrayList<Integer>();
	// SpotCollection newCollection = new SpotCollection();
	// int maxNbSpot = (int)
	// acquisitionMeta.get(MaarsParameters.CUR_MAX_NB_SPOT);
	// if (tmpCollection.getNSpots(visibleOnly) > maxNbSpot) {
	// ReportingUtils.logMessage("Found more than " + maxNbSpot + " spots in
	// cell " + "");
	// for (int i = 0; i < maxNbSpot; i++) {
	// for (Spot s : tmpCollection.iterable(visibleOnly)) {
	// if (tmpSpot == null) {
	// tmpSpot = s;
	// } else {
	// if (Spot.featureComparator("QUALITY").compare(s, tmpSpot) == 1 &&
	// !idToSkip.contains(s.ID())) {
	// tmpSpot = s;
	// }
	// }
	// }
	// newCollection.add(tmpSpot, 0);
	// if (tmpSpot != null) {
	// idToSkip.add(tmpSpot.ID());
	// }
	// tmpSpot = null;
	// }
	// tmpCollection = newCollection;
	// idToSkip = null;
	// newCollection = null;
	// } else {
	// // do nothing
	// }
	// }

	public void run() {
		soc.setAcquisitionMeta(acquisitionMeta);
		soc.addChSpotContainer();
		final ResultsTable roiMeasurements = soc.getRoiMeasurement();
		// TODO project or not
		ImagePlus zProjectedFluoImg = ImgUtils.zProject(fluoImage);
		zProjectedFluoImg = ImgUtils.unitCmToMicron(zProjectedFluoImg);
		SpotsDetector detector = new SpotsDetector(zProjectedFluoImg, acquisitionMeta);
		spots = detector.doDetection();

		this.factors = ImgUtils.getRescaleFactor(bfImgCal, zProjectedFluoImg.getCalibration());

		int nThread = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(nThread);
		int nbCell = soc.size();
		final int[] nbOfCellEachThread = new int[2];
		nbOfCellEachThread[0] = (int) nbCell / nThread;
		nbOfCellEachThread[1] = (int) nbOfCellEachThread[0] + nbCell % nThread;
		for (int i = 0; i < nThread; i++) {
			if (i == 0) {
				es.execute(new AnalyseBlockCells(i, nbOfCellEachThread));
			} else {
				es.execute(new AnalyseBlockCells(i, nbOfCellEachThread));
			}
			System.out.println("Allocating thread : " + i + "_" + acquisitionMeta.get(MaarsParameters.CUR_CHANNEL));
		}
		es.shutdown();
		try {
			es.awaitTermination(15, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

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
			System.out.println("sites :" + begin + " - " + end);
			for (int j = begin; j < end; j++) {
				Cell cell = soc.getCell(j);
				int currCellNb = cell.getCellNumber();

				Roi tmpRoi = null;
				if (factors[0] != factors[1]) {
					tmpRoi = cell.rescaleCellShapeRoi(factors);
				} else {
					tmpRoi = cell.getCellShapeRoi();
				}
				for (Spot s : spots.iterable(false)) {
					if (tmpRoi.contains(
							(int) Math.round(s.getFeature(Spot.POSITION_X) / fluoImage.getCalibration().pixelWidth),
							(int) Math.round(s.getFeature(Spot.POSITION_Y) / fluoImage.getCalibration().pixelHeight))) {
						if (soc.getCurrentFrameNbOfSpot(
								currCellNb) >= (int) acquisitionMeta.get(MaarsParameters.CUR_MAX_NB_SPOT)) {
							Spot lowesetQulitySpot = soc.findLowestQualitySpot(currCellNb);
							if (s.getFeature(Spot.QUALITY) > lowesetQulitySpot.getFeature(Spot.QUALITY)) {
								soc.getCurrentCollectionOfSpot(currCellNb).remove(lowesetQulitySpot,
										Integer.parseInt(MaarsParameters.FRAME));
								soc.putSpot(currCellNb, s);
							}
						} else {
							soc.putSpot(currCellNb, s);
						}
					}
				}
			}
		}
	}
}
