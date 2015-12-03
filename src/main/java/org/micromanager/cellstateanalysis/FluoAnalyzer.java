package org.micromanager.cellstateanalysis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.micromanager.maars.MaarsParameters;
import org.micromanager.utils.ImgUtils;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAnalyzer implements Runnable {

	private ImagePlus zProjectedFluoImg;
	private SetOfCells soc;
	private double[] factors;
	private ConcurrentHashMap<String, Object> acquisitionMeta;

	public FluoAnalyzer(MaarsParameters parameters, ImagePlus fluoImage, Calibration bfImgCal, SetOfCells soc,
			ConcurrentHashMap<String, Object> acquisitionMeta) {
		this.acquisitionMeta = acquisitionMeta;
		this.zProjectedFluoImg = ImgUtils.zProject(fluoImage);
		this.soc = soc;
		this.factors = ImgUtils.getRescaleFactor(bfImgCal, zProjectedFluoImg.getCalibration());
		soc.setAcquisitionMeta(acquisitionMeta);
	}

	public void run() {
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
							cell.setCurrentMetadata(acquisitionMeta);
							cell.createContainers();
							Roi rescaledRoi = cell.rescaleCellShapeRoi(factors);
							cell.setFluoImage(ImgUtils.cropImgWithRoi(zProjectedFluoImg, rescaledRoi));
							cell.addCroppedFluoSlice();
							// fluoanalysis
							cell.detectSpots();
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
							cell.setCurrentMetadata(acquisitionMeta);
							cell.createContainers();
							Roi rescaledRoi = cell.rescaleCellShapeRoi(factors);
							cell.setFluoImage(ImgUtils.cropImgWithRoi(zProjectedFluoImg, rescaledRoi));
							cell.addCroppedFluoSlice();
							// fluoanalysis
							cell.detectSpots();
						}
					}
				});
				cursor += nbOfCellEachThread[0];
			}
		}
		es.shutdown();
		while (!es.isTerminated()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
