import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;

import javax.swing.BoxLayout;

import ij.gui.GenericDialog;

/**
 * Class to create and display a dialog to get parameters of the fluorescent analysis 
 * (detection and measurement of mitotic spindle)
 * @author marie
 *
 */
public class MaarsFluoAnalysisDialog {
	
	private GenericDialog fluoAnalysisDialog;
	private AllMaarsParameters parameters;
	
	/**
	 * Constructor :
	 * @param parameters : parameters displayed in dialog
	 */
	public MaarsFluoAnalysisDialog(AllMaarsParameters parameters) {
		this.parameters = parameters;
		fluoAnalysisDialog = new GenericDialog("MAARS - Fluorescent Analysis parameters");
		
		fluoAnalysisDialog.setBackground(Color.WHITE);
		BoxLayout layout = new BoxLayout(fluoAnalysisDialog, BoxLayout.Y_AXIS);
		fluoAnalysisDialog.setLayout(layout);
		Dimension minimumSize = new Dimension(300, 600);
		fluoAnalysisDialog.setMinimumSize(minimumSize);
		Color labelColor = Color.ORANGE;
		
		Label fluoMovieLabel = new Label("Movie parameters");
		fluoMovieLabel.setBackground(labelColor);
		fluoAnalysisDialog.add(fluoMovieLabel);
		fluoAnalysisDialog.addNumericField("range",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble(), 
				3, 
				5, 
				"micron");
		fluoAnalysisDialog.addNumericField("step",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble(), 
				3, 
				5, 
				"micron");
		fluoAnalysisDialog.addStringField("fluorescence used",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL)
				.getAsString(),
				5);
		fluoAnalysisDialog.addCheckbox("save movies",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SAVE_FLUORESCENT_MOVIES)
				.getAsBoolean());
		
		Label fluoAnaParamLabel = new Label("Spot identification parameter(s)");
		fluoAnaParamLabel.setBackground(labelColor);
		fluoAnalysisDialog.add(fluoAnaParamLabel);
		fluoAnalysisDialog.addNumericField("spot radius",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SPOT_RADIUS)
				.getAsDouble(),
				5);
		fluoAnalysisDialog.addNumericField("Maximum supposed number of spot",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT)
				.getAsInt(), 0);
		
		OKFluoAnalysisParametersAction fluoAction = new OKFluoAnalysisParametersAction(this);
		Button okFluoAnaParamButton = new Button("OK");
		okFluoAnaParamButton.addActionListener(fluoAction);
		
		fluoAnalysisDialog.add(okFluoAnaParamButton);
		
	}
	
	/**
	 * 
	 * @return dialog
	 */
	public GenericDialog getDialog() {
		return fluoAnalysisDialog;
	}

	/**
	 * Display dialog
	 */
	public void show() {
		fluoAnalysisDialog.setVisible(true);
	}
	
	/**
	 * Hide dialog
	 */
	public void hide() {
		fluoAnalysisDialog.setVisible(false);
	}

	/**
	 * 
	 * @return parameters
	 */
	public AllMaarsParameters getParameters() {
		return parameters;
	}
}
