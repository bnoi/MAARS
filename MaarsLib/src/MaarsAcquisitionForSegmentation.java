import java.awt.Color;

import org.json.JSONException;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.MMTags;
import org.micromanager.utils.MMScriptException;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

/**
 * Acquisition calibrated for  image segmentation using package CellsBoundaries_
 * @author marie
 *
 */
public class MaarsAcquisitionForSegmentation {
	
	private MMStudioMainFrame gui;
	private CMMCore mmc;
	private AllMaarsParameters parameters;
	private double positionX;
	private double positionY;
	private String pathToMovie;
	
	/**
	 * Constructor :
	 * @param gui : graphical user interface of Micro-Manager
	 * @param mmc : Core object of Micro-Manager
	 * @param parameters : parameters used for algorithm
	 * @param positionX : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY : y field position (can be defined by ExplorationXYPositions)
	 */
	public MaarsAcquisitionForSegmentation(MMStudioMainFrame gui,
			CMMCore mmc,
			AllMaarsParameters parameters,
			double positionX,
			double positionY) {
		this.gui = gui;
		this.mmc = mmc;
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
	}
	
	/**
	 * Constructor :
	 * @param md : main window of MAARS (contains gui, mmc and parameters)
	 * @param positionX : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY : y field position (can be defined by ExplorationXYPositions)
	 */
	public MaarsAcquisitionForSegmentation(MaarsMainDialog md,
			double positionX,
			double positionY) {
		
		gui = md.getGui();
		mmc = md.getMMC();
		parameters = md.getParameters();
		this.positionX = positionX;
		this.positionY = positionY;
	}
	
	/**
	 * Acquire movie for segmentation
	 * @param show : true to show movie during acquisition
	 */
	public void acquire(boolean show) {
		
		
		System.out.println("Acquire movie for segmentation :");
		System.out.println("________________________________");
		
		System.out.println("Close all previous acquisitions");
		
		
		System.out.println("... Initialize parameters :");
		
		setupParameters();
		
		String rootDirName = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SAVING_PATH)
				.getAsString();
		System.out.println("- saving path : "+rootDirName);
		
		String channelGroup = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL_GROUP)
				.getAsString();
		System.out.println("- channel group : "+channelGroup);
		
		String channel =  parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL)
				.getAsString();
		System.out.println("- channel : "+channel);
		
		Color color = AllMaarsParameters.getColor(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject()
				.get(channel)
				.getAsJsonObject()
				.get(AllMaarsParameters.COLOR)
				.getAsString());
		System.out.println("- color : "+color.toString());

		int frameNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.FRAME_NUMBER)
				.getAsInt();
		System.out.println("- frame number : "+frameNumber);
		
		double range = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble();
		System.out.println("- range size : "+range);
		
		double step = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble();
		System.out.println("- step : "+step);
		
		int sliceNumber = (int) Math.round(range/step);
		System.out.println("- slice number : "+sliceNumber);
		
		String acqName = "movie_X"+Math.round(positionX)+"_Y"+Math.round(positionY);
		System.out.println("- acquisition name : "+acqName);
		
		System.out.println("... Open acquisition");
		try {
			gui.openAcquisition(acqName, rootDirName, frameNumber, 1, sliceNumber+1, show, true);
		} catch (MMScriptException e) {
			System.out.println("Could not open acquisition");
			e.printStackTrace();
		}
		
		System.out.println("... set channel color");
		try {
			gui.setChannelColor(acqName, 0, color);
		} catch (MMScriptException e) {
			System.out.println("Could not set channel color");
			e.printStackTrace();
		}
		System.out.println("... set channel name");
		try {
			gui.setChannelName(acqName, 0, channel);
		} catch (MMScriptException e) {
			System.out.println("could not set channel name");
			e.printStackTrace();
		}
		
		System.out.println("... set shutter open");
		try {
			mmc.setShutterOpen(true);
		} catch (Exception e1) {
			System.out.println("could not open shutter");
			e1.printStackTrace();
		}
		
		// wait a few second to be sure the lamp is well set
		try {
			gui.sleep(3000);
		} catch (MMScriptException e1) {
			System.out.println("could not sleep "+3000+"s");
			e1.printStackTrace();
		}
		
		System.out.println("... get z current position");
		double zFocus = 0;
		try {
			zFocus = mmc.getPosition(mmc.getFocusDevice());
		} catch (Exception e) {
			System.out.println("could not get z current position");
			e.printStackTrace();
		}
		System.out.println("-> z focus is "+zFocus);
		
		System.out.println("... start acquisition");
		double z = zFocus - (range / 2);
		
		for (int k = 0; k <= sliceNumber; k++) {
			System.out.println("- set focus device at position "+z);
			try {
				mmc.setPosition(mmc.getFocusDevice(), z);
				mmc.waitForDevice(mmc.getFocusDevice());
			} catch (Exception e) {
				System.out.println("could not set focus device at position");
				e.printStackTrace();
			}
			
			try {
				mmc.snapImage();
			} catch (Exception e) {
				System.out.println("could not snape image");
				e.printStackTrace();
			}
			
			TaggedImage img = null;
			try {
				img = mmc.getTaggedImage();
			} catch (Exception e) {
				System.out.println("could not get tagged image");
				e.printStackTrace();
			}
			
			try {
				img.tags.put(MMTags.Image.SLICE_INDEX, k);
				img.tags.put(MMTags.Image.FRAME_INDEX, 0);
				img.tags.put(MMTags.Image.CHANNEL_INDEX, 0);
				img.tags.put(MMTags.Image.ZUM, z);
				img.tags.put(MMTags.Image.XUM,0);
				img.tags.put(MMTags.Image.YUM,0);
							
			} catch (JSONException e) {
				System.out.println("could not tag image");
				e.printStackTrace();
			}
			
			try {
				gui.addImage(acqName, img);
			} catch (MMScriptException e) {
				System.out.println("could not add image to gui");
				e.printStackTrace();
			}
			
			z = z+step;
		}
		
		System.out.println("finish image cache");
		try {
			gui.getAcquisitionImageCache(acqName).finished();
		} catch (MMScriptException e1) {
			System.out.println("could not write metadata");
			e1.printStackTrace();
		}
		System.out.println("--- Acquisition done.");
		gui.closeAllAcquisitions();
		
		try {
			mmc.setPosition(mmc.getFocusDevice(), zFocus);
			mmc.waitForDevice(mmc.getFocusDevice());
			mmc.setShutterOpen(false);
			mmc.waitForDevice(mmc.getShutterDevice());
		} catch (Exception e) {
			System.out.println("could not set focus device back to position and close shutter");
			e.printStackTrace();
		}
		
		pathToMovie = rootDirName+acqName;
	}
	
	/**
	 * Get and set up all parameters for acquisition
	 */
	public void setupParameters() {

		try {
			mmc.clearROI();
		} catch (Exception e2) {
			System.out.println("Could not clear ROI");
			e2.printStackTrace();
		}
		
		gui.closeAllAcquisitions();
		try {
			gui.clearMessageWindow();
		} catch (MMScriptException e) {
			System.out.println("could not clear message window");
			e.printStackTrace();
		}
		
		System.out.println("... Initialize parameters :");
		

		String channelGroup = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL_GROUP)
				.getAsString();
		System.out.println("- channel group : "+channelGroup);
		
		String channel =  parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL)
				.getAsString();
		System.out.println("- channel : "+channel);
		
		String shutter =  parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject()
				.get(channel)
				.getAsJsonObject()
				.get(AllMaarsParameters.SHUTTER)
				.getAsString();
		System.out.println("- shutter : "+shutter);
		
		Color color = AllMaarsParameters.getColor(parameters.getParametersAsJsonObject()
						.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
						.getAsJsonObject()
						.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
						.getAsJsonObject()
						.get(channel)
						.getAsJsonObject()
						.get(AllMaarsParameters.COLOR)
						.getAsString());
		System.out.println("- color : "+color.toString());
		
		int exposure = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.DEFAULT_CHANNEL_PARAMATERS)
				.getAsJsonObject()
				.get(channel)
				.getAsJsonObject()
				.get(AllMaarsParameters.EXPOSURE)
				.getAsInt();
		System.out.println("- exposure : "+exposure);
		
		System.out.println("... Set shutter device");
		try {
			mmc.setShutterDevice(shutter);
		} catch (Exception e1) {
			System.out.println("Could not set shutter device");
			e1.printStackTrace();
		}
		
		System.out.println("... Set exposure");
		try {
			mmc.setExposure(exposure);
		} catch (Exception e1) {
			System.out.println("could not set exposure");
			e1.printStackTrace();
		}
		
		System.out.println("... set config");
		try {
			mmc.setConfig(channelGroup, channel);
		} catch (Exception e1) {
			System.out.println("Could not set config");
			e1.printStackTrace();
		}
		
		System.out.println("... wait for config");
		try {
			mmc.waitForConfig(channelGroup, channel);
		} catch (Exception e1) {
			System.out.println("Could not wait for config");
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
