package acquisition;

import mmcorej.CMMCore;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.Image;
import org.micromanager.data.SummaryMetadata;
import org.micromanager.data.SummaryMetadata.SummaryMetadataBuilder;
import org.micromanager.display.DisplaySettings.DisplaySettingsBuilder;
import org.micromanager.display.DisplayWindow;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;

import fiji.plugin.maars.maarslib.MaarsParameters;
import ij.IJ;

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
	private double positionX;
	private double positionY;

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

	public SuperClassAcquisition(MMStudio mm, CMMCore mmc,
			MaarsParameters parameters, double positionX, double positionY) {
		this.mm = mm;
		this.mmc = mmc;
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.channelGroup = parameters.getChannelGroup();
		this.rootDirName = parameters.getSavingPath();
	}

	/**
	 * clean up (ex. clear ROI selected, close previously opened display
	 * windows)
	 */
	public void cleanUp() {
		try {
			mmc.clearROI();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mm.getDisplayManager().closeAllDisplayWindows(false);
	}

	/**
	 * Set shutter used for current acquisition
	 * 
	 * @param shutterLable
	 */
	public void setShutter(String shutterLable) {
		ReportingUtils.logMessage("... Set shutter");
		try {
			mmc.setShutterDevice(shutterLable);
		} catch (Exception e) {
			ReportingUtils.logMessage("could not set shutter");
			e.printStackTrace();
		}
	}

	/**
	 * set channel exposure while acquisition
	 * 
	 * @param exposure
	 */
	public void setChExposure(double exposure) {
		ReportingUtils.logMessage("... Set exposure");
		try {
			mmc.setExposure(exposure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * create a Datastore (a .tiff file with metadata in most of the case)
	 * 
	 * @param pathToMovie
	 * @return
	 */
	public Datastore createDataStore(String pathToMovie) {
		ReportingUtils.logMessage("... Initialize a Datastore");
		Datastore ds = null;
		try {
			// 1st false = do not generate separate metadata
			// 2nd false = do not split positions
			ds = mm.getDataManager().createMultipageTIFFDatastore(pathToMovie,
					false, false);
		} catch (IOException e3) {
			ReportingUtils.logMessage("... Can not initialize Datastore");
			e3.printStackTrace();
		}
		return ds;
	}

	/**
	 * Set DataStore metadata by using parameters in class
	 */
	public Datastore setDatastoreMetadata(Datastore ds, String channelName,
			String acqName, double step) {
		ReportingUtils.logMessage("... Update summaryMetadata");
		SummaryMetadataBuilder summaryMD = ds.getSummaryMetadata().copy();
		summaryMD = summaryMD.channelGroup(channelGroup);
		String[] channels = new String[1];
		channels[0] = channelName;
		summaryMD = summaryMD.channelNames(channels);
		summaryMD = summaryMD.name(acqName);
		summaryMD = summaryMD.zStepUm(step);
		SummaryMetadata newSegMD = summaryMD.build();
		try {
			ds.setSummaryMetadata(newSegMD);
		} catch (DatastoreFrozenException e2) {
			// TODO Auto-generated catch block
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
		DisplaySettingsBuilder displayBuilder = mm.getDisplayManager()
				.getStandardDisplaySettings().copy();
		displayBuilder.shouldAutostretch(true);
		displayBuilder.channelColors(chColors);
		DisplayWindow liveWindow = mm.live().getDisplay();
		liveWindow.setDisplaySettings(displayBuilder.build());
	};

	/**
	 * Do acquisition
	 */
	public void acquire(int frame, String channelName) {
		String acqName = null;
		if (channelName != parameters
				.getSegmentationParameter(MaarsParameters.CHANNEL)) {
			acqName = "movie_X" + Math.round(positionX) + "_Y"
					+ Math.round(positionY) + "_FLUO/" + frame + "_"
					+ channelName;
		} else {
			acqName = "movie_X" + Math.round(positionX) + "_Y"
					+ Math.round(positionY);
		}

		String shutterLable = parameters.getChShutter(channelName);
		double range = Double.parseDouble(parameters
				.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
		double step = Double.parseDouble(parameters
				.getFluoParameter(MaarsParameters.STEP));
		int sliceNumber = (int) Math.round(range / step);
		int exposure = Integer.parseInt(parameters.getChExposure(channelName));
		String pathToMovie = rootDirName + "/" + acqName;
		Color chColor = MaarsParameters.getColor(parameters
				.getChColor(channelName));

		cleanUp();
		setShutter(shutterLable);
		setChExposure(exposure);
		Datastore ds = createDataStore(pathToMovie);
		setDatastoreMetadata(ds, channelName, acqName, step);
		setDisplay(chColor);

		try {
			mmc.setShutterOpen(true);
			mmc.waitForSystem();
		} catch (Exception e) {
			ReportingUtils.logMessage("Can not open shutter");
			e.printStackTrace();
		}
		double zFocus = 0;
		try {
			zFocus = mmc.getPosition(mmc.getFocusDevice());
		} catch (Exception e) {
			ReportingUtils.logMessage("could not get z current position");
			e.printStackTrace();
		}
		ReportingUtils.logMessage("-> z focus is " + zFocus);
		ReportingUtils.logMessage("... start acquisition");
		/*
		 * important to add 2 µm. Depends on different microscopes. in our case,
		 * the z-focus position is not in the middle of z range. It is often
		 * lower than the real medium plan. So we add 2. This parameter needs to
		 * be defined by testing on your own microscope.
		 */
		double z = zFocus - (range / 2) + 2;
		for (int k = 0; k <= sliceNumber; k++) {
			ReportingUtils.logMessage("- set focus device at position " + z);
			try {
				mmc.setPosition(mmc.getFocusDevice(), z);
				// mmc.waitForDevice(mmc.getFocusDevice());
			} catch (Exception e) {
				ReportingUtils
						.logMessage("could not set focus device at position");
			}
			mm.live().snap(true);
			z = z + step;
		}
		ReportingUtils.logMessage("--- Acquisition done.");
		for (Image img : mm.live().snap(false)) {
			try {
				ds.putImage(img);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatastoreFrozenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ds.freeze();
		ds.close();
		mm.getDisplayManager().closeAllDisplayWindows(false);
		try {
			mmc.setPosition(mmc.getFocusDevice(), zFocus);
			mmc.setShutterOpen(false);
		} catch (Exception e) {
			ReportingUtils
					.logMessage("could not set focus device back to position and close shutter");
			e.printStackTrace();
		}
	}
}
