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
import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;

public class CellsBoundaries implements PlugIn {

	// Main window
	private NonBlockingGenericDialog mainWindow;

	// ImageJ object
	IJ ij = new IJ();

	// Parameters of the algorithm
	private ImagePlus imageToAnalyze;
	private double sigma = 3;
	private boolean changeScale = true;
	private boolean filter = true;
	private int maxWidth = 1500;
	private int maxHeight = 1500;
	private double minParticleSize = 500;
	private double maxParticleSize = 40000;
	private double solidityThreshold = 0.84;
	private double meanGreyValueThreshold = -177660;

	// Component allowing to receive parameters from user
	// to get sigma : typical cell size
	private JTextField sizeField;
	private JComboBox sizeComboUnit;

	// to change image scale (number of pixels)
	private Checkbox scaleCkb;
	private JTextField maxWTextField;
	private JTextField maxHTextField;
	private JComboBox maxWComboUnit;
	private JComboBox maxHComboUnit;

	// to filter particles during the analysis and detect only cells
	private JTextField minParticleSizeField;
	private JTextField maxParticleSizeField;
	private JComboBox minParticleSizeComboUnit;
	private JComboBox maxParticleSizeComboUnit;

	// to notify if the cells boundaries are white then black or black then
	// white
	private JComboBox blackOrWhiteComboBox;

	// to filter unusual cell shape
	private Checkbox filterUnususalCkb;
	private JTextField solidityField;

	// to filter background using min gray value
	private Checkbox filterWithMeanGreyValueCkb;
	private JTextField meanGreyValueField;

	// to select what result should be displayed or saved
	private Checkbox displayCorrelationImg;
	private Checkbox displayBinaryImg;
	private Checkbox displayDataFrame;
	private Checkbox displayFocusImage;
	private Checkbox saveCorrelationImg;
	private Checkbox saveBinaryImg;
	private Checkbox saveDataFrame;
	private Checkbox saveFocusImage;
	private Checkbox saveRoi;

	// To Give path of folder to save results
	private JTextField pathDirField;

	// To allow the user to choose the zFocus
	private Checkbox preciseZFocusCheckbox;
	private JTextField preciseZFocusTextField;

	// Allow to display the name of the file used in the algorithm
	// meaning file currently selected or file found with browser
	private JTextField fileNameField;

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

	// Width and Height indexes used in resolution array
	private double[] scale;
	public static final int WIDTH = 0;
	public static final int HEIGHT = 1;
	public static final int DEPTH = 2;

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

		Label sizeLabel = new Label();
		sizeLabel.setText("Typical cell Z size");

		sizeField = new JTextField("" + sigma);
		sizeField.setColumns(5);
		sizeField.setName("sizeField");

		// displays units of size
		sizeComboUnit = new JComboBox(unitList);
		sizeComboUnit.setSelectedIndex(MICRONS);
		sizeComboUnit.setName("sizeComboUnit");

		sizePanel.add(sizeLabel);
		sizePanel.add(sizeField);
		sizePanel.add(sizeComboUnit);

		// Allow to change image scale (change resolution)
		Panel changeScalePanel = new Panel();
		BoxLayout changeScaleBoxLayout = new BoxLayout(changeScalePanel,
				BoxLayout.X_AXIS);
		changeScalePanel.setLayout(changeScaleBoxLayout);

		scaleCkb = new Checkbox("Change image size", changeScale);
		scaleCkb.setName("scaleCkb");
		Label maxWLabel = new Label();
		maxWLabel.setText("maximum  width");
		Label maxHLabel = new Label();
		maxHLabel.setText("maximum height");
		maxWTextField = new JTextField("" + maxWidth);
		maxWTextField.setColumns(5);
		maxWTextField.setName("maxWTextField");
		maxHTextField = new JTextField("" + maxHeight);
		maxHTextField.setColumns(5);
		maxHTextField.setName("maxHTextField");
		maxWComboUnit = new JComboBox(unitList);
		maxWComboUnit.setName("maxWComboUnit");
		maxHComboUnit = new JComboBox(unitList);
		maxHComboUnit.setName("maxHComboUnit");
		Panel maxWPanel = new Panel();
		Panel maxHPanel = new Panel();
		Panel maxValuesPanel = new Panel();
		BoxLayout maxWBox = new BoxLayout(maxWPanel, BoxLayout.X_AXIS);
		BoxLayout maxHBox = new BoxLayout(maxHPanel, BoxLayout.X_AXIS);
		BoxLayout maxValuesBox = new BoxLayout(maxValuesPanel, BoxLayout.Y_AXIS);
		maxWPanel.setLayout(maxWBox);
		maxWPanel.add(maxWLabel);
		maxWPanel.add(maxWTextField);
		maxWPanel.add(maxWComboUnit);
		maxHPanel.setLayout(maxHBox);
		maxHPanel.add(maxHLabel);
		maxHPanel.add(maxHTextField);
		maxHPanel.add(maxHComboUnit);
		maxValuesPanel.setLayout(maxValuesBox);
		maxValuesPanel.add(maxWPanel);
		maxValuesPanel.add(maxHPanel);

		changeScalePanel.add(scaleCkb);
		changeScalePanel.add(maxValuesPanel);

		// Filter abnormal forms
		Panel filterUnusualPanel = new Panel();
		BoxLayout filterUnusualLayout = new BoxLayout(filterUnusualPanel,
				BoxLayout.X_AXIS);
		filterUnusualPanel.setLayout(filterUnusualLayout);
		filterUnususalCkb = new Checkbox("Filter unusual shape using solidity",
				filter);
		filterUnususalCkb.setName("filterUnususalCkb");
		solidityField = new JTextField("" + solidityThreshold);
		solidityField.setColumns(5);

		filterUnusualPanel.add(filterUnususalCkb);
		filterUnusualPanel.add(solidityField);

		// Filter background using min gray value
		Panel filterWithMinGreyValuePanel = new Panel();
		BoxLayout filterWithMinGrayValueLayout = new BoxLayout(
				filterWithMinGreyValuePanel, BoxLayout.X_AXIS);
		filterWithMinGreyValuePanel.setLayout(filterWithMinGrayValueLayout);
		filterWithMeanGreyValueCkb = new Checkbox(
				"Filter background using mean grey value on correlation image",
				filter);
		meanGreyValueField = new JTextField("" + meanGreyValueThreshold);
		meanGreyValueField.setColumns(5);

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
		minParticleSizeField = new JTextField("" + minParticleSize);
		minParticleSizeField.setColumns(5);
		minParticleSizeField.setName("minParticleSizeField");
		minParticleSizeComboUnit = new JComboBox(unitList);
		minParticleSizeComboUnit.setName("minParticleSizeComboUnit");
		minSizePanel.add(minSizeLabel);
		minSizePanel.add(minParticleSizeField);
		minSizePanel.add(minParticleSizeComboUnit);

		Panel maxSizePanel = new Panel();
		BoxLayout maxSizeLayout = new BoxLayout(maxSizePanel, BoxLayout.X_AXIS);
		maxSizePanel.setLayout(maxSizeLayout);

		Label maxSizeLabel = new Label("maximum");
		maxParticleSizeField = new JTextField("" + maxParticleSize);
		maxParticleSizeField.setColumns(5);
		maxParticleSizeField.setName("maxParticleSizeField");
		maxParticleSizeComboUnit = new JComboBox(unitList);
		maxParticleSizeComboUnit.setName("maxParticleSizeComboUnit");
		maxSizePanel.add(maxSizeLabel);
		maxSizePanel.add(maxParticleSizeField);
		maxSizePanel.add(maxParticleSizeComboUnit);

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
		blackOrWhiteComboBox = new JComboBox(blackOrWhiteState);
		blackOrWhiteComboBox.setName("blackOrWhiteComboBox");

		blackOrWhitePanel.add(blackOrWhiteLabel);
		blackOrWhitePanel.add(blackOrWhiteComboBox);

		// Allow the user to choose the zFocus
		Panel preciseZFocusPanel = new Panel();

		preciseZFocusCheckbox = new Checkbox(
				"Precise the slice corresponding to focus (default is the middle one)");
		preciseZFocusCheckbox.setName("preciseZFocusCheckbox");
		preciseZFocusTextField = new JTextField(2);
		preciseZFocusTextField.setName("preciseZFocusTextField");

		preciseZFocusPanel.add(preciseZFocusCheckbox);
		preciseZFocusPanel.add(preciseZFocusTextField);

		prefPanel.add(sizePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(changeScalePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(filterUnusualPanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(filterWithMinGreyValuePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(setMinMaxPaticleSizePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(blackOrWhitePanel);
		prefPanel.add(new JSeparator());
		prefPanel.add(preciseZFocusPanel);

		Panel resultPanel = new Panel();
		resultPanel.setBackground(Color.WHITE);

		// Displays Result options
		displayFocusImage = new Checkbox();
		displayFocusImage.setName("displayFocusImage");

		displayCorrelationImg = new Checkbox();
		displayCorrelationImg.setName("displayCorrelationImg");
		displayBinaryImg = new Checkbox();
		displayBinaryImg.setName("displayBinaryImg");
		displayDataFrame = new Checkbox();
		displayDataFrame.setName("displayDataFrame");

		saveFocusImage = new Checkbox();
		saveFocusImage.setName("saveFocusImage");
		saveRoi = new Checkbox();
		saveRoi.setName("saveRoi");
		saveCorrelationImg = new Checkbox();
		saveCorrelationImg.setName("saveCorrelationImg");
		saveBinaryImg = new Checkbox();
		saveBinaryImg.setName("saveBinaryImg");
		saveDataFrame = new Checkbox();
		saveDataFrame.setName("saveDataFrame");

		/*
		 * TODO : take care to organise default parameters
		 */
		displayFocusImage.setState(true);

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
		displayPanel.add(displayFocusImage);
		displayPanel.add(new Label("does it anyway"));
		displayPanel.add(displayDataFrame);
		displayPanel.add(displayCorrelationImg);
		displayPanel.add(displayBinaryImg);
		savePanel.setLayout(saveBoxLayout);
		savePanel.add(save);
		savePanel.add(new JSeparator());
		savePanel.add(saveFocusImage);
		savePanel.add(saveRoi);
		savePanel.add(saveDataFrame);
		savePanel.add(saveCorrelationImg);
		savePanel.add(saveBinaryImg);

		labDispSavePanel.setLayout(labDispSaveLayout);
		labDispSavePanel.add(labelsPanel);
		labDispSavePanel.add(displayPanel);
		labDispSavePanel.add(savePanel);

		folderPathPanel.setLayout(folderPathLayout);
		Label pathFolderLabel = new Label("Path to save results :");
		pathDirField = new JTextField();
		pathDirField.setColumns(25);
		pathDirField.setName("pathDirField");
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
		fileNameField = new JTextField(20);
		fileNameField.setEditable(false);
		fileNameField.setName("fileNameField");
		browseButton = new Button("Browse");
		browseButton.setName("browseButton");
		browseAction = new BrowseAction(this);
		browseButton.addActionListener(browseAction);
		Panel currentImagePanel = new Panel();
		currentImageButton = new Button("Current Image");
		currentImageButton.setName("currentImageButton");
		currentImageAction = new CurrentImageAction(this);
		currentImageButton.addActionListener(currentImageAction);
		currentImagePanel.add(currentImageButton);

		fieldAndBrowsePanel.add(fileNameField);
		fieldAndBrowsePanel.add(browseButton);

		getPicturePanel.add(fieldAndBrowsePanel);
		getPicturePanel.add(currentImagePanel);

		Panel runCancelPanel = new Panel();
		runButton = new Button("run");
		runButton.setName("runButton");
		runAction = new RunAction(this);
		runButton.addActionListener(runAction);

		cancelButton = new Button("Cancel");
		cancelButton.setName("cancelButton");
		cancelAction = new CancelAction(this);
		cancelButton.addActionListener(cancelAction);

		runCancelPanel.add(runButton);
		runCancelPanel.add(cancelButton);

		Label infoLabel = new Label();
		if (fileNameField.getText().isEmpty()) {
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

	// Setters
	public void setImageToAnalyze(ImagePlus img) {
		imageToAnalyze = img;
	}

	public void setFileNameField(String name) {
		fileNameField.setText(name);
	}

	public void setSigma(int sigma) {
		this.sigma = sigma;
	}

	public void setChangeScale(boolean changeScale) {
		this.changeScale = changeScale;
	}

	public void setUnususalShape(boolean filter) {
		this.filter = filter;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public void setScale(double[] scale) {
		this.scale = scale;
	}

	public void setPathDirField(String pathFolder) {
		pathDirField.setText(pathFolder);
	}

	public void setMinParticleSizeField(String minPartSize) {
		minParticleSizeField.setText(minPartSize);
	}

	public void setMaxParticleSizeField(String maxPartSize) {
		maxParticleSizeField.setText(maxPartSize);
	}

	public void resetFileNameField() {
		Color c = fileNameField.getBackground();
		if (!c.equals(Color.WHITE)) {
			fileNameField.setBackground(Color.WHITE);
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

	// Getters
	public ImagePlus getImageToAnalyze() {
		return imageToAnalyze;
	}

	public JTextField getFileNameField() {
		return fileNameField;
	}

	public IJ getIj() {
		return ij;
	}

	public Checkbox getDisplayCorrelationImg() {
		return displayCorrelationImg;
	}

	public Checkbox getDisplayBinaryImg() {
		return displayBinaryImg;
	}

	public Checkbox getDisplayDataFrame() {
		return displayDataFrame;
	}

	public Checkbox getDisplayFocusImage() {
		return displayFocusImage;
	}

	public Checkbox getSaveFocusImage() {
		return saveFocusImage;
	}

	public Checkbox getSaveRoi() {
		return saveRoi;
	}

	public Checkbox getSaveCorrelationImg() {
		return saveCorrelationImg;
	}

	public Checkbox getSaveBinaryImg() {
		return saveBinaryImg;
	}

	public Checkbox getSaveDataFrame() {
		return saveDataFrame;
	}

	public JTextField getSizeField() {
		return sizeField;
	}

	public JComboBox getSizeComUnit() {
		return sizeComboUnit;
	}

	public Checkbox getScaleCkb() {
		return scaleCkb;
	}

	public JTextField getMaxWTextField() {
		return maxWTextField;
	}

	public JTextField getMaxHTextField() {
		return maxHTextField;
	}

	public JComboBox getMaxWComboUnit() {
		return maxWComboUnit;
	}

	public JComboBox getMaxHComboUnit() {
		return maxHComboUnit;
	}

	public Checkbox getFilterUnususalCkb() {
		return filterUnususalCkb;
	}

	public Checkbox getFilterWithMeanGreyValueCkb() {
		return filterWithMeanGreyValueCkb;
	}

	public void getAlreadryOpenedImage() {
		imageToAnalyze = IJ.getImage().duplicate();
		setPathDirField(IJ.getImage().getOriginalFileInfo().directory);
	}

	public double[] getScale() {
		return scale;
	}

	public JTextField getPathDirField() {
		return pathDirField;
	}

	public JTextField getMinParticleSizeField() {
		return minParticleSizeField;
	}

	public JTextField getMaxParticleSizeField() {
		return maxParticleSizeField;
	}

	public JComboBox getMinParticleSizeComboUnit() {
		return minParticleSizeComboUnit;
	}

	public JTextField getPreciseZFocusTextField() {
		return preciseZFocusTextField;
	}

	public Checkbox getPreciseZFocusCheckbox() {
		return preciseZFocusCheckbox;
	}

	public JComboBox getMaxParticleSizeComboUnit() {
		return maxParticleSizeComboUnit;
	}

	public int getDirection() {

		if (blackOrWhiteComboBox.getSelectedIndex() == 0) {
			return 1;
		} else {
			return -1;
		}
	}

	public JTextField getSolidityField() {
		return solidityField;
	}

	public JTextField getMeanGreyValueThresholdField() {
		return meanGreyValueField;
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
		// displayFocusImage.setState(false);
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
