package fiji.plugin.maars.segmentPombe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import net.imglib2.multithreading.SimpleMultiThreading;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class SegPombe {

	private String savingPath;
	private ImagePlus imageToAnalyze;
	private ImagePlus focusImg;

	private float sigma;
	private float zFocus;
	private double minParticleInMicron;
	private double maxParticleInMicron;

	private double solidityThreshold;
	private double meanGreyValueThreshold;
	private boolean filterAbnormalShape;
	private boolean filtrateWithMeanGrayValue;

	// Variables to get results
	private FloatProcessor correlationImage;
	private ByteProcessor byteImage;
	private ImagePlus binCorrelationImage;
	private ImagePlus imgCorrTemp;
	private ResultsTable resultTable;
	private ParticleAnalyzer particleAnalyzer;
	private Analyzer analyzer;
	private RoiManager roiManager;
	private Roi[] roiArray;

	// Options related to display and save
	private boolean showCorrelationImg;
	private boolean showBinaryImg;
	private boolean showDataFrame;
	private boolean showFocusImage;

	private boolean saveCorrelationImg;
	private boolean saveBinaryImg;
	private boolean saveDataFrame;
	private boolean saveFocusImage;
	private boolean saveRoi;

	private boolean flushImageToAnalyze = false;

	private int direction;

	private boolean roiDetected = true;

	/**
	 * Constructor
	 */
	public SegPombe(SegPombeParameters parameters) {
		this.imageToAnalyze = parameters.getImageToAnalyze();
		this.savingPath = parameters.getSavingPath();

		try {
			PrintStream ps = new PrintStream(savingPath
					+ imageToAnalyze.getShortTitle() + "_Segmentation.LOG");
			System.setOut(ps);
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.sigma = parameters.getSigma();
		this.filterAbnormalShape = parameters.filterAbnormalShape();
		this.filtrateWithMeanGrayValue = parameters.filtrateWithMeanGrayValue();
		this.minParticleInMicron = parameters.getMinParticleSize();
		this.maxParticleInMicron = parameters.getMaxParticleSize();
		this.direction = parameters.getDirection();

		// ResultOptions
		this.showCorrelationImg = parameters.showCorrelationImg();
		this.showBinaryImg = parameters.showBinaryImg();
		this.showDataFrame = parameters.showDataFrame();
		this.showFocusImage = parameters.showFocusImage();

		this.saveCorrelationImg = parameters.saveCorrelationImg();
		this.saveBinaryImg = parameters.saveBinaryImg();
		this.saveDataFrame = parameters.saveDataFrame();
		this.saveFocusImage = parameters.saveFocusImage();
		this.saveRoi = parameters.saveRoi();
		this.zFocus = parameters.getFocusSlide();

		getFocusImage();

		this.solidityThreshold = parameters.getSolidityThreshold();
		this.meanGreyValueThreshold = parameters.getMeanGreyValueThreshold();

	}

	// I know it is messy but I have not found an other solution yet
	public void getFocusImage() {
		System.out.println("get Focus Image");

		imageToAnalyze.setZ((int) Math.round(zFocus) - 1);

		focusImg = new ImagePlus(imageToAnalyze.getShortTitle() + "FocusImage",
				imageToAnalyze.getProcessor().duplicate());

		if (imageToAnalyze.getCalibration().scaled()) {
			focusImg.setCalibration(imageToAnalyze.getCalibration());
		}

		if (saveFocusImage) {
			FileSaver fileSaver = new FileSaver(focusImg);
			fileSaver.saveAsTiff(savingPath + imageToAnalyze.getShortTitle()
					+ "_FocusImage.tif");
		}
	}

	/**
	 * Create an image correlation where each pixel corresponds to the
	 * correlation of a specific curve see equation in computeCorrelation object
	 */
	public void createCorrelationImage() {

		System.out.println("creating correlation image");
		System.out.println("Width : "
				+ String.valueOf(imageToAnalyze.getWidth()) + ", Height : "
				+ String.valueOf(imageToAnalyze.getHeight()));
		correlationImage = new FloatProcessor(imageToAnalyze.getWidth(),
				imageToAnalyze.getHeight());
		// TODO refacto
		int processors = Runtime.getRuntime().availableProcessors();
		final Thread[] threads = SimpleMultiThreading.newThreads(processors);
		for (final int x = 0; x < imageToAnalyze.getWidth(); x++) {

			double progress = (x + 1) * 100 / imageToAnalyze.getWidth();
			IJ.showStatus("Computing correlation image : " + progress + "%");

			for (final int y = 0; y < imageToAnalyze.getHeight(); y++) {
				// initiate variable to compute correlation value
				final float zf = zFocus;
				final float[] iz = new float[imageToAnalyze.getNSlices()];

				for (int z = 0; z < imageToAnalyze.getNSlices(); z++) {
					imageToAnalyze.setZ(z);
					iz[z] = imageToAnalyze.getPixel(x, y)[0];
					// the first element returned by the getPixel function is
					// the grayscale values

				}


				// compute correlation value
				for (int ithread = 0; ithread < threads.length; ithread++) {
					threads[ ithread ] = new Thread(){
						public void run(){
							ComputeCorrelation computeCorrelationImage = new ComputeCorrelation(
									iz, zf, sigma, direction);
							double correlationPixelValue = computeCorrelationImage.integrate(
									0, imageToAnalyze.getNSlices() - 1);
							correlationImage.putPixelValue(x, y, correlationPixelValue);
						}
					};
				}
			}
		}
	}

	/**
	 * This method set a threshold with Ostu method on the correlation image and
	 * convert it into Binary Image
	 */
	public void convertCorrelationToBinaryImage() {

		System.out.println("Convert correlation image to binary image");

		byteImage = correlationImage.convertToByteProcessor(true);
		byteImage.setAutoThreshold(AutoThresholder.Method.Otsu, false,
				BinaryProcessor.BLACK_AND_WHITE_LUT);

		byteImage.dilate();
		byteImage.erode();
		byteImage.applyLut();
		// if the thresholding and the making binary image produced a white
		// background, change it
		if (byteImage.getStatistics().mode > 127) {
			System.out.println("Invert image");
			byteImage.invert();
		}
		BinaryProcessor binImage = new BinaryProcessor(byteImage);
		binCorrelationImage = new ImagePlus("binary Image", binImage);

		if (imageToAnalyze.getCalibration().scaled()) {
			binCorrelationImage.setCalibration(imageToAnalyze.getCalibration());
		}
	}

	/**
	 * Run with output of convertCorrelationToBinaryImage as parameter. It
	 * analyse particles of the image and filter them according to there area,
	 * and there solidity value (if requested)
	 */
	public void analyseAndFilterParticles() {

		System.out.println("Analyse results");

		resultTable = new ResultsTable();

		roiManager = new RoiManager();

		imgCorrTemp = new ImagePlus("Correlation Image of "
				+ imageToAnalyze.getShortTitle(), correlationImage);

		particleAnalyzer = new ParticleAnalyzer(
				ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES
						+ ParticleAnalyzer.SHOW_PROGRESS
						+ ParticleAnalyzer.ADD_TO_MANAGER,
				Measurements.AREA + Measurements.CENTROID
						+ Measurements.PERIMETER
						+ Measurements.SHAPE_DESCRIPTORS + Measurements.ELLIPSE,
				resultTable, minParticleInMicron, maxParticleInMicron);

		System.out.println("minParticleSize " + minParticleInMicron
				+ " maxParticleSize " + maxParticleInMicron);
		System.out.println("Analyse particles on "
				+ binCorrelationImage.getTitle() + " ...");

		particleAnalyzer.analyze(binCorrelationImage);
		System.out.println("Done");

		if (filterAbnormalShape || filtrateWithMeanGrayValue) {

			if (filtrateWithMeanGrayValue) {

				System.out.println("Filtering with mean grey value...");
				ArrayList<Integer> rowTodelete = new ArrayList<Integer>();
				int name = 1;

				System.out.println("- reset result table");
				resultTable.reset();
				Integer nbRoi = roiManager.getCount();
				if (!nbRoi.equals(0)) {

					System.out.println("- get roi as array");

					roiArray = roiManager.getRoisAsArray();
					System.out
							.println("- select and delete all roi from roi manager");

					roiManager.runCommand("Select All");
					roiManager.runCommand("Delete");

					System.out.println("- initialize analyser");

					analyzer = new Analyzer(imgCorrTemp, Measurements.AREA
							+ Measurements.STD_DEV + Measurements.MIN_MAX
							+ Measurements.SHAPE_DESCRIPTORS
							+ Measurements.MEAN + Measurements.CENTROID
							+ Measurements.PERIMETER + Measurements.ELLIPSE,
							resultTable);

					System.out
							.println("- analyze each roi and add it to manager if it is wanted");
					for (int i = 0; i < roiArray.length; i++) {
						roiArray[i].setImage(imgCorrTemp);
						imgCorrTemp.setRoi(roiArray[i]);
						analyzer.measure();
					}
					imgCorrTemp.deleteRoi();

					System.out
							.println("- delete from result table roi unwanted");
					for (int i = 0; i < resultTable
							.getColumn(ResultsTable.MEAN).length; i++) {

						if (resultTable.getValue("Mean", i) <= meanGreyValueThreshold) {
							rowTodelete.add(i);
						} else {
							roiArray[i].setName("" + name);
							roiManager.addRoi(roiArray[i]);
							name++;
						}
					}
					deleteRowOfResultTable(rowTodelete);
					System.out.println("Filter done.");
				} else {
					setRoiDetectedFalse();
				}
			}

			if (filterAbnormalShape) {

				System.out.println("Filtering with solidity...");
				Integer nbRoi = roiManager.getCount();
				if (!nbRoi.equals(0)) {
					System.out.println("- get roi as array");
					roiArray = roiManager.getRoisAsArray();
					System.out
							.println("- select and delete all roi from roi manager");
					roiManager.runCommand("Select All");
					roiManager.runCommand("Delete");

					ArrayList<Integer> rowTodelete = new ArrayList<Integer>();
					int name = 1;

					System.out
							.println("- delete from result table roi unwanted");
					for (int i = 0; i < resultTable
							.getColumn(ResultsTable.SOLIDITY).length; i++) {
						if (resultTable.getValue("Solidity", i) <= solidityThreshold) {
							rowTodelete.add(i);
						} else {
							roiArray[i].setName("" + name);
							roiManager.addRoi(roiArray[i]);
							name++;
						}
					}

					deleteRowOfResultTable(rowTodelete);
					System.out.println("Filter done.");
				} else {
					setRoiDetectedFalse();
				}
			}

		}
	}

	public void deleteRowOfResultTable(ArrayList<Integer> rowToDelete) {
		for (int i = 0; i < rowToDelete.size(); i++) {
			int row = rowToDelete.get(i) - i;
			resultTable.deleteRow(row);
		}
	}

	public RoiManager getRoiManager() {
		return roiManager;
	}

	/**
	 * Method to show and saved specified results and flush unwanted results
	 */
	public void showAndSaveResultsAndCleanUp() {
		Integer nbRoi = roiManager.getCount();
		if (nbRoi.equals(0)) {
			setRoiDetectedFalse();
		}
		System.out.println("Show and save results_" + String.valueOf(nbRoi));

		if (saveDataFrame && roiDetected) {
			System.out.println("saving data frame...");
			try {
				resultTable.saveAs(savingPath + imageToAnalyze.getShortTitle()
						+ "_Results.csv");
			} catch (IOException io) {
				IJ.error("Error", "Could not save DataFrame");
			}
		}

		if (showDataFrame) {
			System.out.println("display data frame");
			resultTable.show("Result");
			System.out.println("done.");
		} else {
			System.out.println("reset data frame");
			resultTable.reset();
		}

		if (saveRoi && roiDetected) {
			System.out.println("saving roi...");
			roiManager.runCommand("Select All");
			roiManager.runCommand("Save",
					savingPath + imageToAnalyze.getShortTitle() + "_ROI.zip");
			System.out.println("Done");
			roiManager.runCommand("Select All");
			roiManager.runCommand("Delete");
			System.out.println("Close roi manager");
			roiManager.close();
		}

		if (showFocusImage) {
			System.out.println("show focus image");
			focusImg.show();
		} else {
			System.out.println("flush focus image");
			focusImg.flush();
		}

		if (saveBinaryImg) {
			System.out.println("save binary image");
			binCorrelationImage.setTitle(imageToAnalyze.getShortTitle()
					+ "_BinaryImage");
			FileSaver fileSaver = new FileSaver(binCorrelationImage);
			fileSaver.saveAsTiff(savingPath + imageToAnalyze.getShortTitle()
					+ "_BinaryImage.tif");
		}
		if (showBinaryImg) {
			System.out.println("show binary image");
			binCorrelationImage.show();
		} else {
			System.out.println("flush binary image");
			binCorrelationImage.flush();
		}

		if (saveCorrelationImg) {
			System.out.println("save correlation image");
			imgCorrTemp.setTitle(imageToAnalyze.getShortTitle()
					+ "_CorrelationImage");
			FileSaver fileSaver = new FileSaver(imgCorrTemp);
			fileSaver.saveAsTiff(savingPath + imageToAnalyze.getShortTitle()
					+ "_CorrelationImage.tif");
		}
		if (showCorrelationImg) {
			System.out.println("show correlation image");
			imgCorrTemp.show();
		} else {
			System.out.println("flush correlation image");
			imgCorrTemp.flush();
		}
		if (flushImageToAnalyze) {
			System.out.println("flush image to analyze");
			imageToAnalyze.flush();
		}
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int newDirection) {
		direction = newDirection;
	}

	/**
	 * Return if any roi detected
	 */
	public boolean roiDetected() {
		return this.roiDetected;
	}

	public void setRoiDetectedFalse() {
		this.roiDetected = false;
	}

}
