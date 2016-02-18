package org.micromanager.acquisition;

import mmcorej.CMMCore;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.Image;
import org.micromanager.data.SummaryMetadata.SummaryMetadataBuilder;
import org.micromanager.display.DisplaySettings.DisplaySettingsBuilder;
import org.micromanager.display.DisplayWindow;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.maars.MaarsParameters;
import org.micromanager.utils.FileUtils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */

public class SuperClassAcquisition {

	private MMStudio mm;
	private CMMCore mmc;
	private MaarsParameters parameters;
	private String channelGroup;
	private String rootDirName;
	private String positionX;
	private String positionY;

	/**
	 * Constructor :
	 *
	 * @param mm
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
	public SuperClassAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters, String positionX,
			String positionY) {
		this.mm = mm;
		this.mmc = mmc;
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.channelGroup = parameters.getChannelGroup();
		this.rootDirName = parameters.getSavingPath();
	}

	/**
	 * clear ROI selected, close previously opened display windows
	 */
	public void cleanUp() {
		try {
			mmc.clearROI();
		} catch (Exception e) {
			System.out.println("Can not clear ROI for MM acquisition");
			e.printStackTrace();
		}
		mm.getDisplayManager().closeAllDisplayWindows(false);
	}

	/**
	 * Set shutter for current acquisition
	 * 
	 * @param shutterLable
	 */
	public void setShutter(String shutterLable) {
		ReportingUtils.logMessage("... Set shutter " + shutterLable);
		try {
			mmc.setShutterDevice(shutterLable);
		} catch (Exception e) {
			ReportingUtils.logMessage("could not set shutter");
			e.printStackTrace();
		}
	}

	/**
	 * set channel exposure of current acquisition
	 * 
	 * @param exposure
	 */
	public void setChExposure(double exposure) {
		ReportingUtils.logMessage("... Set exposure");
		try {
			mmc.setExposure(exposure);
		} catch (Exception e) {
			System.out.println("Can not set exposure");
			e.printStackTrace();
		}
	}

	/**
	 * create a Datastore (here we save them into a .tiff file with metadata in
	 * most of the case)
	 * 
	 * @param pathToMovieFolder
	 * @return an initialized datastore with default metadata.
	 */
	public Datastore createDataStore(String pathToMovieFolder) {
		ReportingUtils.logMessage("... Initialize a Datastore");
		Datastore ds = null;
		if (!FileUtils.exists(pathToMovieFolder)) {
			FileUtils.createFolder(pathToMovieFolder);
		}
		try {
			// 1st false = do not generate separate metadata
			// 2nd false = do not split positions
			ds = mm.getDataManager().createMultipageTIFFDatastore(pathToMovieFolder, false, false);
		} catch (IOException e3) {
			ReportingUtils.logMessage("... Can not initialize Datastore");
			e3.printStackTrace();
		}
		return ds;
	}

	/**
	 * Update DataStore metadata by using parameters in class
	 * 
	 * @param ds
	 *            datastore to be updated
	 * @param channelName
	 *            channel employed of current acquisition
	 * @param acqName
	 *            name of acquisition
	 * @param step
	 *            real distance between two slices (in micron)
	 * @return
	 */
	public Datastore setDatastoreMetadata(Datastore ds, String channelName, String acqName, double step) {
		ReportingUtils.logMessage("... Update summaryMetadata");
		SummaryMetadataBuilder summaryMD = ds.getSummaryMetadata().copy();
		summaryMD = summaryMD.channelGroup(channelGroup);
		String[] channels = new String[1];
		channels[0] = channelName;
		summaryMD = summaryMD.channelNames(channels);
		summaryMD = summaryMD.name(acqName);
		summaryMD = summaryMD.zStepUm(step);
		try {
			ds.setSummaryMetadata(summaryMD.build());
		} catch (DatastoreFrozenException e2) {
			System.out.println("Can not update datastore metadata");
			e2.printStackTrace();
		}
		return ds;
	}

	/**
	 * set visualization color of acquisition
	 * 
	 * @param ds
	 */
	public void setDisplay(Color chColor) {
		Color[] chColors = new Color[1];
		chColors[0] = chColor;
		DisplaySettingsBuilder displayBuilder = mm.getDisplayManager().getStandardDisplaySettings().copy();
		displayBuilder.channelColors(chColors);
		DisplayWindow liveWindow = mm.live().getDisplay();
		liveWindow.setDisplaySettings(displayBuilder.build());
	};

	/**
	 * 
	 * @param frame
	 *            current frame (time point)
	 * @param channelName
	 *            name of the current channel (ex. BF, CFP, GFP, TXRED)
	 * @return a duplicate of acquired images.
	 */
	public ImagePlus acquire(int frame, String channelName, double zFocus) {
		String acqName = null;
		String imgName = null;
		double range = 0;
		double step = 0;
		if (channelName != parameters.getSegmentationParameter(MaarsParameters.CHANNEL)) {
			// initialize parameters for FLUO Acquisitions
			acqName = "movie_X" + positionX + "_Y" + positionY + "_FLUO/" + frame + "_" + channelName;
			range = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
			step = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
			imgName = channelName;
		} else {
			// initialize parameters for Bright-Field Acquisitions
			acqName = "movie_X" + positionX + "_Y" + positionY;
			range = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
			step = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
			imgName = parameters.getSegmentationParameter(MaarsParameters.CHANNEL);
		}

		String shutterLable = parameters.getChShutter(channelName);
		int sliceNumber = (int) Math.round(range / step);
		int exposure = Integer.parseInt(parameters.getChExposure(channelName));
		String pathToMovie = rootDirName + "/" + acqName;
		// Color chColor =
		// MaarsParameters.getColor(parameters.getChColor(channelName));

		cleanUp();
		mmc.setAutoShutter(false);
		setShutter(shutterLable);
		setChExposure(exposure);
		Datastore ds = createDataStore(pathToMovie);
		setDatastoreMetadata(ds, channelName, acqName, step);
		// setDisplay(chColor);
		try {
			mmc.setConfig(channelGroup, channelName);
			mmc.waitForConfig(channelGroup, channelName);
		} catch (Exception e1) {
			System.out.println("Can not set config");
			e1.printStackTrace();
		}
		try {
			mmc.setShutterOpen(true);
			mmc.waitForDevice(shutterLable);
		} catch (Exception e) {
			ReportingUtils.logMessage("Can not open shutter");
			e.printStackTrace();
		}
		String focusDevice = mmc.getFocusDevice();
		ReportingUtils.logMessage("-> z focus is " + zFocus);
		ReportingUtils.logMessage("... start acquisition");
		double z = zFocus - (range / 2);
		List<Image> listImg = new ArrayList<Image>();
		for (int k = 0; k < sliceNumber; k++) {
			System.out.println("- set focus device at position " + z);
			try {
				mmc.setPosition(focusDevice, z);
				mmc.waitForDevice(focusDevice);
			} catch (Exception e) {
				ReportingUtils.logMessage("could not set focus device at position");
			}
			z = z + step;
			listImg.add(mm.live().snap(true).get(0));
		}
		ReportingUtils.logMessage("--- Acquisition done.");
		try {
			mmc.setShutterOpen(false);
			mmc.setPosition(focusDevice, zFocus);
			while (zFocus > zFocus + 0.03 || zFocus < zFocus - 0.03){
				mmc.setPosition(focusDevice, zFocus);
				zFocus = mmc.getPosition(focusDevice);
			}
			mmc.waitForSystem();
		} catch (Exception e) {
			ReportingUtils.logMessage("could not set focus device back to position and close shutter");
			e.printStackTrace();
		}
		// add images into Datastore and imageplus
		ReportingUtils.logMessage("add images into Datastore and imageplus");
		ImageStack imageStack = new ImageStack((int) mmc.getImageWidth(), (int) mmc.getImageHeight());
		for (Image img : listImg) {
			// Prepare a imagePlus (for analysis)
			ImageProcessor imgProcessor = mm.getDataManager().getImageJConverter().createProcessor(img);
			imageStack.addSlice(imgProcessor.convertToShortProcessor());
			try {
				// Datastore (for save)
				ds.putImage(img);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (DatastoreFrozenException e) {
				e.printStackTrace();
			}
		}
		ds.freeze();
		// ImagePlus for further analysis
		ImagePlus imagePlus = new ImagePlus(imgName, imageStack);
		Calibration cal = new Calibration();
		cal.setUnit("micron");
		cal.pixelWidth = mmc.getPixelSizeUm();
		cal.pixelHeight = mmc.getPixelSizeUm();
		cal.pixelDepth = ds.getSummaryMetadata().getZStepUm();
		imagePlus.setCalibration(cal);
		ds.close();
		return imagePlus;
	}
}
