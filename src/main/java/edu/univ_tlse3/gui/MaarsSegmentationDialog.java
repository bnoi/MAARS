package edu.univ_tlse3.gui;

import edu.univ_tlse3.acquisition.AcqLauncher;
import edu.univ_tlse3.acquisition.SegAcqSetting;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.maars.MaarsSegmentation;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.ImgUtils;
import ij.IJ;
import ij.ImagePlus;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.*;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

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
   private MaarsParameters parameters;
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
    * @param parameters : default parameters (which are going to be displayed)
    */
   MaarsSegmentationDialog(final MaarsParameters parameters, final MMStudio mm_) {

      this.parameters = parameters;
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//      this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
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

      JButton testSegBut = new JButton("test");
      testSegBut.addActionListener(new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            updateMAARSParamters();
            String imgPath = parameters.getSavingPath() + File.separator + "X0_Y0" + File.separator
                    + "_1" + File.separator  + "_1_MMStack_Pos0.ome.tif";
            ReportingUtils.logMessage(imgPath);
            if (FileUtils.exists(imgPath)) {
               ImagePlus segImg = IJ.openImage(imgPath);
               MaarsSegmentation ms = new MaarsSegmentation(parameters);
               ms.segmentation(segImg);
            } else {
               SegAcqSetting segAcq = new SegAcqSetting(parameters);
               ArrayList<ChannelSpec> channelSpecs = segAcq.configChannels();
               SequenceSettings acqSettings = segAcq.configAcqSettings(channelSpecs);
               acqSettings.save = false;
               AcquisitionWrapperEngine acqEng = segAcq.buildSegAcqEngine(acqSettings, mm_);
               java.util.List<Image> imageList = AcqLauncher.acquire(acqEng);
               ImagePlus segImg = ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);

               // --------------------------segmentation-----------------------------//
               MaarsSegmentation ms = new MaarsSegmentation(parameters);
               ms.segmentation(segImg);
            }
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

   public void updateMAARSParamters(){
      parameters.setSegmentationParameter(
              MaarsParameters.RANGE_SIZE_FOR_MOVIE, range.getText());
      parameters.setSegmentationParameter(MaarsParameters.STEP,
              step.getText());
      parameters.setSegmentationParameter(
              MaarsParameters.MINIMUM_CELL_AREA, minCellArea.getText());
      parameters.setSegmentationParameter(
              MaarsParameters.MAXIMUM_CELL_AREA, maxCellArea.getText());
      parameters.setSegmentationParameter(
              MaarsParameters.FILTER_MEAN_GREY_VALUE,
              String.valueOf(greyValueFilter.isSelected()));
      parameters.setSegmentationParameter(
              MaarsParameters.MEAN_GREY_VALUE, greyValue.getText());
      parameters.setSegmentationParameter(
              MaarsParameters.FILTER_SOLIDITY,
              String.valueOf(shapeFilter.isSelected()));
      parameters.setSegmentationParameter(MaarsParameters.SOLIDITY,
              solidity.getText());
   }

   /**
    * @return parameters
    */
   public MaarsParameters getParameters() {
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