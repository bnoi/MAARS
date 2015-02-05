package fiji.plugin.maars.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.detection.DogDetector;
import fiji.plugin.trackmate.detection.LogDetector;
import net.imglib2.Iterator;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.Img;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.Interval;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiScaler;
import ij.plugin.filter.Analyzer;

/**
 * This class is to find fluorescent spots in an image using LogDetector
 * @author marie
 *
 */
public class CellFluoAnalysis {
	//private double[] scaleFactorForRoiFromBfToFluo;
	private Cell cell;
	private java.util.List<Spot> res;
	private double factorForThreshold;
	
	private final Interval intervale;
	private long[] dimMins = {0,0};
	private long[] dimMaxs;
	private double[] calibration = {0.0645, 0.0645};
	
	/**
	 * Constructor :
	 * @param cell : Cell object (the cell you want to analyse)
	 * @param spotRadius : spot typical radius
	 */
	@SuppressWarnings("deprecation")
	public CellFluoAnalysis(Cell cell, double spotRadius){
		
		System.out.println("Creating CellFluoAnalysis object ...");
		
		//this.cellShapeRoi = cellShapeRoi;
		//this.scaleFactorForRoiFromBfToFluo = scaleFactorForRoiFromBfToFluo;
		this.cell = cell;
		//ResultsTable rt = new ResultsTable();
		
		//RoiScaler.scale(cellShapeRoi, scaleFactorForRoiFromBfToFluo[0], scaleFactorForRoiFromBfToFluo[1], false);
		
		System.out.println("- change image type so it can be used by spot analyzerS");
		
		final Img<UnsignedShortType> img = ImageJFunctions.wrap( cell.getFluoImage() );
		final ImgPlus<UnsignedShortType> imgPlus = new ImgPlus<UnsignedShortType>(img);
		System.out.println("- done.");
		Calibration cal = cell.getFluoImage().getCalibration();
		
		System.out.println("- create detector");
		dimMaxs[0] = imgPlus.max(0);
		dimMaxs[1] = imgPlus.max(1);
		intervale = new IntervalIterator(dimMins, dimMaxs);
		final LogDetector<UnsignedShortType> detector = new LogDetector<UnsignedShortType>(imgPlus,intervale,calibration, spotRadius/cal.pixelWidth, 0.0, false, true);
//		final DogDetector<UnsignedShortType> detector = new DogDetector<UnsignedShortType>(imgPlus, 0.15/cal.pixelWidth, 0.05/cal.pixelWidth, false, true);
		System.out.println("- done");
		
		if (!detector.checkInput()) {
			System.out.println("- Wrong input for detector");
		}
		else {
			System.out.println("- input ok");
		}
		
		if (!detector.process()) {
			System.out.println("- Detector not processing");
		}
		else {
			System.out.println("- process ok");
		}		
		System.out.println("- compute results");
		res = detector.getResult();
		System.out.println("- Done.");
		
		factorForThreshold = 4;
	}
	
	/**
	 * Method to change threshold if necessary
	 * @param fact
	 */
	public void setFactorForThreshold(double fact) {
		factorForThreshold = fact;
	}
	
	/**
	 * Method to find spots
	 * @return ArrayList<Spot>
	 */
	public ArrayList<Spot> findSpots() {
		
		ArrayList<Spot> spotsToKeep = new ArrayList<Spot>();
		
		//System.out.println("Res : "+res);
		java.util.Iterator<Spot> itr1 = res.iterator();
		
		double[] quality = new double[res.toArray().length];
		int nb = 0;
		while (itr1.hasNext()) {
			Spot spot = itr1.next();
			/*System.out.println("\n___\n");
			System.out.println("spot : "+spot.getName());
			System.out.println(spot.getFeatures());
			*/
			Map<String, Double> features = spot.getFeatures();
			quality[nb] = features.get("QUALITY");
			nb ++;
			/*OvalRoi roi = new OvalRoi(features.get("POSITION_X"), features.get("POSITION_Y"), 2* features.get("RADIUS"), 2* features.get("RADIUS"));
			fluoImage.setSlice((int) Math.round(features.get("POSITION_Z")));
			fluoImage.setRoi(roi);
			Analyzer a = new Analyzer(fluoImage,Measurements.CENTROID+Measurements.MEAN ,rt);
			a.measure();
			*/
		}
		
		System.out.println("initial number of spots : "+nb);
		
		Statistics stat = new Statistics(quality);
		double threshold = stat.getMean() + factorForThreshold * stat.getStdDev();
		
		System.out.println("threshold : "+threshold);
		
		java.util.Iterator<Spot> itr2 = res.iterator();
		while (itr2.hasNext()) {
			Spot spot = itr2.next();
			Map<String, Double> features = spot.getFeatures();
			
			if (features.get("QUALITY") > threshold && cell.getCellShapeRoi().contains((int) Math.round(cell.getCellShapeRoi().getXBase() + features.get("POSITION_X")), (int) Math.round(cell.getCellShapeRoi().getYBase() + features.get("POSITION_Y")))){
				spotsToKeep.add(spot);
				//System.out.println(features);
				/*OvalRoi roi = new OvalRoi(features.get("POSITION_X"), features.get("POSITION_Y"), features.get("RADIUS"), features.get("RADIUS"));
				cell.getFluoImage().setSlice((int) Math.round(features.get("POSITION_Z"))+1);
				cell.getFluoImage().setRoi(roi);
				IJ.wait(5000);*/
			}
		}
		return spotsToKeep;
		//rt.show("Measures");
	}
	
}
