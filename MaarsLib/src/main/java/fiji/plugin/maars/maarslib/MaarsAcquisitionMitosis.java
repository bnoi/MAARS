package fiji.plugin.maars.maarslib;

import fiji.plugin.maars.cellstateanalysis.Cell;
import fiji.plugin.maars.cellstateanalysis.Spindle;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiScaler;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.micromanager.MMStudio;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.api.Autofocus;
import org.micromanager.utils.MMException;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Class to film mitosis.
 * 
 * @author marie
 *
 */
public class MaarsAcquisitionMitosis {

	private MMStudio gui;
	private CMMCore mmc;
	private AllMaarsParameters parameters;
	private MaarsFluoAnalysis mfa;
	private double positionX;
	private double positionY;

	/**
	 * Constructor :
	 * 
	 * @param gui
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param mfa
	 *            : contains everything need to detect and measure mitotic
	 *            spindle
	 * @param positionX
	 *            : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY
	 *            : y field position (can be defined by ExplorationXYPositions)
	 */
	public MaarsAcquisitionMitosis(MMStudio gui, CMMCore mmc,
			AllMaarsParameters parameters, MaarsFluoAnalysis mfa,
			double positionX, double positionY) {

		this.gui = gui;
		this.mmc = mmc;
		this.parameters = parameters;
		this.mfa = mfa;

		this.positionX = positionX;
		this.positionY = positionY;
	}

	/**
	 * Constructor :
	 * 
	 * @param md
	 *            : main window of MAARS (contains gui, mmc and parameters)
	 * @param mfa
	 *            : contains everything need to detect and measure mitotic
	 *            spindle
	 * @param positionX
	 *            : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY
	 *            : y field position (can be defined by ExplorationXYPositions)
	 */
	public MaarsAcquisitionMitosis(MaarsMainDialog md, MaarsFluoAnalysis mfa,
			double positionX, double positionY) {

		this.gui = md.getGui();
		this.mmc = md.getMMC();
		this.parameters = md.getParameters();
		this.mfa = mfa;

		this.positionX = positionX;
		this.positionY = positionY;
	}

	/**
	 * Method to adjust Region of Interest around the cell in mitotic state
	 * (cell to film)
	 * 
	 * @param cellNumber
	 *            : index of cell to film
	 */
	public void crop(Roi cellRoi) {

		try {
			mmc.clearROI();
		} catch (Exception e1) {
			ReportingUtils.logMessage("Could not clear Roi");
			e1.printStackTrace();
		}

		double wtest = mmc.getImageWidth();
		double htest = mmc.getImageHeight();
		int margin = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MARGIN_AROUD_CELL)
				.getAsInt();

		Roi newroi = RoiScaler.scale(cellRoi, wtest
				/ mfa.getSetOfCells().getBFImage().getWidth(), htest
				/ mfa.getSetOfCells().getBFImage().getHeight(), false);

		try {
			mmc.setROI((int) newroi.getXBase() - margin,
					(int) newroi.getYBase() - margin,
					(int) newroi.getBounds().width + margin * 2,
					(int) newroi.getBounds().height + margin * 2);
		} catch (Exception e) {
			ReportingUtils.logMessage("could not crop live image");
			e.printStackTrace();
		}
	}

	/**
	 * Method to acquire movie of mitosis
	 * 
	 * @param show
	 *            : true to show the movie during acquisition
	 * @param cellNumber
	 *            : index of cell to film
	 * @param crop
	 *            : true to adjust ROI around cell to film
	 * @param adjustZRange
	 *            : true adjust range around focal plane
	 */
	public void acquire(boolean show, Cell cell, boolean crop,
			boolean adjustZRange) throws MMScriptException {

		if (crop) {
			crop(cell.getCellShapeRoi());
		}

		boolean keepFilming = true;

		ReportingUtils.logMessage("Acquire Mitosis Movie :");
		ReportingUtils.logMessage("_______________________");
		String fluoAcqName = "movie_X" + Math.round(positionX) + "_Y"
				+ Math.round(positionY) + "_"
				+ cell.getCellShapeRoi().getName() + "_Fluo";
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
				.getAsString()
				+ "movie_X"
				+ Math.round(positionX)
				+ "_Y"
				+ Math.round(positionY) + "/MITOSIS/";
		ReportingUtils.logMessage("- saving path : " + rootDirName);

		JsonArray channelArray = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CHANNEL)
				.getAsJsonArray();

		Iterator<JsonElement> channelIterator = channelArray.iterator();

		String[] channels = new String[channelArray.size()];
		String[] shutters = new String[channelArray.size()];
		Color[] colors = new Color[channelArray.size()];
		int[] exposures = new int[channelArray.size()];
		int channelParam = 0;

		while (channelIterator.hasNext()) {

			channels[channelParam] = channelIterator.next().getAsString();
			ReportingUtils
					.logMessage("--- channel : " + channels[channelParam]);

			shutters[channelParam] = parameters.getParametersAsJsonObject()
					.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
					.getAsJsonObject()
					.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
					.getAsJsonObject().get(channels[channelParam])
					.getAsJsonObject().get(AllMaarsParameters.SHUTTER)
					.getAsString();
			ReportingUtils.logMessage("- shutter : " + shutters[channelParam]);

			colors[channelParam] = AllMaarsParameters.getColor(parameters
					.getParametersAsJsonObject()
					.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
					.getAsJsonObject()
					.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
					.getAsJsonObject().get(channels[channelParam])
					.getAsJsonObject().get(AllMaarsParameters.COLOR)
					.getAsString());
			ReportingUtils.logMessage("- color : "
					+ colors[channelParam].toString());

			exposures[channelParam] = parameters.getParametersAsJsonObject()
					.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
					.getAsJsonObject()
					.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
					.getAsJsonObject().get(channels[channelParam])
					.getAsJsonObject().get(AllMaarsParameters.EXPOSURE)
					.getAsInt();
			ReportingUtils
					.logMessage("- exposure : " + exposures[channelParam]);
			channelParam++;
		}

		int frameNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FRAME_NUMBER)
				.getAsInt();
		ReportingUtils.logMessage("- frame number : " + frameNumber);

		int timeInterval = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.TIME_INTERVAL)
				.getAsInt();
		ReportingUtils.logMessage("- time interval " + timeInterval);

		double fluoRange = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble();
		ReportingUtils.logMessage("- range size : " + fluoRange);

		double fluoStep = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble();
		ReportingUtils.logMessage("- step : " + fluoStep);

		int sliceNumber = (int) Math.round(fluoRange / fluoStep);
		ReportingUtils.logMessage("- fluo slice number : " + sliceNumber);

		double zStartComparedToZFocus = -(fluoRange / 2);

		if (adjustZRange) {
			ReportingUtils.logMessage("---> adjust z range");
			ReportingUtils.logMessage("     get coordinates");
			double[] spbCoord = cell.getLastSpindleComputed()
					.getSPBCoordinates();
			ReportingUtils.logMessage("     get slice number for analysis : ");
			int sliceNbInFluoAnalysis = (int) Math.round(parameters
					.getParametersAsJsonObject()
					.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
					.getAsJsonObject()
					.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE).getAsDouble()
					/ parameters.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject().get(AllMaarsParameters.STEP)
							.getAsDouble()) + 1;
			ReportingUtils.logMessage("     " + sliceNbInFluoAnalysis);

			double rangeBetweenTwoSpots = Math.abs(spbCoord[2] - spbCoord[5])
					* parameters.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject().get(AllMaarsParameters.STEP)
							.getAsDouble();
			ReportingUtils.logMessage("     range between 2 spots "
					+ rangeBetweenTwoSpots);

			if (spbCoord[2] > spbCoord[5]) {
				zStartComparedToZFocus = (spbCoord[5] - (sliceNbInFluoAnalysis / 2))
						* parameters
								.getParametersAsJsonObject()
								.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
								.getAsJsonObject().get(AllMaarsParameters.STEP)
								.getAsDouble();
			} else {
				zStartComparedToZFocus = (spbCoord[2] - (sliceNbInFluoAnalysis / 2))
						* parameters
								.getParametersAsJsonObject()
								.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
								.getAsJsonObject().get(AllMaarsParameters.STEP)
								.getAsDouble();
			}
			zStartComparedToZFocus = zStartComparedToZFocus
					- (fluoRange - rangeBetweenTwoSpots) / 2;

			ReportingUtils.logMessage("     z start : "
					+ zStartComparedToZFocus);
		}

		ReportingUtils.logMessage("- fluo acquisition name : " + fluoAcqName);
		gui.openAcquisition(fluoAcqName, rootDirName, frameNumber,
				channelArray.size(), sliceNumber + 1, show, true);
		gui.setImageSavingFormat(org.micromanager.acquisition.TaggedImageStorageMultipageTiff.class);

		ReportingUtils.logMessage("... set acquisition parameters");
		for (int i = 0; i < channels.length; i++) {

			ReportingUtils.logMessage("... set fluo channel color");
			try {
				gui.setChannelColor(fluoAcqName, i, colors[i]);
			} catch (MMScriptException e) {
				ReportingUtils.logError(e);
			}
			ReportingUtils.logMessage("... set fluo channel name");
			try {
				gui.setChannelName(fluoAcqName, i, channels[i]);
			} catch (MMScriptException e) {
				ReportingUtils.logError(e);
			}
		}
		double zFocus = 0;
		ImagePlus lastImage = null;

		double startTime = System.currentTimeMillis();
		int frame = 0;
		Autofocus autofocus = gui.getAutofocus();
		ReportingUtils.logMessage("start time : " + startTime);
		while (keepFilming) {
			double beginAcq = System.currentTimeMillis();

			for (int channel = 0; channel < channels.length; channel++) {
				ReportingUtils.logMessage("... set config");
				try {
					mmc.setConfig(channelGroup, channels[channel]);
				} catch (Exception e1) {
					ReportingUtils.logMessage("Could not set config");
					e1.printStackTrace();
				}

				ReportingUtils.logMessage("... wait for config");
				try {
					mmc.waitForConfig(channelGroup, channels[channel]);
				} catch (Exception e1) {
					ReportingUtils.logMessage("Could not wait for config");
					e1.printStackTrace();
				}

				ReportingUtils.logMessage("... set shutter open");
				try {
					mmc.setShutterOpen(true);
				} catch (Exception e1) {
					ReportingUtils.logMessage("could not open shutter");
					e1.printStackTrace();
				}
				try {
					mmc.waitForSystem();
				} catch (Exception e3) {
					ReportingUtils.logError(e3);
				}
				ReportingUtils.logMessage("... get z current position");

				try {
					zFocus = mmc.getPosition(mmc.getFocusDevice());
				} catch (Exception e) {
					ReportingUtils
							.logMessage("could not get z current position");
					e.printStackTrace();
				}
				ReportingUtils.logMessage("-> z focus is " + zFocus);

				ReportingUtils.logMessage("... start acquisition");
				ImageStack fluoImageStack = new ImageStack(
						(int) mmc.getImageWidth(), (int) mmc.getImageHeight());
				//TODO
				double fluoZ = zFocus - (fluoRange / 2) + 2;
				for (int k = 0; k <= sliceNumber; k++) {
					ReportingUtils.logMessage("- set focus device at position "
							+ fluoZ);
					try {
						mmc.setPosition(mmc.getFocusDevice(), fluoZ);
						mmc.waitForDevice(mmc.getFocusDevice());
					} catch (Exception e) {
						ReportingUtils
								.logMessage("could not set focus device at position");
						e.printStackTrace();
					}
					gui.snapAndAddImage(fluoAcqName, frame, channel, k, 0);

					if (channels[channel].equals(parameters
							.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject().get(AllMaarsParameters.CHANNEL)
							.getAsString())) {
						MMAcquisition acq = gui
								.getAcquisitionWithName(fluoAcqName);
						TaggedImage img = acq.getImageCache().getImage(channel,
								k, frame, 0);
						ShortProcessor shortProcessor = new ShortProcessor(
								(int) mmc.getImageWidth(),
								(int) mmc.getImageHeight());
						shortProcessor.setPixels(img.pix);

						fluoImageStack.addSlice(shortProcessor);
					}

					fluoZ = fluoZ + fluoStep;

				}

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

				if (channels[channel].equals(parameters
						.getParametersAsJsonObject()
						.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.CHANNEL)
						.getAsString())) {
					ReportingUtils.logMessage("... set config");
					ImagePlus fluoImagePlus = new ImagePlus("Maars_"
							+ fluoAcqName, fluoImageStack);
					Calibration cal = new Calibration();
					cal.setUnit("micron");
					cal.pixelWidth = mmc.getPixelSizeUm();
					cal.pixelHeight = mmc.getPixelSizeUm();
					cal.pixelDepth = fluoStep;
					fluoImagePlus.setCalibration(cal);
					cell.addFluoImage(fluoImagePlus);
					cell.findFluoSpotTempFunction(
							false,
							parameters
									.getParametersAsJsonObject()
									.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
									.getAsJsonObject()
									.get(AllMaarsParameters.SPOT_RADIUS)
									.getAsDouble());
					String savingPath = rootDirName + fluoAcqName + "/" + frame
							+ "_";
					writeSpinldeCoord(cell, savingPath);
					keepFilming = checkEndMovieConditions(lastImage,
							fluoImagePlus, startTime, cell, frame);
					if (frame == 0) {
						lastImage = new ImagePlus("Maars_" + fluoAcqName,
								fluoImageStack);
						lastImage.setCalibration(cal);
					} else {
						lastImage.setImage(fluoImagePlus);
					}
					try {
						mmc.setConfig(channelGroup, "BF");
					} catch (Exception e1) {
						ReportingUtils.logMessage("Could not set config");
						e1.printStackTrace();
					}

					ReportingUtils.logMessage("... wait for config");
					try {
						mmc.waitForConfig(channelGroup, "BF");
					} catch (Exception e1) {
						ReportingUtils.logMessage("Could not wait for config");
						e1.printStackTrace();
					}
					try {
						autofocus.fullFocus();
					} catch (MMException e2) {
						ReportingUtils.logError(e2);
					}
				}
			}

			double acqTook = System.currentTimeMillis() - beginAcq;
			try {
				gui.sleep((long) (timeInterval - acqTook));
			} catch (MMScriptException e) {
				ReportingUtils.logMessage("could not sleep "
						+ (timeInterval - acqTook) + " ms");
				e.printStackTrace();
			}
			frame++;
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
	 * Method to check if new image acquired fit condition to keep filming
	 * 
	 * @param lastImage
	 *            : image acquired before image to analyse
	 * @param newImage
	 *            : image to analyse
	 * @param startTime
	 *            : time of beginning of acquisition (in ms)
	 * @param cell
	 *            : cell to check
	 * @param frame
	 *            : time point of image (0 if it is first image acquired)
	 * @return true if system should keep filming
	 */
	public boolean checkEndMovieConditions(ImagePlus lastImage,
			ImagePlus newImage, double startTime, Cell cell, int frame) {
		boolean ok = true;

		boolean checkTimeLimit = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.TIME_LIMIT)
				.getAsBoolean();

		boolean checkAbsMaxSpSize = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean();

		boolean checkRelativeMaxSpSize = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean();

		boolean checkRelativeSpAngle = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE).getAsBoolean();

		boolean checkGrowing = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.GROWING_SPINDLE)
				.getAsBoolean();

		if (frame == 0) {
			checkGrowing = false;
			ReportingUtils.logMessage("can not check growing (yet)");
		}

		if (checkTimeLimit) {

			ReportingUtils.logMessage("check time limit");
			double timeLimit = parameters.getParametersAsJsonObject()
					.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
					.getAsJsonObject()
					.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
					.getAsJsonObject().get(AllMaarsParameters.VALUES)
					.getAsJsonObject().get(AllMaarsParameters.TIME_LIMIT)
					.getAsDouble() * 60 * 1000;
			ok = ok && (System.currentTimeMillis() - startTime) < timeLimit;
			ReportingUtils.logMessage("time since beginning of acquisition : "
					+ (System.currentTimeMillis() - startTime));
			ReportingUtils.logMessage("ok " + ok);

		}
		if (checkAbsMaxSpSize || checkRelativeMaxSpSize || checkRelativeSpAngle
				|| checkGrowing) {
			Spindle newSp = mfa.getSpindle(newImage, cell);

			if (newSp.getFeature().equals(Spindle.NO_SPINDLE)
					|| newSp.getFeature().equals(Spindle.NO_SPOT)) {
				ok = false;
			} else {
				if (checkAbsMaxSpSize) {
					ReportingUtils
							.logMessage("Check maximum absolute spindle size");
					ok = ok
							&& newSp.getLength() <= parameters
									.getParametersAsJsonObject()
									.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
									.getAsJsonObject()
									.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
									.getAsJsonObject()
									.get(AllMaarsParameters.VALUES)
									.getAsJsonObject()
									.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
									.getAsDouble();
					ReportingUtils.logMessage("ok " + ok);
				}
				if (checkRelativeMaxSpSize) {
					ReportingUtils
							.logMessage("Check maximum relative spindle size");
					ok = ok
							&& newSp.getLengthRatioToMajorAxis() >= parameters
									.getParametersAsJsonObject()
									.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
									.getAsJsonObject()
									.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
									.getAsJsonObject()
									.get(AllMaarsParameters.VALUES)
									.getAsJsonObject()
									.get(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE)
									.getAsDouble();
					ReportingUtils.logMessage("ok " + ok);
				}
				if (checkRelativeSpAngle) {
					ReportingUtils
							.logMessage("Check maximum relative spindle angle to major axis");
					ok = ok
							&& newSp.getAngleToMajorAxis() >= parameters
									.getParametersAsJsonObject()
									.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
									.getAsJsonObject()
									.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
									.getAsJsonObject()
									.get(AllMaarsParameters.VALUES)
									.getAsJsonObject()
									.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
									.getAsDouble();
					ReportingUtils.logMessage("ok " + ok);
				}
				if (checkGrowing) {
					ReportingUtils.logMessage("Check minimum growing");
					Spindle lastSp = mfa.getSpindle(lastImage, cell);
					double growing = newSp.getLength() - lastSp.getLength();
					ok = ok
							&& growing >= parameters
									.getParametersAsJsonObject()
									.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
									.getAsJsonObject()
									.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
									.getAsJsonObject()
									.get(AllMaarsParameters.VALUES)
									.getAsJsonObject()
									.get(AllMaarsParameters.GROWING_SPINDLE)
									.getAsDouble();
					ReportingUtils.logMessage("ok " + ok);
				}
			}
		}

		return ok;
	}

	public int convertMicronToPixelSize(double micronSize,
			double widthOrHeightOrDepth) {
		int pixelSize = (int) Math.round(micronSize / widthOrHeightOrDepth);
		return pixelSize;
	}

	public void writeSpinldeCoord(Cell cell, String pathToWrite) {
		FileWriter spindleWriter = null;
		try {
			spindleWriter = new FileWriter(pathToWrite + "_spindleAnalysis.txt");
		} catch (IOException e) {
			ReportingUtils.logMessage("Could not create " + pathToWrite
					+ "_spindleAnalysis.csv");
			e.printStackTrace();
		}
		try {
			spindleWriter.write("["
					+ cell.getLastSpindleComputed().toString(
							cell.getCellShapeRoi().getName()) + "]");
		} catch (IOException e1) {
			ReportingUtils.logError(e1);
		}
		try {
			spindleWriter.close();
		} catch (IOException e) {
			ReportingUtils.logError(e);
		}
	}
}
