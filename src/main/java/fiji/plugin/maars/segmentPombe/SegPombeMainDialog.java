package fiji.plugin.maars.segmentPombe;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Label;
import java.awt.Panel;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import fiji.plugin.maars.segmentPombe.mainDialogActions.BrowseAction;
import fiji.plugin.maars.segmentPombe.mainDialogActions.CancelAction;
import fiji.plugin.maars.segmentPombe.mainDialogActions.CurrentImageAction;
import fiji.plugin.maars.segmentPombe.mainDialogActions.RunAction;
import ij.IJ;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;

/**
* @author Tong LI, mail: tongli.bioinfo@gmail.com
* @version Nov 4, 2015
*/
public class SegPombeMainDialog implements PlugIn{

	// Main window
	private NonBlockingGenericDialog mainWindow;

	// pombeSegmentor parameter object (with predefined values)
	SegPombeParameters defaultParameters = new SegPombeParameters();

	// Component allowing to receive parameters from user
	// to get sigma : typical cell size
	private JFormattedTextField typicalSizeTf;
	private JComboBox typicalSizeUnitCombo;

	// to change image scale (number of pixels)
	private Checkbox changeScaleCkb;
	private JFormattedTextField maxWidthTf;
	private JFormattedTextField maxHeightTf;
	private JComboBox maxWidthUnitCombo;
	private JComboBox maxHeightUnitCombo;

	// to filter particles during the analysis and detect only cells
	private JFormattedTextField minParticleSizeTf;
	private JFormattedTextField maxParticleSizeTf;
	private JComboBox minParticleSizeUnitCombo;
	private JComboBox maxParticleSizeUnitCombo;

	// to notify if the cells boundaries are white then black or black then
	// white
	private JComboBox blackOrWhiteCombo;

	// to filter abnormal cell shape
	private Checkbox filterAbnormalShapeCkb;
	private JFormattedTextField solidityTf;

	// to filter background using min gray value
	private Checkbox filterWithMeanGreyValueCkb;
	private JFormattedTextField meanGreyValueField;

	// to select what result should be displayed or saved
	private Checkbox showCorrelationImgCkb;
	private Checkbox showBinaryImgCkb;
	private Checkbox showDataFrameCkb;
	private Checkbox showFocusImageCkb;
	private Checkbox saveCorrelationImgCkb;
	private Checkbox saveBinaryImgCkb;
	private Checkbox saveDataFrameCkb;
	private Checkbox saveFocusImageCkb;
	private Checkbox saveRoiCkb;

	// To Give path of folder to save results
	private JTextField saveDirTf;

	// To allow the user to choose the zFocus
	private Checkbox manualZFocusCkb;
	private JFormattedTextField manualZFocusTf;

	// Allow to display the name of the file used in the algorithm
	// meaning file currently selected or file found with browser
	private JTextField imgNameTf;

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

		typicalSizeTf = new JFormattedTextField(Float.class);
		typicalSizeTf.setValue(defaultParameters.getSigma());
		
		// displays units of size
		typicalSizeUnitCombo = new JComboBox(unitList);
		typicalSizeUnitCombo.setSelectedIndex(SegPombeParameters.MICRONS);

		sizePanel.add(sizeLabel);
		sizePanel.add(typicalSizeTf);
		sizePanel.add(typicalSizeUnitCombo);

		// Allow to change image scale (change resolution)
		Panel changeScalePanel = new Panel();
		BoxLayout changeScaleBoxLayout = new BoxLayout(changeScalePanel,
				BoxLayout.X_AXIS);
		changeScalePanel.setLayout(changeScaleBoxLayout);

		changeScaleCkb = new Checkbox("Change image size",
				defaultParameters.changeScale());

		Label maxWidthLabel = new Label();
		maxWidthLabel.setText("maximum  width");
		Label maxHLabel = new Label();
		maxHLabel.setText("maximum height");
		maxWidthTf = new JFormattedTextField(int.class);
		maxWidthTf.setValue(defaultParameters.getMaxWidth());
		
		maxHeightTf = new JFormattedTextField(int.class);
		maxHeightTf.setValue(defaultParameters.getMaxHeight());
		
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

		changeScalePanel.add(changeScaleCkb);
		changeScalePanel.add(maxValuesPanel);

		// Filter abnormal forms
		Panel filterAbnormalShapePanel = new Panel();
		BoxLayout filterAbnormalShapeLayout = new BoxLayout(
				filterAbnormalShapePanel, BoxLayout.X_AXIS);
		filterAbnormalShapePanel.setLayout(filterAbnormalShapeLayout);
		filterAbnormalShapeCkb = new Checkbox(
				"Filter abnormal shape using solidity",
				defaultParameters.filterAbnormalShape());
		solidityTf = new JFormattedTextField(Float.class);
		solidityTf.setValue(defaultParameters.getSolidityThreshold());
		filterAbnormalShapePanel.add(filterAbnormalShapeCkb);
		filterAbnormalShapePanel.add(solidityTf);

		// Filter background using min gray value
		Panel filterWithMinGreyValuePanel = new Panel();
		BoxLayout filterWithMinGrayValueLayout = new BoxLayout(
				filterWithMinGreyValuePanel, BoxLayout.X_AXIS);
		filterWithMinGreyValuePanel.setLayout(filterWithMinGrayValueLayout);
		filterWithMeanGreyValueCkb = new Checkbox(
				"Filter background using mean grey value on correlation image",
				defaultParameters.filterAbnormalShape());
		meanGreyValueField = new JFormattedTextField(Float.class);
		meanGreyValueField.setValue(defaultParameters.getMeanGreyValueThreshold());
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
		minParticleSizeTf = new JFormattedTextField(Float.class);
		minParticleSizeTf.setValue(defaultParameters.getMinParticleSize());
		minParticleSizeUnitCombo = new JComboBox(unitList);
		minSizePanel.add(minSizeLabel);
		minSizePanel.add(minParticleSizeTf);
		minSizePanel.add(minParticleSizeUnitCombo);
		Panel maxSizePanel = new Panel();
		BoxLayout maxSizeLayout = new BoxLayout(maxSizePanel, BoxLayout.X_AXIS);
		maxSizePanel.setLayout(maxSizeLayout);

		Label maxSizeLabel = new Label("maximum");
		maxParticleSizeTf = new JFormattedTextField(Float.class);
		maxParticleSizeTf.setValue(defaultParameters.getMaxParticleSize());
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
		manualZFocusTf = new JFormattedTextField(int.class);
		manualZFocusTf.setValue(0);

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
		showFocusImageCkb = new Checkbox();
		showCorrelationImgCkb = new Checkbox();
		showBinaryImgCkb = new Checkbox();
		showDataFrameCkb = new Checkbox();
		saveFocusImageCkb = new Checkbox();
		saveRoiCkb = new Checkbox();
		saveCorrelationImgCkb = new Checkbox();
		saveBinaryImgCkb = new Checkbox();
		saveDataFrameCkb = new Checkbox();

		/*
		 * TODO : take care to organise default parameters
		 */
		showFocusImageCkb.setState(true);

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
		displayPanel.add(showFocusImageCkb);
		displayPanel.add(new Label("does it anyway"));
		displayPanel.add(showDataFrameCkb);
		displayPanel.add(showCorrelationImgCkb);
		displayPanel.add(showBinaryImgCkb);
		savePanel.setLayout(saveBoxLayout);
		savePanel.add(save);
		savePanel.add(new JSeparator());
		savePanel.add(saveFocusImageCkb);
		savePanel.add(saveRoiCkb);
		savePanel.add(saveDataFrameCkb);
		savePanel.add(saveCorrelationImgCkb);
		savePanel.add(saveBinaryImgCkb);

		labDispSavePanel.setLayout(labDispSaveLayout);
		labDispSavePanel.add(labelsPanel);
		labDispSavePanel.add(displayPanel);
		labDispSavePanel.add(savePanel);

		folderPathPanel.setLayout(folderPathLayout);
		Label pathFolderLabel = new Label("Path to save results :");
		saveDirTf = new JTextField();
		saveDirTf.setColumns(25);
		folderPathPanel.add(pathFolderLabel);
		folderPathPanel.add(saveDirTf);

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
		imgNameTf = new JTextField(20);
		imgNameTf.setEditable(false);
		
		browseButton = new Button("Browse");
		browseAction = new BrowseAction(this, defaultParameters);
		browseButton.addActionListener(browseAction);
		
		Panel currentImagePanel = new Panel();
		currentImageButton = new Button("Current Image");
		currentImageAction = new CurrentImageAction(this, defaultParameters);
		currentImageButton.addActionListener(currentImageAction);
		currentImagePanel.add(currentImageButton);

		fieldAndBrowsePanel.add(imgNameTf);
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
		if (imgNameTf.getText().isEmpty()) {
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
		return new RunAction(this);
	}

	/*
	 * Initialize Cancel action
	 */
	public CancelAction initCancelAction() {
		return new CancelAction(this);
	}

	// Setters

	public void setFileNameField(String name) {
		imgNameTf.setText(name);
	}

	public void setPathDirField(String pathFolder) {
		saveDirTf.setText(pathFolder);
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
		Color c = imgNameTf.getBackground();
		if (!c.equals(Color.WHITE)) {
			imgNameTf.setBackground(Color.WHITE);
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

	public JTextField getImgNameTf() {
		return imgNameTf;
	}

	public Checkbox getShowCorrelationImgCkb() {
		return showCorrelationImgCkb;
	}

	public Checkbox getShowBinaryImgCkb() {
		return showBinaryImgCkb;
	}

	public Checkbox getShowDataFrameCkb() {
		return showDataFrameCkb;
	}

	public Checkbox getShowFocusImageCkb() {
		return showFocusImageCkb;
	}

	public Checkbox getSaveFocusImageCkb() {
		return saveFocusImageCkb;
	}

	public Checkbox getSaveRoiCkb() {
		return saveRoiCkb;
	}

	public Checkbox getSaveCorrelationImgCkb() {
		return saveCorrelationImgCkb;
	}

	public Checkbox getSaveBinaryImgCkb() {
		return saveBinaryImgCkb;
	}

	public Checkbox getSaveDataFrameCkb() {
		return saveDataFrameCkb;
	}

	public JFormattedTextField getTypicalSizeTf() {
		return typicalSizeTf;
	}

	public JComboBox getTypicalSizeUnitCombo() {
		return typicalSizeUnitCombo;
	}

	public Checkbox getChangeScaleCkb() {
		return changeScaleCkb;
	}

	public JFormattedTextField getMaxWidthTf() {
		return maxWidthTf;
	}

	public JFormattedTextField getMaxHeightTf() {
		return maxHeightTf;
	}

	public JComboBox getMaxWidthUnitCombo() {
		return maxWidthUnitCombo;
	}

	public JComboBox getMaxHeightUnitCombo() {
		return maxHeightUnitCombo;
	}

	public Checkbox getFilterAbnormalShapeCkb() {
		return filterAbnormalShapeCkb;
	}

	public Checkbox getFilterWithMeanGreyValueCkb() {
		return filterWithMeanGreyValueCkb;
	}

	public JTextField getSaveDirTf() {
		return saveDirTf;
	}

	public JFormattedTextField getMinParticleSizeTf() {
		return minParticleSizeTf;
	}

	public JFormattedTextField getMaxParticleSizeTf() {
		return maxParticleSizeTf;
	}

	public JComboBox getMinParticleSizeUnitCombo() {
		return minParticleSizeUnitCombo;
	}

	public JFormattedTextField getManualZFocusTf() {
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

	public JFormattedTextField getSolidityTf() {
		return solidityTf;
	}

	public JFormattedTextField getMeanGreyValueField() {
		return meanGreyValueField;
	}

	/*
	 * Get the already opened image and duplicate it, for this reason all
	 * segmentation produced images named with <<DUP>>
	 */
	public void getAlreadryOpenedImage() {
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
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		setMainWindow();
		mainWindow.showDialog();
		
		/**
		 * IN CASE YOU JUST WANT TO RUN WITH A MACRO (you open the image then
		 * run this plugin) UN-COMMENT THIS and COMMENT mainWindow.showDialog();
		 * | V
		 */
		// showFocusImage.setState(false);
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
//	public static void main(String[] args) {
//        SegPombeMainDialog md = new SegPombeMainDialog();
//        md.setMainWindow();
//        md.showMainWindow();
//    }
}
