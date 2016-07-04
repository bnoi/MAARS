package edu.univ_tlse3.acquisition;

import ij.IJ;
import mmcorej.CMMCore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import org.micromanager.SequenceSettings;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;
import edu.univ_tlse3.maars.MaarsParameters;


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
	private String baseSaveDir;

	/**
	 * Constructor :
	 *
	 * @param mm
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 */
	public SuperClassAcquisition(MMStudio mm, CMMCore mmc) {
		this.mm = mm;
		this.mmc = mmc;
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
	
	public ImagePlus convert2Imp(List<Image> listImg, String channelName){
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
		imagePlus.setCalibration(cal);
		return imagePlus;
	}

	/**
	 * 
	 * @param acqSettings
     *
	 * @return a duplicate of acquired images.
	 */
	public List<Image> acquire(SequenceSettings acqSettings, String channelGroup) {
        MAARS_mda mda = new MAARS_mda(mm,acqSettings, channelGroup);
		Datastore ds = mda.acquire();

        List<Image> listImg = new ArrayList<Image>();
        for (Coords coords : ds.getUnorderedImageCoords()){
            IJ.log(coords.getChannel() + "-" + coords.getTime() +"-"+ coords.getZ());
            listImg.add(ds.getImage(coords));
        }
//        listImg.add(ds.getAnyImage());
//        DecimalFormat numberFormat = new DecimalFormat("#.###");
//        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
//        dfs.setDecimalSeparator('.');
//        numberFormat.setDecimalFormatSymbols(dfs);
//		zFocus = Double.parseDouble(numberFormat.format(zFocus));
//		if (!channelName.equals(parameters.getSegmentationParameter(MaarsParameters.CHANNEL))) {
//			// initialize parameters for FLUO Acquisitions
//			range = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
//
//		} else {
//			// initialize parameters for Bright-Field Acquisitions
//			range = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
//			step = Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
//		}
//
//		String shutterLable = parameters.getChShutter(channelName);
//
//		int exposure = Integer.parseInt(parameters.getChExposure(channelName));
//		Color chColor = MaarsParameters.getColor(parameters.getChColor(channelName));
//
//		cleanUp();
//		mmc.setAutoShutter(false);
//		setShutter(shutterLable);
//		setChExposure(exposure);
//		try {
//			mmc.setConfig(channelGroup, channelName);
//			mmc.waitForConfig(channelGroup, channelName);
//		} catch (Exception e1) {
//			System.out.println("Can not set config");
//			e1.printStackTrace();
//		}
//		try {
//			mmc.setShutterOpen(true);
//			mmc.waitForDevice(shutterLable);
//		} catch (Exception e) {
//			ReportingUtils.logMessage("Can not open shutter");
//			e.printStackTrace();
//		}
//		String focusDevice = mmc.getFocusDevice();
//		ReportingUtils.logMessage("-> z focus is " + zFocus);
//		ReportingUtils.logMessage("... start acquisition");
//		double z = zFocus - (range / 2);

//		for (int k = 0; k <= sliceNumber; k++) {
//			z = Double.parseDouble(numberFormat.format(z));
//			System.out.println("- set focus device at position " + z);
//			try {
//				mmc.setPosition(focusDevice, z);
//				mmc.waitForDevice(focusDevice);
//			} catch (Exception e) {
//				ReportingUtils.logMessage("could not set focus device at position");
//			}
//			z = z + step;
//			listImg.add(mm.live().snap(false).get(0));
//			//TODO sometime this display make the program crash
////			if (k == 0) {
////				setDisplay(chColor);
////			}
//		}
//		ReportingUtils.logMessage("--- Acquisition done.");
//		try {
//			mmc.setShutterOpen(false);
//			mmc.setPosition(focusDevice, zFocus);
//			mmc.waitForDevice(focusDevice);
//			double currentZ = mmc.getPosition(focusDevice);
//            currentZ = Double.parseDouble(numberFormat.format(currentZ));
//            IJ.log("first focus: " + zFocus);
//			while (currentZ >= zFocus + 0.025 || currentZ <= zFocus - 0.025) {
//				mmc.setPosition(focusDevice, zFocus);
//				currentZ = mmc.getPosition(focusDevice);
//                currentZ = Double.parseDouble(numberFormat.format(currentZ));
//                mmc.waitForDevice(focusDevice);
//			}
//            mmc.waitForDevice(focusDevice);
//            mm.updateZPos(zFocus);
//            IJ.log("Final focus :" + currentZ);
//        } catch (Exception e) {
//			ReportingUtils.logMessage("could not set focus device back to position and close shutter");
//			e.printStackTrace();
//		}
//		mmc.setAutoShutter(true);
		return listImg;
	}
}
