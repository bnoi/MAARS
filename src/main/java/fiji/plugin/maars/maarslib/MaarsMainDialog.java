package fiji.plugin.maars.maarslib;

import java.awt.Button;
import java.awt.Color;

import java.awt.Dimension;

import java.awt.GridLayout;
import java.awt.Label;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import mmcorej.CMMCore;

import org.micromanager.internal.MMStudio;

import ij.IJ;

import org.micromanager.internal.utils.ReportingUtils;

/**
 * Class to create and display a dialog to get parameters of the whole analysis
 * 
 * @author Tong LI
 *
 */
public class MaarsMainDialog implements ActionListener {

	private JFrame mainDialog;
	private final Label numFieldLabel;
	private final MMStudio mm;
	private final CMMCore mmc;
	private AllMaarsParameters parameters;
	private final double calibration;
	private boolean okClicked;
	private Button autofocusButton;
	private Button okMainDialogButton;
	private Button segmButton;
	private Button fluoAnalysisButton;
	private JFormattedTextField savePathTf;
	private JFormattedTextField widthTf;
	private JFormattedTextField heightTf;
	private JFormattedTextField fluoAcqDurationTf;
	private JCheckBox saveParametersChk;
	private JRadioButton dynamicOpt;
	private JRadioButton staticOpt;

	/**
	 * Constructor :
	 * 
	 * @param mm
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param pathConfigFile
	 *            : path to maars_config.txt file containing all parameters of
	 *            the system (in JSON format)
	 * @throws IOException
	 */
	public MaarsMainDialog(MMStudio mm, CMMCore mmc, String pathConfigFile) {

		// ------------initialization of parameters---------------//
		try {
			PrintStream ps = new PrintStream(System.getProperty("user.dir")
					+ "/MAARS.LOG");
			System.setOut(ps);
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			ReportingUtils.logError(e);
		}
		Color labelColor = Color.ORANGE;
		Color bgColor = Color.WHITE;
		okClicked = false;

		this.mm = mm;
		this.mmc = mmc;

		// initialize mainFrame

		ReportingUtils.logMessage("create main dialog ...");
		mainDialog = new JFrame(
				"Mitosis Analysing And Recording System - MAARS");
		JFrame.setDefaultLookAndFeelDecorated(true);
		mainDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// set minimal dimension of mainDialog

		ReportingUtils.logMessage("- set minimal dimension");
		int maxDialogWidth = 350;
		int maxDialogHeight = 600;
		Dimension minimumSize = new Dimension(maxDialogWidth, maxDialogHeight);
		mainDialog.setMinimumSize(minimumSize);

		// Read config file

		ReportingUtils.logMessage("create parameter object ...");
		try {
			parameters = new AllMaarsParameters(pathConfigFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ReportingUtils.logMessage("Done.");

		// Get number of field to explore

		int defaultXFieldNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.X_FIELD_NUMBER)
				.getAsInt();
		int defaultYFieldNumber = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.EXPLORATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.Y_FIELD_NUMBER)
				.getAsInt();

		// Calculate width and height for each field

		calibration = mm.getCMMCore().getPixelSizeUm();
		double fieldWidth = mmc.getImageWidth() * calibration;
		double fieldHeight = mmc.getImageHeight() * calibration;

		// Exploration Label

		Label explorationLabel = new Label("Area to explore");
		explorationLabel.setBackground(labelColor);

		// field width Panel (Label + textfield)

		JPanel widthPanel = new JPanel(new GridLayout(1, 0));
		widthPanel.setBackground(bgColor);
		JLabel widthLabel = new JLabel("Width :", SwingConstants.CENTER);
		widthTf = new JFormattedTextField(Double.class);
		widthTf.setValue(fieldWidth * defaultXFieldNumber);
		widthTf.addKeyListener(new KeyListener() {
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
		widthPanel.add(widthLabel);
		widthPanel.add(widthTf);

		// field Height Panel (Label + textfield)

		JPanel heightPanel = new JPanel(new GridLayout(1, 0));
		heightPanel.setBackground(bgColor);
		JLabel heightLabel = new JLabel("Height :", SwingConstants.CENTER);
		heightTf = new JFormattedTextField(Double.class);
		heightTf.setValue(fieldHeight * defaultYFieldNumber);
		heightTf.addKeyListener(new KeyListener() {
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
		heightPanel.add(heightLabel);
		heightPanel.add(heightTf);

		// number of field label

		numFieldLabel = new Label("Number of field : " + defaultXFieldNumber
				* defaultYFieldNumber);

		// analysis parameters label

		Label analysisParamLabel = new Label("Analysis parameters",
				SwingConstants.CENTER);
		analysisParamLabel.setBackground(labelColor);

		// autofocus button

		ReportingUtils.logMessage("- create AutofocusButton");
		autofocusButton = new Button("Autofocus");
		autofocusButton.addActionListener(this);

		// segmentation button

		ReportingUtils.logMessage("- create OpenSegmentationDialogButton");
		segmButton = new Button("Segmentation");
		segmButton.addActionListener(this);

		// fluo analysis button

		ReportingUtils.logMessage("- create OpenFluoAnalysisDialog");
		fluoAnalysisButton = new Button("Fluorescent analysis");
		fluoAnalysisButton.addActionListener(this);

		// strategy panel (2 radio button + 1 textfield + 1 label)

		JPanel strategyPanel = new JPanel(new GridLayout(1, 0));
		strategyPanel.setBackground(bgColor);
		dynamicOpt = new JRadioButton("Dynamic");
		dynamicOpt.setSelected(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.DYNAMIC)
				.getAsBoolean());
		staticOpt = new JRadioButton("Static");
		staticOpt.setSelected(!parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.DYNAMIC)
				.getAsBoolean());

		dynamicOpt.addActionListener(this);
		staticOpt.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(dynamicOpt);
		group.add(staticOpt);

		strategyPanel.add(staticOpt);
		strategyPanel.add(dynamicOpt);
		fluoAcqDurationTf = new JFormattedTextField(Double.class);
		fluoAcqDurationTf.setValue(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.TIME_LIMIT)
				.getAsDouble());
		strategyPanel.add(fluoAcqDurationTf);
		strategyPanel.add(new JLabel("min", SwingConstants.CENTER));

		// checkbox : update or not MAARS parameters

		JPanel savingPathChkPanel = new JPanel(new GridLayout(1, 0));
		savingPathChkPanel.setBackground(bgColor);
		saveParametersChk = new JCheckBox("Save parameters", true);
		savingPathChkPanel.add(saveParametersChk);

		// Saving path Panel

		JPanel savePathLabelPanel = new JPanel(new GridLayout(1, 0));
		savePathLabelPanel.setBackground(bgColor);
		JLabel savePathLabel = new JLabel("Save Path :");
		savePathLabelPanel.add(savePathLabel);

		// Saving Path textfield

		JPanel savePathTfPanel = new JPanel(new GridLayout(1, 0));
		savePathTf = new JFormattedTextField(parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH)
				.getAsString());
		savePathTf.setMaximumSize(new Dimension(maxDialogWidth, 1));
		savePathTfPanel.add(savePathTf);

		// Ok button to run

		okMainDialogButton = new Button("OK");
		okMainDialogButton.addActionListener(this);

		// ------------set up and add components to Panel then to Frame---------------//

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(bgColor);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(explorationLabel);
		mainPanel.add(widthPanel);
		mainPanel.add(heightPanel);
		mainPanel.add(numFieldLabel);
		mainPanel.add(analysisParamLabel);
		mainPanel.add(autofocusButton);
		mainPanel.add(segmButton);
		mainPanel.add(fluoAnalysisButton);
		mainPanel.add(strategyPanel);
		mainPanel.add(savingPathChkPanel);
		mainPanel.add(savePathLabelPanel);
		mainPanel.add(savePathTfPanel);
		mainPanel.add(okMainDialogButton);
		mainDialog.add(mainPanel);
		ReportingUtils.logMessage("Done.");
		mainDialog.pack();
	}

	/**
	 * 
	 * @return graphical user interface of Micro-Manager
	 */
	public MMStudio getMM() {
		return mm;
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

		newWidth = (Double) widthTf.getValue();
		newHeigth = (Double) widthTf.getValue();

		;

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

		try {
			parameters.save();
		} catch (IOException e) {
			e.printStackTrace();
			IJ.error("Could not save parameters");
		}
	}

	/**
	 * method to set the strategy selected
	 */
	public void setAnalysisStrategy() {

		if (dynamicOpt.isSelected()) {
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.DYNAMIC, "true");
		} else if (staticOpt.isSelected()) {
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.DYNAMIC, "false");
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
			getMM().showAutofocusDialog();
		} else if (e.getSource() == okMainDialogButton) {
			if (!savePathTf
					.getText()
					.equals(parameters
							.getParametersAsJsonObject()
							.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.SAVING_PATH).getAsString())) {
				AllMaarsParameters.updateGeneralParameter(
						parameters, AllMaarsParameters.SAVING_PATH,
						savePathTf.getText());
			}
			if (!fluoAcqDurationTf.getText().equals(
					parameters.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.TIME_LIMIT).getAsString())) {
				AllMaarsParameters.updateFluoParameter(parameters,
						AllMaarsParameters.TIME_LIMIT,
						fluoAcqDurationTf.getText());
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
			fluoAcqDurationTf.setEditable(true);
		} else if (e.getSource() == staticOpt) {
			setAnalysisStrategy();
			fluoAcqDurationTf.setEditable(false);
		}
	}
}
