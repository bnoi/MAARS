package fiji.plugin.maars.maarslib;

import java.awt.Color;
import java.util.HashMap;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.TaggedImageStorageMultipageTiff;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;
import mmcorej.CMMCore;


/**
 * Acquisition calibrated for image segmentation using package CellsBoundaries_
 *
 * @author marie
 *
 */

public class MaarsAcquisitionForSegmentation {
	private MMStudio gui;
	private CMMCore mmc;
	private AllMaarsParameters parameters;
	private double positionX;
	private double positionY;
	private String pathToMovie;

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
	 */
	public MaarsAcquisitionForSegmentation(MMStudio gui, CMMCore mmc,
			AllMaarsParameters parameters, double positionX, double positionY) {
		this.gui = gui;
		this.mmc = mmc;
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
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
	 */
	public MaarsAcquisitionForSegmentation(MaarsMainDialog md,
			double positionX, double positionY) {
		gui = md.getGui();
		mmc = md.getMMC();
		parameters = md.getParameters();
		this.positionX = positionX;
		this.positionY = positionY;
	}

	/**
	 * Acquire movie for segmentation
	 *
	 * @param show
	 *            : true to show movie during acquisition
	 */
	public void acquire(boolean show) {
		ReportingUtils.logMessage("Acquire movie for segmentation :");
		ReportingUtils.logMessage("________________________________");
		ReportingUtils.logMessage("... Initialize parameters :");
		String rootDirName = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH)
				.getAsString();
		ReportingUtils.logMessage("- saving path : " + rootDirName);
		String channelGroup = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CHANNEL_GROUP)
				.getAsString();
		ReportingUtils.logMessage("- channel group : " + channelGroup);
		String channel = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CHANNEL)
				.getAsString();
		ReportingUtils.logMessage("- channel : " + channel);
		Color color = AllMaarsParameters.getColor(parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject().get(channel).getAsJsonObject()
				.get(AllMaarsParameters.COLOR).getAsString());
		ReportingUtils.logMessage("- color : " + color.toString());
		int frameNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FRAME_NUMBER)
				.getAsInt();
		ReportingUtils.logMessage("- frame number : " + frameNumber);
		double range = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble();
		ReportingUtils.logMessage("- range size : " + range);
		double step = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble();
		ReportingUtils.logMessage("- step : " + step);
		int sliceNumber = (int) Math.round(range / step);
		ReportingUtils.logMessage("- slice number : " + sliceNumber);
		String acqName = "movie_X" + Math.round(positionX) + "_Y"
				+ Math.round(positionY);
		ReportingUtils.logMessage("- acquisition name : " + acqName);
		pathToMovie = rootDirName + acqName;
		
		ReportingUtils.logMessage("... set config");
		try {
			mmc.setConfig(channelGroup, channel);
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not set config");
			e1.printStackTrace();
		}
		
		ReportingUtils
				.logMessage("... Initialize an acquisition");
		try {
			gui.openAcquisition(acqName, rootDirName, frameNumber, 1, sliceNumber, show, true);
		} catch (MMScriptException e2) {
			ReportingUtils.logError(e2);
		}
		ReportingUtils.logMessage("... Set save format to TIF");
		try {
			gui.setImageSavingFormat(TaggedImageStorageMultipageTiff.class);
		} catch (MMScriptException e3) {
			ReportingUtils.logError(e3);
		}
		
		ReportingUtils.logMessage("... Set up Channel");
		try {
			gui.setChannelColor(acqName,0, color);
		} catch (MMScriptException e4) {
			ReportingUtils.logError(e4);
		}
		try {
			gui.setChannelName(acqName, 0, channel);
		} catch (MMScriptException e4) {
			ReportingUtils.logError(e4);
		}
		ReportingUtils.logMessage("... Set shutter open");
		try {
			mmc.setShutterOpen(true);
			mmc.waitForSystem();
		} catch (Exception e1) {
			ReportingUtils.logMessage("could not open shutter");
			e1.printStackTrace();
		}
		ReportingUtils.logMessage("... Get z current position");
		double zFocus = 0;
		try {
			zFocus = mmc.getPosition(mmc.getFocusDevice());
		} catch (Exception e) {
			ReportingUtils.logMessage("could not get z current position");
			e.printStackTrace();
		}
		ReportingUtils.logMessage("-> z focus is " + zFocus);
		ReportingUtils.logMessage("... start acquisition");
		/*important to add 2 µm. Depends on different microscopes.
		in our case, the z-focus position is not in the middle of z range. It
		is often lower than the real medium plan. So we add 2. This parameter 
		needs to be defined by testing on your own microscope.*/
		double z = zFocus - (range / 2) + 2;
		for (int k = 0; k <= sliceNumber; k++) {
			ReportingUtils.logMessage("- set focus device at position " + z);
			try {
				mmc.setPosition(mmc.getFocusDevice(), z);
				mmc.waitForDevice(mmc.getFocusDevice());
			} catch (Exception e) {
				ReportingUtils
						.logMessage("could not set focus device at position");
			}
			ReportingUtils.logMessage("...snap and add images");
			try {
				gui.snapAndAddImage(acqName, frameNumber, 0, k, 0);
			} catch (MMScriptException e) {
				ReportingUtils.logError(e);
			}
			z = z + step;
		}
		ReportingUtils.logMessage("finish image cache");
		try {
			gui.getAcquisitionImageCache(acqName).finished();
		} catch (MMScriptException e1) {
			ReportingUtils.logError(e1);
		}
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
			e.printStackTrace();
		}

	}

	/**
	 * Get parameters for acquisition
	 */
	public HashMap<String, String> getParametersFromConf(
			AllMaarsParameters parameters) {
		HashMap<String, String> params = new HashMap<String, String>();
		try {
			mmc.clearROI();
		} catch (Exception e2) {
			ReportingUtils.logMessage("Could not clear ROI");
			e2.printStackTrace();
		}
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
		params.put("channelGroup", channelGroup);
		String channel = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CHANNEL)
				.getAsString();
		ReportingUtils.logMessage("- channel : " + channel);
		params.put("channel", channel);
		String shutter = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject().get(channel).getAsJsonObject()
				.get(AllMaarsParameters.SHUTTER).getAsString();
		ReportingUtils.logMessage("- shutter : " + shutter);
		params.put("shutter", shutter);
		Color color = AllMaarsParameters.getColor(parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject().get(channel).getAsJsonObject()
				.get(AllMaarsParameters.COLOR).getAsString());
		ReportingUtils.logMessage("- color : " + color.toString());
		params.put("color", color.toString());
		String exposure = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject().get(channel).getAsJsonObject()
				.get(AllMaarsParameters.EXPOSURE).getAsString();
		ReportingUtils.logMessage("- exposure : " + exposure);
		params.put("exposure", exposure);
		return params;
	}

	/**
	 * Set up parameters for acquisition
	 */
	public void setParameters(HashMap<String, String> params) {
		ReportingUtils.logMessage("... Set shutter device");
		try {
			mmc.setShutterDevice(params.get("shutter"));
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not set shutter device");
			e1.printStackTrace();
		}
		ReportingUtils.logMessage("... Set exposure");
		try {
			mmc.setExposure(Integer.parseInt(params.get("exposure")));
		} catch (Exception e1) {
			ReportingUtils.logMessage("could not set exposure");
			e1.printStackTrace();
		}
		ReportingUtils.logMessage("... Set config");
		try {
			mmc.setConfig(params.get("channelGroup"), params.get("channel"));
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not set config");
			e1.printStackTrace();
		}
		ReportingUtils.logMessage("... Wait for config");
		try {
			mmc.waitForConfig(params.get("channelGroup"), params.get("channel"));
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not wait for config");
			e1.printStackTrace();
		}
	}

	/**
	 *
	 * @return path to movie acquired
	 */
	public String getPathToMovie() {
		return pathToMovie;
	}
}