package org.micromanager.maarslib;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;

import org.micromanager.internal.MMStudio;
import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.SummaryMetadata;
import org.micromanager.data.SummaryMetadata.SummaryMetadataBuilder;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.maars.MaarsParameters;

import mmcorej.CMMCore;

/**
 * Acquisition calibrated for image segmentation using package CellsBoundaries_
 *
 * @author Tong & marie
 *
 */

public class MaarsAcquisitionForSegmentation {
	private MMStudio mm;
	private CMMCore mmc;
	private MaarsParameters parameters;
	private double positionX;
	private double positionY;
	private String pathToMovie;

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
	public MaarsAcquisitionForSegmentation(MMStudio mm, CMMCore mmc,
			MaarsParameters parameters, double positionX, double positionY) {
		this.mm = mm;
		this.mmc = mmc;
		this.parameters = parameters;
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
		String rootDirName = parameters.getSavingPath() + "/";
		ReportingUtils.logMessage("- saving path : " + rootDirName);
		String channelGroup = parameters.getChannelGroup();
		ReportingUtils.logMessage("- channel group : " + channelGroup);
		String channel = parameters
				.getSegmentationParameter(MaarsParameters.CHANNEL);
		ReportingUtils.logMessage("- channel : " + channel);
		Color color = MaarsParameters.getColor(parameters.getChColor(channel));
		ReportingUtils.logMessage("- color : " + color.toString());
		
		String shutter = parameters.getChShutter(channel);
		ReportingUtils.logMessage("- shutter : " + shutter);

		int exposure = Integer.parseInt(parameters.getChExposure(channel));
		ReportingUtils.logMessage("- exposure : " + exposure);
		
		int frameNumber = Integer.parseInt(parameters
				.getSegmentationParameter(MaarsParameters.FRAME_NUMBER));
		ReportingUtils.logMessage("- frame number : " + frameNumber);
		double range = Double
				.parseDouble(parameters
						.getSegmentationParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
		ReportingUtils.logMessage("- range size : " + range);
		double step = Double.parseDouble(parameters
				.getSegmentationParameter(MaarsParameters.STEP));
		ReportingUtils.logMessage("- step : " + step);
		int sliceNumber = (int) Math.round(range / step);
		ReportingUtils.logMessage("- slice number : " + sliceNumber);
		String acqName = "X" + Math.round(positionX) + "_Y"
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

		ReportingUtils.logMessage("... Initialize a Datastore");
		Datastore segDS = null;
		try {
			segDS = mm.getDataManager().createMultipageTIFFDatastore(
					pathToMovie, false, false);
		} catch (IOException e3) {
			ReportingUtils.logMessage("... Can not initialize Datastore");
		}
		ReportingUtils.logMessage("... Update summaryMetadata");
		SummaryMetadataBuilder summaryMD = segDS.getSummaryMetadata().copy();
		summaryMD = summaryMD.channelGroup(channelGroup);
		String[] channels = new String[1];
		channels[0] = channel;
		summaryMD = summaryMD.channelNames(channels);
		summaryMD = summaryMD.name(acqName);
		SummaryMetadata newSegMD = summaryMD.build();
		try {
			segDS.setSummaryMetadata(newSegMD);
		} catch (DatastoreFrozenException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
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
				mmc.waitForDevice(mmc.getFocusDevice());
			} catch (Exception e) {
				ReportingUtils
						.logMessage("could not set focus device at position");
			}
			ReportingUtils.logMessage("...snap and add images");
			try {
				segDS.putImage(mm.getSnapLiveManager().snap(true).get(0));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatastoreFrozenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mm.getSnapLiveManager().snap(false).remove(0);
			z = z + step;
		}
		ReportingUtils.logMessage("--- Acquisition done.");
		segDS.close();
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
			MaarsParameters parameters) {
		HashMap<String, String> params = new HashMap<String, String>();
		try {
			mmc.clearROI();
		} catch (Exception e2) {
			ReportingUtils.logMessage("Could not clear ROI");
			e2.printStackTrace();
		}
		mm.getDataManager().clearPipeline();

		mm.getScriptController().clearMessageWindow();

		ReportingUtils.logMessage("... Initialize parameters :");
		String channelGroup = parameters.getChannelGroup();
		ReportingUtils.logMessage("- channel group : " + channelGroup);
		params.put("channelGroup", channelGroup);
		String channel = parameters
				.getSegmentationParameter(MaarsParameters.CHANNEL);
		ReportingUtils.logMessage("- channel : " + channel);
		params.put("channel", channel);
		String shutter = parameters.getChShutter(channel);
		ReportingUtils.logMessage("- shutter : " + shutter);
		params.put("shutter", shutter);
		Color color = MaarsParameters.getColor(parameters.getChColor(channel));
		ReportingUtils.logMessage("- color : " + color.toString());
		params.put("color", color.toString());
		String exposure = parameters.getChExposure(channel);
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