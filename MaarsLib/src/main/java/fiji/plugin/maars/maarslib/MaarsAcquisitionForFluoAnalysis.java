package fiji.plugin.maars.maarslib;

import java.awt.Color;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.micromanager.MMStudio;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

import fiji.plugin.maars.cellstateanalysis.SetOfCells;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiScaler;
import ij.plugin.ZProjector;
import ij.process.ShortProcessor;

/**
 * Acquisition calibrated for fluorescent image analysis using package
 * CellStateAnalysis
 * 
 * @author marie
 *
 */
public class MaarsAcquisitionForFluoAnalysis {

	private MMStudio gui;
	private CMMCore mmc;
	private AllMaarsParameters parameters;
	private double positionX;
	private double positionY;
	private SetOfCells soc;

	/**
	 * Constructor :
	 * 
	 * @param gui
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param positionX
	 *            : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY
	 *            : y field position (can be defined by ExplorationXYPositions)
	 * @param soc
	 *            : Set of cells (object from CellStateAnalysis)
	 */
	public MaarsAcquisitionForFluoAnalysis(MMStudio gui, CMMCore mmc,
			AllMaarsParameters parameters, double positionX, double positionY,
			SetOfCells soc) {
		this.gui = gui;
		this.mmc = mmc;
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.soc = soc;
	}

	/**
	 * Constructor :
	 * 
	 * @param md
	 *            : main window of MAARS (contains gui, mmc and parameters)
	 * @param positionX
	 *            : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY
	 *            : y field position (can be defined by ExplorationXYPositions)
	 * @param soc
	 *            : Set of cells (object from CellStateAnalysis_)
	 */
	public MaarsAcquisitionForFluoAnalysis(MaarsMainDialog md,
			double positionX, double positionY, SetOfCells soc) {
		gui = md.getGui();
		mmc = md.getMMC();
		parameters = md.getParameters();
		this.positionX = positionX;
		this.positionY = positionY;
		this.soc = soc;
	}

	/**
	 * Crop image to film only one cell then acquire specific movie
	 * 
	 * @param show
	 *            : true to see acquisition in live
	 * @param cellNumber
	 *            : index of cell filmed
	 * @return ImagePlus object
	 */
	public ImagePlus acquire(boolean show, int cellNumber) {

		try {
			mmc.clearROI();
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not clear Roi");
			e1.printStackTrace();
		}

		String acqName = "movie_X" + Math.round(positionX) + "_Y"
				+ Math.round(positionY) + "_FLUO_"
				+ soc.getCell(cellNumber).getCellShapeRoi().getName();

		double wtest = mmc.getImageWidth();
		double htest = mmc.getImageHeight();
		int margin = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MARGIN_AROUD_CELL)
				.getAsInt();

		Roi newroi = RoiScaler.scale(soc.getCell(cellNumber).getCellShapeRoi(),
				wtest / soc.getBFImage().getWidth(), htest
						/ soc.getBFImage().getHeight(), false);

		try {
			mmc.setROI((int) newroi.getXBase() - margin,
					(int) newroi.getYBase() - margin,
					(int) newroi.getBounds().width + margin * 2,
					(int) newroi.getBounds().height + margin * 2);
		} catch (Exception e) {
			ReportingUtils.logMessage("could not crop live image");
			ReportingUtils.logError(e);
		}

		try {
			return acquire(show, acqName);
		} catch (MMScriptException e) {
			return null;
		}
	}

	/**
	 * Acquire specific movie
	 * 
	 * @param show
	 *            : true to see acquisition in live
	 * @param acqName
	 *            : name of acquisition
	 * @return ImagePlus object
	 */
	public ImagePlus acquire(boolean show, String acqName)
			throws MMScriptException {

		boolean save = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SAVE_FLUORESCENT_MOVIES).getAsBoolean();

		if (!save) {
			show = false;
		}

		ReportingUtils.logMessage("Acquire movie for fluorescent analysis :");
		ReportingUtils.logMessage("________________________________");

		ReportingUtils.logMessage("Close all previous acquisitions");
		gui.closeAllAcquisitions();
		try {
			gui.clearMessageWindow();
		} catch (MMScriptException e) {
			ReportingUtils.logMessage("could not clear message window");
			ReportingUtils.logError(e);
		}
		ReportingUtils.logMessage("... Initialize parameters :");

		String channelGroup = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CHANNEL_GROUP)
				.getAsString();
		ReportingUtils.logMessage("- channel group : " + channelGroup);

		String rootDirName = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH)
				.getAsString();
		ReportingUtils.logMessage("- saving path : " + rootDirName);

		String channel = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CHANNEL)
				.getAsString();
		ReportingUtils.logMessage("- channel : " + channel);

		String shutter = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject().get(channel).getAsJsonObject()
				.get(AllMaarsParameters.SHUTTER).getAsString();
		ReportingUtils.logMessage("- shutter : " + shutter);

		Color color = AllMaarsParameters.getColor(parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject().get(channel).getAsJsonObject()
				.get(AllMaarsParameters.COLOR).getAsString());
		ReportingUtils.logMessage("- color : " + color.toString());

		int exposure = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject().get(channel).getAsJsonObject()
				.get(AllMaarsParameters.EXPOSURE).getAsInt();
		ReportingUtils.logMessage("- exposure : " + exposure);

		int frameNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FRAME_NUMBER)
				.getAsInt();
		ReportingUtils.logMessage("- frame number : " + frameNumber);

		double range = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble();
		ReportingUtils.logMessage("- range size : " + range);

		double step = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble();
		ReportingUtils.logMessage("- step : " + step);

		int sliceNumber = (int) Math.round(range / step);
		ReportingUtils.logMessage("- slice number : " + sliceNumber);

		ReportingUtils.logMessage("- acquisition name : " + acqName);

		ReportingUtils.logMessage("... Set shutter device");
		try {
			mmc.setShutterDevice(shutter);
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not set shutter device");
			e1.printStackTrace();
		}

		ReportingUtils.logMessage("... Set exposure");
		try {
			mmc.setExposure(exposure);
		} catch (Exception e1) {
			ReportingUtils.logMessage("could not set exposure");
			e1.printStackTrace();
		}

		ReportingUtils.logMessage("... set config");
		try {
			mmc.setConfig(channelGroup, channel);
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not set config");
			e1.printStackTrace();
		}

		ReportingUtils.logMessage("... wait for config");
		try {
			mmc.waitForConfig(channelGroup, channel);
		} catch (Exception e1) {
			ReportingUtils.logError(e1);
		}
		try {
			gui.openAcquisition(acqName, rootDirName, frameNumber, 1 , sliceNumber+1,show,save);
		} catch (MMScriptException e2) {
			ReportingUtils.logError(e2);
		}
		ReportingUtils.logMessage("... set channel color");
		try {
			gui.setChannelColor(acqName,0, color);
		} catch (MMScriptException e) {
			ReportingUtils.logMessage("Could not set channel color");
			ReportingUtils.logError(e);
		}
		ReportingUtils.logMessage("... set channel name");
		try {
			gui.setChannelName(acqName, 0, channel);
		} catch (MMScriptException e) {
			ReportingUtils.logMessage("could not set channel name");
			ReportingUtils.logError(e);
		}
		ReportingUtils.logMessage("... set shutter open");
		try {
			mmc.setShutterOpen(true);
		} catch (Exception e1) {
			ReportingUtils.logMessage("could not open shutter");
			e1.printStackTrace();
		}

		ReportingUtils.logMessage("... get z current position");
		double zFocus = 0;
		try {
			zFocus = mmc.getPosition(mmc.getFocusDevice());
		} catch (Exception e) {
			ReportingUtils.logMessage("could not get z current position");
			ReportingUtils.logError(e);
		}

		ReportingUtils.logMessage("-> z focus is " + zFocus);

		ReportingUtils.logMessage("... start acquisition");
		//TODO 
		double z = zFocus - (range / 2) + 2;
		ReportingUtils.logMessage("- create imagestack");
		ImageStack imageStack = new ImageStack((int) mmc.getImageWidth(),
				(int) mmc.getImageHeight());

		for (int k = 0; k <= sliceNumber; k++) {
//			ReportingUtils.logMessage("- set focus device at position " + z);
			try {
				mmc.setPosition(mmc.getFocusDevice(), z);
				mmc.waitForDevice(mmc.getFocusDevice());
			} catch (Exception e) {
				ReportingUtils
						.logMessage("could not set focus device at position");
				ReportingUtils.logError(e);
			}
			gui.snapAndAddImage(acqName, 0, 0, k, 0);
			MMAcquisition acq = gui
					.getAcquisitionWithName(acqName);

			TaggedImage img = acq.getImageCache().getImage(
					0, k, 0, 0);
//			ReportingUtils.logMessage("- create short processor");
			ShortProcessor shortProcessor = new ShortProcessor(
					(int) mmc.getImageWidth(), (int) mmc.getImageHeight());
			shortProcessor.setPixels(img.pix);

//			ReportingUtils.logMessage("- add slice to imagestack");
			imageStack.addSlice(shortProcessor);

			z = z + step;
		}
		ImagePlus imagePlus = new ImagePlus("Maars", imageStack);
		ZProjector projector = new ZProjector();
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.setImage(imagePlus);
		projector.doProjection();
		ImagePlus zProjectField = projector.getProjection();
		Calibration cal = new Calibration();
		cal.setUnit("micron");
		cal.pixelWidth = mmc.getPixelSizeUm();
		cal.pixelHeight = mmc.getPixelSizeUm();
		zProjectField.setCalibration(cal);
		ReportingUtils.logMessage("--- Acquisition done.");
		gui.closeAllAcquisitions();
		try {
			mmc.setPosition(mmc.getFocusDevice(), zFocus);
			mmc.waitForDevice(mmc.getFocusDevice());
			mmc.setShutterOpen(false);
			mmc.waitForDevice(mmc.getShutterDevice());
		} catch (Exception e) {
			ReportingUtils
					.logMessage("could not set focus device back to position and close shutter");
			ReportingUtils.logError(e);
		}
		return zProjectField;
	}
}
