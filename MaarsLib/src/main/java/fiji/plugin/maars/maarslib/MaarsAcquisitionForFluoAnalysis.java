package fiji.plugin.maars.maarslib;

import java.awt.Color;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.json.JSONException;
import org.micromanager.MMStudio;
import org.micromanager.api.MMTags;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.acquisition.AcquisitionManager;
import org.micromanager.acquisition.MMAcquisition;

import fiji.plugin.maars.cellstateanalysis.SetOfCells;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiScaler;
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
	private AcquisitionManager acqMgr = new AcquisitionManager();
	private MMAcquisition acqForFluo;

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
				+ Math.round(positionY) + "FLUO"
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			ReportingUtils.logMessage("Could not wait for config");
			e1.printStackTrace();
		}
		// TODO
		try {
			acqMgr.openAcquisition(acqName, rootDirName, show, true);
		} catch (MMScriptException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		ReportingUtils.logMessage("... Get acquisition");
		try {
			acqForFluo = acqMgr.getAcquisition(acqName);
		} catch (MMScriptException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		acqForFluo.setImagePhysicalDimensions((int) mmc.getImageWidth(),
				(int) mmc.getImageHeight(), 1, 16, 1);
		ReportingUtils.logMessage("... set channel color");
		try {
			acqForFluo.setChannelColor(0, color.getRGB());
		} catch (MMScriptException e) {
			ReportingUtils.logMessage("Could not set channel color");
			e.printStackTrace();
		}
		ReportingUtils.logMessage("... set channel name");
		try {
			acqForFluo.setChannelName(0, channel);
		} catch (MMScriptException e) {
			ReportingUtils.logMessage("could not set channel name");
			e.printStackTrace();
		}
		acqForFluo.initialize();
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
			e.printStackTrace();
		}
		ReportingUtils.logMessage("-> z focus is " + zFocus);

		ReportingUtils.logMessage("... start acquisition");
		double z = zFocus - (range / 2);

		ImageStack imageStack = new ImageStack((int) mmc.getImageWidth(),
				(int) mmc.getImageHeight());

		for (int k = 0; k <= sliceNumber; k++) {
			ReportingUtils.logMessage("- set focus device at position " + z);
			try {
				mmc.setPosition(mmc.getFocusDevice(), z);
				mmc.waitForDevice(mmc.getFocusDevice());
			} catch (Exception e) {
				ReportingUtils
						.logMessage("could not set focus device at position");
				e.printStackTrace();
			}

			try {
				mmc.snapImage();
			} catch (Exception e) {
				ReportingUtils.logMessage("could not snape image");
				e.printStackTrace();
			}

			TaggedImage img = null;
			try {
				img = mmc.getTaggedImage();
			} catch (Exception e) {
				ReportingUtils.logMessage("could not get tagged image");
				e.printStackTrace();
			}

			try {
				img.tags.put(MMTags.Image.SLICE_INDEX, k);
				img.tags.put(MMTags.Image.FRAME_INDEX, 0);
				img.tags.put(MMTags.Image.CHANNEL_INDEX, 0);
				img.tags.put(MMTags.Image.ZUM, z);
				img.tags.put(MMTags.Image.XUM, 0);
				img.tags.put(MMTags.Image.YUM, 0);

			} catch (JSONException e) {
				ReportingUtils.logMessage("could not tag image");
				e.printStackTrace();
			}

			try {
				// TODO
				acqForFluo.insertTaggedImage(img, frameNumber, 1, k);
			} catch (MMScriptException e) {
				ReportingUtils.logMessage("could not add image to gui");
				ReportingUtils.logError(e);
			}

			ShortProcessor shortProcessor = new ShortProcessor(
					(int) mmc.getImageWidth(), (int) mmc.getImageHeight());
			shortProcessor.setPixels(img.pix);

			imageStack.addSlice(shortProcessor);

			z = z + step;
		}

		ImagePlus imagePlus = new ImagePlus("Maars", imageStack);
		Calibration cal = new Calibration();
		cal.setUnit("micron");
		cal.pixelWidth = mmc.getPixelSizeUm();
		cal.pixelHeight = mmc.getPixelSizeUm();
		cal.pixelDepth = step;
		imagePlus.setCalibration(cal);

		ReportingUtils.logMessage("finish image cache");
		acqForFluo.getImageCache().finished();

		ReportingUtils.logMessage("--- Acquisition done.");
		gui.closeAllAcquisitions();

		try {
			mmc.setPosition(mmc.getFocusDevice(), zFocus);
			mmc.waitForDevice(mmc.getFocusDevice());
			mmc.setShutterOpen(false);
			mmc.waitForDevice(mmc.getShutterDevice());
		} catch (Exception e) {
			System.out
					.println("could not set focus device back to position and close shutter");
			e.printStackTrace();
		}

		return imagePlus;
	}
}
