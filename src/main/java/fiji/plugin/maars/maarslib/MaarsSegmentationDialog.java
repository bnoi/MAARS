package fiji.plugin.maars.maarslib;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Class to create and display a dialog to get parameters for the image
 * segmentation process
 * 
 * @author Tong LI
 *
 */
public class MaarsSegmentationDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AllMaarsParameters parameters;
	private int filedLength = 8;
	private JCheckBox shapeFilter;
	private JTextField solidity;
	private JCheckBox greyValueFilter;
	private JTextField greyValue;
	private JTextField range;
	private JTextField step;
	private JTextField minCellArea;
	private JTextField maxCellArea;
	private Button okBut;

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : default parameters (which are going to be displayed)
	 */
	public MaarsSegmentationDialog(AllMaarsParameters parameters) {

		this.parameters = parameters;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("MAARS - Segmentation parameters");
		this.setLayout(new GridLayout(0, 1));
		this.setBackground(Color.WHITE);
		this.setMinimumSize(new Dimension(300, 600));
		Color labelColor = Color.ORANGE;

		//

		Label segmMovieLabel = new Label("Movie parameters", Label.CENTER);
		segmMovieLabel.setBackground(labelColor);
		this.add(segmMovieLabel);

		//

		JPanel segRangePanel = new JPanel(new GridLayout(1, 2));
		JLabel rangeTitle = new JLabel("Range (micron) : ", SwingConstants.CENTER);
		range = new JTextField(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE).getAsString(), filedLength);
		segRangePanel.add(rangeTitle);
		segRangePanel.add(range);
		this.add(segRangePanel);

		//

		JPanel segStepPanel = new JPanel(new GridLayout(1, 2));
		JLabel stepTitle = new JLabel("Step (micron) : ", SwingConstants.CENTER);
		step = new JTextField(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsString(), filedLength);
		segStepPanel.add(stepTitle);
		segStepPanel.add(step);
		this.add(segStepPanel);

		//

		Label segmParemLabel = new Label("Segementation parameters", Label.CENTER);
		segmParemLabel.setBackground(labelColor);
		this.add(segmParemLabel);

		//

		JPanel minCellAreaPanel = new JPanel(new GridLayout(1, 2));
		JLabel minCellAreaTitle = new JLabel("Min cell Area (micron) : ", SwingConstants.CENTER);
		minCellArea = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.MINIMUM_CELL_AREA).getAsString(),
				filedLength);
		minCellAreaPanel.add(minCellAreaTitle);
		minCellAreaPanel.add(minCellArea);
		this.add(minCellAreaPanel);

		//

		JPanel maxCellAreaPanel = new JPanel(new GridLayout(1, 2));
		JLabel maxCellAreaTitle = new JLabel("Max cell Area (micron) : ", SwingConstants.CENTER);

		maxCellArea = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.MAXIMUM_CELL_AREA).getAsString(),
				filedLength);
		maxCellAreaPanel.add(maxCellAreaTitle);
		maxCellAreaPanel.add(maxCellArea);
		this.add(maxCellAreaPanel);

		//

		JPanel greyValueFilterCheckPanel = new JPanel();
		greyValueFilter = new JCheckBox("Mean grey value background filter",
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.FILTER_MEAN_GREY_VALUE).getAsBoolean());
		greyValueFilter.addActionListener(this);
		greyValueFilterCheckPanel.add(greyValueFilter);
		this.add(greyValueFilterCheckPanel);

		//

		JPanel greyValueFilterPanel = new JPanel(new GridLayout(1, 2));
		JLabel greyValueFilterTitle = new JLabel("Mean grey value : ", SwingConstants.CENTER);
		greyValue = new JTextField(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.MEAN_GREY_VALUE).getAsString(),
				filedLength);
		greyValue.setEditable(greyValueFilter.isSelected());
		greyValueFilterPanel.add(greyValueFilterTitle);
		greyValueFilterPanel.add(greyValue);
		this.add(greyValueFilterPanel);

		//

		JPanel shapeCheckPanel = new JPanel();
		shapeFilter = new JCheckBox("Filter unusual shape using solidity",
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.FILTER_SOLIDITY).getAsBoolean());
		shapeFilter.addActionListener(this);
		shapeCheckPanel.add(shapeFilter);
		this.add(shapeCheckPanel);

		//

		JPanel shapePanel = new JPanel(new GridLayout(1, 2));
		JLabel solidityTitle = new JLabel("Solidity: ", SwingConstants.CENTER);
		solidity = new JTextField(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SOLIDITY).getAsString(), filedLength);
		solidity.setEditable(shapeFilter.isSelected());
		shapePanel.add(solidityTitle);
		shapePanel.add(solidity);
		this.add(shapePanel);

		//

		okBut = new Button("OK");
		okBut.addActionListener(this);
		this.add(okBut);

		// this.pack();
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
		if (e.getSource() == shapeFilter) {
			if (shapeFilter.isSelected()) {
				solidity.setEditable(true);
			} else {
				solidity.setEditable(false);
			}
			;
		} else if (e.getSource() == greyValueFilter) {
			if (greyValueFilter.isSelected()) {
				greyValue.setEditable(true);
			} else {
				greyValue.setEditable(false);
			}
			;
		} else if (e.getSource() == okBut) {
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.RANGE_SIZE_FOR_MOVIE,
					range.getText());
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.STEP, step.getText());
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.MINIMUM_CELL_AREA,
					minCellArea.getText());
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.MAXIMUM_CELL_AREA,
					maxCellArea.getText());
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.FILTER_MEAN_GREY_VALUE,
					String.valueOf(greyValueFilter.isSelected()));
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.MEAN_GREY_VALUE,
					greyValue.getText());
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.FILTER_SOLIDITY,
					String.valueOf(shapeFilter.isSelected()));
			AllMaarsParameters.updateSegmentationParameter(parameters, AllMaarsParameters.SOLIDITY, solidity.getText());
			this.setVisible(false);
			;
		}

	}
}