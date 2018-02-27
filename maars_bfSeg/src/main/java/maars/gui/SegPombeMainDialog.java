package maars.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import maars.main.MaarsSegmentation;
import maars.segmentPombe.SegPombeParameters;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Marie & Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class SegPombeMainDialog implements PlugInFilter {

   private SegPombeParameters parameters_ = new SegPombeParameters();
   private Boolean unitsChecked_ = false;

   // pombeSegmentor parameter object (with predefined values)
   private SegPombeParameters defaultParameters = new SegPombeParameters();

   // Component allowing to receive parameters from user
   // to get sigma : typical cell size
   private JFormattedTextField typicalSizeTf;
   private JComboBox<String> typicalSizeUnitCombo;

   // to change image scale (number of pixels)
//   private Checkbox changeScaleCkb;
//   private JFormattedTextField maxWidthTf;
//   private JFormattedTextField maxHeightTf;
//   private JComboBox<String> maxWidthUnitCombo;
//   private JComboBox<String> maxHeightUnitCombo;

   // to filter particles during the analysis and detect only cells
   private JFormattedTextField minParticleSizeTf;
   private JFormattedTextField maxParticleSizeTf;
   private JComboBox<String> minParticleSizeUnitCombo;
   private JComboBox<String> maxParticleSizeUnitCombo;

   // to notify if the cells boundaries are white then black or black then
   // white
   private JComboBox<String> blackOrWhiteCombo;

   // to filter abnormal cell shape
   private Checkbox filterAbnormalShapeCkb;
   private JFormattedTextField solidityTf;

   // to filter background using min gray value
   private Checkbox filterWithMeanGreyValueCkb;
   private JFormattedTextField meanGreyValueField;

   // to select what result should be displayed or saved
   private Checkbox showIntegratedImgCkb;
   private Checkbox showBinaryImgCkb;
   private Checkbox showDataFrameCkb;
   private Checkbox showFocusImageCkb;
   private Checkbox saveIntegratedImgCkb;
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

   // Units available from comboBoxes
   private String unitList[] = {"pixels", "microns"};

   /*
    * Create the main window in which there is : - a panel to handle the image
    * to Process and run the plugin - a panel to set(???)
    */
   private void showDialog() {
      GenericDialog gd = new GenericDialog("MAARS : bright-field segmentation");
      gd.setMinimumSize(new Dimension(300, 200));
      gd.setSize(750, 500);
      gd.setModalityType(Dialog.ModalityType.MODELESS);
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
      typicalSizeUnitCombo = new JComboBox<>(unitList);
      typicalSizeUnitCombo.setSelectedIndex(SegPombeParameters.MICRONS);

      sizePanel.add(sizeLabel);
      sizePanel.add(typicalSizeTf);
      sizePanel.add(typicalSizeUnitCombo);

      // Allow to change image scale (change resolution)
//      Panel changeScalePanel = new Panel();
//      BoxLayout changeScaleBoxLayout = new BoxLayout(changeScalePanel,
//            BoxLayout.X_AXIS);
//      changeScalePanel.setLayout(changeScaleBoxLayout);

//      changeScaleCkb = new Checkbox("Change image size",
//            defaultParameters.changeScale());
//
//      Label maxWidthLabel = new Label();
//      maxWidthLabel.setText("maximum  width");
//      Label maxHLabel = new Label();
//      maxHLabel.setText("maximum height");
//      maxWidthTf = new JFormattedTextField(int.class);
//      maxWidthTf.setValue(defaultParameters.getMaxWidth());
//
//      maxHeightTf = new JFormattedTextField(int.class);
//      maxHeightTf.setValue(defaultParameters.getMaxHeight());
//
//      maxWidthUnitCombo = new JComboBox<>(unitList);
//      maxHeightUnitCombo = new JComboBox<>(unitList);

      Panel maxValuesPanel = new Panel();
      Panel maxWidthPanel = new Panel();
      Panel maxHeightPanel = new Panel();

      BoxLayout maxWidthBoxLayout = new BoxLayout(maxWidthPanel,
            BoxLayout.X_AXIS);
      BoxLayout maxHeightBoxLayout = new BoxLayout(maxHeightPanel,
            BoxLayout.X_AXIS);
      BoxLayout maxValuesBoxLayout = new BoxLayout(maxValuesPanel,
            BoxLayout.Y_AXIS);
//      maxWidthPanel.setLayout(maxWidthBoxLayout);
//      maxWidthPanel.add(maxWidthLabel);
//      maxWidthPanel.add(maxWidthTf);
//      maxWidthPanel.add(maxWidthUnitCombo);
      maxHeightPanel.setLayout(maxHeightBoxLayout);
//      maxHeightPanel.add(maxHLabel);
//      maxHeightPanel.add(maxHeightTf);
//      maxHeightPanel.add(maxHeightUnitCombo);
      maxValuesPanel.setLayout(maxValuesBoxLayout);
      maxValuesPanel.add(maxWidthPanel);
      maxValuesPanel.add(maxHeightPanel);

//      changeScalePanel.add(changeScaleCkb);
//      changeScalePanel.add(maxValuesPanel);

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
            "Filter background using mean grey value on Integrated image",
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
      minParticleSizeUnitCombo = new JComboBox<>(unitList);
      minSizePanel.add(minSizeLabel);
      minSizePanel.add(minParticleSizeTf);
      minSizePanel.add(minParticleSizeUnitCombo);
      Panel maxSizePanel = new Panel();
      BoxLayout maxSizeLayout = new BoxLayout(maxSizePanel, BoxLayout.X_AXIS);
      maxSizePanel.setLayout(maxSizeLayout);

      Label maxSizeLabel = new Label("maximum");
      maxParticleSizeTf = new JFormattedTextField(Float.class);
      maxParticleSizeTf.setValue(defaultParameters.getMaxParticleSize());
      maxParticleSizeUnitCombo = new JComboBox<>(unitList);
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

      String blackOrWhiteState[] = {
            "First slice white - Last slice black",
            "First slice black - Last slice white"};
      blackOrWhiteCombo = new JComboBox<>(blackOrWhiteState);

      blackOrWhitePanel.add(blackOrWhiteLabel);
      blackOrWhitePanel.add(blackOrWhiteCombo);

      // Allow the user to choose the zFocus
      Panel manualZFocusPanel = new Panel();

      manualZFocusCkb = new Checkbox(
            "Z focus slice number (default is the middle one)");
      manualZFocusTf = new JFormattedTextField(int.class);
      manualZFocusTf.setValue(defaultParameters.getFocusSlide());

      manualZFocusPanel.add(manualZFocusCkb);
      manualZFocusPanel.add(manualZFocusTf);

      prefPanel.add(sizePanel);
//      prefPanel.add(new JSeparator());
//      prefPanel.add(changeScalePanel);
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
      showIntegratedImgCkb = new Checkbox();
      showBinaryImgCkb = new Checkbox();
      showDataFrameCkb = new Checkbox();
      saveFocusImageCkb = new Checkbox();
      saveRoiCkb = new Checkbox();
      saveIntegratedImgCkb = new Checkbox();
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
      Label corrImg = new Label("Integrated Image");
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
      displayPanel.add(showIntegratedImgCkb);
      displayPanel.add(showBinaryImgCkb);
      savePanel.setLayout(saveBoxLayout);
      savePanel.add(save);
      savePanel.add(new JSeparator());
      savePanel.add(saveFocusImageCkb);
      savePanel.add(saveRoiCkb);
      savePanel.add(saveDataFrameCkb);
      savePanel.add(saveIntegratedImgCkb);
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

      Panel currentImagePanel = new Panel();
      Button currentImageButton = new Button("Load");
      currentImageButton.addActionListener(o2->{
         parameters_.setImageToAnalyze(IJ.getImage());
         getAlreadryOpenedImage();
         resetFileNameField();
         setFileNameField(IJ.getImage().getTitle());
      });
      currentImagePanel.add(currentImageButton);

      fieldAndBrowsePanel.add(imgNameTf);

      getPicturePanel.add(fieldAndBrowsePanel);
      getPicturePanel.add(currentImagePanel);

      Panel runCancelPanel = new Panel();
      Button runButton = new Button("run");
      runButton.addActionListener(e -> {
         if (checkParameters()) {
            updateParameters();
            new Thread(new MaarsSegmentation(parameters_)).start();
         }
      });

      runCancelPanel.add(runButton);

      Label infoLabel = new Label();
      if (imgNameTf.getText().isEmpty()) {
         infoLabel.setText("Please open an image and click Load");
      }

      runPanel.add(infoLabel, BorderLayout.NORTH);
      runPanel.add(getPicturePanel, BorderLayout.CENTER);
      runPanel.add(runCancelPanel, BorderLayout.SOUTH);

      JTabbedPane jtp = new JTabbedPane();
      jtp.addTab("Run", runPanel);
      jtp.addTab("Preferences", prefPanel);
      jtp.addTab("Result options", resultPanel);

      gd.add(jtp);
      gd.pack();
      gd.showDialog();
   }

   // Setters

   private void setFileNameField(String name) {
      imgNameTf.setText(name);
   }

   private void setPathDirField(String pathFolder) {
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
   private void resetFileNameField() {
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

   private int getDirection() {

      if (blackOrWhiteCombo.getSelectedIndex() == 0) {
         return 1;
      } else {
         return -1;
      }
   }

   private JFormattedTextField getSolidityTf() {
      return solidityTf;
   }

   private JFormattedTextField getMeanGreyValueField() {
      return meanGreyValueField;
   }

   /*
    * Get the already opened image and duplicate it, for this reason all
    * segmentation produced images named with <<DUP>>
    */
   private void getAlreadryOpenedImage() {
      setPathDirField(IJ.getImage().getOriginalFileInfo().directory);
   }

   /*
    * Check validity of all text fields, this method is called only when using
    * segpombe plugin
    */
   private boolean checkParameters() {

      // check if saving path is valid
      IJ.log("Checking if saving path is valid");
      String savePath = saveDirTf.getText();
      if (!FileUtils.exists(savePath)) {
         IJ.error("Invalid saving path");
         saveDirTf.setBackground(Color.ORANGE);
         return false;
      }
      IJ.log("...OK!");

      // check if image path is valid
      IJ.log("Checking if path to image is valid");
      String pathToImg = savePath + imgNameTf.getText();
      if (!FileUtils.exists(pathToImg)) {
         IJ.error("Invalid movie path");
         imgNameTf.setBackground(Color.ORANGE);
         return false;
      }
      IJ.log("...OK!");

      // Check sigma value
      IJ.log("Checking if sigma value is valid");
      if ((Float) typicalSizeTf.getValue() <= 0) {
         IJ.error("Wrong parameter", "Sigma must be a positive not null value");
         return false;
      }
      IJ.log("...OK!");

//      // Check new image size value
//      IJ.log("Checking if new image size values are valid");
//      if ((Integer) maxHeightTf.getValue() <= 0) {
//         IJ.error("Wrong parameter", "Max height must be a positive not null value");
//         return false;
//      }
//      IJ.log("...OK!");
//      if ((Integer) maxWidthTf.getValue() <= 0) {
//         IJ.error("Wrong parameter", "Max width must be a positive not null value");
//         return false;
//      }
//      IJ.log("...OK!");

      // Check abnoraml cell shape value
      IJ.log("Checking if solidity value is valid");
      if ((Double) getSolidityTf().getValue() <= 0 || (Double) getSolidityTf().getValue() > 1) {
         IJ.error("Wrong parameter", "Solidity must be between 0 and 1");
         return false;
      }
      IJ.log("...OK!");

      // Check minimum cell area
      IJ.log("Checking if minimum particle size is valid");
      if ((Double) minParticleSizeTf.getValue() <= 0) {
         IJ.error("Wrong parameter", "The minimum area must be a positive not null value");
         return false;
      }
      IJ.log("...OK!");

      // Check maximum cell area
      IJ.log("Checking if maximum particle size is valid");
      if ((Double) maxParticleSizeTf.getValue() <= 0) {
         IJ.error("Wrong parameter", "The maximum area must be a positive not null value");
         return false;
      }
      IJ.log("...OK!");

      // Check z focus value
      IJ.log("Checking if z focus value is valid");
      if ((Integer) manualZFocusTf.getValue() <= 0) {
         IJ.error("Wrong parameter", "Focus slide must be a positive not null value");
         return false;
      }
      IJ.log("...OK!");

      // Check if none of the result ckeckBox is selected : in this case,
      // the user would not get any result
      IJ.log("Checking if none of the result ckeckBox is selected");
      boolean thereIsAResult = checkResultOptions();
      if (!thereIsAResult) {
         IJ.error("No result possible", "You have not selected a way to see your results");
         return false;
      }
      IJ.log("...OK!");

      return true;
   }

   /*
     * Get all user chose parameters
	 */

   private void updateParameters() {
      int selectedIndex;
      double tmpDouble;

      parameters_.setSavingPath(saveDirTf.getText());
      parameters_.setDirection(getDirection());
//      parameters_.setChangeScale(changeScaleCkb.getState());
      parameters_.setFilterAbnormalShape(filterAbnormalShapeCkb.getState());
      parameters_.setFiltrateWithMeanGrayValue(filterWithMeanGreyValueCkb.getState());
      parameters_.setShowIntegratedImg(showIntegratedImgCkb.getState());
      parameters_.setShowBinaryImg(showBinaryImgCkb.getState());
      parameters_.setShowDataFrame(showDataFrameCkb.getState());
      parameters_.setShowFocusImage(showFocusImageCkb.getState());
      parameters_.setSaveRoi(saveRoiCkb.getState());
      parameters_.setSaveIntegratedImg(saveIntegratedImgCkb.getState());
      parameters_.setSaveDataFrame(saveDataFrameCkb.getState());
      parameters_.setSaveFocusImage(saveFocusImageCkb.getState());
      parameters_.setSaveBinaryImg(saveBinaryImgCkb.getState());

      ImagePlus imgToAnalysis = parameters_.getImageToAnalyze().duplicate();
      // if the unit chosen is a micron it must be converted
      IJ.log("Check if one of the unite used is micron");
      if (SegPombeParameters.MICRONS == typicalSizeUnitCombo.getSelectedIndex()
//            || SegPombeParameters.MICRONS == maxWidthUnitCombo.getSelectedIndex()
//            || SegPombeParameters.MICRONS == maxHeightUnitCombo.getSelectedIndex()
            || SegPombeParameters.MICRONS == minParticleSizeUnitCombo.getSelectedIndex()
            || SegPombeParameters.MICRONS == maxParticleSizeUnitCombo.getSelectedIndex()) {
         unitsChecked_ = ImgUtils.checkImgUnitsAndScale(imgToAnalysis, parameters_);
      }

      // Convert size into pixels
      selectedIndex = typicalSizeUnitCombo.getSelectedIndex();
      float tmpFloat = (Float) typicalSizeTf.getValue();
      if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked_) {
         parameters_.setSigma(ImgUtils.convertMicronToPixel(tmpFloat, SegPombeParameters.DEPTH, parameters_));
         IJ.log(
               "typical size is in micron, convert it in pixels : " + String.valueOf(parameters_.getSigma()));
      } else if (selectedIndex == SegPombeParameters.PIXELS) {
         parameters_.setSigma(tmpFloat);
      }

      parameters_.setSolidityThreshold((Double) getSolidityTf().getValue());

      // Covert minimum area if needed to
      selectedIndex = minParticleSizeUnitCombo.getSelectedIndex();
      tmpDouble = (Double) minParticleSizeTf.getValue();
      if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked_) {
         parameters_.setMinParticleSize(tmpDouble * ImgUtils.convertMicronToPixel(1, SegPombeParameters.WIDTH, parameters_)
               * ImgUtils.convertMicronToPixel(1, SegPombeParameters.HEIGHT, parameters_));
         IJ.log("Cell Area is in micron, convert it in pixels : "
               + String.valueOf(parameters_.getMinParticleSize()));
      } else {
         if (selectedIndex == SegPombeParameters.PIXELS) {
            parameters_.setMinParticleSize(tmpDouble);
         }
      }

      // Covert maximum area if needed to
      selectedIndex = maxParticleSizeUnitCombo.getSelectedIndex();
      tmpDouble = (Double) maxParticleSizeTf.getValue();
      if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked_) {
         parameters_.setMaxParticleSize(tmpDouble * ImgUtils.convertMicronToPixel(1, SegPombeParameters.WIDTH, parameters_)
               * ImgUtils.convertMicronToPixel(1, SegPombeParameters.HEIGHT, parameters_));
         IJ.log("Cell Area is in micron, convert it in pixels : "
               + String.valueOf(parameters_.getMaxParticleSize()));
      } else {
         if (selectedIndex == SegPombeParameters.PIXELS) {
            parameters_.setMaxParticleSize(tmpDouble);
         }
      }
//      // If the user chose to change the scale
//      IJ.log("Check if user wants to change scale");
//      if (changeScaleCkb.getState()) {
//         selectedIndex = maxWidthUnitCombo.getSelectedIndex();
//         tmpInt = (Integer) maxWidthTf.getValue();
//         if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked_) {
//            parameters_.setMaxWidth(ImgUtils.convertMicronToPixel(tmpInt, SegPombeParameters.WIDTH, parameters_));
//            IJ.log(
//                  "Width value is in micron, convert it in pixel : " + String.valueOf(parameters_.getMaxWidth()));
//         } else {
//            if (selectedIndex == SegPombeParameters.PIXELS) {
//               parameters_.setMaxWidth(tmpInt);
//            }
//         }
//
//         selectedIndex = maxHeightUnitCombo.getSelectedIndex();
//         tmpInt = (Integer) maxHeightTf.getValue();
//         if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked_) {
//
//            parameters_.setMaxHeight(ImgUtils.convertMicronToPixel(tmpInt, SegPombeParameters.HEIGHT, parameters_));
//            IJ.log("Height value is in micron, convert it in pixel : "
//                  + String.valueOf(parameters_.getMaxHeight()));
//         } else {
//            if (selectedIndex == SegPombeParameters.PIXELS) {
//               parameters_.setMaxHeight(tmpInt);
//            }
//         }
//         // Then we can change scale
//         IJ.log("Change scale");
//         ImgUtils.changeScale(imgToAnalysis,parameters_.getMaxWidth(), parameters_.getMaxHeight(), parameters_);
//      }

      IJ.log("Check if user wants to precise z focus");
      if (manualZFocusCkb.getState()) {
         parameters_.setFocusSlide((Integer) manualZFocusTf.getValue());

      } else {
         parameters_.setFocusSlide((imgToAnalysis.getNSlices() / 2) - 1);
      }

      IJ.log("Check if user want to filter background using mean gray value");
      if (filterWithMeanGreyValueCkb.getState()) {
         parameters_.setMeanGreyValueThreshold((Double) getMeanGreyValueField().getValue());
      } else {
         parameters_.setMeanGreyValueThreshold(0);
      }
   }

   /**
    * Method to get the state of all the result Option checkbox and return
    * false if none of them are selected
    */
   private boolean checkResultOptions() {

      return (showIntegratedImgCkb.getState()
            || showBinaryImgCkb.getState() || saveRoiCkb.getState()
            || showDataFrameCkb.getState() || saveIntegratedImgCkb.getState()
            || saveBinaryImgCkb.getState() || saveDataFrameCkb.getState()
            || showFocusImageCkb.getState() || saveFocusImageCkb.getState());
   }

//      /**
//       * IN CASE YOU JUST WANT TO RUN WITH A MACRO (you open the image then
//       * run this plugin) UN-COMMENT THIS and COMMENT mainWindow.showDialog();
//       * | V
//       */
      // showFocusImage.setState(false);
      // saveBinaryImg.setState(true);
      // saveIntegratedImg.setState(true);
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
	public static void main(String[] args) {
      new ImageJ();
      SegPombeMainDialog md = new SegPombeMainDialog();
      md.showDialog();
    }

   @Override
   public int setup(String s, ImagePlus imagePlus) {
      parameters_.setImageToAnalyze(imagePlus);
      return DOES_16;
   }

   @Override
   public void run(ImageProcessor imageProcessor) {
      showDialog();
   }
}

