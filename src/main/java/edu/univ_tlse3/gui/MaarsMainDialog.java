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

public class MaarsMainDialog extends JFrame implements ActionListener {

    private final MMStudio mm;
    private final CMMCore mmc;
    private MaarsParameters parameters;
    private JButton okMainDialogButton;
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

    /**
     * Constructor
     *
     * @param mm         : graphical user interface of Micro-Manager
     * @param parameters :MaarsParameters
     */
    public MaarsMainDialog(MMStudio mm, MaarsParameters parameters) {
        super("Mitosis Analysing And Recording System - MAARS");
        // ------------initialization of parameters---------------//

        this.mm = mm;
        this.mmc = mm.core();
        this.parameters = parameters;

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

        JFormattedTextField posListTf = new JFormattedTextField(String.class);
        posListTf.setText(parameters.getPathToPositionList());
        multiPositionPanel.add(posListTf);

        JPanel posListActionPanel = new JPanel(new GridLayout(1, 0));
        final JButton editPositionListButton = new JButton("Generate...");
        editPositionListButton.addActionListener(e -> mm.showPositionList());
        posListActionPanel.add(editPositionListButton);

        final JButton chosePositionListButton = new JButton("Find...");
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
                posListTf.setText(String.valueOf(chooser.getSelectedFile()));
                parameters.setPathToPositionList(posListTf.getText());
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

        JFormattedTextField pathToBfAcqSettingTf = new JFormattedTextField(String.class);
        pathToBfAcqSettingTf.setText(parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING));
        segAcqAnaParamPanel.add(pathToBfAcqSettingTf);

        // segmentation button

        JPanel segPanel = new JPanel(new GridLayout(0, 3));

        final JButton editBFAcqSettingButton = new JButton("Generate...");
        editBFAcqSettingButton.addActionListener(e -> mm.openAcqControlDialog());
        segPanel.add(editBFAcqSettingButton);

        segmButton = new JButton("Parameters");
        segmButton.addActionListener(this);
        segPanel.add(segmButton);

        final JButton choseBFAcqSettingButton = new JButton("Find...");
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
                pathToBfAcqSettingTf.setText(String.valueOf(chooser.getSelectedFile()));
                parameters.setSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING, pathToBfAcqSettingTf.getText());
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

        JFormattedTextField pathToFluoAcqSettingTf = new JFormattedTextField(String.class);
        pathToFluoAcqSettingTf.setText(parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING));
        fluoAcqAnaParamPanel.add(pathToFluoAcqSettingTf);

        // fluo analysis button

        JPanel fluoPanel = new JPanel(new GridLayout(0, 3));

        final JButton editFluoAcqSettingButton = new JButton("Generate...");
        editFluoAcqSettingButton.addActionListener(e -> mm.openAcqControlDialog());
        fluoPanel.add(editFluoAcqSettingButton);

        fluoAnalysisButton = new JButton("Parameters");
        fluoAnalysisButton.addActionListener(this);
        fluoPanel.add(fluoAnalysisButton);

        final JButton choseFluoAcqSettingButton = new JButton("Find...");
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
                pathToFluoAcqSettingTf.setText(String.valueOf(chooser.getSelectedFile()));
                parameters.setFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING, pathToFluoAcqSettingTf.getText());
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
        IJ.log("Done.");
        pack();
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
        if (!savePathTf.getText().equals(parameters.getSavingPath())) {
            parameters.setSavingPath(savePathTf.getText());
        }
        if (!fluoAcqDurationTf.getText().equals(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT))) {
            parameters.setFluoParameter(MaarsParameters.TIME_LIMIT, fluoAcqDurationTf.getText());
        }
        try {
            if (FileUtils.exists(parameters.getSavingPath())) {
                parameters.save(parameters.getSavingPath());
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
            parameters.setFluoParameter(MaarsParameters.DYNAMIC, "" + true);
        } else if (staticOpt.isSelected()) {
            parameters.setFluoParameter(MaarsParameters.DYNAMIC, "" + false);
        }
    }

    private SOCVisualizer createVisualizer() {
        final SOCVisualizer socVisualizer = new SOCVisualizer();
        socVisualizer.createGUI(soc_);
        return socVisualizer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okMainDialogButton) {
            if (socVisualizer_ == null) {
                socVisualizer_ = createVisualizer();
                if (parameters.useDynamic()) {
                    socVisualizer_.setVisible(true);
                }
            }
            saveParameters();
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.execute(new MAARS(mm, mmc, parameters, socVisualizer_, tasksSet_, soc_));
            es.shutdown();
        } else if (e.getSource() == segmButton) {
            saveParameters();
            if (segDialog_ != null) {
                segDialog_.setVisible(true);
            } else {
                segDialog_ = new MaarsSegmentationDialog(this, parameters, mm);
            }

        } else if (e.getSource() == fluoAnalysisButton) {
            saveParameters();
            if (fluoDialog_ != null) {
                fluoDialog_.setVisible(true);
            } else {
                fluoDialog_ = new MaarsFluoAnalysisDialog(this, mm, parameters);
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
                RoiManager roiManager = RoiManager.getInstance();
                roiManager.runCommand("Select All");
                roiManager.runCommand("Delete");
                roiManager.reset();
                roiManager.close();
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
