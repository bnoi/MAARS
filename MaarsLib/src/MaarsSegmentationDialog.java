import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;

import javax.swing.BoxLayout;

import ij.gui.GenericDialog;

/**
 * Class to create and display a dialog to get parameters for the image segmentation process
 * @author marie
 *
 */
public class MaarsSegmentationDialog {
	
	private GenericDialog segmentationDialog;
	private AllMaarsParameters parameters;
	
	/**
	 * Constructor :
	 * @param parameters : default parameters (which are going to be displayed)
	 */
	public MaarsSegmentationDialog(AllMaarsParameters parameters) {
		
		segmentationDialog = new GenericDialog("MAARS - Segmentation parameters");
		BoxLayout layout = new BoxLayout(segmentationDialog, BoxLayout.Y_AXIS);
		segmentationDialog.setLayout(layout);
		segmentationDialog.setBackground(Color.WHITE);
		Dimension minimumSize = new Dimension(300, 600);
		segmentationDialog.setMinimumSize(minimumSize);
		segmentationDialog.centerDialog(true);
		Color labelColor = Color.ORANGE;
		
		this.parameters = parameters;
		Label segmMovieLabel = new Label("Movie parameters");
		segmMovieLabel.setBackground(labelColor);
		segmentationDialog.add(segmMovieLabel);
		
		segmentationDialog.addNumericField("range",
				parameters.getParametersAsJsonObject()
					.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
					.getAsJsonObject()
					.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
					.getAsDouble(), 
				3, 
				5, 
				"micron");
		segmentationDialog.addNumericField("step",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble(), 
				3, 
				5, 
				"micron");
		
		Label segmParemLabel = new Label("Segementation parameters");
		segmParemLabel.setBackground(labelColor);
		segmentationDialog.add(segmParemLabel);
		segmentationDialog.addNumericField("typical cell z size",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CELL_SIZE)
				.getAsDouble(),
				3,
				5,
				"micron");
		segmentationDialog.addNumericField("minimum cell area",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MINIMUM_CELL_AREA)
				.getAsDouble(),
				3,
				5,
				"micron");
		segmentationDialog.addNumericField("maximum cell area",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_CELL_AREA)
				.getAsDouble(),
				3,
				5,
				"micron");
		segmentationDialog.addCheckbox("Filter background using mean grey value on correlation image",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.FILTER_MEAN_GREY_VALUE)
				.getAsBoolean());
		segmentationDialog.addNumericField("mean grey value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MEAN_GREY_VALUE)
				.getAsDouble(),
				3,
				5,
				"intensity");
		segmentationDialog.addCheckbox("Filter unusual shape using solidity",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.FILTER_SOLIDITY)
				.getAsBoolean());
		segmentationDialog.addNumericField("solidity",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SOLIDITY)
				.getAsDouble(), 
				3, 
				5, 
				"");
		OKSegmentationParamAction okSeg = new OKSegmentationParamAction(this);
		Button okSegParam = new Button("OK");
		okSegParam.addActionListener(okSeg);
		
		segmentationDialog.add(okSegParam);
		
	}
	
	/**
	 * 
	 * @return dialog
	 */
	public GenericDialog getDialog() {
		return segmentationDialog;
	}
	
	/**
	 * Show dialog
	 */
	public void show() {
		segmentationDialog.setVisible(true);
	}
	
	/**
	 * Hide dialog
	 */
	public void hide() {
		segmentationDialog.setVisible(false);
	}
	
	/**
	 * 
	 * @return parameters
	 */
	public AllMaarsParameters getParameters() {
		return parameters;
	}
}
