package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	private ResultsTable roiMeasurements;

	public FluoAnalyzer(MaarsParameters parameters, ImagePlus fluoImage, Calibration bfImgCal, SetOfCells soc,
			ConcurrentHashMap<String, Object> acquisitionMeta) {
		this.acquisitionMeta = acquisitionMeta;
		this.fluoImage = fluoImage;
		this.soc = soc;
		this.bfImgCal = bfImgCal;
	}
	
	/**
	 * if number of spot still higher than expected number of spot in the
	 * channel get the n highest quality spot(s)
	 * 
	 * @param visibleOnly
	 */
	public void findBestNSpotInCell(Boolean visibleOnly) {
		Spot tmpSpot = null;
		ArrayList<Integer> idToSkip = new ArrayList<Integer>();
		SpotCollection newCollection = new SpotCollection();
		int maxNbSpot = (int) acquisitionMeta.get(MaarsParameters.CUR_MAX_NB_SPOT);
		if (tmpCollection.getNSpots(visibleOnly) > maxNbSpot) {
			ReportingUtils.logMessage("Found more than " + maxNbSpot +" spots in cell " + "");
			for (int i = 0; i < maxNbSpot; i++) {
				for (Spot s : tmpCollection.iterable(visibleOnly)) {
					if (tmpSpot == null) {
						tmpSpot = s;
					} else {
						if (Spot.featureComparator("QUALITY").compare(s, tmpSpot) == 1 && !idToSkip.contains(s.ID())) {
							tmpSpot = s;
						}
					}
				}
				newCollection.add(tmpSpot, 0);
				if (tmpSpot != null) {
					idToSkip.add(tmpSpot.ID());
				}
				tmpSpot = null;
			}
			tmpCollection = newCollection;
			idToSkip = null;
			newCollection = null;
		} else {
			// do nothing
		}
	}

	public void run() {
		roiMeasurements = soc.getRoiMeasurement();
		// TODO project or not
		ImagePlus zProjectedFluoImg = ImgUtils.zProject(fluoImage);
		SpotsDetector detector = new SpotsDetector(zProjectedFluoImg, acquisitionMeta);
		spots = detector.doDetection();

		this.factors = ImgUtils.getRescaleFactor(bfImgCal, zProjectedFluoImg.getCalibration());
		soc.setAcquisitionMeta(acquisitionMeta);
		int nThread = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(nThread);
		int nbCell = soc.size();
		final int[] nbOfCellEachThread = new int[2];
		nbOfCellEachThread[0] = (int) nbCell / nThread;
		nbOfCellEachThread[1] = (int) nbOfCellEachThread[0] + nbCell % nThread;
		zProjectedFluoImg = ImgUtils.unitCmToMicron(zProjectedFluoImg);
		int cursor = 0;
		for (int i = 0; i < nThread; i++) {
			if (i == 0) {
				final int begin = cursor;
				final int end = cursor + nbOfCellEachThread[1];
				es.execute(new Runnable() {
					@Override
					public void run() {
						for (int j = begin; j < end; j++) {
							final Cell cell = soc.getCell(j);
							final Roi currentCellShapeRoi = cell.getCellShapeRoi();
							int nbSpotInCell = 0;
							for (Spot s : spots.iterable(false)) {
								if (currentCellShapeRoi.contains((int) Math.round(s.getFeature(Spot.POSITION_X)),
										(int) Math.round(s.getFeature(Spot.POSITION_Y)))) {
									if (nbSpotInCell >= (int) acquisitionMeta.get(MaarsParameters.CUR_MAX_NB_SPOT)) {
										break;
									}
									s.putFeature(SetOfCells.CELL_NUMBER, (double) cell.getCellNumber());
									nbSpotInCell += 1;
								}
							}
//							new SpotsAnalyser(, roiMeasurements);
//
//							Roi rescaledRoi = cell.rescaleCellShapeRoi(factors);
//							cell.setFluoImage(ImgUtils.cropImgWithRoi(zProjectedFluoImg, rescaledRoi));
//							cell.addCroppedFluoSlice();
						}
					}
				});
				cursor += nbOfCellEachThread[1];
			} else {
				final int begin = cursor;
				final int end = cursor + nbOfCellEachThread[0];
				es.execute(new Runnable() {
					@Override
					public void run() {
						for (int j = begin; j < end; j++) {
							final Cell cell = soc.getCell(j);
//							cell.setCurrentMetadata(acquisitionMeta);
//							cell.createContainers();
//							Roi rescaledRoi = cell.rescaleCellShapeRoi(factors);
//							cell.setFluoImage(ImgUtils.cropImgWithRoi(zProjectedFluoImg, rescaledRoi));
//							cell.addCroppedFluoSlice();
//							// fluoanalysis
//							cell.detectSpots();
						}
					}
				});
				cursor += nbOfCellEachThread[0];
			}
		}
		es.shutdown();
		while (!es.isTerminated()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
