package org.micromanager.maarslib;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.CellChannelFactory;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.maars.MaarsParameters;
import org.micromanager.segmentPombe.SegPombeParameters;
import org.micromanager.utils.FileUtils;
import org.micromanager.utils.ImgUtils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAnalyzer extends Thread {

	private MaarsParameters parameters;
	private String pathToFluoDir;
	private int frame;
	private CellChannelFactory currentFactory;
	private ImagePlus focusImage;
	private ImagePlus zProjectedFluoImg;
	private Calibration bfImgCal;
	private SetOfCells soc;
	private double[] factors;
	private Thread thread;

	public FluoAnalyzer(MaarsParameters parameters, SegPombeParameters segParam, ImagePlus fluoImage, ImagePlus bfImage,
			SetOfCells soc, String channel, int frame, double positionX, double positionY) {
		zProjectedFluoImg = ImgUtils.zProject(fluoImage);
		this.parameters = parameters;
		createCellChannelFactory(channel);
		this.frame = frame;
		this.bfImgCal = bfImage.getCalibration();
		ImageStack stack = bfImage.getStack();
		this.focusImage = new ImagePlus(bfImage.getShortTitle(), stack.getProcessor(segParam.getFocusSlide()));
		focusImage.setCalibration(bfImgCal);
		this.pathToFluoDir = FileUtils.convertPath(parameters.getSavingPath() + "/movie_X" + Math.round(positionX)
				+ "_Y" + Math.round(positionY) + "_FLUO");
		this.soc = soc;
		factors = ImgUtils.getRescaleFactor(bfImgCal, zProjectedFluoImg.getCalibration());
	}

	public void createCellChannelFactory(String currentChannel) {
		currentFactory = new CellChannelFactory(currentChannel,
				Integer.parseInt(parameters.getChMaxNbSpot(currentChannel)),
				Double.parseDouble(parameters.getChSpotRaius(currentChannel)));
	}

	public void run() {
		int nThread = Runtime.getRuntime().availableProcessors();
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
				class Analyzer implements Runnable {
					@Override
					public void run() {
						for (int j = begin; j < end; j++) {
							final Cell cell = soc.getCell(j);
							cell.setFocusImage(ImgUtils.cropImgWithRoi(focusImage, cell.getCellShapeRoi()));
							Roi rescaledRoi = cell.rescaleRoi(factors);
							cell.setFluoImage(ImgUtils.cropImgWithRoi(zProjectedFluoImg, rescaledRoi));
							cell.addCroppedFluoSlice();
							// save cropped cells
							cell.saveCroppedImage(pathToFluoDir);
							// fluoanalysis
							cell.setChannelRelated(currentFactory);
							cell.setCurrentFrame(frame);
							cell.measureBfRoi();
							cell.findFluoSpotTempFunction();
							// can be optional
							FileUtils.writeSpotFeatures(parameters.getSavingPath(), cell.getCellNumber(),
									currentFactory.getChannel(), cell.getModelOf(currentFactory.getChannel()));
						}
					}
				}
				thread = new Thread(new Analyzer(), "SubSet_" + i);
				thread.start();
				cursor += nbOfCellEachThread[1];
			} else {
				final int begin = cursor;
				final int end = cursor + nbOfCellEachThread[0];
				class Analyzer implements Runnable {
					@Override
					public void run() {
						for (int x = begin; x < end; x++) {
							final Cell cell = soc.getCell(x);
							cell.setFocusImage(ImgUtils.cropImgWithRoi(focusImage, cell.getCellShapeRoi()));
							Roi rescaledRoi = cell.rescaleRoi(factors);
							cell.setFluoImage(ImgUtils.cropImgWithRoi(zProjectedFluoImg, rescaledRoi));
							cell.addCroppedFluoSlice();
							// save cropped cells
							cell.saveCroppedImage(pathToFluoDir);
							// fluoanalysis
							cell.setChannelRelated(currentFactory);
							cell.setCurrentFrame(frame);
							cell.measureBfRoi();
							cell.findFluoSpotTempFunction();
							// can be optional
							FileUtils.writeSpotFeatures(parameters.getSavingPath(), cell.getCellNumber(),
									currentFactory.getChannel(), cell.getModelOf(currentFactory.getChannel()));
						}
					}
				}
				thread = new Thread(new Analyzer(), "SubSet_" + i);
				thread.start();
				cursor += nbOfCellEachThread[0];
			}
		}
	}
}
