package edu.univ_tlse3.gui;

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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.univ_tlse3.acquisition.FluoAcqSetting;
import edu.univ_tlse3.acquisition.AcqLauncher;
import edu.univ_tlse3.cellstateanalysis.MaarsTrackmate;

import edu.univ_tlse3.maars.MaarsParameters;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.internal.MMStudio;

import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.ImgUtils;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.IJ;
import ij.ImagePlus;

import javax.swing.*;

/**
 * Class to create and display a dialog to get parameters of the fluorescent
 * analysis (detection and measurement of mitotic spindle)
 * 
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 *
 */
class MaarsFluoAnalysisDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MMStudio mm;
	private MaarsParameters parameters;
	private JTextField range;
	private JTextField step;
	private JTextField timeInterval;
	private JPanel channel1Panel;
	private JPanel channel2Panel;
	private JPanel channel3Panel;
	private JFormattedTextField maxNumberSpotCh1Tf;
	private JFormattedTextField spotRadiusCh1Tf;
	private JFormattedTextField qualityCh1Tf;
	private JFormattedTextField maxNumberSpotCh2Tf;
	private JFormattedTextField spotRadiusCh2Tf;
	private JFormattedTextField qualityCh2Tf;
	private JFormattedTextField maxNumberSpotCh3Tf;
	private JFormattedTextField spotRadiusCh3Tf;
	private JFormattedTextField qualityCh3Tf;
	private JCheckBox saveFlims;
	private JCheckBox doAnalysis;
	private JButton test1;
	private JButton test2;
	private JButton test3;
	private JButton okFluoAnaParamButton;
	private JComboBox channel1Combo;
	private JComboBox channel2Combo;
	private JComboBox channel3Combo;
	private static String NONE = "None";

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : parameters displayed in dialog
	 */
	MaarsFluoAnalysisDialog(MMStudio mm, MaarsParameters parameters) {

		// set up this dialog
		this.mm = mm;
		this.parameters = parameters;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("MAARS - Fluorescent Analysis parameters");
		this.setBackground(Color.WHITE);
		this.setLayout(new GridLayout(0, 1));
		this.setMinimumSize(new Dimension(250, 500));
		Color labelColor = Color.ORANGE;

		// Movie parameters label

		Label fluoMovieLabel = new Label("Movie parameters", Label.CENTER);
		fluoMovieLabel.setBackground(labelColor);

		//

		JPanel fluoRangePanel = new JPanel(new GridLayout(1, 2));
		JLabel rangeTitle = new JLabel("Range (micron) : ", SwingConstants.CENTER);
		int fieldLength = 8;
		range = new JTextField(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE), fieldLength);
		fluoRangePanel.add(rangeTitle);
		fluoRangePanel.add(range);

		//

		JPanel fluoStepPanel = new JPanel(new GridLayout(1, 2));
		JLabel stepTitle = new JLabel("Step (micron) : ", SwingConstants.CENTER);
		step = new JTextField(parameters.getFluoParameter(MaarsParameters.STEP), fieldLength);
		fluoStepPanel.add(stepTitle);
		fluoStepPanel.add(step);

		//

		JPanel timeIntervalPanel = new JPanel(new GridLayout(1, 2));
		JLabel timeIntervalTitle = new JLabel("Time Interval (ms) : ", SwingConstants.CENTER);
		timeInterval = new JTextField(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL), fieldLength);
		timeIntervalPanel.add(timeIntervalTitle);
		timeIntervalPanel.add(timeInterval);
		timeInterval.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (Double.parseDouble(timeInterval.getText()) < 10000) {
					doAnalysis.setSelected(false);
					doAnalysis.setEnabled(false);
				} else {
					doAnalysis.setSelected(true);
					doAnalysis.setEnabled(true);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		//

		JPanel checkBoxPanel = new JPanel(new GridLayout(1, 0));
		saveFlims = new JCheckBox("Save Movies",
				Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES)));
		doAnalysis = new JCheckBox("Do Analysis", true);
		checkBoxPanel.add(saveFlims);
		checkBoxPanel.add(doAnalysis);

		//

		Label fluoAnaParamLabel = new Label("Spot identification parameter(s)", SwingConstants.CENTER);
		fluoAnaParamLabel.setBackground(labelColor);

		//

		JPanel channelTitlePanel = new JPanel(new GridLayout(1, 0));
		JLabel fluoChannelsTitle = new JLabel("Fluo Channel", SwingConstants.CENTER);
		JLabel maxNbSpotTitle = new JLabel("Max # of spot", SwingConstants.CENTER);
		JLabel spotRaiusTitle = new JLabel("Spot Radius", SwingConstants.CENTER);
		JLabel qualityTitle = new JLabel("Quality", SwingConstants.CENTER);
		channelTitlePanel.add(fluoChannelsTitle);
		channelTitlePanel.add(maxNbSpotTitle);
		channelTitlePanel.add(spotRaiusTitle);
		channelTitlePanel.add(qualityTitle);
		channelTitlePanel.add(new JLabel());

		//

		channel1Panel = new JPanel(new GridLayout(1, 0));
		String[] channelList = {"GFP", "CFP", "TxRed", "DAPI"};
		channel1Combo = new JComboBox(channelList);
		maxNumberSpotCh1Tf = new JFormattedTextField(Integer.class);
		spotRadiusCh1Tf = new JFormattedTextField(Double.class);
		qualityCh1Tf = new JFormattedTextField(Double.class);
		JPanel buttonPanel1 = new JPanel(new GridLayout(1, 0));
		test1 = new JButton("test");
		test1.addActionListener(this);
		buttonPanel1.add(test1);
		channel1Panel.add(channel1Combo);
		channel1Panel.add(maxNumberSpotCh1Tf);
		channel1Panel.add(spotRadiusCh1Tf);
		channel1Panel.add(qualityCh1Tf);
		channel1Panel.add(buttonPanel1);
		//

		channel2Panel = new JPanel(new GridLayout(1, 0));
		String[] channelListWithNone = {NONE, "GFP", "CFP", "TxRed", "DAPI"};
		channel2Combo = new JComboBox(channelListWithNone);
		channel2Combo.addActionListener(this);
		maxNumberSpotCh2Tf = new JFormattedTextField(Integer.class);
		spotRadiusCh2Tf = new JFormattedTextField(Double.class);
		qualityCh2Tf = new JFormattedTextField(Double.class);
		JPanel buttonPanel2 = new JPanel(new GridLayout(1, 0));
		test2 = new JButton("test");
		test2.addActionListener(this);
		buttonPanel2.add(test2);
		maxNumberSpotCh2Tf.setText("");
		spotRadiusCh2Tf.setText("");
		qualityCh2Tf.setText("");
		channel2Panel.add(channel2Combo);
		channel2Panel.add(maxNumberSpotCh2Tf);
		channel2Panel.add(spotRadiusCh2Tf);
		channel2Panel.add(qualityCh2Tf);
		channel2Panel.add(buttonPanel2);

		//

		channel3Panel = new JPanel(new GridLayout(1, 0));
		channel3Combo = new JComboBox(channelListWithNone);
		channel3Combo.addActionListener(this);
		maxNumberSpotCh3Tf = new JFormattedTextField(Integer.class);
		spotRadiusCh3Tf = new JFormattedTextField(Double.class);
		qualityCh3Tf = new JFormattedTextField(Double.class);
		JPanel buttonPanel3 = new JPanel(new GridLayout(1, 0));
		test3 = new JButton("test");
		test3.setEnabled(false);
		test3.addActionListener(this);
		buttonPanel3.add(test3);
		maxNumberSpotCh3Tf.setText("");
		spotRadiusCh3Tf.setText("");
		qualityCh3Tf.setText("");
		channel3Panel.add(channel3Combo);
		channel3Panel.add(maxNumberSpotCh3Tf);
		channel3Panel.add(spotRadiusCh3Tf);
		channel3Panel.add(qualityCh3Tf);
		channel3Panel.add(buttonPanel3);

		//

		String channelsString = parameters.getUsingChannels();
		String[] arrayChannels = channelsString.split(",", -1);
		if (arrayChannels.length == 1) {
			setChPanelValue(channel1Panel, arrayChannels[0]);
		} else if (arrayChannels.length == 2) {
			setChPanelValue(channel1Panel, arrayChannels[0]);
			setChPanelValue(channel2Panel, arrayChannels[1]);
		} else if (arrayChannels.length == 3) {
			setChPanelValue(channel1Panel, arrayChannels[0]);
			setChPanelValue(channel2Panel, arrayChannels[1]);
			setChPanelValue(channel3Panel, arrayChannels[2]);
		}

		//

		okFluoAnaParamButton = new JButton("OK");
		okFluoAnaParamButton.addActionListener(this);

		//

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setLayout(new GridLayout(0, 1));
		mainPanel.setMinimumSize(new Dimension(250, 500));
		mainPanel.add(fluoMovieLabel);
		mainPanel.add(fluoRangePanel);
		mainPanel.add(fluoStepPanel);
		mainPanel.add(timeIntervalPanel);
		mainPanel.add(checkBoxPanel);
		mainPanel.add(fluoAnaParamLabel);
		mainPanel.add(channelTitlePanel);
		mainPanel.add(channel1Panel);
		mainPanel.add(channel2Panel);
		mainPanel.add(channel3Panel);
		mainPanel.add(okFluoAnaParamButton);
		this.add(mainPanel);

		//

		this.pack();
		this.setVisible(true);
	}

	/**
	 * 
	 * @return parameters
	 */
	public MaarsParameters getParameters() {
		return parameters;
	}

	private void setChPanelValue(JPanel jp, String ch) {
		@SuppressWarnings("unchecked")
		JComboBox tmpCombo = (JComboBox) jp.getComponent(0);
		tmpCombo.setSelectedItem(ch);

		JFormattedTextField tmpTf = (JFormattedTextField) jp.getComponent(1);
		tmpTf.setValue(parameters.getChMaxNbSpot(ch));
		tmpTf = (JFormattedTextField) jp.getComponent(2);
		tmpTf.setValue(parameters.getChSpotRaius(ch));
		tmpTf = (JFormattedTextField) jp.getComponent(3);
		tmpTf.setValue(parameters.getChQuality(ch));

	}

	/**
	 * retrieve parameters from interface and update parameter object, then
	 * return selected channels
	 * 
	 * @return channels : list of selected object
	 */
	private String updateFluoChParameters() {
		String channels;
		String channel1 = channel1Combo.getSelectedItem().toString();
		parameters.setChMaxNbSpot(channel1, maxNumberSpotCh1Tf.getText());
		parameters.setChSpotRaius(channel1, spotRadiusCh1Tf.getText());
		parameters.setChQuality(channel1, qualityCh1Tf.getText());
		if (!channel2Combo.getSelectedItem().equals(NONE)) {
			String channel2 = channel2Combo.getSelectedItem().toString();
			parameters.setChMaxNbSpot(channel2, maxNumberSpotCh2Tf.getText());
			parameters.setChSpotRaius(channel2, spotRadiusCh2Tf.getText());
			parameters.setChQuality(channel2, qualityCh2Tf.getText());
			if (!channel3Combo.getSelectedItem().equals(NONE)) {
				String channel3 = channel3Combo.getSelectedItem().toString();
				parameters.setChMaxNbSpot(channel3, maxNumberSpotCh3Tf.getText());
				parameters.setChSpotRaius(channel3, spotRadiusCh3Tf.getText());
				parameters.setChQuality(channel3, qualityCh3Tf.getText());
				channels = channel1 + "," + channel2 + "," + channel3;
			} else {
				channels = channel1 + "," + channel2;
			}
		} else {
			channels = channel1;
		}
		return channels;

	}

	private void testTrackmate(JPanel jp, ImagePlus img) {
		JFormattedTextField tmpTf = (JFormattedTextField) jp.getComponent(2);
		double spotRadius = Double.parseDouble((String) tmpTf.getValue());
		tmpTf = (JFormattedTextField) jp.getComponent(3);
		double quality = Double.parseDouble((String) tmpTf.getValue());
		// ImagePlus img = IJ.getImage().duplicate();
		img.show();
		ImagePlus zProjectedFluoImg = ImgUtils.zProject(img);
		zProjectedFluoImg.setCalibration(img.getCalibration());
		MaarsTrackmate tmTest = new MaarsTrackmate(zProjectedFluoImg, spotRadius, quality);
		Model model = tmTest.doDetection(false);
		model.getSpots().setVisible(true);
		SelectionModel selectionModel = new SelectionModel(model);
		HyperStackDisplayer displayer = new HyperStackDisplayer(model, selectionModel, zProjectedFluoImg);
		IJ.run(zProjectedFluoImg, "Enhance Contrast", "saturated=0.35");
		displayer.render();
		displayer.refresh();
	}

	private void testTrackmate(JPanel jp) {
		String channelName = getSelectedChannel(jp);
		String imgPath = parameters.getSavingPath() + File.separator + "X0_Y0_FLUO" + File.separator + "0_"
				+ channelName +"_1" + File.separator + "0_"+ channelName+ "_1_MMStack_Pos0.ome.tif";
		if (FileUtils.exists(imgPath)) {
			testTrackmate(jp, IJ.openImage(imgPath));
		} else {
			testTrackmate(jp, acquireTestImg(jp));
		}
	}

	private ImagePlus acquireTestImg(JPanel jp) {
        double zRange = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
        double zStep = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));

		MaarsParameters testParam = parameters.duplicate();
		String channelName = getSelectedChannel(jp);
		testParam.setUsingChannels(channelName);
		testParam.setFluoParameter(MaarsParameters.TIME_LIMIT, "0");
		testParam.setFluoParameter(MaarsParameters.TIME_INTERVAL, this.timeInterval.getText());
		testParam.setFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES, "false");
		FluoAcqSetting acq = new FluoAcqSetting(testParam);

		SequenceSettings fluoAcqSetting = acq.configAcqSettings(acq.configChannels(channelName));
		fluoAcqSetting.keepShutterOpenSlices = true;
		AcquisitionWrapperEngine acqEng = mm.getAcquisitionEngine();
		acqEng.setSequenceSettings(fluoAcqSetting);
		acqEng.enableZSliceSetting(true);
		acqEng.setSlices(-zRange/2, zRange/2, zStep, false);
		acqEng.setChannelGroup(fluoAcqSetting.channelGroup);
		return AcqLauncher.acquire(acqEng);
	}

	private String getSelectedChannel(JPanel jp) {
		JComboBox tmpCombo = (JComboBox) jp.getComponent(0);
		return (String) tmpCombo.getSelectedItem();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == okFluoAnaParamButton) {
			parameters.setFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE, range.getText());
			parameters.setFluoParameter(MaarsParameters.STEP, step.getText());
			parameters.setFluoParameter(MaarsParameters.DO_ANALYSIS, String.valueOf(doAnalysis.isSelected()));
			parameters.setFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES,
					String.valueOf(saveFlims.isSelected()));
			parameters.setFluoParameter(MaarsParameters.TIME_INTERVAL, timeInterval.getText());
			String channelsList = updateFluoChParameters();
			parameters.setUsingChannels(channelsList);
			try {
				parameters.save();
			} catch (IOException e1) {
				System.out.println("Can not save MAARS parameters");
				e1.printStackTrace();
			}
			this.dispose();
		} else if (src == channel2Combo) {
			if (channel2Combo.getSelectedItem() != NONE) {
				setChPanelValue(channel2Panel, channel2Combo.getSelectedItem().toString());
				channel3Combo.setEnabled(true);
				maxNumberSpotCh2Tf.setEditable(true);
				spotRadiusCh2Tf.setEditable(true);
				qualityCh2Tf.setEditable(true);
				test2.setEnabled(true);
			} else {
				maxNumberSpotCh2Tf.setText("");
				spotRadiusCh2Tf.setText("");
				qualityCh2Tf.setText("");
				maxNumberSpotCh2Tf.setEditable(false);
				spotRadiusCh2Tf.setEditable(false);
				qualityCh2Tf.setEditable(false);
				test2.setEnabled(false);
				channel3Combo.setSelectedItem(NONE);
				maxNumberSpotCh3Tf.setText("");
				spotRadiusCh3Tf.setText("");
				qualityCh3Tf.setText("");
				channel3Combo.setEnabled(false);
				maxNumberSpotCh3Tf.setEditable(false);
				spotRadiusCh3Tf.setEditable(false);
				qualityCh3Tf.setEditable(false);
				test3.setEnabled(false);
			}
		} else if (src == channel3Combo) {
			if (channel3Combo.getSelectedItem() != NONE) {
				setChPanelValue(channel3Panel, channel3Combo.getSelectedItem().toString());
				maxNumberSpotCh3Tf.setEditable(true);
				spotRadiusCh3Tf.setEditable(true);
				qualityCh3Tf.setEditable(true);
				test3.setEnabled(true);
			} else {
				maxNumberSpotCh3Tf.setText("");
				spotRadiusCh3Tf.setText("");
				qualityCh3Tf.setText("");
				maxNumberSpotCh3Tf.setEditable(false);
				spotRadiusCh3Tf.setEditable(false);
				qualityCh3Tf.setEditable(false);
				test3.setEnabled(false);
			}
		} else if (src == test1) {
			testTrackmate(channel1Panel);
		} else if (src == test2) {
			testTrackmate(channel2Panel);
		} else if (src == test3) {
			testTrackmate(channel3Panel);
		}
	}
}
