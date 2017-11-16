package maars.gui;

import ij.IJ;
import ij.gui.YesNoCancelDialog;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.main.Maars_Interface;
import maars.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Class to create and display a dialog to get parameters for the image
 * segmentation process
 *
 * @author Tong LI
 */
public class MaarsSegmentationDialog extends JDialog implements ActionListener {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private MaarsParameters parameters_;
   private JCheckBox shapeFilter;
   private JTextField solidity;
   private JCheckBox greyValueFilter;
   private JTextField greyValue;
   private JTextField minCellArea;
   private JTextField maxCellArea;
   private JCheckBox skipSegChBox;
   private JButton okBut;

   public MaarsSegmentationDialog(final MaarsParameters parameters, JFrame maarsMainFrame){
      super(maarsMainFrame);
      createSegmentationDialog(parameters);
   }
   /**
    * @param parameters     default parameters (which are going to be displayed)
    */
   public void createSegmentationDialog(final MaarsParameters parameters) {
      parameters_ = parameters;
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      setModalityType(ModalityType.DOCUMENT_MODAL);
      setTitle("MAARS - Segmentation parameters");
      setLayout(new GridLayout(0, 1));
      setMinimumSize(new Dimension(200, 200));

      //

      JPanel segmParemPanel = new JPanel(new GridLayout(1, 2));
      segmParemPanel.setBackground(GuiUtils.bgColor);
      segmParemPanel.setBorder(GuiUtils.addPanelTitle("Segmentation parameters"));
      add(segmParemPanel);

      //

      int filedLength = 8;
      JPanel minCellAreaPanel = new JPanel(new GridLayout(1, 1));
      minCellAreaPanel.setBackground(GuiUtils.bgColor);
      minCellAreaPanel.setBorder(GuiUtils.addSecondaryTitle("Min cell Area (micron) : "));
      minCellArea = new JTextField(
            parameters
                  .getSegmentationParameter(MaarsParameters.MINIMUM_CELL_AREA),
            filedLength);
      minCellAreaPanel.add(minCellArea);
      segmParemPanel.add(minCellAreaPanel);

      //

      JPanel maxCellAreaPanel = new JPanel(new GridLayout(1, 1));
      maxCellAreaPanel.setBackground(GuiUtils.bgColor);
      maxCellAreaPanel.setBorder(GuiUtils.addSecondaryTitle("Max cell Area (micron) : "));
      maxCellArea = new JTextField(
            parameters
                  .getSegmentationParameter(MaarsParameters.MAXIMUM_CELL_AREA),
            filedLength);
      maxCellAreaPanel.add(maxCellArea);
      segmParemPanel.add(maxCellAreaPanel);

      //

      JPanel greyValueFilterCheckPanel = new JPanel(new GridLayout(1, 2));
      greyValueFilterCheckPanel.setBackground(GuiUtils.bgColor);
      greyValueFilterCheckPanel.setBorder(GuiUtils.addPanelTitle("Mean grey value background filter"));
      add(greyValueFilterCheckPanel);

      greyValueFilter = new JCheckBox(
            "",
            Boolean.parseBoolean(parameters
                  .getSegmentationParameter(MaarsParameters.FILTER_MEAN_GREY_VALUE)));
      greyValueFilter.addActionListener(this);
      greyValueFilterCheckPanel.add(greyValueFilter);

      //

      greyValue = new JTextField(
            parameters
                  .getSegmentationParameter(MaarsParameters.MEAN_GREY_VALUE),
            filedLength);
      greyValue.setEditable(greyValueFilter.isSelected());
      greyValueFilterCheckPanel.add(greyValue);

      //

      JPanel shapeCheckPanel = new JPanel(new GridLayout(1, 2));
      shapeCheckPanel.setBackground(GuiUtils.bgColor);
      shapeCheckPanel.setBorder(GuiUtils.addPanelTitle("Filter unusual shape using solidity"));
      add(shapeCheckPanel);

      shapeFilter = new JCheckBox(
            "",
            Boolean.parseBoolean(parameters
                  .getSegmentationParameter(MaarsParameters.FILTER_SOLIDITY)));
      shapeFilter.addActionListener(this);
      shapeCheckPanel.add(shapeFilter);

      //

      solidity = new JTextField(
            parameters.getSegmentationParameter(MaarsParameters.SOLIDITY),
            filedLength);
      solidity.setEditable(shapeFilter.isSelected());
      shapeCheckPanel.add(solidity);

      //

      JPanel skipTestPanel = new JPanel(new GridLayout(1, 2));
      skipTestPanel.setBorder(GuiUtils.addPanelTitle("Options : "));
      skipTestPanel.setBackground(GuiUtils.bgColor);
      add(skipTestPanel);

      JButton testSegBut = new JButton("Test segmentation");
      testSegBut.addActionListener(actionEvent -> {
         updateMAARSParamters();
         MaarsParameters parameters_dup = parameters.duplicate();
         parameters_dup.setSavingPath(parameters.getSavingPath() + File.separator +
               parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX) + Maars_Interface.SEGANALYSIS_SUFFIX);
         // --------------------------segmentation-----------------------------//
         new Thread(new MaarsSegmentation(parameters_dup, IJ.getImage(), "test")).start();
      });

      //

      skipSegChBox = new JCheckBox();
      skipSegChBox.setSelected(Boolean.parseBoolean(parameters_.getSkipSegmentation()));
      skipSegChBox.setText("Skip segmentation");
      skipSegChBox.addActionListener(e -> {
         if (skipSegChBox.isSelected()) {
            YesNoCancelDialog yesNoCancelDialog = new YesNoCancelDialog(null,
                  "Skip segmentation",
                  "Do you have ROI.zip and BF_Results.csv in all your folders ?");
            parameters_.setSkipSegmentation(yesNoCancelDialog.yesPressed());
            skipSegChBox.setSelected(yesNoCancelDialog.yesPressed());
            parameters_.setSkipSegmentation(yesNoCancelDialog.yesPressed());
         } else {
            parameters_.setSkipSegmentation(false);
         }
      });
      skipTestPanel.add(skipSegChBox);
      skipTestPanel.add(testSegBut);

      //

      okBut = new JButton("OK");
      getRootPane().setDefaultButton(okBut);
      okBut.addActionListener(this);
      add(okBut, BorderLayout.SOUTH);

      pack();
      setVisible(true);
   }

   private void updateMAARSParamters() {
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
      parameters_.setSkipSegmentation(skipSegChBox.isSelected());
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
         solidity.setEditable(shapeFilter.isSelected());
      } else if (e.getSource() == greyValueFilter) {
         greyValue.setEditable(greyValueFilter.isSelected());
      } else if (e.getSource() == okBut) {
         updateMAARSParamters();
         setVisible(false);
      }
   }
}