package org.micromanager.gui;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Dialog;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

import org.micromanager.maars.MaarsParameters;

/**
 * Class to create and display a dialog to get parameters of the fluorescent
 * analysis (detection and measurement of mitotic spindle)
 * 
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 *
 */
public class MaarsFluoAnalysisDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MaarsParameters parameters;
	private int filedLength = 8;
	private JTextField range;
	private JTextField step;
	private JTextField timeInterval;
	private JPanel channel1Panel;
	private JPanel channel2Panel;
	private JPanel channel3Panel;
	private JFormattedTextField maxNumberSpotCh1Tf;
	private JFormattedTextField spotRadiusCh1Tf;
	private JFormattedTextField maxNumberSpotCh2Tf;
	private JFormattedTextField spotRadiusCh2Tf;
	private JFormattedTextField maxNumberSpotCh3Tf;
	private JFormattedTextField spotRadiusCh3Tf;
	private JCheckBox saveFlims;
	private Button okFluoAnaParamButton;
	private JComboBox<String> channel1Combo;
	private JComboBox<String> channel2Combo;
	private JComboBox<String> channel3Combo;
	private static String NONE = "None";
	// TODO
	String channelListWithNone[] = { NONE, "GFP", "CFP", "TxRed", "DAPI" };
	String channelList[] = { "GFP", "CFP", "TxRed", "DAPI" };

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : parameters displayed in dialog
	 */
	public MaarsFluoAnalysisDialog(MaarsParameters parameters) {

		// set up this dialog

		this.parameters = parameters;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
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
		JLabel rangeTitle = new JLabel("Range (micron) : ",
				SwingConstants.CENTER);
		range = new JTextField(
				parameters
						.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE),
				filedLength);
		fluoRangePanel.add(rangeTitle);
		fluoRangePanel.add(range);

		//

		JPanel fluoStepPanel = new JPanel(new GridLayout(1, 2));
		JLabel stepTitle = new JLabel("Step (micron) : ", SwingConstants.CENTER);
		step = new JTextField(
				parameters.getFluoParameter(MaarsParameters.STEP), filedLength);
		fluoStepPanel.add(stepTitle);
		fluoStepPanel.add(step);

		//

		JPanel timeIntervalPanel = new JPanel(new GridLayout(1, 2));
		JLabel timeIntervalTitle = new JLabel("Time Interval (ms) : ",
				SwingConstants.CENTER);
		timeInterval = new JTextField(
				parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL),
				filedLength);
		timeIntervalPanel.add(timeIntervalTitle);
		timeIntervalPanel.add(timeInterval);

		//

		JPanel saveMoviesChkPanel = new JPanel(new GridLayout(1, 0));
		saveFlims = new JCheckBox(
				"Save Movies",
				Boolean.parseBoolean(parameters
						.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES)));
		saveMoviesChkPanel.add(saveFlims);

		//

		Label fluoAnaParamLabel = new Label("Spot identification parameter(s)",
				SwingConstants.CENTER);
		fluoAnaParamLabel.setBackground(labelColor);

		//

		JPanel channelTitlePanel = new JPanel(new GridLayout(1, 0));
		JLabel fluoChannelsTitle = new JLabel("Fluo Channel",
				SwingConstants.CENTER);
		JLabel maxNbSpotTitle = new JLabel("Max # of spot",
				SwingConstants.CENTER);
		JLabel spotRaiusTitle = new JLabel("Spot Radius", SwingConstants.CENTER);
		channelTitlePanel.add(fluoChannelsTitle);
		channelTitlePanel.add(maxNbSpotTitle);
		channelTitlePanel.add(spotRaiusTitle);

		//

		channel1Panel = new JPanel(new GridLayout(1, 0));
		channel1Combo = new JComboBox<String>(channelList);
		maxNumberSpotCh1Tf = new JFormattedTextField(Integer.class);
		spotRadiusCh1Tf = new JFormattedTextField(Double.class);
		channel1Panel.add(channel1Combo);
		channel1Panel.add(maxNumberSpotCh1Tf);
		channel1Panel.add(spotRadiusCh1Tf);

		//

		channel2Panel = new JPanel(new GridLayout(1, 0));
		channel2Combo = new JComboBox<String>(channelListWithNone);
		channel2Combo.addActionListener(this);
		maxNumberSpotCh2Tf = new JFormattedTextField(Integer.class);
		spotRadiusCh2Tf = new JFormattedTextField(Double.class);
		maxNumberSpotCh2Tf.setText("");
		spotRadiusCh2Tf.setText("");
		channel2Panel.add(channel2Combo);
		channel2Panel.add(maxNumberSpotCh2Tf);
		channel2Panel.add(spotRadiusCh2Tf);

		//

		channel3Panel = new JPanel(new GridLayout(1, 0));
		channel3Combo = new JComboBox<String>(channelListWithNone);
		channel3Combo.addActionListener(this);
		maxNumberSpotCh3Tf = new JFormattedTextField(Integer.class);
		spotRadiusCh3Tf = new JFormattedTextField(Double.class);
		maxNumberSpotCh3Tf.setText("");
		spotRadiusCh3Tf.setText("");
		channel3Panel.add(channel3Combo);
		channel3Panel.add(maxNumberSpotCh3Tf);
		channel3Panel.add(spotRadiusCh3Tf);

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

		okFluoAnaParamButton = new Button("OK");
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
		mainPanel.add(saveMoviesChkPanel);
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
	 * @return dialog
	 */
	public JDialog getDialog() {
		return this;
	}

	/**
	 * 
	 * @return parameters
	 */
	public MaarsParameters getParameters() {
		return parameters;
	}

	public void setChPanelValue(JPanel jp, String ch) {
		JComboBox<String> tmpCombo = (JComboBox<String>) jp.getComponent(0);
		tmpCombo.setSelectedItem(ch);

		JFormattedTextField tmpTf = (JFormattedTextField) jp.getComponent(1);
		tmpTf.setValue(parameters.getChMaxNbSpot(ch));
		tmpTf = (JFormattedTextField) jp.getComponent(2);
		tmpTf.setValue(parameters.getChSpotRaius(ch));

	}

	/**
	 * retrieve parameters from interface and update parameter object, then
	 * return selected channels
	 * 
	 * @return channels : list of selected object
	 */
	public String updateFluoChParameters() {
		String channels;
		String channel1 = channel1Combo.getSelectedItem().toString();
		parameters.setChMaxNbSpot(channel1, maxNumberSpotCh1Tf.getText());
		parameters.setChSpotRaius(channel1, spotRadiusCh1Tf.getText());
		if (!channel2Combo.getSelectedItem().equals(NONE)) {
			String channel2 = channel2Combo.getSelectedItem().toString();
			parameters.setChMaxNbSpot(channel2, maxNumberSpotCh2Tf.getText());
			parameters.setChSpotRaius(channel2, spotRadiusCh2Tf.getText());
			if (!channel3Combo.getSelectedItem().equals(NONE)) {
				String channel3 = channel3Combo.getSelectedItem().toString();
				parameters.setChMaxNbSpot(channel3,
						maxNumberSpotCh3Tf.getText());
				parameters.setChSpotRaius(channel3, spotRadiusCh3Tf.getText());
				channels = channel1 + "," + channel2 + "," + channel3;
			} else {
				channels = channel1 + "," + channel2;
			}
		} else {
			channels = channel1;
		}
		return channels;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == okFluoAnaParamButton) {
			parameters.setFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE,
					range.getText());
			parameters.setFluoParameter(MaarsParameters.STEP, step.getText());
			parameters.setFluoParameter(
					MaarsParameters.SAVE_FLUORESCENT_MOVIES,
					String.valueOf(saveFlims.isSelected()));
			parameters.setFluoParameter(MaarsParameters.TIME_INTERVAL,
					timeInterval.getText());
			String channelsList = updateFluoChParameters();
			parameters.setUsingChannels(channelsList);
			try {
				parameters.save();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			this.dispose();
		} else if (src == channel2Combo) {
			if (channel2Combo.getSelectedItem() != NONE) {
				setChPanelValue(channel2Panel, channel2Combo.getSelectedItem()
						.toString());
				channel3Combo.setEnabled(true);
				maxNumberSpotCh2Tf.setEditable(true);
				spotRadiusCh2Tf.setEditable(true);
			} else {
				maxNumberSpotCh2Tf.setText("");
				spotRadiusCh2Tf.setText("");
				maxNumberSpotCh2Tf.setEditable(false);
				spotRadiusCh2Tf.setEditable(false);
				channel3Combo.setSelectedItem(NONE);
				maxNumberSpotCh3Tf.setText("");
				spotRadiusCh3Tf.setText("");
				channel3Combo.setEnabled(false);
				maxNumberSpotCh3Tf.setEditable(false);
				spotRadiusCh3Tf.setEditable(false);
			}
		} else if (src == channel3Combo) {
			if (channel3Combo.getSelectedItem() != NONE) {
				setChPanelValue(channel3Panel, channel3Combo.getSelectedItem()
						.toString());
				maxNumberSpotCh3Tf.setEditable(true);
				spotRadiusCh3Tf.setEditable(true);
			} else {
				maxNumberSpotCh3Tf.setText("");
				spotRadiusCh3Tf.setText("");
				maxNumberSpotCh3Tf.setEditable(false);
				spotRadiusCh3Tf.setEditable(false);
			}
		}
	}
}
