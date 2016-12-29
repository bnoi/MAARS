package edu.univ_tlse3.gui;

import edu.univ_tlse3.acquisition.AcqLauncher;
import edu.univ_tlse3.acquisition.SegAcqSetting;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.maars.MaarsSegmentation;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.ImgUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.YesNoCancelDialog;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Class to create and display a dialog to get parameters for the image
 * segmentation process
 *
 * @author Tong LI
 */
class MaarsSegmentationDialog extends JDialog implements ActionListener {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private MaarsParameters parameters_;
   private JCheckBox shapeFilter;
   private JTextField solidity;
   private JCheckBox greyValueFilter;
   private JTextField greyValue;
   private JTextField range;
   private JTextField step;
   private JTextField minCellArea;
   private JTextField maxCellArea;
   private Button okBut;
   private ExecutorService es_;
   private JComboBox<String> configurationCombo_;
   private JComboBox<String> bfChannelCombo_;

   /**
    *
    * @param maarsMainFrame
    * @param parameters       default parameters (which are going to be displayed)
    * @param mm_
    * @param es
    */
   MaarsSegmentationDialog(JFrame maarsMainFrame, final MaarsParameters parameters, final MMStudio mm_, ExecutorService es) {
      super(maarsMainFrame);
      es_ = es;
      parameters_ = parameters;
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      setModalityType(ModalityType.APPLICATION_MODAL);
      setTitle("MAARS - Segmentation parameters");
      setLayout(new GridLayout(0, 1));
      setBackground(Color.WHITE);
      setMinimumSize(new Dimension(400, 750));
      Color labelColor = Color.ORANGE;

      //

      JPanel skipChkBoxPanel = new JPanel();
      JCheckBox skipSegChBox = new JCheckBox();
      skipSegChBox.setSelected(Boolean.parseBoolean(parameters_.getSkipSegmentation()));
      skipSegChBox.setText("Skip segmentation");
      skipSegChBox.addActionListener(e -> {
         if (skipSegChBox.isSelected()){
            YesNoCancelDialog yesNoCancelDialog = new YesNoCancelDialog(null,
                    "Skip segmentation",
                    "Do you have ROI.zip and BF_Results.csv in all your folders ?");
            if (yesNoCancelDialog.yesPressed()){
               parameters_.setSkipSegmentation(true);
            }else{
               skipSegChBox.setSelected(false);
               parameters_.setSkipSegmentation(false);
            }
         }else{
            parameters_.setSkipSegmentation(false);
         }
      });
      skipChkBoxPanel.add(skipSegChBox, JPanel.CENTER_ALIGNMENT);
      this.add(skipChkBoxPanel);
      //

      Label segmMovieLabel = new Label("Movie parameters", Label.CENTER);
      segmMovieLabel.setBackground(labelColor);
      this.add(segmMovieLabel);

      //

      JPanel segRangePanel = new JPanel(new GridLayout(1, 2));
      JLabel rangeTitle = new JLabel("Range (micron) : ",
              SwingConstants.CENTER);
      int filedLength = 8;
      range = new JTextField(
              parameters
                      .getSegmentationParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE),
              filedLength);
      segRangePanel.add(rangeTitle);
      segRangePanel.add(range);
      this.add(segRangePanel);

      //

      JPanel segStepPanel = new JPanel(new GridLayout(1, 2));
      JLabel stepTitle = new JLabel("Step (micron) : ", SwingConstants.CENTER);
      step = new JTextField(
              parameters.getSegmentationParameter(MaarsParameters.STEP),
              filedLength);
      segStepPanel.add(stepTitle);
      segStepPanel.add(step);
      this.add(segStepPanel);

      //

      Label segmParemLabel = new Label("Segementation parameters",
              Label.CENTER);
      segmParemLabel.setBackground(labelColor);
      this.add(segmParemLabel);

      //

      JPanel minCellAreaPanel = new JPanel(new GridLayout(1, 2));
      JLabel minCellAreaTitle = new JLabel("Min cell Area (micron) : ",
              SwingConstants.CENTER);
      minCellArea = new JTextField(
              parameters
                      .getSegmentationParameter(MaarsParameters.MINIMUM_CELL_AREA),
              filedLength);
      minCellAreaPanel.add(minCellAreaTitle);
      minCellAreaPanel.add(minCellArea);
      this.add(minCellAreaPanel);

      //

      JPanel maxCellAreaPanel = new JPanel(new GridLayout(1, 2));
      JLabel maxCellAreaTitle = new JLabel("Max cell Area (micron) : ",
              SwingConstants.CENTER);

      maxCellArea = new JTextField(
              parameters
                      .getSegmentationParameter(MaarsParameters.MAXIMUM_CELL_AREA),
              filedLength);
      maxCellAreaPanel.add(maxCellAreaTitle);
      maxCellAreaPanel.add(maxCellArea);
      this.add(maxCellAreaPanel);

      //

      JPanel greyValueFilterCheckPanel = new JPanel();
      greyValueFilter = new JCheckBox(
              "Mean grey value background filter",
              Boolean.parseBoolean(parameters
                      .getSegmentationParameter(MaarsParameters.FILTER_MEAN_GREY_VALUE)));
      greyValueFilter.addActionListener(this);
      greyValueFilterCheckPanel.add(greyValueFilter);
      this.add(greyValueFilterCheckPanel);

      //

      JPanel greyValueFilterPanel = new JPanel(new GridLayout(1, 2));
      JLabel greyValueFilterTitle = new JLabel("Mean grey value : ",
              SwingConstants.CENTER);
      greyValue = new JTextField(
              parameters
                      .getSegmentationParameter(MaarsParameters.MEAN_GREY_VALUE),
              filedLength);
      greyValue.setEditable(greyValueFilter.isSelected());
      greyValueFilterPanel.add(greyValueFilterTitle);
      greyValueFilterPanel.add(greyValue);
      this.add(greyValueFilterPanel);

      //

      JPanel shapeCheckPanel = new JPanel();
      shapeFilter = new JCheckBox(
              "Filter unusual shape using solidity",
              Boolean.parseBoolean(parameters
                      .getSegmentationParameter(MaarsParameters.FILTER_SOLIDITY)));
      shapeFilter.addActionListener(this);
      shapeCheckPanel.add(shapeFilter);
      this.add(shapeCheckPanel);

      //

      JPanel shapePanel = new JPanel(new GridLayout(1, 2));
      JLabel solidityTitle = new JLabel("Solidity: ", SwingConstants.CENTER);
      solidity = new JTextField(
              parameters.getSegmentationParameter(MaarsParameters.SOLIDITY),
              filedLength);
      solidity.setEditable(shapeFilter.isSelected());
      shapePanel.add(solidityTitle);
      shapePanel.add(solidity);
      this.add(shapePanel);

      //

      JPanel bfChannelPanel = new JPanel();
      bfChannelPanel.setLayout(new BorderLayout(1,2));

      Label bfChannelLabel = new Label("Bright-field Channel ?", Label.CENTER);
      bfChannelLabel.setBackground(Color.lightGray);
      bfChannelCombo_ = new JComboBox<>();
      bfChannelPanel.add(bfChannelLabel, BorderLayout.NORTH);
      bfChannelPanel.add(bfChannelCombo_, BorderLayout.SOUTH);

      //

      JPanel configurationGroupPanel = new JPanel();
      configurationGroupPanel.setLayout(new BorderLayout(1,2));

      Label configurationGroupLabel = new Label("Configuration Group", Label.CENTER);
      configurationGroupLabel.setBackground(Color.lightGray);

      configurationCombo_ = new JComboBox<>(mm_.getCore().getAvailableConfigGroups().toArray());
      configurationCombo_.addActionListener(actionEvent -> {
         String selectedGroup= (String) configurationCombo_.getSelectedItem();
         parameters_.setChannelGroup(selectedGroup);
         bfChannelCombo_.removeAllItems();
         String[] newConfigs = mm_.getCore().getAvailableConfigs(selectedGroup).toArray();
         for (String s : newConfigs){
            bfChannelCombo_.addItem(s);
         }
      });
      configurationCombo_.setSelectedItem(parameters_.getChannelGroup());
      configurationGroupPanel.add(configurationGroupLabel, BorderLayout.NORTH);
      configurationGroupPanel.add(configurationCombo_, BorderLayout.SOUTH);


      //

      JPanel bfPanel = new JPanel();
      bfPanel.add(configurationGroupPanel);
      bfPanel.add(bfChannelPanel);
      this.add(bfPanel);
      //

      JButton testSegBut = new JButton("test segmentation");
      testSegBut.addActionListener(actionEvent -> {
         updateMAARSParamters();
         String segDir = parameters.getSavingPath() + File.separator + "X0_Y0";
         String imgPath =  segDir + File.separator + "_1" + File.separator  + "_1_MMStack_Pos0.ome.tif";
         MaarsParameters parameters_dup = parameters.duplicate();
         parameters_dup.setSavingPath(segDir);
         FileUtils.createFolder(segDir);
         if (FileUtils.exists(imgPath)) {
            ImagePlus segImg = IJ.openImage(imgPath);
            MaarsSegmentation ms = new MaarsSegmentation(parameters_dup, segImg);
            es_.submit(ms);
         } else {
            SegAcqSetting segAcq = new SegAcqSetting(parameters_dup);
            ArrayList<ChannelSpec> channelSpecs = segAcq.configChannels();
            SequenceSettings acqSettings = segAcq.configAcqSettings(channelSpecs);
            acqSettings.save = false;
            AcquisitionWrapperEngine acqEng = segAcq.buildSegAcqEngine(acqSettings, mm_);
            java.util.List<Image> imageList = AcqLauncher.acquire(acqEng);
            ImagePlus segImg = ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);

            // --------------------------segmentation-----------------------------//
            MaarsSegmentation ms = new MaarsSegmentation(parameters_dup, segImg);
            es_.submit(ms);
         }
      });
      this.add(testSegBut);

      //

      okBut = new Button("OK");
      okBut.addActionListener(this);
      this.add(okBut);


      // this.pack();
      this.setVisible(true);
   }

   private void updateMAARSParamters(){
      parameters_.setSegmentationParameter(
              MaarsParameters.RANGE_SIZE_FOR_MOVIE, range.getText());
      parameters_.setSegmentationParameter(MaarsParameters.STEP,
              step.getText());
      parameters_.setSegmentationParameter(
              MaarsParameters.MINIMUM_CELL_AREA, minCellArea.getText());
      parameters_.setSegmentationParameter(
              MaarsParameters.MAXIMUM_CELL_AREA, maxCellArea.getText());
      parameters_.setSegmentationParameter(
              MaarsParameters.FILTER_MEAN_GREY_VALUE,
              String.valueOf(greyValueFilter.isSelected()));
      parameters_.setSegmentationParameter(
              MaarsParameters.MEAN_GREY_VALUE, greyValue.getText());
      parameters_.setSegmentationParameter(
              MaarsParameters.FILTER_SOLIDITY,
              String.valueOf(shapeFilter.isSelected()));
      parameters_.setSegmentationParameter(MaarsParameters.SOLIDITY,
              solidity.getText());
      parameters_.setSegChannel((String) bfChannelCombo_.getSelectedItem());
   }

   /**
    * @return parameters
    */
   public MaarsParameters getParameters() {
      return parameters_;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == shapeFilter) {
         if (shapeFilter.isSelected()) {
            solidity.setEditable(true);
         } else {
            solidity.setEditable(false);
         }
      } else if (e.getSource() == greyValueFilter) {
         if (greyValueFilter.isSelected()) {
            greyValue.setEditable(true);
         } else {
            greyValue.setEditable(false);
         }
      } else if (e.getSource() == okBut) {
         updateMAARSParamters();
         this.setVisible(false);
      }
   }
}