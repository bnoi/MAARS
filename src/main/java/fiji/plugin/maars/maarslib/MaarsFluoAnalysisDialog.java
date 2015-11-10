package fiji.plugin.maars.maarslib;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Dialog;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

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
	private AllMaarsParameters parameters;
	private int filedLength = 8;
	private JTextField range;
	private JTextField step;
	private JTextField spotRadius;
	private JTextField maxNbSpot;
	private JTextField timeInterval;
	private JFormattedTextField maxNumberSpotCh1Tf;
	private JFormattedTextField spotRadiusCh1Tf;
	private JFormattedTextField maxNumberSpotCh2Tf;
	private JFormattedTextField spotRadiusCh2Tf;
	private JFormattedTextField maxNumberSpotCh3Tf;
	private JFormattedTextField spotRadiusCh3Tf;
	private JCheckBox saveFlims;
	private Button okFluoAnaParamButton;
	private JComboBox channel1Combo;
	private JComboBox channel2Combo;
	private JComboBox channel3Combo;

	// TODO
	String channelListWithNone[] = { "None", "GFP", "CFP", "TxRed", "DAPI" };
	String channelList[] = { "GFP", "CFP", "TxRed", "DAPI" };

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : parameters displayed in dialog
	 */
	public MaarsFluoAnalysisDialog(AllMaarsParameters parameters) {

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
		range = new JTextField(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE)
				.getAsString(), filedLength);
		fluoRangePanel.add(rangeTitle);
		fluoRangePanel.add(range);

		//

		JPanel fluoStepPanel = new JPanel(new GridLayout(1, 2));
		JLabel stepTitle = new JLabel("Step (micron) : ", SwingConstants.CENTER);
		step = new JTextField(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsString(),
				filedLength);
		fluoStepPanel.add(stepTitle);
		fluoStepPanel.add(step);

		//

		JPanel timeIntervalPanel = new JPanel(new GridLayout(1, 2));
		JLabel timeIntervalTitle = new JLabel("Time Interval (ms) : ",
				SwingConstants.CENTER);
		timeInterval = new JTextField(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.TIME_INTERVAL)
				.getAsString(), filedLength);
		timeIntervalPanel.add(timeIntervalTitle);
		timeIntervalPanel.add(timeInterval);

		//

		JPanel saveMoviesChkPanel = new JPanel(new GridLayout(1, 0));
		saveFlims = new JCheckBox("Save Movies", parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.SAVE_FLUORESCENT_MOVIES).getAsBoolean());
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

		JPanel channel1Panel = new JPanel(new GridLayout(1, 0));
		channel1Combo = new JComboBox(channelList);
		maxNumberSpotCh1Tf = new JFormattedTextField(Integer.class);
		maxNumberSpotCh1Tf.setValue(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS)
				.getAsJsonObject().get(AllMaarsParameters.GFP)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsInt());
		spotRadiusCh1Tf = new JFormattedTextField(Double.class);
		spotRadiusCh1Tf.setValue(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS)
				.getAsJsonObject().get(AllMaarsParameters.GFP)
				.getAsJsonObject()
				.get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());
		channel1Panel.add(channel1Combo);
		channel1Panel.add(maxNumberSpotCh1Tf);
		channel1Panel.add(spotRadiusCh1Tf);

		//

		JPanel channel2Panel = new JPanel(new GridLayout(1, 0));
		channel2Combo = new JComboBox(channelListWithNone);
		channel2Combo.setSelectedItem(AllMaarsParameters.CFP);
		channel2Combo.addActionListener(this);
		maxNumberSpotCh2Tf = new JFormattedTextField(Integer.class);
		maxNumberSpotCh2Tf.setValue(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS)
				.getAsJsonObject().get(AllMaarsParameters.CFP)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsInt());
		spotRadiusCh2Tf = new JFormattedTextField(Double.class);
		spotRadiusCh2Tf.setValue(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS)
				.getAsJsonObject().get(AllMaarsParameters.CFP)
				.getAsJsonObject()
				.get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());
		maxNumberSpotCh2Tf.setEditable(false);
		spotRadiusCh2Tf.setEditable(false);
		channel2Panel.add(channel2Combo);
		channel2Panel.add(maxNumberSpotCh2Tf);
		channel2Panel.add(spotRadiusCh2Tf);

		//

		JPanel channel3Panel = new JPanel(new GridLayout(1, 0));
		channel3Combo = new JComboBox(channelListWithNone);
		channel3Combo.setSelectedItem(AllMaarsParameters.DAPI);
		channel3Combo.addActionListener(this);
		maxNumberSpotCh3Tf = new JFormattedTextField(Integer.class);
		maxNumberSpotCh3Tf.setValue(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS)
				.getAsJsonObject().get(AllMaarsParameters.DAPI)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsInt());
		spotRadiusCh3Tf = new JFormattedTextField(Double.class);
		spotRadiusCh3Tf.setValue(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS)
				.getAsJsonObject().get(AllMaarsParameters.DAPI)
				.getAsJsonObject()
				.get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());
		channel3Combo.setEnabled(false);
		maxNumberSpotCh3Tf.setEditable(false);
		spotRadiusCh3Tf.setEditable(false);
		channel3Panel.add(channel3Combo);
		channel3Panel.add(maxNumberSpotCh3Tf);
		channel3Panel.add(spotRadiusCh3Tf);

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
	public AllMaarsParameters getParameters() {
		return parameters;
	}

	/**
	 * Method be called every time combobox changed value.
	 * Update
	 * 
	 * @param channel
	 */
	 public void updateFluoChParameters(String channel){
		channel
		 parameters.getParametersAsJsonObject()
			.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
			.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS)
			.getAsJsonObject().get(AllMaarsParameters.DAPI)
			.getAsJsonObject()
			.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsInt()
	
	 }

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == okFluoAnaParamButton) {
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.RANGE_SIZE_FOR_MOVIE, range.getText());
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.STEP, step.getText());
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.SAVE_FLUORESCENT_MOVIES,
					String.valueOf(saveFlims.isSelected()));
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.SPOT_RADIUS, spotRadius.getText());
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT,
					maxNbSpot.getText());
			AllMaarsParameters.updateFluoParameter(parameters,
					AllMaarsParameters.TIME_INTERVAL, timeInterval.getText());
			// ArrayList<JComboBox>
			// for ()
			this.dispose();
		} else if (src == channel2Combo) {
			if (channel2Combo.getSelectedItem() != "None") {
				maxNumberSpotCh2Tf.setEditable(true);
				spotRadiusCh2Tf.setEditable(true);
				channel3Combo.setEnabled(true);
			} else if (channel2Combo.getSelectedItem() == "None") {
				maxNumberSpotCh2Tf.setEditable(false);
				spotRadiusCh2Tf.setEditable(false);
				channel3Combo.setSelectedIndex(0);
				channel3Combo.setEnabled(false);
				maxNumberSpotCh3Tf.setEditable(false);
				spotRadiusCh3Tf.setEditable(false);
			}
		} else if (src == channel3Combo) {
			if (channel3Combo.getSelectedItem() == "None") {
				channel3Combo.setSelectedIndex(0);
				channel3Combo.setEnabled(false);
				maxNumberSpotCh3Tf.setEditable(false);
				spotRadiusCh3Tf.setEditable(false);
			} else if (channel3Combo.getSelectedItem() != "None") {
				maxNumberSpotCh3Tf.setEditable(true);
				spotRadiusCh3Tf.setEditable(true);
			}
		}
	}
}
