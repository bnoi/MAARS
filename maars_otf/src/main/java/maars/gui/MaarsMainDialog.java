package maars.gui;

import ij.IJ;
import ij.gui.YesNoCancelDialog;
import ij.plugin.frame.RoiManager;
import maars.agents.DefaultSetOfCells;
import maars.display.SOCVisualizer;
import maars.io.IOUtils;
//import maars.main.MAARS;
import maars.main.MaarsParameters;
import maars.utils.FileUtils;
import maars.utils.GuiUtils;
import mmcorej.CMMCore;
import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;
import org.micromanager.internal.MMStudio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

/**
 * Class to create and display a dialog to get parameters of the whole analysis
 *
 * @author Tong LI
 */

public class MaarsMainDialog extends JFrame implements ActionListener {

   public static JButton okMainDialogButton;
   private final MMStudio mm_;
   private final CMMCore mmc_;
   private MaarsParameters parameters_;
   private JButton showDataVisualizer_;
   private JButton segmButton;
   private JButton fluoAnalysisButton;
   private JFormattedTextField savePathTf;
   private JFormattedTextField fluoAcqDurationTf;
   private JRadioButton dynamicOpt;
   private JRadioButton staticOpt;
   private MaarsFluoAnalysisDialog fluoDialog_;
   private MaarsSegmentationDialog segDialog_;
//   private DefaultSocSet socSet_ = new DefaultSocSet();
   private HashMap<String, SOCVisualizer> socVisualizerList_ = new HashMap<>();
   private JButton stopButton_;
   private CopyOnWriteArrayList<Map<String, Future>> tasksSet_ = new CopyOnWriteArrayList<>();
   private JFormattedTextField posListTf_;
   private JFormattedTextField pathToBfAcqSettingTf_;
   private JFormattedTextField pathToFluoAcqSettingTf_;


   /**
    * Constructor
    *
    * @param mm         : graphical user interface of Micro-Manager
    * @param parameters :MaarsParameters
    */
   public MaarsMainDialog(MMStudio mm, MaarsParameters parameters) {
      super("Mitosis Analysing And Recording System - MaarsOTFSeg");
      // ------------initialization of parameters---------------//

      mm_ = mm;
      mmc_ = mm.core();
      parameters_ = parameters;

      IJ.log("create main dialog ...");
      setDefaultLookAndFeelDecorated(true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      // set minimal dimension of mainDialog

      int maxDialogWidth = 350;
      int maxDialogHeight = 600;
      Dimension minimumSize = new Dimension(maxDialogWidth, maxDialogHeight);
      setMinimumSize(minimumSize);

      // Exploration Label

      JPanel multiPositionPanel = new JPanel(new GridLayout(2, 1));
      multiPositionPanel.setBackground(GuiUtils.bgColor);
      multiPositionPanel.setBorder(GuiUtils.addPanelTitle("Path to position list (.pos) or empty"));

      posListTf_ = new JFormattedTextField(String.class);
      posListTf_.setText(parameters.getPathToPositionList());
      multiPositionPanel.add(posListTf_);

      JPanel posListActionPanel = new JPanel(new GridLayout(1, 0));
      final JButton editPositionListButton = new JButton("Generate...");
      editPositionListButton.addActionListener(e -> mm.showPositionList());
      posListActionPanel.add(editPositionListButton);

      final JButton chosePositionListButton = new JButton("Browse...");
      chosePositionListButton.addActionListener(actionEvent -> {
         JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new File("."));
         chooser.setDialogTitle("choosertitle");
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         chooser.setAcceptAllFileFilterUsed(false);
         FileNameExtensionFilter posListFilter = new FileNameExtensionFilter(
               "MM position list files (.pos) ", "pos");
         chooser.setFileFilter(posListFilter);
         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            posListTf_.setText(String.valueOf(chooser.getSelectedFile()));
            parameters_.setPathToPositionList(posListTf_.getText());
            saveParameters();
         } else {
            System.out.println("No Selection ");
         }
      });
      posListActionPanel.add(chosePositionListButton);

      multiPositionPanel.add(posListActionPanel);

      // segmentation acq/analysis parameters Panel

      JPanel segAcqAnaParamPanel = new JPanel(new GridLayout(2, 1));
      segAcqAnaParamPanel.setBackground(GuiUtils.bgColor);
      segAcqAnaParamPanel.setBorder(GuiUtils.addPanelTitle("Segmentation Acq / Analysis"));

      pathToBfAcqSettingTf_ = new JFormattedTextField(String.class);
      pathToBfAcqSettingTf_.setText(parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING));
      segAcqAnaParamPanel.add(pathToBfAcqSettingTf_);

      // segmentation button

      JPanel segPanel = new JPanel(new GridLayout(0, 3));

      final JButton editBFAcqSettingButton = new JButton("Generate...");
      editBFAcqSettingButton.addActionListener(e -> mm.openAcqControlDialog());
      segPanel.add(editBFAcqSettingButton);

      segmButton = new JButton("Parameters");
      segmButton.addActionListener(this);
      segPanel.add(segmButton);

      final JButton choseBFAcqSettingButton = new JButton("Browse...");
      choseBFAcqSettingButton.addActionListener(actionEvent -> {
         JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new File("."));
         chooser.setDialogTitle("choosertitle");
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         chooser.setAcceptAllFileFilterUsed(false);
         FileNameExtensionFilter posListFilter = new FileNameExtensionFilter(
               "MM acquisition setting file ", "txt");
         chooser.setFileFilter(posListFilter);
         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            pathToBfAcqSettingTf_.setText(String.valueOf(chooser.getSelectedFile()));
            parameters_.setSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING, pathToBfAcqSettingTf_.getText());
            saveParameters();
         } else {
            System.out.println("No Selection ");
         }
      });
      segPanel.add(choseBFAcqSettingButton);
      segAcqAnaParamPanel.add(segPanel);

      // fluo acq/analysis parameters Panel

      JPanel fluoAcqAnaParamPanel = new JPanel(new GridLayout(2, 1));
      fluoAcqAnaParamPanel.setBackground(GuiUtils.bgColor);
      fluoAcqAnaParamPanel.setBorder(GuiUtils.addPanelTitle("Fluo Acq / Analysis"));

      pathToFluoAcqSettingTf_ = new JFormattedTextField(String.class);
      pathToFluoAcqSettingTf_.setText(parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING));
      fluoAcqAnaParamPanel.add(pathToFluoAcqSettingTf_);

      // fluo analysis button

      JPanel fluoPanel = new JPanel(new GridLayout(0, 3));

      final JButton editFluoAcqSettingButton = new JButton("Generate...");
      editFluoAcqSettingButton.addActionListener(e -> mm.openAcqControlDialog());
      fluoPanel.add(editFluoAcqSettingButton);

      fluoAnalysisButton = new JButton("Parameters");
      fluoAnalysisButton.addActionListener(this);
      fluoPanel.add(fluoAnalysisButton);

      final JButton choseFluoAcqSettingButton = new JButton("Browse...");
      choseFluoAcqSettingButton.addActionListener(actionEvent -> {
         JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new File("."));
         chooser.setDialogTitle("choosertitle");
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         chooser.setAcceptAllFileFilterUsed(false);
         FileNameExtensionFilter posListFilter = new FileNameExtensionFilter(
               "MM acquisition setting file ", "txt");
         chooser.setFileFilter(posListFilter);
         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            pathToFluoAcqSettingTf_.setText(String.valueOf(chooser.getSelectedFile()));
            parameters_.setFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING, pathToFluoAcqSettingTf_.getText());
            saveParameters();
         } else {
            System.out.println("No Selection ");
         }
      });
      fluoPanel.add(choseFluoAcqSettingButton);

      fluoAcqAnaParamPanel.add(fluoPanel);

      // strategy panel (2 radio button + 1 textfield + 1 label)

      JPanel strategyPanel = new JPanel(new GridLayout(1, 0));
      strategyPanel.setBorder(GuiUtils.addPanelTitle("Strategy"));
      strategyPanel.setToolTipText("Which strategy to use");

//      strategyPanel.setBackground(panelColor);
      dynamicOpt = new JRadioButton("Dynamic");
      dynamicOpt.setSelected(parameters.useDynamic());
      staticOpt = new JRadioButton("Static");
      staticOpt.setSelected(!parameters.useDynamic());

      dynamicOpt.addActionListener(this);
      staticOpt.addActionListener(this);

      ButtonGroup group = new ButtonGroup();
      group.add(dynamicOpt);
      group.add(staticOpt);

      strategyPanel.add(staticOpt);
      strategyPanel.add(dynamicOpt);
      fluoAcqDurationTf = new JFormattedTextField(Double.class);
      fluoAcqDurationTf.setValue(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT));
      strategyPanel.add(fluoAcqDurationTf);
      strategyPanel.add(new JLabel("min", SwingConstants.CENTER));
      strategyPanel.setBackground(GuiUtils.bgColor);

      // Saving path Panel

      JPanel savePathPanel = new JPanel(new GridLayout(1, 0));
      savePathPanel.setBackground(GuiUtils.bgColor);
      savePathPanel.setBorder(GuiUtils.addPanelTitle("Acquisition root folder :"));
      savePathPanel.setToolTipText("Path to saving folder");

      // Saving Path textfield

      savePathTf = new JFormattedTextField(parameters.getSavingPath());
      savePathTf.setMaximumSize(new Dimension(maxDialogWidth, 1));
      savePathPanel.add(savePathTf);

      // show visualiwer acquisitions button

      JPanel stopAndVisualizerButtonPanel_ = new JPanel(new GridLayout(1, 0));
      stopAndVisualizerButtonPanel_.setBackground(GuiUtils.bgColor);
      stopAndVisualizerButtonPanel_.setBorder(GuiUtils.addPanelTitle("Visualizer and stop"));
      showDataVisualizer_ = new JButton("Show visualizer");
      showDataVisualizer_.addActionListener(this);
      showDataVisualizer_.setEnabled(false);
      stopAndVisualizerButtonPanel_.add(showDataVisualizer_);

      //

      stopButton_ = new JButton("Stop");
      stopButton_.addActionListener(this);
      stopAndVisualizerButtonPanel_.add(stopButton_);


      // Ok button to run

      JPanel okPanel = new JPanel(new GridLayout(1, 0));
      okMainDialogButton = new JButton("Go !");
      okMainDialogButton.setBackground(GuiUtils.butColor);
      okMainDialogButton.addActionListener(this);
      getRootPane().setDefaultButton(okMainDialogButton);
      okPanel.add(okMainDialogButton);

      // ------------set up and add components to Panel then to Frame---------------//

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.add(multiPositionPanel);
      mainPanel.add(segAcqAnaParamPanel);
      mainPanel.add(fluoAcqAnaParamPanel);
      mainPanel.add(strategyPanel);
      mainPanel.add(savePathPanel);
      mainPanel.add(okPanel);
      mainPanel.add(stopAndVisualizerButtonPanel_);
      add(mainPanel);
      pack();
      setVisible(true);
   }

   /**
    * method to save the parameters entered
    */
   private void saveParameters() {
      if (!savePathTf.getText().equals(parameters_.getSavingPath())) {
         parameters_.setSavingPath(savePathTf.getText());
      }
      if (!fluoAcqDurationTf.getText().equals(parameters_.getFluoParameter(MaarsParameters.TIME_LIMIT))) {
         parameters_.setFluoParameter(MaarsParameters.TIME_LIMIT, fluoAcqDurationTf.getText());
      }

      parameters_.setPathToPositionList(posListTf_.getText());
      parameters_.setSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING, pathToBfAcqSettingTf_.getText());
      parameters_.setFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING, pathToFluoAcqSettingTf_.getText());
      if (FileUtils.exists(parameters_.getSavingPath())) {
         parameters_.save(parameters_.getSavingPath());
      }
   }

   /**
    * method to set the strategy selected
    */
   private void setAnalysisStrategy() {

      if (dynamicOpt.isSelected()) {
         parameters_.setFluoParameter(MaarsParameters.DYNAMIC, "" + true);
      } else if (staticOpt.isSelected()) {
         parameters_.setFluoParameter(MaarsParameters.DYNAMIC, "" + false);
      }
   }

   private String[] loadPositions() {
      PositionList pl = new PositionList();
      try {
         if (FileUtils.exists(parameters_.getPathToPositionList())) {
            pl.load(parameters_.getPathToPositionList());
            IJ.log(pl.getNumberOfPositions() + "-position file loaded");
         } else {
            String xyStage = mmc_.getXYStageDevice();
            String zStage = mmc_.getFocusDevice();
            MultiStagePosition currentPos = new MultiStagePosition(xyStage, mm_.getCachedXPosition(), mm_.getCachedYPosition(),
                  zStage, mm_.getCachedZPosition());
            pl.addPosition(currentPos);
         }
         mm_.positions().setPositionList(pl);
      } catch (Exception e1) {
         IOUtils.printErrorToIJLog(e1);
      }
      String[] posNames = new String[pl.getPositions().length];
      for (int i = 0; i<pl.getPositions().length;i++){
         posNames[i] = pl.getPositions()[i].getLabel();
      }
      return posNames;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
//      MAARS maars = null;
      if (e.getSource() == okMainDialogButton) {
         saveParameters();
         String[] posNames = loadPositions();
         for (int i = 0; i < posNames.length; i++) {
            DefaultSetOfCells soc = new DefaultSetOfCells(posNames[i]+"");
//            socSet_.put(posNames[i], soc);
//            SOCVisualizer socVisualizer = createVisualizer(soc);
//            socVisualizerList_.put(posNames[i],socVisualizer);
//            if (parameters_.useDynamic()&& Objects.equals(posNames[i], "Pos0")) {
//               socVisualizer.setVisible(true);
//            }
         }
         try {
            parameters_.save();
         } catch (IOException e1) {
            e1.printStackTrace();
         }
//         maars = new MAARS(mm_, mmc_, parameters_, socVisualizerList_, tasksSet_, socSet_);
//         new Thread(maars).start();
         okMainDialogButton.setEnabled(false);
         showDataVisualizer_.setEnabled(true);
      } else if (e.getSource() == segmButton) {
         saveParameters();
         if (segDialog_ != null) {
            segDialog_.setVisible(true);
         } else {
            segDialog_ = new MaarsSegmentationDialog(parameters_, this);
         }

      } else if (e.getSource() == fluoAnalysisButton) {
         saveParameters();
         if (fluoDialog_ != null) {
            fluoDialog_.setVisible(true);
         } else {
            fluoDialog_ = new MaarsFluoAnalysisDialog(parameters_, this);
         }
      } else if (e.getSource() == dynamicOpt) {
         setAnalysisStrategy();
         fluoAcqDurationTf.setEditable(true);
      } else if (e.getSource() == staticOpt) {
         setAnalysisStrategy();
         fluoAcqDurationTf.setEditable(false);
      } else if (e.getSource() == showDataVisualizer_) {
         for (SOCVisualizer socVisualizer : socVisualizerList_.values()) {
            socVisualizer.setVisible(true);
         }
      } else if (e.getSource() == stopButton_) {
         YesNoCancelDialog yesNoCancelDialog = new YesNoCancelDialog(this, "Abandon current acquisition?",
               "Stop current analysis ?");
         yesNoCancelDialog.setAlwaysOnTop(true);
         if (yesNoCancelDialog.yesPressed()) {
//            maars.interrupt();
            RoiManager roiManager = RoiManager.getInstance();
            if (roiManager != null) {
               roiManager.reset();
               roiManager.close();
            }
//            for (String posName : socSet_.getPositionNames()) {
//               socSet_.getSoc(posName).reset();
               //TODO
//               socVisualizerList_.get(i).cleanUp();
//               socVisualizerList_.get(i).setVisible(false);
//               socVisualizerList_.get(i).createGUI(socSet_.getSoc(posName));
//            }
         }
      } else {
         IJ.log("MaarsOTFSeg don't understand what you want, sorry");
      }
   }
}
