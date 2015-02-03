package fiji.plugin.maars.cellboundaries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

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


public class CellsBoundariesIdentification {
	
	// Parameters for algorithm
	private ImagePlus imageToAnalyze;
	private ImagePlus focusImage;
	private ImagePlus imgCorrTemp;
	private int sigma;
	private float zFocus;
	private int direction; // this is the direction of the equation to integrate
	// it is 1 for image with cell boundaries be black then white
	// it is -1 for image with cell boundaries be white then black
	
	private boolean enableDoSomethingElseInParallel;
	
	// Parameters to filter results
	private double minParticleSize;
    private double maxParticleSize;
    private boolean filterUnusualShape;
    private double unusualShapeFilteringThreshold;
    private boolean filterWithMeanGrayValue;
    private double meanGreyValueThreshold;
	
	// Variables to get results
    private FloatProcessor correlationImage = null;
    private ByteProcessor byteImage;
    private ImagePlus binCorrelationImage;
    private ResultsTable resultTable;
    private ParticleAnalyzer particleAnalyzer;
    private Analyzer analyzer;
    private RoiManager roiManager;
    private Roi[] roiArray;
    
    // Result Optionsshuffle [] java
 	private boolean displayCorrelationImg;
 	private boolean displayBinaryImg;
 	private boolean displayDataFrame;
	private boolean displayFocusImage;
 	private boolean saveCorrelationImg;
 	private boolean saveBinaryImg;
 	private boolean saveDataFrame;
	private boolean saveFocusImage;
	private boolean saveRoi;
 	private String savingPath;
 	private boolean flushImageToAnalyze;
    
    /**
     * Constructor : need only a CellsBoundaries_ object 
     		int sigma : is the typical height of the cells you want to get
			int direction : depends on how you made you movie
								-> -1 if the boundaries of the cells are black on the first slice and white on the last one
								-> 1 if the boundaries of the cells are white on the first slice and black on the last one
			double minParticleSize : minimum area of cells
			double maxParticleSize : maximum area of cells
			float zf : slice corresponding to focus on the movie
			double solidityThreshold : threshold to filter unusual shape using solidity measure
			double meanGrayValueThreshold : threshold to filter background using minimum gray value measure
			boolean makeLogFile : create a file with steps and errors of the process 
			boolean flushImageToAnalyze : to empty the ImageProcessor of image to analyze
     */
	public CellsBoundariesIdentification (CellsBoundaries cB,
			int sigma,
			double minParticleSize,
			double maxParticleSize,
			int direction,
			float zf,
			double solidityThreshold,
			double meanGrayValueThreshold,
			boolean makeLogFile,
			boolean flushImageToAnalyze) {
		
		this.imageToAnalyze = cB.getImageToAnalyze();
		this.savingPath = cB.getPathDirField().getText();
		
		if (makeLogFile) {
			try {
				PrintStream ps = new PrintStream(savingPath+imageToAnalyze.getShortTitle()+"BoundariesIdentification.LOG");
				System.setOut(ps);
				System.setErr(ps);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		this.sigma = sigma;
		this.filterUnusualShape = cB.getFilterUnususalCkb().getState();
		this.filterWithMeanGrayValue = cB.getFilterWithMeanGreyValueCkb().getState();
		this.minParticleSize = minParticleSize;
		this.maxParticleSize = maxParticleSize;
		this.direction = direction;
		
		
		// ResultOptions
		displayCorrelationImg = cB.getDisplayCorrelationImg().getState();
		displayBinaryImg = cB.getDisplayBinaryImg().getState();
		displayDataFrame = cB.getDisplayDataFrame().getState();
		displayFocusImage = cB.getDisplayFocusImage().getState();
		
		saveCorrelationImg = cB.getSaveCorrelationImg().getState();
		saveBinaryImg = cB.getSaveBinaryImg().getState();
		saveDataFrame = cB.getSaveDataFrame().getState();
		saveFocusImage = cB.getSaveFocusImage().getState();
		saveRoi = cB.getSaveRoi().getState();
		this.flushImageToAnalyze = flushImageToAnalyze;
		zFocus = zf;
		
		getFocusImage();
		
		unusualShapeFilteringThreshold = solidityThreshold;
		this.meanGreyValueThreshold = meanGrayValueThreshold;
		
		enableDoSomethingElseInParallel = true;
		
	}
	
	/**
	 * Constructor : need an ImagePlus object, a sigma value and a boolean notifying if the cells must be filtered
	 * according to there shape. Default result options are also set : all is true.
	 */
	public CellsBoundariesIdentification (ImagePlus imageToAnalyse,
			int sigma,
			boolean filterUnusualShape,
			boolean filterWithMinGrayValue,
			String savingPath,
			double minParticleSize,
			double maxParticleSize,
			int direction,
			float zf,
			double solidityThreshold,
			double meanGrayValueThreshold){
		
		this.savingPath = savingPath;
		
		this.imageToAnalyze = imageToAnalyse;
		
		try {
			PrintStream ps = new PrintStream(savingPath+imageToAnalyse.getShortTitle()+"BoundariesIdentification.LOG");
			System.setOut(ps);
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.sigma = sigma;
		this.filterUnusualShape = filterUnusualShape;
		this.direction = direction;
		
		// Default ResultOptions
		displayCorrelationImg = true;
		displayBinaryImg = true;
		displayDataFrame = true;
		displayFocusImage = true;
		
		saveCorrelationImg = true;
		saveBinaryImg = true;
		saveDataFrame = true;
		saveFocusImage = true;
		saveRoi = true;
		

		this.flushImageToAnalyze = true;
		
		this.minParticleSize = minParticleSize;
		this.maxParticleSize = maxParticleSize;
		
		enableDoSomethingElseInParallel = true;
		
		zFocus = zf;
		
		unusualShapeFilteringThreshold = solidityThreshold;
		this.meanGreyValueThreshold = meanGrayValueThreshold;
		this.filterWithMeanGrayValue = filterWithMinGrayValue;

		getFocusImage();
	}
	
	/**
	 * Constructor : need to enter all the variables.
	  		int sigma : is the typical height of the cells you want to get
			int direction : depends on how you made you movie
								-> -1 if the boundaries of the cells are black on the first slice and white on the last one
								-> 1 if the boundaries of the cells are white on the first slice and black on the last one
			IJ ij : IJ object to display error message
			boolean enableDoSomethingElseInParallel : this variable is supposed to allow or not to run the program on one thread only
														true means you can do something else during the running
			boolean filterUnusualShape : filter shape with solidity < threshold
			boolean filterWithMinGrayValue : filter background with min >= threshold
			boolean displayCorrelationImg : show correlation image
			boolean displayBinaryImg : show binary image
			boolean displayDataFrame : show data frame
			boolean displayFocusImage : show focus Image
			boolean saveCorrelationImg :  save correlation image
			boolean saveBinaryImg : save binary image
			boolean saveDataFrame : save data frame
			boolean saveFocusImage : save focus image
			boolean saveRoi : save Regions of Interest in a zip folder
			String savingPath : path where the data are being saved
			double minParticleSize : minimum area of cells
			double maxParticleSize : maximum area of cells
			float zf : slice corresponding to focus on the movie
			double solidityThreshold : threshold to filter unusual shape using solidity measure
			double meanGrayValueThreshold : threshold to filter background using minimum gray value measure
			boolean makeLogFile : create a file with steps and errors of the process 
			boolean flushImageToAnalyze : to empty the ImageProcessor of image to analyze
	 */
	public CellsBoundariesIdentification (ImagePlus imageToAnalyse,
			int sigma,
			int direction,
			boolean enableDoSomethingElseInParallel,
			boolean filterUnusualShape,
			boolean filterWithMinGrayValue,
			boolean displayCorrelationImg,
			boolean displayBinaryImg,
			boolean displayDataFrame,
			boolean displayFocusImage,
			boolean saveCorrelationImg,
			boolean saveBinaryImg,
			boolean saveDataFrame,
			boolean saveFocusImage,
			boolean saveRoi,
			String savingPath,
			double minParticleSize,
			double maxParticleSize,
			float zf,
			double solidityThreshold,
			double meanGrayValueThreshold,
			boolean makeLogFile,
			boolean flushImageToAnalyze){
		
		this.savingPath =  savingPath;
		
		this.imageToAnalyze = imageToAnalyse;
		
		if (makeLogFile) {
			try {
				PrintStream ps = new PrintStream(savingPath+imageToAnalyse.getShortTitle()+"BoundariesIdentification.LOG");
				System.setOut(ps);
				System.setErr(ps);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.sigma = sigma;
		this.filterUnusualShape = filterUnusualShape;
		this.direction = direction;
		
		// ResultOptions
		this.displayCorrelationImg = displayCorrelationImg;
		this.displayBinaryImg = displayBinaryImg;
		this.displayDataFrame = displayDataFrame;
		this.displayFocusImage = displayFocusImage;
		
		this.saveCorrelationImg = saveCorrelationImg;
		this.saveBinaryImg = saveBinaryImg;
		this.saveDataFrame = saveDataFrame;
		this.saveFocusImage = saveFocusImage;
		this.saveRoi = saveRoi;

		this.flushImageToAnalyze = flushImageToAnalyze;
		
		this.minParticleSize = minParticleSize;
		this.maxParticleSize = maxParticleSize;
		
		this.enableDoSomethingElseInParallel = enableDoSomethingElseInParallel;

		zFocus = zf;
		
		unusualShapeFilteringThreshold = solidityThreshold;

		this.meanGreyValueThreshold = meanGrayValueThreshold;
		this.filterWithMeanGrayValue = filterWithMinGrayValue;
		
		getFocusImage();
	}
	
	// I know it is messy but I have not found an other solution yet
	public void getFocusImage() {
		System.out.println("get Focus Image");
		
		imageToAnalyze.setZ((int) Math.round(zFocus)-1);
		
		focusImage = new ImagePlus(imageToAnalyze.getShortTitle()+"FocusImage", imageToAnalyze.getProcessor().duplicate());
		
		if(imageToAnalyze.getCalibration().scaled()) {
			focusImage.setCalibration(imageToAnalyze.getCalibration());
		}
		
		if (saveFocusImage) {
			FileSaver fileSaver = new FileSaver(focusImage);
			fileSaver.saveAsTiff(savingPath+imageToAnalyze.getShortTitle()+"FocusImage.tif");
		}
	}
	
	/**
	 * Create an image correlation where each pixel corresponds to the correlation of a specific curve
	 * see equation in computeCorrelation object
	 */
	public void createCorrelationImage() {

    	System.out.println("creating correlation image");
		
    	
    	correlationImage = new FloatProcessor(imageToAnalyze.getWidth(), imageToAnalyze.getHeight());
    	
    	for (int x = 0; x < imageToAnalyze.getWidth(); x++) {
    		
    		double progress = (x+1)*100/imageToAnalyze.getWidth();
    		IJ.showStatus("Computing correlation image : "+progress+"%");
    		
    		for (int y = 0; y < imageToAnalyze.getHeight(); y++) {
    			// initiate variable to compute correlation value
    			float zf = zFocus;
    			float[] iz = new float[imageToAnalyze.getNSlices()];
    			
    			for (int z = 0; z < imageToAnalyze.getNSlices(); z++) {
    				imageToAnalyze.setZ(z);
    				iz[z] = imageToAnalyze.getPixel(x, y)[0];
    				// the first element returned by the getPixel function is the grayscale values
    				
    			}
    			
    			ComputeCorrelation computeCorrelationImage = new ComputeCorrelation(iz, zf, sigma, direction);
    			
    			// compute correlation value
    			double correlationPixelValue = 0;
    			
    			correlationPixelValue = computeCorrelationImage.integrate(0, imageToAnalyze.getNSlices()-1);
    			
    			
    			correlationImage.putPixelValue(x, y, correlationPixelValue);
    			
    		}
    	}
	}
	
	/**
	 * This method set a threshold with Ostu method on the correlation image and convert it into Binary Image
	 */
    public void convertCorrelationToBinaryImage() {
    	
    	System.out.println("Convert correlation image to binary image");
    	
        byteImage = correlationImage.convertToByteProcessor(true);
        byteImage.setAutoThreshold(AutoThresholder.Method.Otsu, false, BinaryProcessor.BLACK_AND_WHITE_LUT);
        //byteImage.autoThreshold();
        //byteImage.threshold(byteImage.getAutoThreshold());        

        byteImage.dilate();
        byteImage.erode();


        // if the thresholding and the making binary image produced a white background, change it
        if(byteImage.getStatistics().mode < 127) {
        	System.out.println("Invert image");
        	byteImage.invert();
        }
        binCorrelationImage = new ImagePlus("binary correlation Image", byteImage);
        
        if(imageToAnalyze.getCalibration().scaled()) {
        	binCorrelationImage.setCalibration(imageToAnalyze.getCalibration());
        }
    }
    
    /**
     * Run with output of convertCorrelationToBinaryImage as parameter.
     * It analyse particles of the image and filter them according to there area, and there solidity value
     * (if requested)
     */
    public void analyseAndFilterParticles(){
    	
    	System.out.println("Analyse results");
    	
    	resultTable = new ResultsTable();
    	
    	roiManager = new RoiManager();    	
    	
		imgCorrTemp = new ImagePlus("Correlation Image of "+imageToAnalyze.getShortTitle(), correlationImage);
    	
		particleAnalyzer = new ParticleAnalyzer(
				ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES
        		+ParticleAnalyzer.SHOW_PROGRESS
        		+ParticleAnalyzer.ADD_TO_MANAGER,
        		Measurements.AREA
        		+Measurements.CENTROID
        		+Measurements.PERIMETER
        		+Measurements.SHAPE_DESCRIPTORS
        		+Measurements.ELLIPSE,
        		resultTable, minParticleSize, maxParticleSize);
		
		System.out.println("minParticleSize "+minParticleSize+" maxParticleSize "+maxParticleSize);

		System.out.println("Analyse particles on "+binCorrelationImage.getTitle()+" ...");
		particleAnalyzer.analyze(binCorrelationImage);
        System.out.println("Done");
        
        
        if (filterUnusualShape || filterWithMeanGrayValue) {
        	

	        if (filterWithMeanGrayValue) {
	        	
	        	System.out.println("Filtering with mean grey value...");
	        	ArrayList<Integer> rowTodelete = new ArrayList<Integer>();
	        	int name = 1;
	        	
	        	System.out.println("- reset result table");
	        	resultTable.reset();
	        	System.out.println("- get roi as array");
	        	roiArray = roiManager.getRoisAsArray();
	        	
	        	System.out.println("- select and delete all roi from roi manager");
		    	roiManager.runCommand("Select All");
	        	roiManager.runCommand("Delete");
	        	
	        	System.out.println("- initialize analyser");
		    	analyzer = new Analyzer(imgCorrTemp, Measurements.AREA
		    			+Measurements.STD_DEV
		        		+Measurements.MIN_MAX
		        		+Measurements.SHAPE_DESCRIPTORS
		        		+Measurements.MEAN
		        		+Measurements.CENTROID
		        		+Measurements.PERIMETER
		        		+Measurements.ELLIPSE
        		, resultTable);
		    	
	        	System.out.println("- analyze each roi and add it to manager if it is wanted");
		    	for (int i = 0; i < roiArray.length; i++) {
		    		roiArray[i].setImage(imgCorrTemp);
		    		imgCorrTemp.setRoi(roiArray[i]);
		    		analyzer.measure();
		    	}
		    	imgCorrTemp.deleteRoi();
		    	
		    	System.out.println("- delete from result table roi unwanted");
	        	for (int i = 0; i < resultTable.getColumn(ResultsTable.MEAN).length; i++) {
	        		
	        		if (resultTable.getValue("Mean", i) <= meanGreyValueThreshold) {
	        			rowTodelete.add(i);
	        		}
	        		else {
	        			roiArray[i].setName(""+name);
	        			roiManager.addRoi(roiArray[i]);
	        			name++;
	        		}
	        	}
	        	deleteRowOfResultTable(rowTodelete);
	        	System.out.println("Filter done.");
	        }
	    	
	        if (filterUnusualShape) {
	        	
	        	System.out.println("Filtering with solidity...");
	        	
	        	System.out.println("- get roi as array");
		        roiArray = roiManager.getRoisAsArray();
		        
		        System.out.println("- select and delete all roi from roi manager");
		    	roiManager.runCommand("Select All");
		    	roiManager.runCommand("Delete");
		    	
	        	ArrayList<Integer> rowTodelete = new ArrayList<Integer>();
	        	int name = 1;
	        	
	        	System.out.println("- delete from result table roi unwanted");
	        	for (int i = 0; i < resultTable.getColumn(ResultsTable.SOLIDITY).length; i++) {
	        		if (resultTable.getValue("Solidity", i) <= unusualShapeFilteringThreshold) {
	        			rowTodelete.add(i);
	        		}
	        		else {
	        			roiArray[i].setName(""+name);
	        			roiManager.addRoi(roiArray[i]);
	        			name++;
	        		}
	        	}

	        	deleteRowOfResultTable(rowTodelete);
	        	System.out.println("Filter done.");
	        }
	        
        }
    }
    
    public void deleteRowOfResultTable(ArrayList<Integer> rowToDelete){
    	for (int i = 0; i < rowToDelete.size(); i++) {
    		int row = rowToDelete.get(i)-i;
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
    	
    	System.out.println("Show and save results");
    	
        if (saveDataFrame){
        	System.out.println("saving data frame...");
        	try {
        		resultTable.saveAs(savingPath+imageToAnalyze.getShortTitle()+"Results.csv");
        	}
        	catch (IOException io){
        		IJ.error("Error", "Could not save DataFrame");
        	}
        }

        if (displayDataFrame){
        	System.out.println("display data frame");
        	resultTable.show("Result");
        	System.out.println("done.");
        }
        else {
        	System.out.println("reset data frame");
        	resultTable.reset();
        }
        
        if(saveRoi) {
        	System.out.println("saving roi...");
        	roiManager.runCommand("Select All");
        	roiManager.runCommand("Save", savingPath+imageToAnalyze.getShortTitle()+"ROI.zip");
        	System.out.println("Done");
        	System.out.println("Close roi manager");
        	roiManager.close();
        	
        }
        

    	if (displayFocusImage) {
    		System.out.println("show focus image");
    		focusImage.show();
    	}
    	else {
    		System.out.println("flush focus image");
    		focusImage.flush();
    	}

        if (saveBinaryImg) {
        	System.out.println("save binary image");
        	binCorrelationImage.setTitle(imageToAnalyze.getShortTitle()+"BinaryImage");
			FileSaver fileSaver = new FileSaver(binCorrelationImage);
			fileSaver.saveAsTiff(savingPath+imageToAnalyze.getShortTitle()+"BinaryImage.tif");
        }
        if (displayBinaryImg) {
        	System.out.println("show binary image");
        	binCorrelationImage.show();
        }
        else {
        	System.out.println("flush binary image");
        	binCorrelationImage.flush();
        }
        
    	if (saveCorrelationImg) {
    		System.out.println("save correlation image");
    		imgCorrTemp.setTitle(imageToAnalyze.getShortTitle()+"CorrelationImage");
    		FileSaver fileSaver = new FileSaver(imgCorrTemp);
    		fileSaver.saveAsTiff(savingPath+imageToAnalyze.getShortTitle()+"CorrelationImage.tif");
    	}
    	if (displayCorrelationImg){
    		System.out.println("show correlation image");
    		imgCorrTemp.show();
    	}
    	else {
    		System.out.println("flush correlation image");
    		imgCorrTemp.flush();
    	}
    	if (flushImageToAnalyze) {
	        System.out.println("flush image to analyze");
	        imageToAnalyze.flush();
    	}
    }

	/** Create a Thread[] array as large as the number of processors available. 
    * From Stephan Preibisch's Multithreading.java class. See: 
    * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
    */  
    private Thread[] newThreadArray() {  
        int n_cpus = Runtime.getRuntime().availableProcessors();  
        return new Thread[n_cpus];  
    }
    
    public int getDirection(){
    	return direction;
    }
    
    public void setDirection(int newDirection){
    	direction = newDirection;
    }
    
    /**
     * Run algorithm
     */
    public void identifyCellesBoundaries () {
    	
    	// Run the Methods on one thread only
    	// So it is possible to run other process while this one is running
    	// NB : createCorrelation is the longest method to execute
    	if (enableDoSomethingElseInParallel) {
	    	final Thread[] threads = newThreadArray();
	    	threads[0] = new Thread() {  
	            
	            { setPriority(Thread.NORM_PRIORITY); }  
	
	            public void run (){
	            	createCorrelationImage();
	            	convertCorrelationToBinaryImage();
	            	analyseAndFilterParticles();
	            	showAndSaveResultsAndCleanUp();
	            }
	    	
	    	};
	    	threads[0].start();
	    	
	    }
    	else {
    		System.out.println("Do not create threads");
    		createCorrelationImage();
    		convertCorrelationToBinaryImage();
        	analyseAndFilterParticles();
        	showAndSaveResultsAndCleanUp();
    	}
    }
    
    
}
