package fiji.plugin.maars.maarslib;

import fiji.plugin.maars.cellstateanalysis.Spindle;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiScaler;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.util.Calendar;
import java.util.Iterator;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.json.JSONException;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.AcquisitionManager;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.api.MMTags;
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
	private AcquisitionManager acqMgr;
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
	public void crop(int cellNumber) {

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

		Roi newroi = RoiScaler.scale(mfa.getSetOfCells().getCell(cellNumber)
				.getCellShapeRoi(), wtest
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
	public void acquire(boolean show, int cellNumber, boolean crop,
			boolean adjustZRange) throws MMScriptException {

		if (crop) {
			crop(cellNumber);
		}

		boolean keepFilming = true;

		ReportingUtils.logMessage("Acquire Mitosis Movie :");
		ReportingUtils.logMessage("_______________________");

		String acqName = "movie_X"
				+ Math.round(positionX)
				+ "_Y"
				+ Math.round(positionY)
				+ "_"
				+ mfa.getSetOfCells().getCell(cellNumber).getCellShapeRoi()
						.getName();

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
				+ "MITOSIS/";
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

		double range = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble();
		ReportingUtils.logMessage("- range size : " + range);

		double step = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble();
		ReportingUtils.logMessage("- step : " + step);

		int sliceNumber = (int) Math.round(range / step);
		ReportingUtils.logMessage("- slice number : " + sliceNumber);

		double zStartComparedToZFocus = -(range / 2);

		if (adjustZRange) {
			ReportingUtils.logMessage("---> adjust z range");
			ReportingUtils.logMessage("     get coordinates");
			double[] spbCoord = mfa.getSetOfCells().getCell(cellNumber)
					.getLastSpindleComputed().getSPBCoordinates();
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
					- (range - rangeBetweenTwoSpots) / 2;

			ReportingUtils.logMessage("     z start : "
					+ zStartComparedToZFocus);
		}

		ReportingUtils.logMessage("- acquisition name : " + acqName);

		double zFocus = 0;
		ReportingUtils
				.logMessage("... Open acquisition in acquisition manager");
		try {
			acqMgr.openAcquisition(acqName, rootDirName, show, true);
		} catch (MMScriptException e2) {
			ReportingUtils.logError(e2);
		}
		ReportingUtils.logMessage("... Get acquisition");
		try {
			acqForFluo = acqMgr.getAcquisition(acqName);
		} catch (MMScriptException e2) {
			ReportingUtils.logError(e2);
		}
		for (int channel = 0; channel < channels.length; channel++) {
			ReportingUtils.logMessage("... set channel color");
			try {
				acqForFluo.setChannelColor(channel, colors[channel].getRGB());
			} catch (MMScriptException e) {
				ReportingUtils.logError(e);
			}
			ReportingUtils.logMessage("... set channel name");
			try {
				acqForFluo.setChannelName(channel, channels[channel]);
			} catch (MMScriptException e) {
				ReportingUtils.logError(e);
			}
		}
		//TODO
		acqForFluo.setProperty("PixelType", "GRAY16");
		
		ImagePlus lastImage = null;
		double startTime = System.currentTimeMillis();
		ReportingUtils.logMessage("start time : " + startTime);
		int frame = 0;
		while (keepFilming) {

			double beginAcq = System.currentTimeMillis();

			for (int channel = 0; channel < channels.length; channel++) {

				ReportingUtils
						.logMessage("set up everything to acquire with channel "
								+ channels[channel]);

				ReportingUtils.logMessage("... Set shutter device");
				try {
					mmc.setShutterDevice(shutters[channel]);
				} catch (Exception e1) {
					ReportingUtils.logMessage("Could not set shutter device");
					e1.printStackTrace();
				}

				ReportingUtils.logMessage("... Set exposure");
				try {
					mmc.setExposure(exposures[channel]);
				} catch (Exception e1) {
					ReportingUtils.logMessage("could not set exposure");
					e1.printStackTrace();
				}

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
				double z = zFocus + zStartComparedToZFocus;

				ImageStack imageStack = new ImageStack(
						(int) mmc.getImageWidth(), (int) mmc.getImageHeight());

				for (int k = 0; k <= sliceNumber; k++) {
					ReportingUtils.logMessage("- set focus device at position "
							+ z);
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
						img.tags.put(MMTags.Image.FRAME_INDEX, frame);
						img.tags.put(MMTags.Image.CHANNEL_INDEX, channel);
						img.tags.put(MMTags.Image.TIME, Calendar.getInstance()
								.getTime());
						img.tags.put(MMTags.Image.ZUM, z);
						img.tags.put(MMTags.Image.XUM, 0);
						img.tags.put(MMTags.Image.YUM, 0);

					} catch (JSONException e) {
						ReportingUtils.logMessage("could not tag image");
						e.printStackTrace();
					}

					try {
						//TODO
						acqForFluo.insertTaggedImage(img, frame, channel, k);
					} catch (MMScriptException e) {
						ReportingUtils
								.logMessage("could not add image to acquisition");
						e.printStackTrace();
					}

					if (channels[channel].equals(parameters
							.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject().get(AllMaarsParameters.CHANNEL)
							.getAsString())) {

						ShortProcessor shortProcessor = new ShortProcessor(
								(int) mmc.getImageWidth(),
								(int) mmc.getImageHeight());
						shortProcessor.setPixels(img.pix);

						imageStack.addSlice(shortProcessor);
					}

					z = z + step;

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

					ImagePlus imagePlus = new ImagePlus("Maars " + acqName,
							imageStack);
					Calibration cal = new Calibration();
					cal.setUnit("micron");
					cal.pixelWidth = mmc.getPixelSizeUm();
					cal.pixelHeight = mmc.getPixelSizeUm();
					cal.pixelDepth = step;
					imagePlus.setCalibration(cal);

					keepFilming = checkEndMovieConditions(lastImage, imagePlus,
							startTime, cellNumber, frame);
					if (frame == 0) {
						lastImage = new ImagePlus("Maars " + acqName,
								imageStack);
						lastImage.setCalibration(cal);
					} else {
						lastImage.setImage(imagePlus);
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
		ReportingUtils.logMessage("finish image cache");
		acqForFluo.getImageCache().finished();
		ReportingUtils.logMessage("--- Acquisition done.");
		acqMgr.closeAll();

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
	 * @param cellNumber
	 *            : index of cell to check
	 * @param frame
	 *            : time point of image (0 if it is first image acquired)
	 * @return true if system should keep filming
	 */
	public boolean checkEndMovieConditions(ImagePlus lastImage,
			ImagePlus newImage, double startTime, int cellNumber, int frame) {
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
			Spindle newSp = mfa.getSpindle(newImage, cellNumber);

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
					Spindle lastSp = mfa.getSpindle(lastImage, cellNumber);
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
}
