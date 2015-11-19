package acquisition;

import mmcorej.CMMCore;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
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
	private String channelName;
	private String channelGroup;
	private String shutterLable;
	private int exposure;
	private String acqName;
	private String pathToMovie;

	private Color chColor;

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
	public SuperClassAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters, double positionX,
			double positionY, int frame, String channelName) {
		this.mm = mm;
		this.mmc = mmc;
		this.channelGroup = parameters.getChannelGroup();
		this.channelName = channelName;
		this.shutterLable = parameters.getChShutter(channelName);
		this.range = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
		this.step = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
		this.sliceNumber = (int) Math.round(range / step);
		this.exposure = Integer.parseInt(parameters.getChExposure(channelName));
		this.acqName = "movie_X" + Math.round(positionX) + "_Y" + Math.round(positionY) + "_FLUO/" + frame + "_"
				+ channelName;
		String rootDirName = parameters.getSavingPath();
		pathToMovie = rootDirName + "/" + acqName;

		this.chColor = MaarsParameters.getColor(parameters.getChColor(channelName));
		IJ.im
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
	 * @param shutter
	 */
	public void setShutter() {
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
	 */
	public void setChExposure() {
		ReportingUtils.logMessage("... Set exposure");
		try {
			mmc.setExposure(exposure);
		} catch (Exception e1) {
			ReportingUtils.logMessage("could not set exposure");
			e1.printStackTrace();
		}
	}

	/**
	 * create a Datastore (a .tiff file with metadata in most of the case)
	 * 
	 * @param pathToMovie
	 * @return
	 */
	public Datastore createDataStore() {
		ReportingUtils.logMessage("... Initialize a Datastore");
		Datastore ds = null;
		try {
			// 1st false = do not generate separate metadata
			// 2nd false = do not split positions
			ds = mm.getDataManager().createMultipageTIFFDatastore(pathToMovie, false, false);
		} catch (IOException e3) {
			ReportingUtils.logMessage("... Can not initialize Datastore");
			e3.printStackTrace();
		}
		return ds;
	}

	/**
	 * Set DataStore metadata by using parameters in class
	 */
	public Datastore setDatastoreMetadata(Datastore ds) {
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
	public void setDisplay(Datastore ds) {
		Color[] chColors = new Color[1];
		chColors[0] = chColor;
		DisplaySettingsBuilder displayBuilder = mm.getDisplayManager().getStandardDisplaySettings().copy();
		displayBuilder.shouldAutostretch(true);
		displayBuilder.channelColors(chColors);
		List<DisplayWindow> setting = mm.getDisplayManager().getDisplays(ds);
		setting.get(0).setDisplaySettings(displayBuilder.build());
	};
}
