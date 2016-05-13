package org.univ_tlse3.acquisition;

import mmcorej.CMMCore;

import java.awt.Color;
import java.io.File;
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
import org.univ_tlse3.maars.MaarsParameters;
import org.univ_tlse3.utils.FileUtils;

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
	private String baseSaveDir;

	/**
	 * Constructor :
	 *
	 * @param mm
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param parameters
	 *            : parameters used for algorithm
	 */
	public SuperClassAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters) {
		this.mm = mm;
		this.mmc = mmc;
		this.parameters = parameters;
		this.channelGroup = parameters.getChannelGroup();
		
	}
	
	public void setBaseSaveDir(String baseSaveDir){
		this.baseSaveDir = baseSaveDir;
	}

	/**
	 * clear ROI selected, close previously opened display windows
	 */
	private void cleanUp() {
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
	 * label of shutter
	 */
	private void setShutter(String shutterLable) {
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
	 * exposure of channel
	 */
	private void setChExposure(double exposure) {
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
	 * path to movie folder
	 * @return an initialized data store with default metadata.
	 */
	private Datastore createDataStore(String pathToMovieFolder) {
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
	 */
	private void setDatastoreMetadata(Datastore ds, String channelName, String acqName, double step) {
		ReportingUtils.logMessage("... Update summaryMetadata");
		SummaryMetadataBuilder summaryMD = ds.getSummaryMetadata().copy();
		summaryMD = summaryMD.channelGroup(channelGroup);
		summaryMD = summaryMD.channelNames(new String[] { channelName });
		summaryMD = summaryMD.name(acqName);
		summaryMD = summaryMD.zStepUm(step);
		try {
			ds.setSummaryMetadata(summaryMD.build());
		} catch (DatastoreFrozenException e2) {
			System.out.println("Can not update datastore metadata");
			e2.printStackTrace();
		}
	}

	/**
	 * set visualization color of acquisition
	 *
	 */
	public void setDisplay(Color chColor) {
		Color[] chColors = new Color[1];
		chColors[0] = chColor;
		DisplaySettingsBuilder displayBuilder = mm.getDisplayManager().getStandardDisplaySettings().copy();
		displayBuilder.channelColors(chColors);
		DisplayWindow liveWindow = mm.live().getDisplay();
		liveWindow.setDisplaySettings(displayBuilder.build());
	}

	public void save(List<Image> listImg, int frame, String channelName, double step) {
		String pathToMovie;
		if (!channelName.equals(parameters.getSegmentationParameter(MaarsParameters.CHANNEL))) {
			// initialize parameters for FLUO Acquisitions
			pathToMovie = baseSaveDir +  File.separator + frame + "_" + channelName;
		} else {
			// initialize parameters for Bright-Field Acquisitions
			pathToMovie = baseSaveDir;
		}
		Datastore ds = createDataStore(pathToMovie);
		setDatastoreMetadata(ds, channelName, String.valueOf(frame), step);
		for (Image img : listImg) {
			try {
				ds.putImage(img);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (DatastoreFrozenException e) {
				e.printStackTrace();
			}
		}
		ds.freeze();
		ds.close();
	}
	
	public ImagePlus convert2Imp(List<Image> listImg, String channelName, Double step){
		ImageStack imageStack = new ImageStack((int) mmc.getImageWidth(), (int) mmc.getImageHeight());
		for (Image img : listImg) {
			// Prepare a imagePlus (for analysis)
			ImageProcessor imgProcessor = mm.getDataManager().getImageJConverter().createProcessor(img);
			imageStack.addSlice(imgProcessor.convertToShortProcessor());
		}
		// ImagePlus for further analysis
		ImagePlus imagePlus = new ImagePlus(channelName, imageStack);
		Calibration cal = new Calibration();
		cal.setUnit("micron");
		cal.pixelWidth = mmc.getPixelSizeUm();
		cal.pixelHeight = mmc.getPixelSizeUm();
		cal.pixelDepth = step;
		imagePlus.setCalibration(cal);
		return imagePlus;
	}

	/**
	 * 
	 * @param channelName
	 *            name of the current channel (ex. BF, CFP, GFP, TXRED)
	 * @param zFocus
	 *            zFocus where maximum # of spot can be seen
	 * @return a duplicate of acquired images.
	 */
	public List<Image> acquire(String channelName, double zFocus) {
		double range;
		double step;
		if (channelName != parameters.getSegmentationParameter(MaarsParameters.CHANNEL)) {
			// initialize parameters for FLUO Acquisitions
			range = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
			step = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
		} else {
			// initialize parameters for Bright-Field Acquisitions
			range = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
			step = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
		}

		String shutterLable = parameters.getChShutter(channelName);
		int sliceNumber = (int) Math.round(range / step);
		int exposure = Integer.parseInt(parameters.getChExposure(channelName));
		Color chColor = MaarsParameters.getColor(parameters.getChColor(channelName));

		cleanUp();
		mmc.setAutoShutter(false);
		setShutter(shutterLable);
		setChExposure(exposure);
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
		for (int k = 0; k <= sliceNumber; k++) {
			System.out.println("- set focus device at position " + z);
			try {
				mmc.setPosition(focusDevice, z);
				mmc.waitForDevice(focusDevice);
			} catch (Exception e) {
				ReportingUtils.logMessage("could not set focus device at position");
			}
			z = z + step;
			listImg.add(mm.live().snap(false).get(0));
			//TODO sometime this display make the program crash
//			if (k == 0) {
//				setDisplay(chColor);
//			}
		}
		ReportingUtils.logMessage("--- Acquisition done.");
		try {
			mmc.setShutterOpen(false);
			mmc.setPosition(focusDevice, zFocus);
			mmc.waitForDevice(focusDevice);
			double currentZ = mmc.getPosition(focusDevice);
			while (currentZ > zFocus + 0.03 || currentZ < zFocus - 0.03) {
				mmc.setPosition(focusDevice, zFocus);
				mmc.waitForDevice(focusDevice);
				currentZ = mmc.getPosition(focusDevice);
			}
			mmc.waitForSystem();
		} catch (Exception e) {
			ReportingUtils.logMessage("could not set focus device back to position and close shutter");
			e.printStackTrace();
		}
		mmc.setAutoShutter(true);
		return listImg;
	}
}
