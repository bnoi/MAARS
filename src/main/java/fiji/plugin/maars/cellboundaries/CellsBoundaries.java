package fiji.plugin.maars.cellboundaries;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Label;
import java.awt.Panel;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import ij.IJ;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;

public class CellsBoundaries implements PlugIn {

	// Main window
	private NonBlockingGenericDialog mainWindow;

	// pombeSegmentor parameter object
	CBParameters parameters = new CBParameters();

	// Component allowing to receive parameters from user
	// to get sigma : typical cell size
	private JTextField typicalSizeTf;
	private JComboBox typicalSizeUnitCombo;

	// to change image scale (number of pixels)
	private Checkbox willChangeScaleCkb;
	private JTextField maxWidthTf;
	private JTextField maxHeightTf;
	private JComboBox maxWidthUnitCombo;
	private JComboBox maxHeightUnitCombo;

	// to filter particles during the analysis and detect only cells
	private JTextField minParticleSizeTf;
	private JTextField maxParticleSizeTf;
	private JComboBox minParticleSizeUnitCombo;
	private JComboBox maxParticleSizeUnitCombo;

	// to notify if the cells boundaries are white then black or black then
	// white
	private JComboBox blackOrWhiteCombo;

	// to filter abnormal cell shape
	private Checkbox filterAbnormalShapeCkb;
	private JTextField solidityTf;

	// to filter background using min gray value
	private Checkbox filterWithMeanGreyValueCkb;
	private JTextField meanGreyValueField;

	// to select what result should be displayed or saved
	private Checkbox willShowCorrelationImgCkb;
	private Checkbox willShowBinaryImgCkb;
	private Checkbox willShowDataFrameCkb;
	private Checkbox willShowFocusImageCkb;
	private Checkbox willSaveCorrelationImgCkb;
	private Checkbox willSaveBinaryImgCkb;
	private Checkbox willSaveDataFrameCkb;
	private Checkbox willSaveFocusImageCkb;
	private Checkbox willSaveRoiCkb;

	// To Give path of folder to save results
	private JTextField pathDirField;

	// To allow the user to choose the zFocus
	private Checkbox manualZFocusCkb;
	private JTextField manualZFocusTf;

	// Allow to display the name of the file used in the algorithm
	// meaning file currently selected or file found with browser
	private JTextField fileNameTf;

	// Button of the mainWindow
	private Button browseButton;
	private Button currentImageButton;
	private Button runButton;
	private Button cancelButton;

	// Action associated with buttons
	private RunAction runAction;
	private BrowseAction browseAction;
	private CurrentImageAction currentImageAction;
	private CancelAction cancelAction;

	// Units available from comboBoxes
	String unitList[] = { "pixels", "microns" };
	public static final int PIXELS = 0;
	public static final int MICRONS = 1;

	/*
	 * Create the main window in which there is : - a panel to handle the image
	 * to Process and run the plugin - a panel to set(???)
	 */
	public void setMainWindow() {

		mainWindow = new NonBlockingGenericDialog("Cells Boundaries");

		Panel prefPanel = new Panel();
		prefPanel.setBackground(Color.WHITE);
		BoxLayout prefLayout = new BoxLayout(prefPanel, BoxLayout.Y_AXIS);
		prefPanel.setLayout(prefLayout);

		// Allows to change typical cell size
		Panel sizePanel = new Panel();

		Label sizeLabel = new Label("Typical cell Z size");

		typicalSizeTf = new JTextField(String.valueOf(parameters.getSigma()), 5);

		// displays units of size
		typicalSizeUnitCombo = new JComboBox(unitList);
		typicalSizeUnitCombo.setSelectedIndex(MICRONS);

		sizePanel.add(sizeLabel);
		sizePanel.add(typicalSizeTf);
		sizePanel.add(typicalSizeUnitCombo);

		// Allow to change image scale (change resolution)
		Panel changeScalePanel = new Panel();
		BoxLayout changeScaleBoxLayout = new BoxLayout(changeScalePanel,
				BoxLayout.X_AXIS);
		changeScalePanel.setLayout(changeScaleBoxLayout);

		willChangeScaleCkb = new Checkbox("Change image size",
				parameters.getChangeScale());

		Label maxWidthLabel = new Label();
		maxWidthLabel.setText("maximum  width");
		Label maxHLabel = new Label();
		maxHLabel.setText("maximum height");
		maxWidthTf = new JTextField(String.valueOf(parameters.getMaxWidth()), 5);
		maxHeightTf = new JTextField(String.valueOf(parameters.getMaxHeight()),
				5);
		maxWidthUnitCombo = new JComboBox(unitList);
		maxHeightUnitCombo = new JComboBox(unitList);

		Panel maxValuesPanel = new Panel();
		Panel maxWidthPanel = new Panel();
		Panel maxHeightPanel = new Panel();

		BoxLayout maxWidthBoxLayout = new BoxLayout(maxWidthPanel,
				BoxLayout.X_AXIS);
		BoxLayout maxHeightBoxLayout = new BoxLayout(maxHeightPanel,
				BoxLayout.X_AXIS);
		BoxLayout maxValuesBoxLayout = new BoxLayout(maxValuesPanel,
				BoxLayout.Y_AXIS);
		maxWidthPanel.setLayout(maxWidthBoxLayout);
		maxWidthPanel.add(maxWidthLabel);
		maxWidthPanel.add(maxWidthTf);
		maxWidthPanel.add(maxWidthUnitCombo);
		maxHeightPanel.setLayout(maxHeightBoxLayout);
		maxHeightPanel.add(maxHLabel);
		maxHeightPanel.add(maxHeightTf);
		maxHeightPanel.add(maxHeightUnitCombo);
		maxValuesPanel.setLayout(maxValuesBoxLayout);
		maxValuesPanel.add(maxWidthPanel);
		maxValuesPanel.add(maxHeightPanel);

		changeScalePanel.add(willChangeScaleCkb);
		changeScalePanel.add(maxValuesPanel);

		// Filter abnormal forms
		Panel filterAbnormalShapePanel = new Panel();
		BoxLayout filterAbnormalShapeLayout = new BoxLayout(
				filterAbnormalShapePanel, BoxLayout.X_AXIS);
		filterAbnormalShapePanel.setLayout(filterAbnormalShapeLayout);
		filterAbnormalShapeCkb = new Checkbox(
				"Filter abnormal shape using solidity",
				parameters.willFiltrateUnusualShape());
		solidityTf = new JTextField(String.valueOf(parameters
				.getSolidityThreshold()), 5);
		filterAbnormalShapePanel.add(filterAbnormalShapeCkb);
		filterAbnormalShapePanel.add(solidityTf);

		// Filter background using min gray value
		Panel filterWithMinGreyValuePanel = new Panel();
		BoxLayout filterWithMinGrayValueLayout = new BoxLayout(
				filterWithMinGreyValuePanel, BoxLayout.X_AXIS);
		filterWithMinGreyValuePanel.setLayout(filterWithMinGrayValueLayout);
		filterWithMeanGreyValueCkb = new Checkbox(
				"Filter background using mean grey value on correlation image",
				parameters.willFiltrateUnusualShape());
		meanGreyValueField = new JTextField(String.valueOf(parameters
				.getMeanGreyValueThreshold()), 5);
		filterWithMinGreyValuePanel.add(filterWithMeanGreyValueCkb);
		filterWithMinGreyValuePanel.add(meanGreyValueField);

		// Determine minimum cell area and maximum cell area allowed when
		// analysing particles
		Panel setMinMaxPaticleSizePanel = new Panel();
		BoxLayout setMinMaxPaticleSizeLayout = new BoxLayout(
				setMinMaxPaticleSizePanel, BoxLayout.X_AXIS);
		setMinMaxPaticleSizePanel.setLayout(setMinMaxPaticleSizeLayout);

		Label setMinMaxPaticleSizeLabel = new Label("Range for cell area");

		Panel minMaxPartSizePanel = new Panel();
		BoxLayout minMaxPartSizeLayout = new BoxLayout(minMaxPartSizePanel,
				BoxLayout.Y_AXIS);
		minMaxPartSizePanel.setLayout(minMaxPartSizeLayout);

		Panel minSizePanel = new Panel();
		BoxLayout minSizeLayout = new BoxLayout(minSizePanel, BoxLayout.X_AXIS);
		minSizePanel.setLayout(minSizeLayout);

		Label minSizeLabel = new Label("minimum ");
		minParticleSizeTf = new JTextField(String.valueOf(parameters
				.getMinParticleSize()), 5);
		minParticleSizeUnitCombo = new JComboBox(unitList);
		minSizePanel.add(minSizeLabel);
		minSizePanel.add(minParticleSizeTf);
		minSizePanel.add(minParticleSizeUnitCombo);
		Panel maxSizePanel = new Panel();
		BoxLayout maxSizeLayout = new BoxLayout(maxSizePanel, BoxLayout.X_AXIS);
		maxSizePanel.setLayout(maxSizeLayout);

		Label maxSizeLabel = new Label("maximum");
		maxParticleSizeTf = new JTextField(String.valueOf(parameters
				.getMaxParticleSize()), 5);
		maxParticleSizeUnitCombo = new JComboBox(unitList);
		maxSizePanel.add(maxSizeLabel);
		maxSizePanel.add(maxParticleSizeTf);
		maxSizePanel.add(maxParticleSizeUnitCombo);

		minMaxPartSizePanel.add(minSizePanel);
		minMaxPartSizePanel.add(maxSizePanel);

		setMinMaxPaticleSizePanel.add(setMinMaxPaticleSizeLabel);
		setMinMaxPaticleSizePanel.add(minMaxPartSizePanel);

		// notify if the cells boundaries are white then black or black then
		// white
		Panel blackOrWhitePanel = new Panel();
		BoxLayout blackOrWhiteLayout = new BoxLayout(blackOrWhitePanel,
				BoxLayout.Y_AXIS);
		blackOrWhitePanel.setLayout(blackOrWhiteLayout);

		Label blackOrWhiteLabel = new Label(
				"Evolution of cells boundries on the movie :");

		String blackOrWhiteState[] = { "First slice black - Last slice white",
				"First slice white - Last slice black" };
		blackOrWhiteCombo = new JComboBox(blackOrWhiteState);

		blackOrWhitePanel.add(blackOrWhiteLabel);
		blackOrWhitePanel.add(blackOrWhiteCombo);

		// Allow the user to choose the zFocus
		Panel manualZFocusPanel = new Panel();

		manualZFocusCkb = new Checkbox(
				"Precise the slice corresponding to focus (default is the middle one)");
		manualZFocusTf = new JTextField(2);

		manualZFocusPanel.add(manualZFocusCkb);
		manualZFocusPanel.add(manualZFocusTf);

		prefPanel.add(sizePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(changeScalePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(filterAbnormalShapePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(filterWithMinGreyValuePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(setMinMaxPaticleSizePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(blackOrWhitePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(manualZFocusPanel);

		Panel resultPanel = new Panel();
		resultPanel.setBackground(Color.WHITE);

		// Displays Result options
		willShowFocusImageCkb = new Checkbox();
		willShowCorrelationImgCkb = new Checkbox();
		willShowBinaryImgCkb = new Checkbox();
		willShowDataFrameCkb = new Checkbox();
		willSaveFocusImageCkb = new Checkbox();
		willSaveRoiCkb = new Checkbox();
		willSaveCorrelationImgCkb = new Checkbox();
		willSaveBinaryImgCkb = new Checkbox();
		willSaveDataFrameCkb = new Checkbox();

		/*
		 * TODO : take care to organise default parameters
		 */
		willShowFocusImageCkb.setState(true);

		Label display = new Label("Display");
		Label save = new Label("Save");
		Label results = new Label("Result types");

		Label focusImg = new Label("Focus Image");
		Label rois = new Label("ROIs");
		Label corrImg = new Label("Correlation Image");
		Label BinaryImg = new Label("Binary Image");
		Label dataFrame = new Label("Data Frame");

		Panel labelsPanel = new Panel();
		Panel displayPanel = new Panel();
		Panel savePanel = new Panel();

		Panel labDispSavePanel = new Panel();
		Panel folderPathPanel = new Panel();

		BoxLayout resultLayout = new BoxLayout(resultPanel, BoxLayout.Y_AXIS);
		BoxLayout labelsBoxLayout = new BoxLayout(labelsPanel, BoxLayout.Y_AXIS);
		BoxLayout displayBoxLayout = new BoxLayout(displayPanel,
				BoxLayout.Y_AXIS);
		BoxLayout saveBoxLayout = new BoxLayout(savePanel, BoxLayout.Y_AXIS);

		BoxLayout labDispSaveLayout = new BoxLayout(labDispSavePanel,
				BoxLayout.X_AXIS);
		BoxLayout folderPathLayout = new BoxLayout(folderPathPanel,
				BoxLayout.Y_AXIS);

		labelsPanel.setLayout(labelsBoxLayout);
		labelsPanel.add(results);
		labelsPanel.add(new JSeparator());
		labelsPanel.add(focusImg);
		labelsPanel.add(rois);
		labelsPanel.add(dataFrame);
		labelsPanel.add(corrImg);
		labelsPanel.add(BinaryImg);
		displayPanel.setLayout(displayBoxLayout);
		displayPanel.add(display);
		displayPanel.add(new JSeparator());
		displayPanel.add(willShowFocusImageCkb);
		displayPanel.add(new Label("does it anyway"));
		displayPanel.add(willShowDataFrameCkb);
		displayPanel.add(willShowCorrelationImgCkb);
		displayPanel.add(willShowBinaryImgCkb);
		savePanel.setLayout(saveBoxLayout);
		savePanel.add(save);
		savePanel.add(new JSeparator());
		savePanel.add(willSaveFocusImageCkb);
		savePanel.add(willSaveRoiCkb);
		savePanel.add(willSaveDataFrameCkb);
		savePanel.add(willSaveCorrelationImgCkb);
		savePanel.add(willSaveBinaryImgCkb);

		labDispSavePanel.setLayout(labDispSaveLayout);
		labDispSavePanel.add(labelsPanel);
		labDispSavePanel.add(displayPanel);
		labDispSavePanel.add(savePanel);

		folderPathPanel.setLayout(folderPathLayout);
		Label pathFolderLabel = new Label("Path to save results :");
		pathDirField = new JTextField();
		pathDirField.setColumns(25);
		folderPathPanel.add(pathFolderLabel);
		folderPathPanel.add(pathDirField);

		resultPanel.setLayout(resultLayout);
		resultPanel.add(labDispSavePanel);
		resultPanel.add(new JSeparator());
		resultPanel.add(folderPathPanel);

		// Panel where is shown name of the file which is going to be used for
		// algorithm
		// and where there are run and cancel button
		Panel runPanel = new Panel();
		runPanel.setBackground(Color.WHITE);
		BorderLayout runLayout = new BorderLayout();
		runPanel.setLayout(runLayout);

		Panel getPicturePanel = new Panel();
		BoxLayout getPicLayout = new BoxLayout(getPicturePanel,
				BoxLayout.Y_AXIS);
		getPicturePanel.setLayout(getPicLayout);

		Panel fieldAndBrowsePanel = new Panel();
		fileNameTf = new JTextField(20);
		fileNameTf.setEditable(false);
		
		browseButton = new Button("Browse");
		browseAction = new BrowseAction(this, parameters);
		browseButton.addActionListener(browseAction);
		
		Panel currentImagePanel = new Panel();
		currentImageButton = new Button("Current Image");
		currentImageAction = new CurrentImageAction(this, parameters);
		currentImageButton.addActionListener(currentImageAction);
		currentImagePanel.add(currentImageButton);

		fieldAndBrowsePanel.add(fileNameTf);
		fieldAndBrowsePanel.add(browseButton);

		getPicturePanel.add(fieldAndBrowsePanel);
		getPicturePanel.add(currentImagePanel);

		Panel runCancelPanel = new Panel();
		runButton = new Button("run");
		runAction = initRunAction();
		runButton.addActionListener(runAction);

		cancelButton = new Button("Cancel");
		cancelAction = initCancelAction();
		cancelButton.addActionListener(cancelAction);

		runCancelPanel.add(runButton);
		runCancelPanel.add(cancelButton);

		Label infoLabel = new Label();
		if (fileNameTf.getText().isEmpty()) {
			infoLabel.setText("Please select the file you want to analyse");
		}

		runPanel.add(infoLabel, BorderLayout.NORTH);
		runPanel.add(getPicturePanel, BorderLayout.CENTER);
		runPanel.add(runCancelPanel, BorderLayout.SOUTH);

		JTabbedPane jtp = new JTabbedPane();
		jtp.addTab("Run", runPanel);
		jtp.addTab("Preferences", prefPanel);
		jtp.addTab("Result options", resultPanel);

		mainWindow.add(jtp);
	}

	/*
	 * Initialize Run action
	 */
	public RunAction initRunAction() {
		return new RunAction(this, parameters);
	}

	/*
	 * Initialize Cancel action
	 */
	public CancelAction initCancelAction() {
		return new CancelAction(this);
	}

	// Setters

	public void setFileNameField(String name) {
		fileNameTf.setText(name);
	}

	public void setPathDirField(String pathFolder) {
		pathDirField.setText(pathFolder);
	}

	public void setMinParticleSizeField(String minPartSize) {
		minParticleSizeTf.setText(minPartSize);
	}

	public void setMaxParticleSizeField(String maxPartSize) {
		maxParticleSizeTf.setText(maxPartSize);
	}

	/*
	 * Refresh file name field if it's not white
	 */
	public void resetFileNameField() {
		Color c = fileNameTf.getBackground();
		if (!c.equals(Color.WHITE)) {
			fileNameTf.setBackground(Color.WHITE);
		}
	}

	/*
	 * Reset a panel color (which might have been changed in case of error
	 * message)
	 */
	public void resetPanelColor(Panel panel) {
		Color c = panel.getBackground();
		if (!c.equals(Color.WHITE)) {
			panel.setBackground(Color.WHITE);
		}
	}

	public JTextField getFileNameField() {
		return fileNameTf;
	}

	public Checkbox getWillShowCorrelationImgCkb() {
		return willShowCorrelationImgCkb;
	}

	public Checkbox getWillShowBinaryImgCkb() {
		return willShowBinaryImgCkb;
	}

	public Checkbox getWillShowDataFrameCkb() {
		return willShowDataFrameCkb;
	}

	public Checkbox getWillShowFocusImageCkb() {
		return willShowFocusImageCkb;
	}

	public Checkbox getWillSaveFocusImageCkb() {
		return willSaveFocusImageCkb;
	}

	public Checkbox getwillSaveRoiCkb() {
		return willSaveRoiCkb;
	}

	public Checkbox getWillSaveCorrelationImgCkb() {
		return willSaveCorrelationImgCkb;
	}

	public Checkbox getWillSaveBinaryImgCkb() {
		return willSaveBinaryImgCkb;
	}

	public Checkbox getWillSaveDataFrameCkb() {
		return willSaveDataFrameCkb;
	}

	public JTextField getTypicalSizeTf() {
		return typicalSizeTf;
	}

	public JComboBox getTypicalSizeUnitCombo() {
		return typicalSizeUnitCombo;
	}

	public Checkbox getWillChangeScaleCkb() {
		return willChangeScaleCkb;
	}

	public JTextField getMaxWTextField() {
		return maxWidthTf;
	}

	public JTextField getMaxHeightTf() {
		return maxHeightTf;
	}

	public JComboBox getmaxWidthUnitCombo() {
		return maxWidthUnitCombo;
	}

	public JComboBox getMaxHeightUnitCombo() {
		return maxHeightUnitCombo;
	}

	public Checkbox getFilterUnususalCkb() {
		return filterAbnormalShapeCkb;
	}

	public Checkbox getFilterWithMeanGreyValueCkb() {
		return filterWithMeanGreyValueCkb;
	}

	public JTextField getPathDirField() {
		return pathDirField;
	}

	public JTextField getMinParticleSizeTf() {
		return minParticleSizeTf;
	}

	public JTextField getMaxParticleSizeTf() {
		return maxParticleSizeTf;
	}

	public JComboBox getMinParticleSizeUnitCombo() {
		return minParticleSizeUnitCombo;
	}

	public JTextField getManualZFocusTf() {
		return manualZFocusTf;
	}

	public Checkbox getManualZFocusCkb() {
		return manualZFocusCkb;
	}

	public JComboBox getMaxParticleSizeUnitCombo() {
		return maxParticleSizeUnitCombo;
	}

	public int getDirection() {

		if (blackOrWhiteCombo.getSelectedIndex() == 0) {
			return 1;
		} else {
			return -1;
		}
	}

	public JTextField getSolidityTf() {
		return solidityTf;
	}

	public JTextField getMeanGreyValueField() {
		return meanGreyValueField;
	}

	/*
	 * Get the already opened image and duplicate it, for this reason all
	 * segmentation produced images named with <<DUP>>
	 */
	public void getAlreadryOpenedImage() {
		parameters.setImageToAnalyze(IJ.getImage().duplicate());
		setPathDirField(IJ.getImage().getOriginalFileInfo().directory);
	}

	public RunAction getRunAction() {
		return runAction;
	}

	public void showMainWindow() {
		mainWindow.showDialog();
	}

	public void hideMainWindow() {
		mainWindow.setVisible(false);
	}

	public void run(String arg) {
		setMainWindow();
		mainWindow.showDialog();

		/**
		 * IN CASE YOU JUST WANT TO RUN WITH A MACRO (you open the image then
		 * run this plugin) UN-COMMENT THIS and COMMENT mainWindow.showDialog();
		 * | V
		 */
		// willShowFocusImage.setState(false);
		// saveBinaryImg.setState(true);
		// saveCorrelationImg.setState(true);
		// saveDataFrame.setState(true);
		// saveFocusImage.setState(true);
		// saveRoi.setState(true);
		// filterUnususalCkb.setState(true);
		// filterWithMeanGrayValueCkb.setState(true);
		// getAlreadryOpenedImage();
		// runAction.checkUnitsAndScale();
		// runAction.changeScale(1500, 1500);
		// CellsBoundariesIdentification cBI = new
		// CellsBoundariesIdentification(this, 13, 200, 900000, -1, (int)
		// Math.round(imageToAnalyze.getSlice()/2),0.84 ,-177660);
		// cBI.identifyCellesBoundaries();

	}

}
