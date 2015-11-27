package org.micromanager.maarslib;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.CellChannelFactory;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.internal.utils.ReportingUtils;
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

	public FluoAnalyzer(MaarsParameters parameters, SegPombeParameters segParam, ImagePlus fluoImage, ImagePlus bfImage,
			String channel, int frame, double positionX, double positionY) {
		zProjectedFluoImg = ImgUtils.zProject(fluoImage);
		createCellChannelFactory(channel);
		this.frame = frame;
		this.bfImgCal = bfImage.getCalibration();
		ImageStack stack = bfImage.getStack();
		this.focusImage = new ImagePlus(bfImage.getShortTitle(), stack.getProcessor(segParam.getFocusSlide()));
		focusImage.setCalibration(bfImgCal);
		this.pathToFluoDir = FileUtils.convertPath(parameters.getSavingPath() + "/movie_X" + Math.round(positionX)
				+ "_Y" + Math.round(positionY) + "_FLUO");
		soc = new SetOfCells(segParam);
	}

	public void createCellChannelFactory(String currentChannel) {
		currentFactory = new CellChannelFactory(currentChannel,
				Integer.parseInt(parameters.getChMaxNbSpot(currentChannel)),
				Double.parseDouble(parameters.getChSpotRaius(currentChannel)));
	}

	public void run() {
		int nThread = Runtime.getRuntime().availableProcessors();
		int nbCell = soc.size();
		double[] nbOfCellEachThread = new double[2];
		nbOfCellEachThread[0] = nbCell / nThread;
		nbOfCellEachThread[1] = nbCell - (nbOfCellEachThread[0] * (nThread - 1));
		ReportingUtils.logMessage("Cropping cell");
		zProjectedFluoImg = ImgUtils.unitCmToMicron(zProjectedFluoImg);
		double[] factors = ImgUtils.getRescaleFactor(bfImgCal, zProjectedFluoImg.getCalibration());
		// TODO to split soc
		for (Cell cell : soc) {
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
			// FileUtils.writeSpotFeatures(parameters.getSavingPath(),
			// cell.getCellNumber(), currentFactory.getChannel(),
			// cell.getModelOf(currentFactory.getChannel()));
		}
	}
}
