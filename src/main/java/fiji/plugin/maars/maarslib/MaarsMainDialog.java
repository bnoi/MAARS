package fiji.plugin.maars.maarslib;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.BoxLayout;

import mmcorej.CMMCore;

import org.micromanager.MMStudio;

import ij.IJ;
import ij.gui.NonBlockingGenericDialog;

import org.micromanager.utils.ReportingUtils;

/**
 * Class to create and display a dialog to get parameters of the whole analysis
 * 
 * @author marie
 *
 */
public class MaarsMainDialog {

	private final NonBlockingGenericDialog mainDialog;
	private final Label numFieldLabel;
	private final MMStudio gui;
	private final CMMCore mmc;
	private final AllMaarsParameters parameters;
	private final double calibration;
	private boolean okClicked;

	/**
	 * Constructor :
	 * 
	 * @param gui
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param pathConfigFile
	 *            : path to maars_config.txt file containing all parameters of
	 *            the system (in JSON format)
	 * @throws IOException
	 */
	public MaarsMainDialog(MMStudio gui, CMMCore mmc, String pathConfigFile)
			throws IOException {
		//------------initialization of parameters---------------//
		try {
			PrintStream ps = new PrintStream(System.getProperty("user.dir") + "/MAARS.LOG");
			System.setOut(ps);
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			ReportingUtils.logError(e);
		}
		Color labelColor = Color.ORANGE;
		okClicked = false;

		this.gui = gui;
		this.mmc = mmc;

		ReportingUtils.logMessage("create parameter object ...");

		parameters = new AllMaarsParameters(pathConfigFile);

		ReportingUtils.logMessage("Done.");

		int defaultXFieldNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.X_FIELD_NUMBER)
				.getAsInt();
		int defaultYFieldNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.Y_FIELD_NUMBER)
				.getAsInt();

		ReportingUtils.logMessage("create main dialog ...");

		calibration = gui.getMMCore().getPixelSizeUm();
		double fieldWidth = mmc.getImageWidth() * calibration;
		double fieldHeight = mmc.getImageHeight() * calibration;

		//------------set up the dialog---------------//
		mainDialog = new NonBlockingGenericDialog(
				"Mitosis Analysing And Recording System - MAARS");
		mainDialog.setBackground(Color.WHITE);
		ReportingUtils.logMessage("- set Layout");
		BoxLayout layout = new BoxLayout(mainDialog, BoxLayout.Y_AXIS);
		mainDialog.setLayout(layout);

		ReportingUtils.logMessage("- set minimal dimension");
		Dimension minimumSize = new Dimension(300, 600);
		mainDialog.setMinimumSize(minimumSize);
		mainDialog.centerDialog(true);

		ReportingUtils.logMessage("- create panel");
		Panel analysisParamPanel = new Panel();

		analysisParamPanel.setBackground(Color.WHITE);
		BoxLayout analysisParamLayout = new BoxLayout(analysisParamPanel,
				BoxLayout.Y_AXIS);
		analysisParamPanel.setLayout(analysisParamLayout);
		//------------analysis parameters---------------//
		Label analysisParamLabel = new Label("Analysis parameters");
		analysisParamLabel.setBackground(labelColor);

		ReportingUtils
				.logMessage("- create OpenSegmentationDialogButtonAction");
		OpenSegmentationDialogButtonAction openSegAction = new OpenSegmentationDialogButtonAction(
				parameters);
		Button segmButton = new Button("Segmentation");
		segmButton.addActionListener(openSegAction);

		ReportingUtils.logMessage("- create OpenFluoAnalysisDialogAction");
		OpenFluoAnalysisDialogAction openFluoAnaAction = new OpenFluoAnalysisDialogAction(
				parameters);
		Button fluoAnalysisButton = new Button("Fluorescent analysis");
		fluoAnalysisButton.addActionListener(openFluoAnaAction);

		ReportingUtils.logMessage("- create AutofocusButtonAction");
		AutofocusButtonAction autofocusButtonAction = new AutofocusButtonAction(
				this);
		Button autofocusButton = new Button("Autofocus");
		autofocusButton.addActionListener(autofocusButtonAction);

		ReportingUtils.logMessage("- add buttons to panel");
		analysisParamPanel.add(analysisParamLabel);
		analysisParamPanel.add(autofocusButton);
		analysisParamPanel.add(segmButton);
		analysisParamPanel.add(fluoAnalysisButton);

		ReportingUtils.logMessage("- add label for text field");
		Label explorationLabel = new Label("Area to explore");
		explorationLabel.setBackground(labelColor);
		numFieldLabel = new Label("Number of field : " + defaultXFieldNumber
				* defaultYFieldNumber);
		//------------area to explore---------------//
		ReportingUtils
				.logMessage("- add button, textfield and label to mainDialog");
		mainDialog.add(explorationLabel);
		mainDialog.addNumericField("Width", fieldWidth * defaultXFieldNumber,
				0, 6, "micron");
		mainDialog.addNumericField("Height", fieldHeight * defaultYFieldNumber,
				0, 6, "micron");
		Vector<TextField> numFields = mainDialog.getNumericFields();
		numFields.get(0).addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				refreshNumField();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		numFields.get(1).addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				refreshNumField();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		mainDialog.add(numFieldLabel);
		//------------mitosis movies parameters---------------//
		mainDialog.addPanel(analysisParamPanel);

		ReportingUtils.logMessage("- create OpenMitosisMovieParamDialog");
		Label mitosisMovieLabel = new Label("Mitosis movie parameters");
		mitosisMovieLabel.setBackground(labelColor);
		OpenMitosisMovieParamDialog mitosisMovieAction = new OpenMitosisMovieParamDialog(
				parameters);
		Button mitosisMovieButton = new Button("Parameters");
		mitosisMovieButton.addActionListener(mitosisMovieAction);

		ReportingUtils.logMessage("- add button and label to panel");
		mainDialog.add(mitosisMovieLabel);
		mainDialog.add(mitosisMovieButton);

		mainDialog.addCheckbox("Save parameters", true);
		ReportingUtils.logMessage("- create OKMaarsMainDialog");
		OKMaarsMainDialog maarsOkAction = new OKMaarsMainDialog(this);
		Button okMainDialogButton = new Button("OK");
		okMainDialogButton.addActionListener(maarsOkAction);

		ReportingUtils.logMessage("- add button");
		mainDialog.add(okMainDialogButton);

		ReportingUtils.logMessage("Done.");

	}

	/**
	 * 
	 * @return graphical user interface of Micro-Manager
	 */
	public MMStudio getGui() {
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
	 * 
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

		int newXFieldNumber = (int) Math.round(newWidth
				/ (calibration * mmc.getImageWidth()));
		int newYFieldNumber = (int) Math.round(newHeigth
				/ (calibration * mmc.getImageHeight()));

		numFieldLabel.setText("Number of field : " + newXFieldNumber
				* newYFieldNumber);

		parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().remove(AllMaarsParameters.X_FIELD_NUMBER);
		parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject()
				.addProperty(AllMaarsParameters.X_FIELD_NUMBER,
						Integer.valueOf(newXFieldNumber));

		parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().remove(AllMaarsParameters.Y_FIELD_NUMBER);
		parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject()
				.addProperty(AllMaarsParameters.Y_FIELD_NUMBER,
						Integer.valueOf(newYFieldNumber));
	}

	/**
	 * method to save the parameters entered
	 */
	public void saveParameters() {

		if (mainDialog.getNextBoolean()) {
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
