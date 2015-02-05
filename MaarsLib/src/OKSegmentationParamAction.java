import ij.gui.GenericDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action performed when user clicks on "ok" button of MaarsSegmentationDialog
 * @author marie
 *
 */
public class OKSegmentationParamAction  implements ActionListener  {
	
	private MaarsSegmentationDialog maarsSD;
	
	/**
	 * 
	 * @param maarsSD : dialog containing button clicked
	 */
	public OKSegmentationParamAction(MaarsSegmentationDialog maarsSD) {
		this.maarsSD = maarsSD;
	}
	
	/**
	 * update parameters according to what the user has changed and close dialog
	 */
	public void actionPerformed(ActionEvent e) {
		
		GenericDialog dialog = maarsSD.getDialog();
		
		double range = dialog.getNextNumber();
		
		if(range != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE, Double.valueOf(range));
		}
		
		double step = dialog.getNextNumber();
		if(step != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.STEP);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.STEP, Double.valueOf(step));
		}
		
		double cellSize = dialog.getNextNumber();
		if(cellSize != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CELL_SIZE)
				.getAsDouble()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.CELL_SIZE);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.CELL_SIZE, Double.valueOf(cellSize));
		}
		
		double minCellArea = dialog.getNextNumber();
		if(minCellArea != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MINIMUM_CELL_AREA)
				.getAsDouble()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.MINIMUM_CELL_AREA);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.MINIMUM_CELL_AREA, Double.valueOf(minCellArea));
		}
		
		double maxCellArea = dialog.getNextNumber();
		if(minCellArea != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_CELL_AREA)
				.getAsDouble()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.MAXIMUM_CELL_AREA);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.MAXIMUM_CELL_AREA, Double.valueOf(maxCellArea));
		}
		
		boolean filterMeanGrey = dialog.getNextBoolean();
		if(filterMeanGrey != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.FILTER_MEAN_GREY_VALUE)
				.getAsBoolean()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.FILTER_MEAN_GREY_VALUE);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.FILTER_MEAN_GREY_VALUE, Boolean.valueOf(filterMeanGrey));
		}
		
		double meanGreyVal = dialog.getNextNumber();
		if(meanGreyVal != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MEAN_GREY_VALUE)
				.getAsDouble()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.MEAN_GREY_VALUE);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.MEAN_GREY_VALUE, Double.valueOf(meanGreyVal));
		}
		
		boolean filterSolidity = dialog.getNextBoolean();
		if(filterSolidity != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.FILTER_SOLIDITY)
				.getAsBoolean()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.FILTER_SOLIDITY);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.FILTER_SOLIDITY, Boolean.valueOf(filterSolidity));
		}
		
		double solidity = dialog.getNextNumber();
		if(solidity != maarsSD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SOLIDITY)
				.getAsDouble()) {
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.SOLIDITY);
			
			maarsSD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.SOLIDITY, Double.valueOf(solidity));
		}
		
		maarsSD.hide();
	}
}
