package fiji.plugin.maars.maarslib;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Dialog;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
	private int count = 2;
	private JTextField range;
	private JTextField step;
	private JTextField fluoChannels;
	private JTextField spotRadius;
	private JTextField maxNbSpot;
	private JTextField timeInterval;
	private JTextField tfield;
	private String nameTField = "channel_";
	private JCheckBox saveFlims;
	private Button okFluoAnaParamButton;
	private Button addChannelButton;
	private Button delChannelButton;
	private JPanel channelNbSpotPanel;
	private JComboBox channel1;
	private JComboBox channel2;
	private JComboBox channel3;
	
	String channelList[] = { "GFP", "CFP","TxRed","DAPI"};
	

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : parameters displayed in dialog
	 */
	public MaarsFluoAnalysisDialog(AllMaarsParameters parameters) {
		this.parameters = parameters;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("MAARS - Fluorescent Analysis parameters");
		this.setBackground(Color.WHITE);
		this.setLayout(new GridLayout(0, 1));
		this.setMinimumSize(new Dimension(250, 500));
		Color labelColor = Color.ORANGE;

		//

		Label fluoMovieLabel = new Label("Movie parameters", Label.CENTER);
		fluoMovieLabel.setBackground(labelColor);
		this.add(fluoMovieLabel);

		//

		JPanel fluoRangePanel = new JPanel(new GridLayout(1, 2));
		JLabel rangeTitle = new JLabel("Range (micron) : ", SwingConstants.CENTER);
		range = new JTextField(parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE).getAsString(), filedLength);
		fluoRangePanel.add(rangeTitle);
		fluoRangePanel.add(range);
		this.add(fluoRangePanel);

		//

		JPanel fluoStepPanel = new JPanel(new GridLayout(1, 2));
		JLabel stepTitle = new JLabel("Step (micron) : ", SwingConstants.CENTER);
		step = new JTextField(parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsString(), filedLength);
		fluoStepPanel.add(stepTitle);
		fluoStepPanel.add(step);
		this.add(fluoStepPanel);

		//

		JPanel timeIntervalPanel = new JPanel(new GridLayout(1, 2));
		JLabel timeIntervalTitle = new JLabel("Time Interval (ms) : ", SwingConstants.CENTER);
		timeInterval = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.TIME_INTERVAL).getAsString(),
				filedLength);
		timeIntervalPanel.add(timeIntervalTitle);
		timeIntervalPanel.add(timeInterval);
		this.add(timeIntervalPanel);
		//

		JPanel saveMoviesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		saveFlims = new JCheckBox("Save Movies",
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.SAVE_FLUORESCENT_MOVIES).getAsBoolean());
		saveMoviesPanel.add(saveFlims);
		this.add(saveMoviesPanel);

		//

		Label fluoAnaParamLabel = new Label("Spot identification parameter(s)", SwingConstants.CENTER);
		fluoAnaParamLabel.setBackground(labelColor);
		this.add(fluoAnaParamLabel);

		//

		JPanel spotRadiusPanel = new JPanel(new GridLayout(1, 2));
		JLabel spotRadiusTitle = new JLabel("spot radius (micron) : ", SwingConstants.CENTER);
		spotRadius = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.SPOT_RADIUS).getAsString(),
				filedLength);
		spotRadiusPanel.add(spotRadiusTitle);
		spotRadiusPanel.add(spotRadius);
		this.add(spotRadiusPanel);

		//
		
		channelNbSpotPanel = new JPanel(new GridLayout(0,2));
		JLabel fluoChannelsTitle = new JLabel("Fluo-channel(s) used", SwingConstants.CENTER);
		JLabel maxNbSpotTitle = new JLabel("Max number of spot(s)", SwingConstants.CENTER);
		channelNbSpotPanel.add(fluoChannelsTitle);
		channelNbSpotPanel.add(maxNbSpotTitle);
		//
		
		channel1 = new JComboBox(channelList);
		fluoChannels = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.FLUO_CHANNELS).getAsString(),
				filedLength);
		channelNbSpotPanel.add(fluoChannels);
		
		//
		
		maxNbSpot = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsString(),
				filedLength);
		channelNbSpotPanel.add(maxNbSpot);
		
		//
		
		addChannelButton = new Button("Add channel");
		addChannelButton.addActionListener(this);
		channelNbSpotPanel.add(addChannelButton);
		
		//
		
		delChannelButton = new Button("Delete channel");
		delChannelButton.addActionListener(this);
		channelNbSpotPanel.add(delChannelButton);
		
		//
		
		this.add(channelNbSpotPanel);
		
		//

		okFluoAnaParamButton = new Button("OK");
		okFluoAnaParamButton.addActionListener(this);

		this.add(okFluoAnaParamButton);

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

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == okFluoAnaParamButton) {
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.RANGE_SIZE_FOR_MOVIE,
					range.getText());
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.STEP, step.getText());
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.FLUO_CHANNELS,
					fluoChannels.getText());
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.SAVE_FLUORESCENT_MOVIES,
					String.valueOf(saveFlims.isSelected()));
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.SPOT_RADIUS, spotRadius.getText());
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT,
					maxNbSpot.getText());
			AllMaarsParameters.updateFluoParameter(parameters, AllMaarsParameters.TIME_INTERVAL,
					timeInterval.getText());
			this.setVisible(false);
		}else if(src == addChannelButton){
			channelNbSpotPanel.remove(addChannelButton);
			channelNbSpotPanel.remove(delChannelButton);
			for (int i = 0;i<2; i++){
				tfield = new JTextField();
	            tfield.setName(nameTField + count);
	            channelNbSpotPanel.add(tfield);
	            count++;
			}
			channelNbSpotPanel.add(addChannelButton);
			channelNbSpotPanel.add(delChannelButton);
			if(count >=6 ){
				addChannelButton.setEnabled(false);
			}
			if (count > 2){
				delChannelButton.setEnabled(true);
			}
            channelNbSpotPanel.revalidate();  // For JDK 1.7 or above.
            //frame.getContentPane().revalidate(); // For JDK 1.6 or below.
            channelNbSpotPanel.repaint();
		}else if(src == delChannelButton){
			channelNbSpotPanel.remove(addChannelButton);
			channelNbSpotPanel.remove(delChannelButton);
			for (int i = 0;i<2; i++){
				channelNbSpotPanel.remove(count);
	            count--;
			}
			channelNbSpotPanel.add(addChannelButton);
			channelNbSpotPanel.add(delChannelButton);
			if(count < 6 ){
				addChannelButton.setEnabled(true);
			}
			if (count <= 2){
				delChannelButton.setEnabled(false);
			}
            channelNbSpotPanel.revalidate();  // For JDK 1.7 or above.
            //frame.getContentPane().revalidate(); // For JDK 1.6 or below.
            channelNbSpotPanel.repaint();
		}

	}
}
