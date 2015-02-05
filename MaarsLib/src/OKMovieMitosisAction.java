import ij.gui.GenericDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * Action performed when user clicks on "ok" button of MaarsMitosisMovieDialog
 * @author marie
 *
 */
public class OKMovieMitosisAction implements ActionListener {
	
	MaarsMitosisMovieDialog maarsMMD;
	
	/**
	 * 
	 * @param maarsMMD : dialog containing button clicked
	 */
	public OKMovieMitosisAction(MaarsMitosisMovieDialog maarsMMD) {
		this.maarsMMD = maarsMMD;
	}
	
	/**
	 * update parameters according to what the user has changed and close dialog
	 */
	public void actionPerformed(ActionEvent e) {
		
		GenericDialog dialog = maarsMMD.getDialog();
		
		boolean  absMinSpindleSizeF = dialog.getNextBoolean();
		
		if(absMinSpindleSizeF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE, Boolean.valueOf(absMinSpindleSizeF));
		}
		
		double  absMinSpindleSize = dialog.getNextNumber();
		
		if(absMinSpindleSize != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE, Double.valueOf(absMinSpindleSize));
		}
		

		boolean  absMaxSpindleSizeF = dialog.getNextBoolean();
		
		if(absMaxSpindleSizeF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE, Boolean.valueOf(absMaxSpindleSizeF));
		}
		
		double  absMaxSpindleSize = dialog.getNextNumber();
		
		if(absMaxSpindleSize != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE, Double.valueOf(absMaxSpindleSize));
		}
		
		boolean  relativeAngleSpindleF = dialog.getNextBoolean();
		
		if(relativeAngleSpindleF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE, Boolean.valueOf(relativeAngleSpindleF));
		}
		
		double  relativeAngleSpindle = dialog.getNextNumber();
		
		if(relativeAngleSpindle != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE, Double.valueOf(relativeAngleSpindle));
		}
		
		boolean  maxAbsSpindleSizeF = dialog.getNextBoolean();
		
		if(maxAbsSpindleSizeF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE, Boolean.valueOf(maxAbsSpindleSizeF));
		}
		
		double  maxAbsSpindleSize = dialog.getNextNumber();
		
		if(maxAbsSpindleSize != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE, Double.valueOf(maxAbsSpindleSize));
		}
		
		boolean  maxRelSpindleSizeF = dialog.getNextBoolean();
		
		if(maxRelSpindleSizeF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE, Boolean.valueOf(maxRelSpindleSizeF));
		}
		
		double  maxRelSpindleSize = dialog.getNextNumber();
		
		if(maxRelSpindleSize != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RELATIVE_MAXIMUM_SPINDLE_SIZE, Double.valueOf(maxRelSpindleSize));
		}

		boolean  relSpindleAngleF = dialog.getNextBoolean();
		
		if(relSpindleAngleF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE, Boolean.valueOf(relSpindleAngleF));
		}
		
		double  relSpindleAngle = dialog.getNextNumber();
		
		if(relSpindleAngle != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE, Double.valueOf(relSpindleAngle));
		}
		

		boolean  timeLimitF = dialog.getNextBoolean();
		
		if(timeLimitF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.TIME_LIMIT)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.TIME_LIMIT);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.TIME_LIMIT, Boolean.valueOf(timeLimitF));
		}
		
		double  timeLimit = dialog.getNextNumber();
		
		if(timeLimit != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.TIME_LIMIT)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.TIME_LIMIT);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.TIME_LIMIT, Double.valueOf(timeLimit));
		}

		boolean  minGrowingF = dialog.getNextBoolean();
		
		if(minGrowingF != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.GROWING_SPINDLE)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.GROWING_SPINDLE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.CONDITIONS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.GROWING_SPINDLE, Boolean.valueOf(minGrowingF));
		}
		
		double  minGrowing = dialog.getNextNumber();
		
		if(minGrowing != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.VALUES)
				.getAsJsonObject()
				.get(AllMaarsParameters.GROWING_SPINDLE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.remove(AllMaarsParameters.GROWING_SPINDLE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.get(AllMaarsParameters.END_MOVIE_CONDITIONS)
			.getAsJsonObject()
			.get(AllMaarsParameters.VALUES)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.GROWING_SPINDLE, Double.valueOf(minGrowing));
		}
		
		boolean oneMitosis = dialog.getNextBoolean();
		if (oneMitosis != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.FIND_BEST_MITOSIS_IN_FIELD)
				.getAsBoolean()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.FIND_BEST_MITOSIS_IN_FIELD);
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.FIND_BEST_MITOSIS_IN_FIELD, Boolean.valueOf(oneMitosis));
		}
		
		String[] fluoUsedArray = dialog.getNextString().split(",");
		JsonArray fluoUsed = new JsonArray();
		for (int i = 0; i < fluoUsedArray.length; i++) {
			JsonPrimitive str = new JsonPrimitive(fluoUsedArray[i]);
			fluoUsed.add(str);
		}
		
		if(!fluoUsed.equals(maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL)
				.getAsJsonArray())) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.CHANNEL);
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.add(AllMaarsParameters.CHANNEL, fluoUsed);
		}
		
		double  timeInterval = dialog.getNextNumber();
		
		if(timeInterval != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.TIME_INTERVAL)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.TIME_INTERVAL);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.TIME_INTERVAL, Double.valueOf(timeInterval));
		}

		double  range = dialog.getNextNumber();
		
		if(range != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE, Double.valueOf(range));
		}

		double  step = dialog.getNextNumber();
		
		if(range != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.STEP);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.STEP, Double.valueOf(step));
		}

		double  marginAroundCell = dialog.getNextNumber();
		
		if(marginAroundCell != maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MARGIN_AROUD_CELL)
				.getAsDouble()) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.MARGIN_AROUD_CELL);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.MARGIN_AROUD_CELL, Double.valueOf(marginAroundCell));
		}

		String  savingPath = dialog.getNextString();
		
		if(!savingPath.equals(maarsMMD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SAVING_PATH)
				.getAsString())) {
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.SAVING_PATH);
			
			
			maarsMMD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.SAVING_PATH, savingPath);
		}
		
		
		maarsMMD.hide();
	}
}
