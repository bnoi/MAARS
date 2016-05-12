package org.univ_tlse3.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import mmcorej.CMMCore;

import org.micromanager.internal.MMStudio;

import ij.IJ;

import org.univ_tlse3.maars.MAARS;
import org.univ_tlse3.maars.MAARSNoAcq;
import org.univ_tlse3.maars.MaarsParameters;
import org.univ_tlse3.utils.FileUtils;

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
	private MaarsParameters parameters;
	private final double calibration;
	private JButton autofocusButton;
	private JButton okMainDialogButton;
	private JButton segmButton;
	private JButton fluoAnalysisButton;
	private JFormattedTextField savePathTf;
	private JFormattedTextField widthTf;
	private JFormattedTextField heightTf;
	private JFormattedTextField fluoAcqDurationTf;
	private JCheckBox saveParametersChk;
	private JCheckBox withOutAcqChk;
	private JRadioButton dynamicOpt;
	private JRadioButton staticOpt;

	/**
	 * Constructor
	 * 
	 * @param mm
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param parameters
	 *            :MaarsParameters
	 */
	public MaarsMainDialog(MMStudio mm, MaarsParameters parameters) {

		// ------------initialization of parameters---------------//
		Color labelColor = Color.ORANGE;
		Color bgColor = Color.WHITE;

		this.mm = mm;
		this.mmc = mm.core();
		this.parameters = parameters;

		// initialize mainFrame

		IJ.log("create main dialog ...");
		mainDialog = new JFrame("Mitosis Analysing And Recording System - MAARS");
		JFrame.setDefaultLookAndFeelDecorated(true);
		mainDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// set minimal dimension of mainDialog

		int maxDialogWidth = 350;
		int maxDialogHeight = 600;
		Dimension minimumSize = new Dimension(maxDialogWidth, maxDialogHeight);
		mainDialog.setMinimumSize(minimumSize);

		// Get number of field to explore

		int defaultXFieldNumber = parameters.getFieldNb(MaarsParameters.X_FIELD_NUMBER);
		int defaultYFieldNumber = parameters.getFieldNb(MaarsParameters.Y_FIELD_NUMBER);

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
			}

			@Override
			public void keyReleased(KeyEvent e) {
				refreshNumField();
			}

			@Override
			public void keyPressed(KeyEvent e) {
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
			}

			@Override
			public void keyReleased(KeyEvent e) {
				refreshNumField();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		heightPanel.add(heightLabel);
		heightPanel.add(heightTf);

		// number of field label

		numFieldLabel = new Label("Number of field : " + defaultXFieldNumber * defaultYFieldNumber);

		// analysis parameters label

		Label analysisParamLabel = new Label("Analysis parameters", SwingConstants.CENTER);
		analysisParamLabel.setBackground(labelColor);

		// autofocus button

		JPanel autoFocusPanel = new JPanel(new GridLayout(1, 0));
		autofocusButton = new JButton("Autofocus");
		autofocusButton.addActionListener(this);
		autoFocusPanel.add(autofocusButton);

		// segmentation button

		JPanel segPanel = new JPanel(new GridLayout(1, 0));
		segmButton = new JButton("Segmentation");
		segmButton.addActionListener(this);
		segPanel.add(segmButton);

		// fluo analysis button

		JPanel fluoAnalysisPanel = new JPanel(new GridLayout(1, 0));
		fluoAnalysisButton = new JButton("Fluorescent analysis");
		fluoAnalysisButton.addActionListener(this);
		fluoAnalysisPanel.add(fluoAnalysisButton);

		// strategy panel (2 radio button + 1 textfield + 1 label)

		JPanel strategyPanel = new JPanel(new GridLayout(1, 0));
		strategyPanel.setBackground(bgColor);
		dynamicOpt = new JRadioButton("Dynamic");
		dynamicOpt.setSelected(parameters.useDynamic());
		staticOpt = new JRadioButton("Static");
		staticOpt.setSelected(!parameters.useDynamic());

		dynamicOpt.addActionListener(this);
		staticOpt.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(dynamicOpt);
		group.add(staticOpt);

		strategyPanel.add(staticOpt);
		strategyPanel.add(dynamicOpt);
		fluoAcqDurationTf = new JFormattedTextField(Double.class);
		fluoAcqDurationTf.setValue(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT));
		strategyPanel.add(fluoAcqDurationTf);
		strategyPanel.add(new JLabel("min", SwingConstants.CENTER));

		// checkbox : update or not MAARS parameters

		JPanel chkPanel = new JPanel(new GridLayout(1, 0));
		chkPanel.setBackground(bgColor);
		saveParametersChk = new JCheckBox("Save parameters", true);
		withOutAcqChk = new JCheckBox("Don't do acquisition", false);
		withOutAcqChk.addActionListener(this);
		chkPanel.add(withOutAcqChk);
		chkPanel.add(saveParametersChk);

		// Saving path Panel

		JPanel savePathLabelPanel = new JPanel(new GridLayout(1, 0));
		savePathLabelPanel.setBackground(bgColor);
		JLabel savePathLabel = new JLabel("Saving Path :");
		savePathLabelPanel.add(savePathLabel);

		// Saving Path textfield

		JPanel savePathTfPanel = new JPanel(new GridLayout(1, 0));
		savePathTf = new JFormattedTextField(parameters.getSavingPath());
		savePathTf.setMaximumSize(new Dimension(maxDialogWidth, 1));
		savePathTfPanel.add(savePathTf);

		// Ok button to run

		JPanel okPanel = new JPanel(new GridLayout(1, 0));
		okMainDialogButton = new JButton("OK");
		okMainDialogButton.addActionListener(this);
		mainDialog.getRootPane().setDefaultButton(okMainDialogButton);
		okPanel.add(okMainDialogButton);

		// ------------set up and add components to Panel then to
		// Frame---------------//

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(bgColor);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(explorationLabel);
		mainPanel.add(widthPanel);
		mainPanel.add(heightPanel);
		mainPanel.add(numFieldLabel);
		mainPanel.add(analysisParamLabel);
		mainPanel.add(autoFocusPanel);
		mainPanel.add(segPanel);
		mainPanel.add(fluoAnalysisPanel);
		mainPanel.add(strategyPanel);
		mainPanel.add(chkPanel);
		mainPanel.add(savePathLabelPanel);
		mainPanel.add(savePathTfPanel);
		mainPanel.add(okPanel);
		mainDialog.add(mainPanel);
		IJ.log("Done.");
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
		mainDialog.dispose();
	}

	/**
	 * Method to display number of field the program has to scan
	 */
	public void refreshNumField() {
		double newWidth = 0;
		double newHeigth = 0;

		newWidth = (Double) widthTf.getValue();
		newHeigth = (Double) widthTf.getValue();

		int newXFieldNumber = (int) Math.round(newWidth / (calibration * mmc.getImageWidth()));
		int newYFieldNumber = (int) Math.round(newHeigth / (calibration * mmc.getImageHeight()));
		int totoalNbField = newXFieldNumber * newYFieldNumber;
		if (totoalNbField == 0) {
			numFieldLabel.setForeground(Color.red);
			numFieldLabel.setText("Number of field : " + totoalNbField);
		} else {
			numFieldLabel.setForeground(Color.black);
			numFieldLabel.setText("Number of field : " + totoalNbField);
		}

		parameters.setFieldNb(MaarsParameters.X_FIELD_NUMBER, "" + newXFieldNumber);
		parameters.setFieldNb(MaarsParameters.Y_FIELD_NUMBER, "" + newYFieldNumber);
	}

	/**
	 * method to save the parameters entered
	 */
	public void saveParameters() {
		if (!savePathTf.getText().equals(parameters.getSavingPath())) {
			parameters.setSavingPath(savePathTf.getText());
		}
		if (!fluoAcqDurationTf.getText().equals(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT))) {
			parameters.setFluoParameter(MaarsParameters.TIME_LIMIT, fluoAcqDurationTf.getText());
		}
		try {
			parameters.save();
		} catch (IOException e) {
			IJ.error("Could not save parameters");
		}
	}

	/**
	 * method to set the strategy selected
	 */
	public void setAnalysisStrategy() {

		if (dynamicOpt.isSelected()) {
			parameters.setFluoParameter(MaarsParameters.DYNAMIC, "" + true);
		} else if (staticOpt.isSelected()) {
			parameters.setFluoParameter(MaarsParameters.DYNAMIC, "" + false);
		}
	}

	public int overWrite(String path) {
		int overWrite = 0;
		if (FileUtils.exists(path + File.separator + "X0_Y0" + File.separator + "MMStack.ome.tif")) {
			overWrite = JOptionPane.showConfirmDialog(mainDialog, "Overwrite existing acquisitions?");
		}
		return overWrite;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == autofocusButton) {
			getMM().showAutofocusDialog();
		} else if (e.getSource() == okMainDialogButton) {
			if ((Double) widthTf.getValue() * (Double) heightTf.getValue() == 0) {
				IJ.error("Session aborted, 0 field to analyse");
			} else {
				Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
					public void uncaughtException(Thread th, Throwable ex) {
						System.out.println("Uncaught exception: " + ex);
					}
				};
				saveParameters();
				Thread th = null;
				if (withOutAcqChk.isSelected()) {
					th = new Thread(new MAARSNoAcq(parameters));
				} else {
					if (overWrite(parameters.getSavingPath()) == JOptionPane.YES_OPTION) {
						th = new Thread(new MAARS(mm, mmc, parameters));
					}
				}
				th.setUncaughtExceptionHandler(h);
				th.start();
			}
		} else if (e.getSource() == segmButton) {
			new MaarsSegmentationDialog(parameters);
		} else if (e.getSource() == fluoAnalysisButton) {
			saveParameters();
			new MaarsFluoAnalysisDialog(mm, mmc, parameters);
		} else if (e.getSource() == dynamicOpt) {
			setAnalysisStrategy();
			fluoAcqDurationTf.setEditable(true);
		} else if (e.getSource() == staticOpt) {
			setAnalysisStrategy();
			fluoAcqDurationTf.setEditable(false);
		} else if (e.getSource() == withOutAcqChk) {
			if (withOutAcqChk.isSelected()) {
				widthTf.setText("MAARS do it for you");
				widthTf.setEditable(false);
				heightTf.setText("MAARS do it for you");
				heightTf.setEditable(false);
				numFieldLabel.setText("Don't worry about this");
			} else {
				widthTf.setText(String.valueOf(mmc.getImageWidth() * calibration));
				widthTf.setEditable(true);
				heightTf.setText(String.valueOf(mmc.getImageHeight() * calibration));
				heightTf.setEditable(true);
				refreshNumField();
			}
		} else {
			IJ.log("MAARS don't understand what you want, sorry");
		}
	}
}
