import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;

import com.google.gson.JsonElement;

import ij.gui.GenericDialog;

/**
 * Class to create and display a dialog to get parameters for the mitosis movie
 * @author marie
 *
 */
public class MaarsMitosisMovieDialog {
	
	private GenericDialog mitosisMovieDialog;
	private AllMaarsParameters parameters;
	
	/**
	 * Constructor :
	 * @param parameters : default parameters (which are going to be displayed)
	 */
	public MaarsMitosisMovieDialog(AllMaarsParameters parameters) {
		
		System.out.println("Create MaarsMitosisMovieDialog...");
		
		this.parameters = parameters;
		Color labelColor = Color.ORANGE;
		
		System.out.println("- create generic dialog");
		
		mitosisMovieDialog = new GenericDialog("MAARS - Mitosis movie parameters");
		
		//BoxLayout layout = new BoxLayout(mitosisMovieDialog, BoxLayout.Y_AXIS);
		GridLayout layout = new GridLayout(0, 2, 0, 6);
		mitosisMovieDialog.setLayout(layout);
		
		mitosisMovieDialog.setBackground(Color.WHITE);
		Dimension minimumSize = new Dimension(400, 850);
		mitosisMovieDialog.setMinimumSize(minimumSize);
		mitosisMovieDialog.centerDialog(true);
		
		System.out.println("- add label for start movie conditions and value");
		
		Label startLabel = new Label("Choose conditions to START movie :");
		startLabel.setBackground(labelColor);
		mitosisMovieDialog.add(startLabel);
		mitosisMovieDialog.add(new Label());
		
		System.out.println("- "+AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE);
		System.out.println("-- add checkbox condition");
		
		mitosisMovieDialog.addCheckbox("Minimum absolute spindle size",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());
		
		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
				.getAsDouble(),
				3, 5, "micron");
		
		System.out.println("- "+AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE);
		System.out.println("-- add checkbox condition");
		
		mitosisMovieDialog.addCheckbox("Maximum absolute spindle size",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean());

		mitosisMovieDialog.add(new Label());
		
		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsDouble(),
				3, 5, "micron");

		System.out.println("- "+AllMaarsParameters.RELATIVE_SPINDLE_ANGLE);
		System.out.println("-- add checkbox condition");
		
		mitosisMovieDialog.addCheckbox("Relative spindle angle to major axis",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());

		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsDouble(),
				3, 5, "degree");
		
		System.out.println("- add label for end movie conditions and value");
		
		Label endLabel = new Label("Choose conditions to END movie :");
		endLabel.setBackground(labelColor);
		mitosisMovieDialog.add(endLabel);
		mitosisMovieDialog.add(new Label());
		
		System.out.println("- "+AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE);
		System.out.println("-- add checkbox condition");
		
		mitosisMovieDialog.addCheckbox("Maximum absolute spindle size",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());
		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsDouble(),
				3, 5, "micron");
		
		System.out.println("- "+AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE);
		System.out.println("-- add checkbox condition");
		
		mitosisMovieDialog.addCheckbox("Maximum relative spindle size",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());
		
		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE)
				.getAsDouble(),
				3, 5, "");
		

		System.out.println("- "+AllMaarsParameters.RELATIVE_SPINDLE_ANGLE);
		System.out.println("-- add checkbox condition");
		
		
		mitosisMovieDialog.addCheckbox("Relative spindle maximum angle to major axis",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());
		
		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsDouble(),
				3, 5, "degree");
		


		System.out.println("- "+AllMaarsParameters.TIME_LIMIT);
		System.out.println("-- add checkbox condition");
		
		
		mitosisMovieDialog.addCheckbox("Time limit",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.TIME_LIMIT)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());

		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.TIME_LIMIT)
				.getAsDouble(),
				3, 5, "min");


		System.out.println("- "+AllMaarsParameters.GROWING_SPINDLE);
		System.out.println("-- add checkbox condition");
		
		mitosisMovieDialog.addCheckbox("Minimum growing of spindle required during time interval",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.GROWING_SPINDLE)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());
		

		System.out.println("-- add field value");
		
		mitosisMovieDialog.addNumericField("value",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.GROWING_SPINDLE)
				.getAsDouble(),
				3, 5, "micron/time interval");

		System.out.println("- add checkbox to film only one cell in the field");
		mitosisMovieDialog.addCheckbox("Film only one mitosis per field (with smallest spindle)",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.FIND_BEST_MITOSIS_IN_FIELD)
				.getAsBoolean());
		mitosisMovieDialog.add(new Label());
		
		System.out.println("- add label for acquisition parameters");
		
		Label acquireParamLabel = new Label("Acquisition parameters");
		acquireParamLabel.setBackground(labelColor);
		
		mitosisMovieDialog.add(acquireParamLabel);
		mitosisMovieDialog.add(new Label());
		
		System.out.println("- add field for "+AllMaarsParameters.CHANNEL);
		
		java.util.Iterator<JsonElement> fluoArray = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL)
				.getAsJsonArray()
				.iterator();
		String fluo = fluoArray.next().getAsString();
		while(fluoArray.hasNext()) {
			fluo = fluo+","+fluoArray.next().getAsString();
		}
		
		mitosisMovieDialog.addStringField("Fluorescence",
				fluo,
				5);
		

		System.out.println("- add field for "+AllMaarsParameters.TIME_INTERVAL);
		mitosisMovieDialog.addNumericField("Time interval",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.TIME_INTERVAL)
				.getAsInt(),
				3, 5, "ms");

		System.out.println("- add field for "+AllMaarsParameters.RANGE_SIZE_FOR_MOVIE);
		mitosisMovieDialog.addNumericField("Range",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsInt(),
				3, 5, "micron");

		System.out.println("- add field for "+AllMaarsParameters.STEP);
		mitosisMovieDialog.addNumericField("Step",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble(),
				3, 5, "micron");

		System.out.println("- add field for "+AllMaarsParameters.MARGIN_AROUD_CELL);
		mitosisMovieDialog.addNumericField("Margin around cell",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MARGIN_AROUD_CELL)
				.getAsDouble(),
				3, 5, "pixel");
		

		System.out.println("- add field for "+AllMaarsParameters.SAVING_PATH);
		mitosisMovieDialog.addStringField("Saving path for all movies \n(linux style path)",
				parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SAVING_PATH)
				.getAsString(),
				5);
		
		OKMovieMitosisAction okAction = new OKMovieMitosisAction(this);
		Button okButton = new Button("OK");
		okButton.addActionListener(okAction);
		
		mitosisMovieDialog.add(okButton);
		System.out.println("Done");
	}
	
	/**
	 * 
	 * @return dialog
	 */
	public GenericDialog getDialog() {
		return mitosisMovieDialog;
	}
	
	/**
	 * 
	 * @return parameters
	 */
	public AllMaarsParameters getParameters() {
		return parameters;
	}
	
	/**
	 * Show dialog
	 */
	public void show() {
		mitosisMovieDialog.setVisible(true);
	}
	
	/**
	 * Hide dialog
	 */
	public void hide() {
		mitosisMovieDialog.setVisible(false);
	}
}
