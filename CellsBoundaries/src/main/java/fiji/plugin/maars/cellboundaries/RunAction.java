package fiji.plugin.maars.cellboundaries;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;


public class RunAction implements ActionListener{
	
	private CellsBoundaries cB;
	
	// Parameters of the algorithm
	private CellsBoundariesIdentification cellBoundId;
	private int sigma;
	private int maxWidth;
	private int maxHeight;
	private double minParticleSize;
    private double maxParticleSize;
    private float zFocus;
    private double solidityThreshold;
    private double meanGrayValThreshold;
	
	// Allow to display the name of the file used in the algorithm
	// meaning file currently selected or file found with browser
	private JTextField fileNameField;
	
	// Variable for the actionPerformed : if this is true that means units has been checked
	private boolean unitsChecked;
	
	public RunAction (CellsBoundaries cB) {
		this.cB = cB;
		fileNameField = cB.getFileNameField();
		unitsChecked = false;
		
	}
	
	/**
	 * Method to get the state of all the result Option checkbox and return false
	 * if none of them are selected
	 */
	public boolean checkResultOptions() {
			
		return (cB.getDisplayCorrelationImg().getState()
				|| cB.getDisplayBinaryImg().getState()
				|| cB.getDisplayDataFrame().getState()
				|| cB.getSaveCorrelationImg().getState()
				|| cB.getSaveBinaryImg().getState()
				|| cB.getSaveDataFrame().getState()
				|| cB.getDisplayFocusImage().getState()
				|| cB.getSaveFocusImage().getState()
				|| cB.getSaveRoi().getState());
	}
	
	/**
	 * Allows to convert width or height or depth of a size in microns to a size in pixels.
	 * To choose if you want to convert width or height you can use CellsBoundaries constants : WIDTH and HEIGHT and DEPTH.
	 * Return an int width or height in pixels.
	 */
	public int convertMicronToPixelSize(double micronSize, int widthOrHeightOrDepth) {
		int pixelSize = (int) Math.round(micronSize/cB.getScale()[widthOrHeightOrDepth]);
		return pixelSize;
	}
	
	
	/**
	 * Allows to convert width or height or depth of a size in pixels to a size in microns.
	 * To choose if you want to convert width or height you can use CellsBoundaries constants : WIDTH and HEIGHT and DEPTH.
	 * Return an double width or height in microns.
	 */
	public double convertPixelToMicronSize (int pixelSize, int widthOrHeightOrDepth){
		double micronSize = (double) cB.getScale()[widthOrHeightOrDepth]*pixelSize;
		
		return micronSize;
	}
	
	/**
	 * Method to check if the image is scaled and if the unit matches 'micron'
	 */
	public void checkUnitsAndScale(){
		System.out.println("Check if image is scaled");
		if (cB.getImageToAnalyze().getCalibration().scaled()){
			
			if(cB.getImageToAnalyze().getCalibration().getUnit().equals("cm")) {
				cB.getImageToAnalyze().getCalibration().setUnit("micron");
				cB.getImageToAnalyze().getCalibration().pixelWidth = cB.getImageToAnalyze().getCalibration().pixelWidth * 10000;
				cB.getImageToAnalyze().getCalibration().pixelHeight = cB.getImageToAnalyze().getCalibration().pixelHeight * 10000;
			}
			
			System.out.println("Check if unit of calibration is micron");
			if (cB.getImageToAnalyze().getCalibration().getUnit().equals("micron") || cB.getImageToAnalyze().getCalibration().getUnit().equals("µm")) {
				double[] scale = new double[3];
				scale[CellsBoundaries.WIDTH] = cB.getImageToAnalyze().getCalibration().pixelWidth;
				scale[CellsBoundaries.HEIGHT] = cB.getImageToAnalyze().getCalibration().pixelHeight;
				scale[CellsBoundaries.DEPTH] = cB.getImageToAnalyze().getCalibration().pixelDepth;
				
				cB.setScale(scale);
				unitsChecked = true;
				System.out.println("Get and set calibration as scale");
			}
			else {
				IJ.error("Wrong scale unit", "The scale of your image must be in microns.\nTo change it you can go to Properties ...");
			}
		}
		else {
			IJ.error("No scale", "You must set a scale to your image\nif you want to enter measures in micron.\nYou can go to Properties ...");
		}
	}
	
	/**
	 * Method to change the image scale if it is bigger than supposed to
	 * the maximum width an height are in pixels
	 */
	public void changeScale(int maxWidth, int maxHeight){
		int newWidth;
		int newHeight;
		int newMaxParticleSize;
		int newMinParticleSize;
		Calibration newCal = new Calibration();
		newCal.setUnit("micron");
		//TODO refact
		if (cB.getImageToAnalyze().getWidth() > maxWidth) {
			System.out.println("Image width is greater than maximum width allowed");
			
			newWidth = maxWidth;
        	newHeight = (int)cB.getImageToAnalyze().getHeight()*maxWidth/cB.getImageToAnalyze().getWidth(); 
        	
        	newMinParticleSize = (int)minParticleSize*maxWidth/cB.getImageToAnalyze().getWidth();
        	newMaxParticleSize = (int)maxParticleSize*maxWidth/cB.getImageToAnalyze().getWidth();
        	
        	if (cB.getImageToAnalyze().getCalibration().scaled()) {
        	
	        	newCal.pixelWidth = cB.getScale()[CellsBoundaries.WIDTH]*cB.getImageToAnalyze().getWidth()/maxWidth;
	        	newCal.pixelHeight = cB.getScale()[CellsBoundaries.HEIGHT]*cB.getImageToAnalyze().getWidth()/maxWidth;
	        	newCal.pixelDepth = cB.getScale()[CellsBoundaries.DEPTH];
        	}
        	
        	System.out.println("New values : w = "+newWidth+" h = "+newHeight);
        	if (newHeight > maxHeight) {
        		System.out.println("New height is still greater than maximum height allowed");
        		newHeight = maxHeight;
        		newWidth = (int)cB.getImageToAnalyze().getWidth()*maxHeight/cB.getImageToAnalyze().getHeight();
        		
        		newMinParticleSize = (int)minParticleSize*maxHeight/cB.getImageToAnalyze().getHeight();
            	newMaxParticleSize = (int)maxParticleSize*maxHeight/cB.getImageToAnalyze().getHeight();
            	
            	if (cB.getImageToAnalyze().getCalibration().scaled()) {
	            	newCal.pixelWidth = cB.getScale()[CellsBoundaries.WIDTH]*cB.getImageToAnalyze().getHeight()/maxHeight;
	            	newCal.pixelHeight = cB.getScale()[CellsBoundaries.HEIGHT]*cB.getImageToAnalyze().getHeight()/maxHeight;
	            	newCal.pixelDepth = cB.getScale()[CellsBoundaries.DEPTH];
            	}
        		
        		System.out.println("New values : w = "+newWidth+" h = "+newHeight);
        		
        	}
        	
        	rescale(newWidth, newHeight, newMinParticleSize, newMaxParticleSize, newCal);
    	}
		else {
			if (cB.getImageToAnalyze().getHeight() > maxHeight) {
				System.out.println("Image height is greater than maximum width allowed");
				
				newHeight = maxHeight;
        		newWidth = (int)cB.getImageToAnalyze().getWidth()*maxHeight/cB.getImageToAnalyze().getHeight();

        		newMinParticleSize = (int)minParticleSize*maxHeight/cB.getImageToAnalyze().getHeight();
            	newMaxParticleSize = (int)maxParticleSize*maxHeight/cB.getImageToAnalyze().getHeight();
            	

            	if (cB.getImageToAnalyze().getCalibration().scaled()) {
	            	newCal.pixelWidth = cB.getScale()[CellsBoundaries.WIDTH]*cB.getImageToAnalyze().getHeight()/maxHeight;
	            	newCal.pixelHeight = cB.getScale()[CellsBoundaries.HEIGHT]*cB.getImageToAnalyze().getHeight()/maxHeight;
	            	newCal.pixelDepth = cB.getScale()[CellsBoundaries.DEPTH];
            	}
        		
        		System.out.println("New values : w = "+newWidth+" h = "+newHeight);
        		
        		if (newWidth > maxWidth) {
        			System.out.println("New Width is still greater than maximum height allowed");
        			
        			newWidth = maxWidth;
                	newHeight = (int)cB.getImageToAnalyze().getHeight()*maxWidth/cB.getImageToAnalyze().getWidth();
                	

                	if (cB.getImageToAnalyze().getCalibration().scaled()) {
	                	newCal.pixelWidth = cB.getScale()[CellsBoundaries.WIDTH]*cB.getImageToAnalyze().getWidth()/maxWidth;
	                	newCal.pixelHeight = cB.getScale()[CellsBoundaries.HEIGHT]*cB.getImageToAnalyze().getWidth()/maxWidth;
	                	newCal.pixelDepth = cB.getScale()[CellsBoundaries.DEPTH];
                	}
                	
                	System.out.println("New values : w = "+newWidth+" h = "+newHeight);
                	
                	newMinParticleSize = (int)minParticleSize*maxWidth/cB.getImageToAnalyze().getWidth();
                	newMaxParticleSize = (int)maxParticleSize*maxWidth/cB.getImageToAnalyze().getWidth();
                	
        		}

            	rescale(newWidth, newHeight, newMinParticleSize, newMaxParticleSize, newCal);
			}
		}
	}
	
	/**
	 * Method to change size and scale of the image to analyse :
	 * need to compute parameters before so it can be coherent
	 */
	public void rescale(int newWidth, int newHeight, int newMinParticleSize, int newMaxParticleSize, Calibration newCal){
		
		minParticleSize = newMinParticleSize;
    	maxParticleSize = newMaxParticleSize;
    	
		
    	System.out.println("min area = "+newMinParticleSize+" max area = "+newMaxParticleSize);
    	
    	ImageStack newImgStack = new ImageStack(newWidth, newHeight);
    	
    	for (int slice = 0; slice < cB.getImageToAnalyze().getNSlices(); slice ++) {
    		cB.getImageToAnalyze().setZ(slice);
    		newImgStack.addSlice(cB.getImageToAnalyze().getProcessor().resize(newWidth, newHeight));
    	}
    	
    	ImagePlus newImagePlus = new ImagePlus("rescaled_"+cB.getImageToAnalyze().getTitle(), newImgStack);
    	newImagePlus.setCalibration(newCal);
    	
    	cB.setImageToAnalyze(newImagePlus);
		
		checkUnitsAndScale();
	}

	
	/**
	 * Action performed when run Button is triggered.
	 * It checks all parameters then run Algorithm.
	 */
	public void actionPerformed(ActionEvent e) {
		//TODO maybe to refacto, too much condition test
		// Check if fileNameField is empty meaning if there is a file to process
		System.out.println("Check if fileNameField is empty");
		
		if (fileNameField.getText().isEmpty()) {
			IJ.error("No file","You must select a file to process");
			fileNameField.setBackground(Color.ORANGE);
		}
		else {
			// Check if none of the result ckeckBox is selected : in this case, the user would not get any result
			System.out.println("Check if none of the result ckeckBox is selected");
			boolean thereIsAResult = checkResultOptions();
			if (!thereIsAResult) {
				IJ.error("No result possible","You have not selected a way to see your results");
			}
			else {
				// check if typical cell size field has been filled
				System.out.println("check if typical cell Z size field has been filled");
				if (cB.getSizeField().getText().isEmpty()) {
					IJ.error("Missing parameter","You need to fill the typical cell Z size field");
				}
				else {
					
					// if the unit chosen is a micron it must be converted
					System.out.println("Check if one of the unite used is micron");
					if(CellsBoundaries.MICRONS == cB.getSizeComUnit().getSelectedIndex()
							|| CellsBoundaries.MICRONS == cB.getMaxWComboUnit().getSelectedIndex()
							|| CellsBoundaries.MICRONS == cB.getMaxHComboUnit().getSelectedIndex()
							|| CellsBoundaries.MICRONS == cB.getMinParticleSizeComboUnit().getSelectedIndex()
							|| CellsBoundaries.MICRONS == cB.getMaxParticleSizeComboUnit().getSelectedIndex()) {
						checkUnitsAndScale();
					}
					
					// Get cell typical size and check if the user did not make any mistake while filling the field
					double typicalCellSize = 0;
					System.out.println("Try to get typical size");
					try {
						typicalCellSize = Double.parseDouble(cB.getSizeField().getText());
					}
					catch (NumberFormatException nfe){
						IJ.error("Wrong parameter", "The typical cell size is supposed to be a number");
					}
					// Check if the cell size is a size
					if (typicalCellSize <= 0){
						IJ.error("Wrong parameter", "The typical cell size must be a positive, not null value");
					}
					else {
					
						// Convert size into pixels
						if (CellsBoundaries.MICRONS == cB.getSizeComUnit().getSelectedIndex() && unitsChecked) {
							sigma = convertMicronToPixelSize(typicalCellSize, CellsBoundaries.DEPTH);
							System.out.println("typical size is in micron, convert it in pixels : "+sigma);
						}
						else  {
							if (CellsBoundaries.PIXELS == cB.getSizeComUnit().getSelectedIndex()){
								sigma = (int) typicalCellSize;
							}
						}
						
						// Read minimum cell area 
						System.out.println("Try to read minimum particle size");
						double minParticleSizeTemp = 0;
						
						try {
							minParticleSizeTemp = Double.parseDouble(cB.getMinParticleSizeField().getText());
						}
						catch (NumberFormatException nfe){
							IJ.error("Wrong parameter", "The minimum area is supposed to be a number");
						}

						if (minParticleSizeTemp <= 0){
							IJ.error("Wrong parameter", "The minimum area must be a positive not null value");
						}
						else {
							// Covert minimum area if needed to
							if (CellsBoundaries.MICRONS == cB.getMinParticleSizeComboUnit().getSelectedIndex() && unitsChecked) {
								minParticleSize = minParticleSizeTemp*convertMicronToPixelSize(1, CellsBoundaries.WIDTH)*convertMicronToPixelSize(1, CellsBoundaries.HEIGHT);
								System.out.println("Cell Area is in micron, convert it in pixels : "+minParticleSize);
							}
							else  {
								if (CellsBoundaries.PIXELS == cB.getMinParticleSizeComboUnit().getSelectedIndex()){
									minParticleSize = minParticleSizeTemp;
								}
							}
							
							// Read maximum cell area 
							System.out.println("Try to read maximum particle size");
							double maxParticleSizeTemp = 0;
							
							try {
								maxParticleSizeTemp = Double.parseDouble(cB.getMaxParticleSizeField().getText());
							}
							catch (NumberFormatException nfe){
								IJ.error("Wrong parameter", "The maximum area is supposed to be a number");
							}

							if (maxParticleSizeTemp <= 0){
								IJ.error("Wrong parameter", "The maximum area must be a positive not null value");
							}
							else {
								// Covert maximum area if needed to
								if (CellsBoundaries.MICRONS == cB.getMaxParticleSizeComboUnit().getSelectedIndex() && unitsChecked) {
									maxParticleSize = maxParticleSizeTemp*convertMicronToPixelSize(1, CellsBoundaries.WIDTH)*convertMicronToPixelSize(1, CellsBoundaries.HEIGHT);
									System.out.println("Cell Area is in micron, convert it in pixels : "+maxParticleSize);
								}
								else  {
									if (CellsBoundaries.PIXELS == cB.getMaxParticleSizeComboUnit().getSelectedIndex()){
										maxParticleSize = maxParticleSizeTemp;
									}
								}
								
								
								// If the user chose to change the scale
								System.out.println("Check if user wants to change scale");
								if (cB.getScaleCkb().getState()){
									// Check if the user entered correct values then get the values and convert them if necessary
									double newMaxWidth = 0;
									System.out.println("Try to read width value");
									try {
										newMaxWidth = Double.parseDouble(cB.getMaxWTextField().getText());
									}
									catch (NumberFormatException nfe){
										IJ.error("Wrong parameter", "The maximum width is supposed to be a number");
									}
									
									if (newMaxWidth <= 0){
										IJ.error("Wrong parameter", "The maximum width must be a positive not null value");
									}
									else {
									
										double newMaxHeight = 0;
										System.out.println("Try to read height value");
										try {
											newMaxHeight = Double.parseDouble(cB.getMaxHTextField().getText());
										}
										catch (NumberFormatException nfe){
											IJ.error("Wrong parameter", "The maximum height is supposed to be a number");
										}
										
										if (newMaxHeight <= 0){
											IJ.error("Wrong parameter", "The maximum height must be a positive not null value");
										}
										else {
				
											if(CellsBoundaries.MICRONS == cB.getMaxWComboUnit().getSelectedIndex() && unitsChecked) {
												
												maxWidth = convertMicronToPixelSize(newMaxWidth, CellsBoundaries.WIDTH);
												System.out.println("Width value is in micron, convert it in pixel : "+maxWidth);
											}
											else {
												if (CellsBoundaries.PIXELS == cB.getMaxWComboUnit().getSelectedIndex()){
													maxWidth =  (int) newMaxWidth;
												}
											}
											if(CellsBoundaries.MICRONS == cB.getMaxHComboUnit().getSelectedIndex() && unitsChecked){
												
												maxHeight = convertMicronToPixelSize(newMaxHeight, CellsBoundaries.HEIGHT);
												System.out.println("Height value is in micron, convert it in pixel : "+maxHeight);
											}
											else {
												if(CellsBoundaries.PIXELS == cB.getMaxHComboUnit().getSelectedIndex()){
													maxHeight = (int) newMaxHeight;
												}
											}
											// Then we can change scale
											System.out.println("Change scale");
											changeScale(maxWidth, maxHeight);
											
										}
									}
								}

								System.out.println("Check if user wants to precise z focus");
								if (cB.getPreciseZFocusCheckbox().getState()) {
									
									float zf = (cB.getImageToAnalyze().getNSlices()/2);
									
									System.out.println("Try to read z focus value");
									try {
										zf = Float.parseFloat(cB.getPreciseZFocusTextField().getText());
									}
									catch (NumberFormatException nfe){
										IJ.error("Wrong parameter", "The slice corresponding to focus\nis supposed to be a number\n1 <= focus slice <= "+cB.getImageToAnalyze().getNSlices()+"\nProgram will continue with default focus slice");
										cB.getPreciseZFocusCheckbox().setState(false);
									}
									if (zf <= 0) {
										IJ.error("Wrong parameter", "If you want to precise focus slice,\nplease fill correctly the field\n1 <= focus slice <= "+cB.getImageToAnalyze().getNSlices()+"\nProgram will continue with default focus slice");
										cB.getPreciseZFocusCheckbox().setState(false);
									}
									
									zFocus = zf-1;
									
								}
								else {
									zFocus = (cB.getImageToAnalyze().getNSlices()/2) -1;
								}
								
								System.out.println("Check if user want to filter shape using solidity");
								if(cB.getFilterUnususalCkb().getState()) {
									double sldtThrshld = 0;
									
									System.out.println("Try to read solidity threshold value");
									try {
										sldtThrshld = Double.parseDouble(cB.getSolidityField().getText());
									}
									catch (NumberFormatException nfe){
										IJ.error("Wrong parameter", "The solidity threshold value\nis supposed to be a number\nProgram will continue without filter");
										cB.getFilterUnususalCkb().setState(false);
									}
									if (sldtThrshld <= 0 || sldtThrshld > 1) {
										IJ.error("Wrong parameter", "If you want to filter unusual shapes,\nplease fill correctly the field\n0 < soliditythreshold <= 1\nProgram will continue without filter");
										cB.getFilterUnususalCkb().setState(false);
									}
									else {
										solidityThreshold = sldtThrshld;
									}
									
								}
								else {
									solidityThreshold = 0;
								}
								
								System.out.println("Check if user want to filter background using mean gray value");
								if(cB.getFilterWithMeanGreyValueCkb().getState()) {
									double mnGryVlThrshld = 0;
									
									System.out.println("Try to read solidity threshold value");
									try {
										mnGryVlThrshld = Double.parseDouble(cB.getMeanGreyValueThresholdField().getText());
									}
									catch (NumberFormatException nfe){
										IJ.error("Wrong parameter", "The mean gray value threshold value\nis supposed to be a number\nProgram will continue without filter");
										cB.getFilterWithMeanGreyValueCkb().setState(false);
									}
									if (mnGryVlThrshld == 0) {
										IJ.error("Wrong parameter", "If you want to filter unusual background,\nplease fill correctly the field\nProgram will continue without filter");
										cB.getFilterWithMeanGreyValueCkb().setState(false);
									}
									else {
										meanGrayValThreshold = mnGryVlThrshld;
									}
									
								}
								else {
									meanGrayValThreshold = 0;
								}
				
								// Then Algorithm can be run
								System.out.println("Aaaaand ACTION!");
								
								cellBoundId = new CellsBoundariesIdentification(cB, sigma, minParticleSize, maxParticleSize, cB.getDirection(), zFocus, solidityThreshold, meanGrayValThreshold, false, true);
								cellBoundId.identifyCellesBoundaries();
							}
						}
					}
				}
			}
		}
	}
}
