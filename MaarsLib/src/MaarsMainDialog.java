import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.BoxLayout;

import mmcorej.CMMCore;

import org.micromanager.MMStudioMainFrame;

import ij.IJ;
import ij.gui.NonBlockingGenericDialog;

/**
 * Class to create and display a dialog to get parameters of the whole analysis
 * @author marie
 *
 */
public class MaarsMainDialog {
	
	private NonBlockingGenericDialog mainDialog;
	private Label numFieldLabel;
	private MMStudioMainFrame gui;
	private CMMCore mmc;
	private AllMaarsParameters parameters;
	private double calibration;
	private boolean okClicked;
	
	/**
	 * Constructor :
	 * @param gui : graphical user interface of Micro-Manager
	 * @param mmc : Core object of Micro-Manager
	 * @param pathConfigFile : path to maars_config.txt file containing all parameters of the system (in JSON format)
	 * @throws IOException
	 */
	public MaarsMainDialog(MMStudioMainFrame gui,
			CMMCore mmc,
			String pathConfigFile) throws IOException {
		
		try {
			PrintStream ps = new PrintStream(pathConfigFile+"Maars.LOG");
			System.setOut(ps);
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			IJ.error("CAN NOT write LOG file");
		}
		Color labelColor = Color.ORANGE;
		okClicked = false;
		
		this.gui = gui;
		this.mmc = mmc;
		
		System.out.println("create parameter object ...");
		
		parameters = new AllMaarsParameters(pathConfigFile+"maars_config.txt");
		
		System.out.println("Done.");
		
		int defaultXFieldNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.X_FIELD_NUMBER).getAsInt();
		int defaultYFieldNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.Y_FIELD_NUMBER).getAsInt();
		
		System.out.println("create main dialog ...");
		
		calibration = gui.getMMCore().getPixelSizeUm();
		double fieldWidth = mmc.getImageWidth()* calibration;
		double fieldHeight = mmc.getImageHeight()* calibration;
		
		mainDialog = new NonBlockingGenericDialog("Mitosis Analysing And Recording System - MAARS");
		mainDialog.setBackground(Color.WHITE);
		System.out.println("- set Layout");
		BoxLayout layout = new BoxLayout(mainDialog, BoxLayout.Y_AXIS);
		mainDialog.setLayout(layout);
		
		System.out.println("- set minimal dimension");
		Dimension minimumSize = new Dimension(300, 600);
		mainDialog.setMinimumSize(minimumSize);
		mainDialog.centerDialog(true);
		
		System.out.println("- create panel");
		Panel analysisParamPanel = new Panel();
		
			analysisParamPanel.setBackground(Color.WHITE);
			BoxLayout analysisParamLayout = new BoxLayout(analysisParamPanel, BoxLayout.Y_AXIS);
			analysisParamPanel.setLayout(analysisParamLayout);
		
		Label analysisParamLabel = new Label("Analysis parameters");
		analysisParamLabel.setBackground(labelColor);
		
		System.out.println("- create OpenSegmentationDialogButtonAction");
		OpenSegmentationDialogButtonAction openSegAction = new OpenSegmentationDialogButtonAction(parameters);
		Button segmButton = new Button("Segmentation");
		segmButton.addActionListener(openSegAction);
		
		System.out.println("- create OpenFluoAnalysisDialogAction");
		OpenFluoAnalysisDialogAction openFluoAnaAction = new OpenFluoAnalysisDialogAction(parameters);
		Button fluoAnalysisButton = new Button("Fluorescent analysis");
		fluoAnalysisButton.addActionListener(openFluoAnaAction);
		
		System.out.println("- create AutofocusButtonAction");
		AutofocusButtonAction autofocusButtonAction = new AutofocusButtonAction(this);
		Button autofocusButton = new Button("Autofocus");
		autofocusButton.addActionListener(autofocusButtonAction);
		
		System.out.println("- add buttons to panel");
		analysisParamPanel.add(analysisParamLabel);
		analysisParamPanel.add(autofocusButton);
		analysisParamPanel.add(segmButton);
		analysisParamPanel.add(fluoAnalysisButton);
		
		System.out.println("- add label for text field");
		Label explorationLabel = new Label("Area to explore");
		explorationLabel.setBackground(labelColor);
		numFieldLabel = new Label("Number of field : "+defaultXFieldNumber*defaultYFieldNumber);
		
		System.out.println("- create RefreshAreaToExploreAction");
		RefreshAreaToExploreAction refreshAction = new RefreshAreaToExploreAction(this);
		Button numfieldRefreshButton = new Button("Refresh");
		numfieldRefreshButton.addActionListener(refreshAction);
		
		System.out.println("- add button, textfield and label to mainDialog");
		mainDialog.add(explorationLabel);
		mainDialog.addNumericField("Width", fieldWidth*defaultXFieldNumber, 0, 6, "micron");
		mainDialog.addNumericField("Height", fieldHeight*defaultYFieldNumber, 0, 6, "micron");
		mainDialog.add(numfieldRefreshButton);
		mainDialog.add(numFieldLabel);
		
		mainDialog.addPanel(analysisParamPanel);
		
		System.out.println("- create OpenMitosisMovieParamDialog");
		Label mitosisMovieLabel = new Label("Mitosis movie parameters");
		mitosisMovieLabel.setBackground(labelColor);
		OpenMitosisMovieParamDialog mitosisMovieAction = new OpenMitosisMovieParamDialog(parameters);
		Button mitosisMovieButton = new Button("Parameters");
		mitosisMovieButton.addActionListener(mitosisMovieAction);
		
		System.out.println("- add button and label to panel");
		mainDialog.add(mitosisMovieLabel);
		mainDialog.add(mitosisMovieButton);
		
		mainDialog.addCheckbox("Save parameters", true);
		
		System.out.println("- create OKMaarsMainDialog");
		OKMaarsMainDialog maarsOkAction = new OKMaarsMainDialog(this);
		Button okMainDialogButton = new Button("OK");
		okMainDialogButton.addActionListener(maarsOkAction);
		
		System.out.println("- add button");
		mainDialog.add(okMainDialogButton);
		
		System.out.println("Done.");
		
	}
	
	/**
	 * 
	 * @return graphical user interface of Micro-Manager
	 */
	public MMStudioMainFrame getGui() {
		return gui;
	}
	
	/**
	 * 
	 * @return Core object of Micro-Manager
	 */
	public CMMCore getMMC() {
		return mmc;
	}
	
	/**
	 * Show dialog
	 */
	public void show() {
		mainDialog.setVisible(true);
	}
	
	/**
	 * Hide dialog
	 */
	public void hide() {
		mainDialog.setVisible(false);
	}
	
	/**
	 * 
	 * @return true if dialog is visible
	 */
	public boolean isVisible() {
		return mainDialog.isVisible();
	}
	
	/**
	 * Method to record if the user has clicked on "ok" button
	 * @param ok
	 */
	public void setOkClicked(boolean ok) {
		okClicked = ok;
	}
	
	/**
	 * 
	 * @return true if "ok" button is clicked
	 */
	public boolean isOkClicked() {
		return okClicked;
	}
	
	/**
	 * Method to display number of field the program has to scan
	 */
	public void refreshNumField() {
		
		Vector<TextField> numFields = mainDialog.getNumericFields();
		double newWidth = Double.parseDouble(numFields.get(0).getText());
		double newHeigth = Double.parseDouble(numFields.get(1).getText());
		
		int newXFieldNumber = (int) Math.round(newWidth/(calibration*mmc.getImageWidth()));
		int newYFieldNumber = (int) Math.round(newHeigth/(calibration*mmc.getImageHeight()));
		
		numFieldLabel.setText("Number of field : "+newXFieldNumber*newYFieldNumber);
		
		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS)
			.getAsJsonObject().remove(AllMaarsParameters.X_FIELD_NUMBER);
		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.X_FIELD_NUMBER, Integer.valueOf(newXFieldNumber));
	
		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS)
			.getAsJsonObject().remove(AllMaarsParameters.Y_FIELD_NUMBER);
		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS)
			.getAsJsonObject()
			.addProperty(AllMaarsParameters.Y_FIELD_NUMBER, Integer.valueOf(newYFieldNumber));
	}
	
	/**
	 * method to save the parameters entered
	 */
	public void saveParameters() {
		
		if(mainDialog.getNextBoolean()) {
			try {
				parameters.save();
			} catch (IOException e) {
				e.printStackTrace();
				IJ.error("Could not save parameters");
			}
		}
	}
	
	/**
	 * 
	 * @return parameters
	 */
	public AllMaarsParameters getParameters() {
		return parameters;
	}
}
