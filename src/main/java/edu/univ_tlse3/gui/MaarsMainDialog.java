package edu.univ_tlse3.gui;

import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.maars.MAARS;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.GuiUtils;
import edu.univ_tlse3.utils.IOUtils;
import ij.IJ;
import ij.gui.YesNoCancelDialog;
import ij.plugin.frame.RoiManager;
import mmcorej.CMMCore;
import org.micromanager.internal.MMStudio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Class to create and display a dialog to get parameters of the whole analysis
 *
 * @author Tong LI
 */

public class MaarsMainDialog extends JFrame implements ActionListener{

    private final MMStudio mm_;
    private final CMMCore mmc_;
    private MaarsParameters parameters_;
    public static JButton okMainDialogButton;
    private JButton showDataVisualizer_;
    private JButton segmButton;
    private JButton fluoAnalysisButton;
    private JFormattedTextField savePathTf;
    private JFormattedTextField fluoAcqDurationTf;
    private JRadioButton dynamicOpt;
    private JRadioButton staticOpt;
    private MaarsFluoAnalysisDialog fluoDialog_;
    private MaarsSegmentationDialog segDialog_;
    private SetOfCells soc_ = new SetOfCells();
    private SOCVisualizer socVisualizer_;
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
        super("Mitosis Analysing And Recording System - MAARS");
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
     *
     * @param tasksSet  tasks to be terminated
     */
    public static void waitAllTaskToFinish(CopyOnWriteArrayList<Map<String, Future>> tasksSet) {
        for (Map<String, Future> aFutureSet : tasksSet) {
            for (String channel : aFutureSet.keySet()) {
                try {
                    aFutureSet.get(channel).get();
                } catch (InterruptedException | ExecutionException e) {
                    IOUtils.printErrorToIJLog(e);
                }
                IJ.showStatus("Terminating analysis...");
            }
        }
        IJ.log("Spot detection finished! Proceed to saving and analysis...");
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
        try {
            if (FileUtils.exists(parameters_.getSavingPath())) {
                parameters_.save(parameters_.getSavingPath());
            }
        } catch (IOException e) {
            IJ.error("Could not save parameters");
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

    private SOCVisualizer createVisualizer() {
        final SOCVisualizer socVisualizer = new SOCVisualizer();
        socVisualizer.createGUI(soc_);
        return socVisualizer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MAARS maars = new MAARS(mm_, mmc_, parameters_, socVisualizer_, tasksSet_, soc_);
        if (e.getSource() == okMainDialogButton) {
            if (socVisualizer_ == null) {
                socVisualizer_ = createVisualizer();
                if (parameters_.useDynamic()) {
                    socVisualizer_.setVisible(true);
                }
            }
            saveParameters();
            try {
                parameters_.save();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            new Thread(maars).start();
            okMainDialogButton.setEnabled(false);
        } else if (e.getSource() == segmButton) {
            saveParameters();
            if (segDialog_ != null) {
                segDialog_.setVisible(true);
            } else {
                segDialog_ = new MaarsSegmentationDialog(parameters_,this);
            }

        } else if (e.getSource() == fluoAnalysisButton) {
            saveParameters();
            if (fluoDialog_ != null) {
                fluoDialog_.setVisible(true);
            } else {
                fluoDialog_ = new MaarsFluoAnalysisDialog(parameters_,this);
            }
        } else if (e.getSource() == dynamicOpt) {
            setAnalysisStrategy();
            fluoAcqDurationTf.setEditable(true);
        } else if (e.getSource() == staticOpt) {
            setAnalysisStrategy();
            fluoAcqDurationTf.setEditable(false);
        } else if (e.getSource() == showDataVisualizer_) {
            if (socVisualizer_ == null) {
                socVisualizer_ = createVisualizer();
            }
            socVisualizer_.setVisible(true);
        } else if (e.getSource() == stopButton_) {
            YesNoCancelDialog yesNoCancelDialog = new YesNoCancelDialog(this, "Abandon current acquisition?",
                    "Stop current analysis ?");
            yesNoCancelDialog.setAlwaysOnTop(true);
            if (yesNoCancelDialog.yesPressed()) {
                maars.interrupt();
                RoiManager roiManager = RoiManager.getInstance();
                if (roiManager!=null) {
                    roiManager.reset();
                    roiManager.close();
                }
                soc_.reset();
                socVisualizer_.cleanUp();
                socVisualizer_.setVisible(false);
                socVisualizer_.createGUI(soc_);
            }
        } else {
            IJ.log("MAARS don't understand what you want, sorry");
        }
    }
}
