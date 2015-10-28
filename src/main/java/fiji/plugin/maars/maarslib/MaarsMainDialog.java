package fiji.plugin.maars.maarslib;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

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
public class MaarsMainDialog implements ActionListener {

	private final NonBlockingGenericDialog mainDialog;
	private final Label numFieldLabel;
	private final MMStudio gui;
	private final CMMCore mmc;
	private final AllMaarsParameters parameters;
	private final double calibration;
	private boolean okClicked;
	private Button autofocusButton;
	private Button okMainDialogButton;
	private Button segmButton;
	private Button fluoAnalysisButton;
	private JTextField savePath;
	private JTextField fluoAcqDuration;
	private JRadioButton dynamicOpt;
	private JRadioButton staticOpt;
	// private Button mitosisMovieButton;

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
	public MaarsMainDialog(MMStudio gui, CMMCore mmc, String pathConfigFile) throws IOException {
		// ------------initialization of parameters---------------//
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

		int defaultXFieldNumber = parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.X_FIELD_NUMBER).getAsInt();
		int defaultYFieldNumber = parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.Y_FIELD_NUMBER).getAsInt();

		ReportingUtils.logMessage("create main dialog ...");

		calibration = gui.getMMCore().getPixelSizeUm();
		double fieldWidth = mmc.getImageWidth() * calibration;
		double fieldHeight = mmc.getImageHeight() * calibration;

		// ------------set up the dialog---------------//
		mainDialog = new NonBlockingGenericDialog("Mitosis Analysing And Recording System - MAARS");
		mainDialog.setBackground(Color.WHITE);
		ReportingUtils.logMessage("- set Layout");
		BoxLayout layout = new BoxLayout(mainDialog, BoxLayout.Y_AXIS);
		mainDialog.setLayout(layout);

		ReportingUtils.logMessage("- set minimal dimension");
		Dimension minimumSize = new Dimension(350, 600);
		mainDialog.setMinimumSize(minimumSize);
		mainDialog.centerDialog(true);

		ReportingUtils.logMessage("- create panel");
		Panel analysisParamPanel = new Panel();

		analysisParamPanel.setBackground(Color.WHITE);
		BoxLayout analysisParamLayout = new BoxLayout(analysisParamPanel, BoxLayout.Y_AXIS);
		analysisParamPanel.setLayout(analysisParamLayout);
		// ------------analysis parameters---------------//
		Label analysisParamLabel = new Label("Analysis parameters");
		analysisParamLabel.setBackground(labelColor);

		ReportingUtils.logMessage("- create AutofocusButtonAction");
		autofocusButton = new Button("Autofocus");
		autofocusButton.addActionListener(this);

		ReportingUtils.logMessage("- create OpenSegmentationDialogButtonAction");
		segmButton = new Button("Segmentation");
		segmButton.addActionListener(this);

		ReportingUtils.logMessage("- create OpenFluoAnalysisDialogAction");

		fluoAnalysisButton = new Button("Fluorescent analysis");
		fluoAnalysisButton.addActionListener(this);

		ReportingUtils.logMessage("- add buttons to panel");
		analysisParamPanel.add(analysisParamLabel);
		analysisParamPanel.add(autofocusButton);
		analysisParamPanel.add(segmButton);
		analysisParamPanel.add(fluoAnalysisButton);

		ReportingUtils.logMessage("- add label for text field");
		Label explorationLabel = new Label("Area to explore");
		explorationLabel.setBackground(labelColor);
		numFieldLabel = new Label("Number of field : " + defaultXFieldNumber * defaultYFieldNumber);
		// ------------area to explore---------------//
		ReportingUtils.logMessage("- add button, textfield and label to mainDialog");
		mainDialog.add(explorationLabel);
		mainDialog.addNumericField("Width", fieldWidth * defaultXFieldNumber, 0, 6, "micron");
		mainDialog.addNumericField("Height", fieldHeight * defaultYFieldNumber, 0, 6, "micron");
		final Vector<TextField> numFields = mainDialog.getNumericFields();
		numFields.get(0).addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void keyReleased(KeyEvent e) {
				refreshNumField();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		numFields.get(1).addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void keyReleased(KeyEvent e) {
				refreshNumField();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		mainDialog.add(numFieldLabel);
		
		// 
		
		mainDialog.addPanel(analysisParamPanel);

		JPanel strategyPanel = new JPanel();
		dynamicOpt = new JRadioButton("Dynamic");
		dynamicOpt.setSelected(parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.DYNAMIC).getAsBoolean());
		staticOpt = new JRadioButton("Static");
		staticOpt.setSelected(!parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.DYNAMIC).getAsBoolean());

		dynamicOpt.addActionListener(this);
		staticOpt.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(dynamicOpt);
		group.add(staticOpt);

		strategyPanel.add(staticOpt);
		strategyPanel.add(dynamicOpt);
		fluoAcqDuration = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.TIME_LIMIT).getAsString(),
				4);
		strategyPanel.add(fluoAcqDuration);
		strategyPanel.add(new JLabel("min"));
		mainDialog.add(strategyPanel);

		//

		mainDialog.addCheckbox("Save parameters", true);
		ReportingUtils.logMessage("- create OKMaarsMainDialog");

		JLabel savePathLabel = new JLabel("Save Path :");
		savePath = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH).getAsString(),
				20);
		JPanel savePathPanel = new JPanel();
		savePathPanel.add(savePathLabel);
		savePathPanel.add(savePath);
		mainDialog.add(savePathPanel);

		//

		okMainDialogButton = new Button("OK");
		okMainDialogButton.addActionListener(this);

		ReportingUtils.logMessage("- add button");
		mainDialog.add(okMainDialogButton);

		ReportingUtils.logMessage("Done.");

		mainDialog.pack();

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
		double newWidth = 0;
		double newHeigth = 0;
		Vector<TextField> numFields = mainDialog.getNumericFields();
		try {
			newWidth = Double.parseDouble(numFields.get(0).getText());
			newHeigth = Double.parseDouble(numFields.get(1).getText());
		} catch (NumberFormatException e) {
			// not a double
		}
		;

		int newXFieldNumber = (int) Math.round(newWidth / (calibration * mmc.getImageWidth()));
		int newYFieldNumber = (int) Math.round(newHeigth / (calibration * mmc.getImageHeight()));

		numFieldLabel.setText("Number of field : " + newXFieldNumber * newYFieldNumber);

		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS).getAsJsonObject()
				.remove(AllMaarsParameters.X_FIELD_NUMBER);
		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS).getAsJsonObject()
				.addProperty(AllMaarsParameters.X_FIELD_NUMBER, Integer.valueOf(newXFieldNumber));

		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS).getAsJsonObject()
				.remove(AllMaarsParameters.Y_FIELD_NUMBER);
		parameters.getParametersAsJsonObject().get(AllMaarsParameters.EXPLORATION_PARAMETERS).getAsJsonObject()
				.addProperty(AllMaarsParameters.Y_FIELD_NUMBER, Integer.valueOf(newYFieldNumber));
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
	 * method to set the strategy selected
	 */
	public void setAnalysisStrategy() {

		if (dynamicOpt.isSelected()) {
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.DYNAMIC, "true");
		} else if (staticOpt.isSelected()) {
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.DYNAMIC, "false");
		}
	}

	/**
	 * 
	 * @return parameters
	 */
	public AllMaarsParameters getParameters() {
		return parameters;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == autofocusButton) {
			getGui().showAutofocusDialog();
		} else if (e.getSource() == okMainDialogButton) {
			if (!savePath.getText()
					.equals(parameters.getParametersAsJsonObject().get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
							.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH).getAsString())) {
				AllMaarsParameters.updateGeneralParameter(parameters, AllMaarsParameters.SAVING_PATH, savePath.getText());			}
			if (!fluoAcqDuration.getText()
					.equals(parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject().get(AllMaarsParameters.TIME_LIMIT)
							.getAsString())) {
				AllMaarsParameters.updateGeneralParameter(parameters, AllMaarsParameters.TIME_LIMIT, fluoAcqDuration.getText());
			}
			saveParameters();
			setOkClicked(true);
			hide();
		} else if (e.getSource() == segmButton) {
			new MaarsSegmentationDialog(parameters);
		} else if (e.getSource() == fluoAnalysisButton) {
			new MaarsFluoAnalysisDialog(parameters);
		} else if (e.getSource() == dynamicOpt) {
			setAnalysisStrategy();
			fluoAcqDuration.setEditable(true);
		} else if (e.getSource() == staticOpt) {
			setAnalysisStrategy();
			fluoAcqDuration.setEditable(false);
		}
	}
}
