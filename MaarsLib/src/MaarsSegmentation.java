import java.io.File;

import ij.IJ;

/**
 * Class to segment an specific type of image and find and record cell shape and location
 * @author marie
 *
 */
public class MaarsSegmentation {
	private AllMaarsParameters parameters;
	private String moviePath;
	private CellsBoundaries_ cB;
	
	/**
	 * Constructor :
	 * @param parameters : parameters of segmentation
	 * @param moviePath : where is stored image to segment
	 */
	public MaarsSegmentation(AllMaarsParameters parameters,
			String moviePath) {
		
		this.parameters = parameters;
		this.moviePath = AllMaarsParameters.convertPath(moviePath);
		System.out.println("Movie path for segmentation : "+moviePath);
		
	}
	
	/**
	 * Get the parameters and use them to segment image
	 */
	public void segmentation() {
		
		IJ.open(moviePath);
		cB =  new CellsBoundaries_();
		cB.setMainWindow();
		
		cB.getDisplayFocusImage().setState(false);
		cB.getSaveBinaryImg().setState(true);
		cB.getSaveCorrelationImg().setState(true);
		cB.getSaveDataFrame().setState(true);
		cB.getSaveFocusImage().setState(true);
		cB.getSaveRoi().setState(true);
		cB.getFilterUnususalCkb().setState(true);
		cB.getFilterWithMeanGreyValueCkb().setState(true);
		cB.getAlreadryOpenedImage();
		cB.getImageToAnalyze().getCalibration().pixelDepth = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble();
		
		cB.getRunAction().checkUnitsAndScale();
		cB.getRunAction().changeScale(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.NEW_MAX_WIDTH_FOR_CHANGE_SCALE)
				.getAsInt()
				, parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.NEW_MAX_HEIGTH_FOR_CHANGE_SCALE)
				.getAsInt());
		
		int cellSizePixel = (int) Math.round(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CELL_SIZE)
				.getAsDouble() / parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble());
		
		int minSize = (int)  Math.round(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MINIMUM_CELL_AREA)
				.getAsDouble() / cB
				.getImageToAnalyze()
				.getCalibration()
				.pixelWidth);
				
		int maxSize = (int)  Math.round(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_CELL_AREA)
				.getAsDouble() / cB
				.getImageToAnalyze()
				.getCalibration()
				.pixelWidth);
		
		double solidity = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SOLIDITY)
				.getAsDouble();
		
		double meanGrey = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MEAN_GREY_VALUE)
				.getAsDouble();
		
		CellsBoundariesIdentification cBI = new CellsBoundariesIdentification(cB,
			cellSizePixel,
			minSize,
			maxSize,
			-1,
			(int) Math.round(cB.getImageToAnalyze().getNSlices()/2),
			solidity ,
			meanGrey,
			true,
			false);
		
		cBI.identifyCellesBoundaries();
		
		IJ.getImage().close();
	}
	
	/**
	 * 
	 * @return true if program is still working on segmentation
	 */
	public boolean isAnalysing() {
		
		File file = new File(cB.getPathDirField().getText()+cB.getImageToAnalyze().getShortTitle()+"CorrelationImage.tif");
		if(file.exists()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * 
	 * @return CellsBoundaries_ object
	 */
	public CellsBoundaries_ getSegmentationObject() {
		return cB;
	}
}
