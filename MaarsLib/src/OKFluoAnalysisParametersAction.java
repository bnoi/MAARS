import ij.gui.GenericDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action performed when user clicks on "ok" button of MaarsFluoAnalysisDialog
 * @author marie
 *
 */
public class OKFluoAnalysisParametersAction implements ActionListener {

	private MaarsFluoAnalysisDialog maarsFAD;
	
	/**
	 * Constructor :
	 * @param maarsFAD : dialog containing button clicked
	 */
	public OKFluoAnalysisParametersAction(MaarsFluoAnalysisDialog maarsFAD) {
		this.maarsFAD = maarsFAD;
	}
	
	/**
	 * update parameters according to what the user has changed and close dialog
	 */
	public void actionPerformed(ActionEvent e) {
		
		GenericDialog dialog = maarsFAD.getDialog();
		
		double range = dialog.getNextNumber();
		if(range != maarsFAD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsDouble()) {
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE);
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE, Double.valueOf(range));
		}
		
		double step = dialog.getNextNumber();
		if(step != maarsFAD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.STEP)
				.getAsDouble()) {
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.STEP);
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.STEP, Double.valueOf(step));
		}
		
		String fluoUsed = dialog.getNextString();
		if(fluoUsed != maarsFAD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.CHANNEL)
				.getAsString()) {
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.CHANNEL);
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.CHANNEL, fluoUsed);
		}
		
		boolean saveMovies = dialog.getNextBoolean();
		if(saveMovies != maarsFAD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SAVE_FLUORESCENT_MOVIES)
				.getAsBoolean()) {
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.SAVE_FLUORESCENT_MOVIES);
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.SAVE_FLUORESCENT_MOVIES, Boolean.valueOf(saveMovies));
		}
		
		double spotRadius = dialog.getNextNumber();
		if(spotRadius != maarsFAD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SPOT_RADIUS)
				.getAsDouble()) {
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.SPOT_RADIUS);
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.SPOT_RADIUS, Double.valueOf(spotRadius));
		}
		int spotNumber = (int) dialog.getNextNumber();
		if (spotNumber != maarsFAD.getParameters()
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT)
				.getAsInt()) {
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.remove(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT);
			
			maarsFAD.getParameters()
			.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT, Integer.valueOf(spotNumber));
		}
			
		
		maarsFAD.hide();
	}
}
