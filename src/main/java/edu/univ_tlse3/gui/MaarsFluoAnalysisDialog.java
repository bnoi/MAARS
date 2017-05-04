package edu.univ_tlse3.gui;

import edu.univ_tlse3.acquisition.MAARS_mda;
import edu.univ_tlse3.cellstateanalysis.MaarsTrackmate;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.GuiUtils;
import edu.univ_tlse3.utils.ImgUtils;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.YesNoCancelDialog;
import ij.io.FileInfo;
import ij.io.TiffDecoder;
import org.micromanager.internal.MMStudio;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to create and display a dialog to get parameters_ of the fluorescent
 * analysis (detection and measurement of mitotic spindle)
 *
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 */
public class MaarsFluoAnalysisDialog extends JDialog implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static Boolean saveRam_ = false;
    private MMStudio mm;
    private MaarsParameters parameters_;
    private JTextField range;
    private JTextField step;
    private JTextField timeInterval;
    private JFormattedTextField mitosisDurationTf_;
    private JCheckBox saveFlims;
    private JCheckBox doAnalysis;
    private JButton okFluoAnaParamButton;
    private JComboBox<String> configurationCombo_;
    private ArrayList<HashMap<String, Component>> listChCompos_ = new ArrayList<>();
    private ArrayList<ArrayList<Component>> allChComponents_;
    private String spindleChannel_ = null;
    private String USING = "Using";
    private String CHANNELS = "Channels";
    private String MAXNBSPOT = "Max # of spot";
    private String SPOTRADIUS = "Spot Radius";
    private String QUALITY = "Quality";
    private String CHEXPOSURE = "Channel Exposure";
    private String PREVIEW = "Preview";
    private String SPINDLE = "Spindle ?";
    private JComboBox<String> ch1Combo_;
    private JComboBox<String> ch2Combo_;
    private JComboBox<String> ch3Combo_;
    private JFormattedTextField maxNbSpotCh1Tf_;
    private JFormattedTextField maxNbSpotCh2Tf_;
    private JFormattedTextField maxNbSpotCh3Tf_;
    private JFormattedTextField spotRadiusCh1Tf_;
    private JFormattedTextField spotRadiusCh2Tf_;
    private JFormattedTextField spotRadiusCh3Tf_;
    private JFormattedTextField qualityCh1Tf_;
    private JFormattedTextField qualityCh2Tf_;
    private JFormattedTextField qualityCh3Tf_;
    private JLabel summaryLabel_;
    private JCheckBox projected_;

    public MaarsFluoAnalysisDialog(MaarsParameters parameters){
        super();
        setUpDialog(new Dimension(500, 200));
        int timePointsNb = MaarsParameters.getTimePointsNb(parameters);
        int slicePerFrame = MaarsParameters.getSliceNb(parameters);
        int nbChannel = MaarsParameters.getChNb(parameters);
        add(generateSummaryPanel(timePointsNb, slicePerFrame, nbChannel), BorderLayout.NORTH);
        add(generateAnaParamPanel(parameters), BorderLayout.CENTER);
        add(generateButtonPanel(parameters), BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    private JPanel generateButtonPanel(MaarsParameters parameters){
        JPanel butPanel = new JPanel(new GridLayout(0,3));
        JButton overwriteBut =  new JButton("Overwrite");
        butPanel.add(overwriteBut);
        overwriteBut.addActionListener(action->{
            int overWrite = JOptionPane.showConfirmDialog(this,
                    "This will overwrite loaded configuration, still proceed?");
            if (JOptionPane.YES_OPTION == overWrite){
                try {
                    updateParameters(parameters);
                    parameters.save(parameters.getSavingPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        JButton saveBut = new JButton("Save");
        butPanel.add(saveBut);
        saveBut.addActionListener(o -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Directory to save config file");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    parameters.save(String.valueOf(chooser.getSelectedFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                IJ.log("No folder Selected");
            }

        });
        JButton okBut = new JButton("Go");
        getRootPane().setDefaultButton(okBut);
        butPanel.add(okBut);
        okBut.addActionListener(actionEvent -> {
            updateParameters(parameters);
            parameters_ = parameters;
            setVisible(false);
        });
        return butPanel;
    }

    private void updateParameters(MaarsParameters parameters) {
        parameters.setMinimumMitosisDuration(mitosisDurationTf_.getText());
        parameters.setProjected(String.valueOf(projected_.isSelected()));
        String[] chNames = MaarsParameters.getChArray(parameters);
        for (int i= 0 ; i< chNames.length; i++){
            String ch= chNames[i];
            ArrayList array = allChComponents_.get(i);
            parameters.setChMaxNbSpot(ch,((JFormattedTextField) array.get(1)).getText());
            parameters.setChSpotRaius(ch,((JFormattedTextField) array.get(2)).getText());
            parameters.setChQuality(ch,((JFormattedTextField) array.get(3)).getText());
            if (((JRadioButton) array.get(5)).isSelected()){
                parameters.setDetectionChForMitosis(ch);
            }
        }
    }

    private JPanel generateAnaParamPanel(MaarsParameters parameters){
        JPanel anaParamPanel = new JPanel(new BorderLayout(2,0));
        anaParamPanel.setBackground(GuiUtils.bgColor);
        anaParamPanel.setBorder(GuiUtils.addSecondaryTitle("Analysis parameters"));
        anaParamPanel.add(generateChbxPanel(parameters), BorderLayout.NORTH);
        anaParamPanel.add(generateFluoParamPanel(parameters), BorderLayout.CENTER);
        return anaParamPanel;
    }

    private JPanel generateFluoParamPanel(MaarsParameters parameters){
        int chNb = MaarsParameters.getChNb(parameters);
        ArrayList<JPanel> arrayPanels = generateParamPanels(chNb);
        allChComponents_ = generateAllChComponents(parameters);

        JPanel p = new JPanel(new GridLayout(0, 6));
        p.setBackground(GuiUtils.bgColor);
        ButtonGroup group = new ButtonGroup();
        for (ArrayList<Component> chComponents : allChComponents_){
            for (int i = 0 ; i < chComponents.size(); i++){
                arrayPanels.get(i).add(chComponents.get(i));
                if (i == chComponents.size()-1){
                    group.add((JRadioButton) chComponents.get(i));
                }
            }
        }
        for (JPanel jp : arrayPanels) {
            p.add(jp);
        }
        return p;
    }

    private ArrayList<ArrayList<Component>> generateAllChComponents(MaarsParameters parameters){
        ArrayList<ArrayList<Component>> allChComponents = new ArrayList<>();
        for (String ch:MaarsParameters.getChArray(parameters)){
            allChComponents.add(generateComposOfCh(ch, parameters));
        }
        return allChComponents;
    }

    private ArrayList<Component> generateComposOfCh(String ch, MaarsParameters parameters){
        ArrayList<Component> components = new ArrayList<>();
        JLabel chLabel = new JLabel(ch);
        JFormattedTextField maxDotTf = new JFormattedTextField(Integer.class);
        maxDotTf.setText(parameters.getChMaxNbSpot(ch));
        JFormattedTextField radiusTf = new JFormattedTextField(Double.class);
        radiusTf.setText(parameters.getChSpotRaius(ch));
        JFormattedTextField qualityTf = new JFormattedTextField(Double.class);
        qualityTf.setText(parameters.getChQuality(ch));
        JButton previewBut = new JButton("Preview");
        JRadioButton radioBut = new JRadioButton();
        radioBut.setSelected(parameters.getDetectionChForMitosis().equals(ch));
        components.add(chLabel);
        components.add(maxDotTf);
        components.add(radiusTf);
        components.add(qualityTf);
        components.add(previewBut);
        components.add(radioBut);

        return components;
    }

    private ArrayList<JPanel> generateParamPanels(int chNb){
        ArrayList<JPanel> array = new ArrayList<>();

        JPanel chNamesP = new JPanel(new GridLayout(chNb,0));
        chNamesP.setBackground(GuiUtils.bgColor);
        chNamesP.setBorder(GuiUtils.addSecondaryTitle("Channel Names"));
        JPanel maxDotP = new JPanel(new GridLayout(chNb,0));
        maxDotP.setBackground(GuiUtils.bgColor);
        maxDotP.setBorder(GuiUtils.addSecondaryTitle("Max # of dot"));
        JPanel dotRadiusP = new JPanel(new GridLayout(chNb,0));
        dotRadiusP.setBackground(GuiUtils.bgColor);
        dotRadiusP.setBorder(GuiUtils.addSecondaryTitle("Dot Radius"));
        JPanel qualityP = new JPanel(new GridLayout(chNb,0));
        qualityP.setBackground(GuiUtils.bgColor);
        qualityP.setBorder(GuiUtils.addSecondaryTitle("Quality"));
        JPanel previewP = new JPanel(new GridLayout(chNb,0));
        previewP.setBackground(GuiUtils.bgColor);
        previewP.setBorder(GuiUtils.addSecondaryTitle("Preview detection"));
        JPanel spindleP = new JPanel(new GridLayout(chNb,0));
        spindleP.setBackground(GuiUtils.bgColor);
        spindleP.setBorder(GuiUtils.addSecondaryTitle("Spindle ?"));

        array.add(chNamesP);
        array.add(maxDotP);
        array.add(dotRadiusP);
        array.add(qualityP);
        array.add(previewP);
        array.add(spindleP);
        return array;
    }

    private JPanel generateChbxPanel(MaarsParameters parameters){
        JPanel chbxPanel = new JPanel(new GridLayout(0,2));
        chbxPanel.setBackground(GuiUtils.bgColor);
        projected_ = new JCheckBox("Project cropped images?",
                Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.PROJECTED)));
        chbxPanel.add(projected_);
        JPanel mitosisDurationPanel = new JPanel(new GridLayout(1, 1));
        mitosisDurationPanel.setBackground(GuiUtils.bgColor);
        mitosisDurationPanel.setBorder(GuiUtils.addSecondaryTitle("Minimum mitosis duration (s)"));
        mitosisDurationTf_ = new JFormattedTextField(Integer.class);
        mitosisDurationTf_.setText(parameters.getMinimumMitosisDuration());
        mitosisDurationPanel.add(mitosisDurationTf_);
        chbxPanel.add(mitosisDurationPanel);
        return chbxPanel;
    }
    private JPanel generateSummaryPanel(int timePointsNb, int slicePerFrame, int nbChannel) {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setBackground(GuiUtils.bgColor);
        summaryPanel.setBorder(GuiUtils.addSecondaryTitle("Acquisition summary"));
        String lineSep = "<br>";
        JLabel summaryLabel = new JLabel();
        summaryLabel.setVerticalTextPosition(JLabel.CENTER);
        summaryLabel.setText(
                "<html><body>Nb of time points: " + String.valueOf(timePointsNb) + lineSep +
                        "Nb of slices: " + String.valueOf(slicePerFrame) + lineSep +
                        "Nb of channels: " + String.valueOf(nbChannel) + lineSep);
        summaryPanel.add(summaryLabel);
        return summaryPanel;
    }

    public MaarsParameters getParameters(){
        return parameters_;
    }


    /**
     * @param maarsMainFrame MAARS main frame
     * @param mm             mmstudio object
     * @param parameters     : parameters_ displayed in dialog
     */
    MaarsFluoAnalysisDialog(JFrame maarsMainFrame, MMStudio mm, MaarsParameters parameters) {
        super(maarsMainFrame);
        String channelsString = parameters.getUsingChannels();
        String[] arrayChannels = channelsString.split(",", -1);
        String originSpindleChannel = parameters.getDetectionChForMitosis();
        // set up this dialog
        this.mm = mm;
        parameters_ = parameters;
        setUpDialog(new Dimension(750, 500));

        // Movie parameters_ label

        JPanel movieParaPanel = new JPanel(new GridLayout(1, 2));
        movieParaPanel.setBorder(GuiUtils.addPanelTitle("Movie parameters"));
        movieParaPanel.setBackground(GuiUtils.bgColor);
        add(movieParaPanel, BorderLayout.PAGE_START);

        //

        JPanel paramInputPanel = new JPanel(new GridLayout(3, 1));
        JPanel summaryPanel = new JPanel();
        summaryLabel_ = new JLabel();
        summaryLabel_.setVerticalTextPosition(JLabel.CENTER);
        summaryPanel.add(summaryLabel_);
        movieParaPanel.add(paramInputPanel);
        movieParaPanel.add(summaryPanel);

        //

        DocumentListener myListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                updateMAARSFluoChParameters();
                updateSummary();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                updateMAARSFluoChParameters();
                updateSummary();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                updateMAARSFluoChParameters();
                updateSummary();
            }
        };

        //

        JPanel fluoRangePanel = new JPanel(new GridLayout(1, 1));
        fluoRangePanel.setBackground(GuiUtils.bgColor);
        fluoRangePanel.setBorder(GuiUtils.addSecondaryTitle("Z Range (micron) : "));
        int fieldLength = 8;
        range = new JTextField(parameters_.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE), fieldLength);
        range.getDocument().addDocumentListener(myListener);
        fluoRangePanel.add(range);
        paramInputPanel.add(fluoRangePanel);

        //

        JPanel fluoStepPanel = new JPanel(new GridLayout(1, 1));
        fluoStepPanel.setBackground(GuiUtils.bgColor);
        fluoStepPanel.setBorder(GuiUtils.addSecondaryTitle("Z Step (micron) : "));
        step = new JTextField(parameters_.getFluoParameter(MaarsParameters.STEP), fieldLength);
        step.getDocument().addDocumentListener(myListener);
        fluoStepPanel.add(step);
        paramInputPanel.add(fluoStepPanel);

        //

        JPanel timeIntervalPanel = new JPanel(new GridLayout(1, 1));
        timeIntervalPanel.setBackground(GuiUtils.bgColor);
        timeIntervalPanel.setBorder(GuiUtils.addSecondaryTitle("Time Interval (ms) : "));
        timeInterval = new JTextField(parameters_.getFluoParameter(MaarsParameters.TIME_INTERVAL), fieldLength);
        timeInterval.getDocument().addDocumentListener(myListener);
        ;
        timeIntervalPanel.add(timeInterval);
        timeInterval.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateDoAnalysisButton();
            }
        });
        paramInputPanel.add(timeIntervalPanel);

        //

        JPanel fluoAnaParamLabel = new JPanel(new BorderLayout());
        fluoAnaParamLabel.setBackground(GuiUtils.bgColor);
        fluoAnaParamLabel.setBorder(GuiUtils.addPanelTitle("Fluo-acquisition parameters"));
        add(fluoAnaParamLabel, BorderLayout.CENTER);

        //

        HashMap<String, Component> ch1 = new HashMap<>();
        HashMap<String, Component> ch2 = new HashMap<>();
        HashMap<String, Component> ch3 = new HashMap<>();

        JPanel channelCheckPanel = new JPanel(new GridLayout(3, 0));
        channelCheckPanel.setBorder(GuiUtils.addSecondaryTitle(USING));
        JCheckBox useChannel1 = new JCheckBox("", true);
        useChannel1.addActionListener(actionEvent -> {
            enableChannelPanel(ch1, useChannel1.isSelected());
            updateSummary();
        });
        JCheckBox useChannel2 = new JCheckBox("", true);
        useChannel2.addActionListener(actionEvent -> {
            enableChannelPanel(ch2, useChannel2.isSelected());
            updateSummary();
        });
        JCheckBox useChannel3 = new JCheckBox("", true);
        useChannel3.addActionListener(actionEvent -> {
            enableChannelPanel(ch3, useChannel3.isSelected());
            updateSummary();
        });
        channelCheckPanel.add(useChannel1);
        channelCheckPanel.add(useChannel2);
        channelCheckPanel.add(useChannel3);
        ch1.put(USING, useChannel1);
        ch2.put(USING, useChannel2);
        ch3.put(USING, useChannel3);

        JPanel chComboPanel = new JPanel(new GridLayout(3, 0));
        chComboPanel.setBorder(GuiUtils.addSecondaryTitle(CHANNELS));
        ch1Combo_ = new JComboBox<>();
        ch1Combo_.addActionListener(actionEvent -> updateChSetting(ch1));
        ch2Combo_ = new JComboBox<>();
        ch2Combo_.addActionListener(actionEvent -> updateChSetting(ch2));
        ch3Combo_ = new JComboBox<>();
        ch3Combo_.addActionListener(actionEvent -> updateChSetting(ch3));
        chComboPanel.add(ch1Combo_);
        chComboPanel.add(ch2Combo_);
        chComboPanel.add(ch3Combo_);
        ch1.put(CHANNELS, ch1Combo_);
        ch2.put(CHANNELS, ch2Combo_);
        ch3.put(CHANNELS, ch3Combo_);

        JPanel maxNbSpotPanel = new JPanel(new GridLayout(3, 0));
        maxNbSpotPanel.setBorder(GuiUtils.addSecondaryTitle(MAXNBSPOT));
        maxNbSpotCh1Tf_ = new JFormattedTextField(Integer.class);
        maxNbSpotCh2Tf_ = new JFormattedTextField(Integer.class);
        maxNbSpotCh3Tf_ = new JFormattedTextField(Integer.class);
        maxNbSpotPanel.add(maxNbSpotCh1Tf_);
        maxNbSpotPanel.add(maxNbSpotCh2Tf_);
        maxNbSpotPanel.add(maxNbSpotCh3Tf_);
        ch1.put(MAXNBSPOT, maxNbSpotCh1Tf_);
        ch2.put(MAXNBSPOT, maxNbSpotCh2Tf_);
        ch3.put(MAXNBSPOT, maxNbSpotCh3Tf_);

        JPanel spotRadiusPanel = new JPanel(new GridLayout(3, 0));
        spotRadiusPanel.setBorder(GuiUtils.addSecondaryTitle(SPOTRADIUS));
        spotRadiusCh1Tf_ = new JFormattedTextField(Double.class);
        spotRadiusCh2Tf_ = new JFormattedTextField(Double.class);
        spotRadiusCh3Tf_ = new JFormattedTextField(Double.class);
        spotRadiusPanel.add(spotRadiusCh1Tf_);
        spotRadiusPanel.add(spotRadiusCh2Tf_);
        spotRadiusPanel.add(spotRadiusCh3Tf_);
        ch1.put(SPOTRADIUS, spotRadiusCh1Tf_);
        ch2.put(SPOTRADIUS, spotRadiusCh2Tf_);
        ch3.put(SPOTRADIUS, spotRadiusCh3Tf_);

        JPanel qualityPanel = new JPanel(new GridLayout(3, 0));
        qualityPanel.setBorder(GuiUtils.addSecondaryTitle(QUALITY));
        qualityCh1Tf_ = new JFormattedTextField(Double.class);
        qualityCh2Tf_ = new JFormattedTextField(Double.class);
        qualityCh3Tf_ = new JFormattedTextField(Double.class);
        qualityPanel.add(qualityCh1Tf_);
        qualityPanel.add(qualityCh2Tf_);
        qualityPanel.add(qualityCh3Tf_);
        ch1.put(QUALITY, qualityCh1Tf_);
        ch2.put(QUALITY, qualityCh2Tf_);
        ch3.put(QUALITY, qualityCh3Tf_);

        JPanel exposurePanel = new JPanel(new GridLayout(3, 0));
        exposurePanel.setBorder(GuiUtils.addSecondaryTitle(CHEXPOSURE));
        JFormattedTextField exposureCh1Tf = new JFormattedTextField(Integer.class);
        JFormattedTextField exposureCh2Tf = new JFormattedTextField(Integer.class);
        JFormattedTextField exposureCh3Tf = new JFormattedTextField(Integer.class);
        exposurePanel.add(exposureCh1Tf);
        exposurePanel.add(exposureCh2Tf);
        exposurePanel.add(exposureCh3Tf);
        ch1.put(CHEXPOSURE, exposureCh1Tf);
        ch2.put(CHEXPOSURE, exposureCh2Tf);
        ch3.put(CHEXPOSURE, exposureCh3Tf);


        JPanel previewPanel = new JPanel(new GridLayout(3, 0));
        previewPanel.setBorder(GuiUtils.addSecondaryTitle(PREVIEW));
        JButton preview1But = new JButton(PREVIEW);
        preview1But.setEnabled(false);
        JButton preview2But = new JButton(PREVIEW);
        JButton preview3But = new JButton(PREVIEW);
        previewPanel.add(preview1But);
        previewPanel.add(preview2But);
        previewPanel.add(preview3But);
        ch1.put(PREVIEW, preview1But);
        ch2.put(PREVIEW, preview2But);
        ch3.put(PREVIEW, preview3But);

        JPanel radioPanel = new JPanel(new GridLayout(3, 0));
        radioPanel.setBorder(GuiUtils.addSecondaryTitle(SPINDLE));
        JRadioButton ch1Button = new JRadioButton("");
        ch1Button.setActionCommand((String) ch1Combo_.getSelectedItem());
        JRadioButton ch2Button = new JRadioButton("");
        ch2Button.setActionCommand((String) ch2Combo_.getSelectedItem());
        JRadioButton ch3Button = new JRadioButton("");
        ch3Button.setActionCommand((String) ch3Combo_.getSelectedItem());
        radioPanel.add(ch1Button);
        radioPanel.add(ch2Button);
        radioPanel.add(ch3Button);
        ch1.put(SPINDLE, ch1Button);
        ch2.put(SPINDLE, ch2Button);
        ch3.put(SPINDLE, ch3Button);

        listChCompos_.add(0, ch1);
        listChCompos_.add(1, ch2);
        listChCompos_.add(2, ch3);

        preview1But.addActionListener(e -> {
            updateMAARSFluoChParameters();
            testTrackmate(ch1);
        });
        preview2But.addActionListener(e -> {
            updateMAARSFluoChParameters();
            testTrackmate(ch2);
        });
        preview3But.addActionListener(e -> {
            updateMAARSFluoChParameters();
            testTrackmate(ch3);
        });


        //

        ButtonGroup group = new ButtonGroup();
        group.add(ch1Button);
        group.add(ch2Button);
        group.add(ch3Button);

        //

        JPanel configurationGroupPanel = new JPanel(new GridLayout(1, 1));
        configurationGroupPanel.setBackground(GuiUtils.bgColor);
        configurationGroupPanel.setBorder(GuiUtils.addSecondaryTitle("Configuration Group"));
        configurationCombo_ = new JComboBox<>(mm.getCore().getAvailableConfigGroups().toArray());
        configurationCombo_.addActionListener(this);
        configurationCombo_.setSelectedItem(parameters_.getChannelGroup());
        configurationGroupPanel.add(configurationCombo_);

        //

        JPanel mitosisDurationPanel = new JPanel(new GridLayout(1, 1));
        mitosisDurationPanel.setBackground(GuiUtils.bgColor);
        mitosisDurationPanel.setBorder(GuiUtils.addSecondaryTitle("Minimum mitosis duration (s)"));
        mitosisDurationTf_ = new JFormattedTextField(Integer.class);
        mitosisDurationTf_.setText(parameters.getMinimumMitosisDuration());
        mitosisDurationPanel.add(mitosisDurationTf_);

        //

        saveFlims = new JCheckBox("Save Movies",
                Boolean.parseBoolean(parameters_.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES)));
        doAnalysis = new JCheckBox("Do Analysis", true);
        projected_ = new JCheckBox("Project cropped images?",
                Boolean.parseBoolean(parameters_.getFluoParameter(MaarsParameters.PROJECTED)));
        updateDoAnalysisButton();
        JPanel optionPanel = new JPanel(new GridLayout(1, 4));
        optionPanel.setBackground(GuiUtils.bgColor);
        optionPanel.add(saveFlims);
        optionPanel.add(doAnalysis);
        optionPanel.add(projected_);
        optionPanel.add(configurationGroupPanel);
        optionPanel.add(mitosisDurationPanel);

        //

        fluoAnaParamLabel.add(optionPanel, BorderLayout.NORTH);

        JPanel configPanel = new JPanel(new GridLayout(1, 8));
        configPanel.add(channelCheckPanel);
        configPanel.add(chComboPanel);
        configPanel.add(maxNbSpotPanel);
        configPanel.add(spotRadiusPanel);
        configPanel.add(qualityPanel);
        configPanel.add(exposurePanel);
        configPanel.add(previewPanel);
        configPanel.add(radioPanel);

        fluoAnaParamLabel.add(configPanel, BorderLayout.CENTER);

        //

        okFluoAnaParamButton = new JButton("OK");
        okFluoAnaParamButton.addActionListener(this);
        add(okFluoAnaParamButton, BorderLayout.PAGE_END);

        //

        int j = 0;
        for (HashMap chConfigHashMap : listChCompos_) {
            JCheckBox tmpChkbox = (JCheckBox) chConfigHashMap.get(USING);
            if (j < arrayChannels.length) {
                JComboBox tmpChannelCombo = (JComboBox) chConfigHashMap.get(CHANNELS);
                tmpChannelCombo.setSelectedItem(arrayChannels[j]);
                tmpChkbox.setSelected(true);
                if (originSpindleChannel.equals(arrayChannels[j])) {
                    JRadioButton tmpRadio = (JRadioButton) chConfigHashMap.get(SPINDLE);
                    tmpRadio.setSelected(true);
                }
                j += 1;
            } else {
                tmpChkbox.setSelected(false);
            }
            enableChannelPanel(chConfigHashMap, tmpChkbox.isSelected());
        }

        ch1Button.addActionListener(e -> ch1Button.setActionCommand((String) ch1Combo_.getSelectedItem()));
        ch2Button.addActionListener(e -> ch2Button.setActionCommand((String) ch2Combo_.getSelectedItem()));
        ch3Button.addActionListener(e -> ch3Button.setActionCommand((String) ch3Combo_.getSelectedItem()));
        ch1Button.setActionCommand((String) ch1Combo_.getSelectedItem());
        ch2Button.setActionCommand((String) ch2Combo_.getSelectedItem());
        ch3Button.setActionCommand((String) ch3Combo_.getSelectedItem());

        //

        updateSummary();

        pack();
        setVisible(true);
    }

    private void setUpDialog(Dimension dim) {
        setModalityType(ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("MAARS - Fluorescence Analysis Parameters");
        setLayout(new BorderLayout());
        setMinimumSize(dim);
        setSize(750, 500);
        setBackground(GuiUtils.bgColor);
    }

    /**
     * @param ch channel name
     */
    private void setChPanelValue(HashMap<String, Component> channelHashMap, String ch) {
        for (String channel : parameters_.getAllChannels()) {
            if (ch != null && ch.equals(channel)) {
                JCheckBox tmpChk = (JCheckBox) channelHashMap.get(USING);
                if (tmpChk.isSelected()) {
                    JButton tmpButton = (JButton) channelHashMap.get(PREVIEW);
                    tmpButton.setEnabled(true);
                }
                JFormattedTextField tmpTf = (JFormattedTextField) channelHashMap.get(MAXNBSPOT);
                tmpTf.setValue(parameters_.getChMaxNbSpot(ch));
                tmpTf = (JFormattedTextField) channelHashMap.get(SPOTRADIUS);
                tmpTf.setValue(parameters_.getChSpotRaius(ch));
                tmpTf = (JFormattedTextField) channelHashMap.get(QUALITY);
                tmpTf.setValue(parameters_.getChQuality(ch));
                tmpTf = (JFormattedTextField) channelHashMap.get(CHEXPOSURE);
                tmpTf.setValue(parameters_.getChExposure(ch));
                JComboBox tmpCombo = (JComboBox) channelHashMap.get(CHANNELS);
                String tmpCh = (String) tmpCombo.getSelectedItem();
                if (tmpCh != null && !tmpCh.equals(ch)) {
                    tmpCombo.setSelectedItem(ch);
                }
            }
        }
    }

    /**
     *
     */
    private void updateDoAnalysisButton() {
        if (Double.parseDouble(timeInterval.getText()) < 10000) {
            doAnalysis.setSelected(false);
            doAnalysis.setEnabled(false);
        } else {
            doAnalysis.setSelected(true);
            doAnalysis.setEnabled(true);
        }
    }

    /**
     * @param enable enable or not
     */
    private void enableChannelPanel(HashMap<String, Component> channelHashMap, Boolean enable) {
        for (String param : channelHashMap.keySet()) {
            if (!param.equals(USING)) {
                channelHashMap.get(param).setEnabled(enable);
            }
        }
    }

    /**
     *
     */
    private void updateChSetting(HashMap<String, Component> channelHashMap) {
        JComboBox tmpCombo = (JComboBox) channelHashMap.get(CHANNELS);
        String selectedChannel = (String) tmpCombo.getSelectedItem();
        setChPanelValue(channelHashMap, selectedChannel);
    }

    /**
     *
     */
    public void updateSummary() {
        int timePointsNb = MaarsParameters.getTimePointsNb(parameters_);
        double rangeValue = Double.parseDouble(range.getText());
        double stepValue = Double.parseDouble(step.getText());
        int slicePerFrame = (int) (rangeValue / stepValue + 1);
        int nbChannel = 0;
        for (HashMap h : listChCompos_) {
            if (((JCheckBox) h.get(USING)).isSelected()) {
                nbChannel++;
            }
        }
        int totalNbImages = timePointsNb * slicePerFrame * nbChannel;

        String segDir = parameters_.getSavingPath() + File.separator + "X0_Y0" + File.separator + "_1" + File.separator;
        String imgName = "_1_MMStack_Pos0.ome.tif";
        long imageLen = 0;
        TiffDecoder td = new TiffDecoder(segDir, imgName);
        FileInfo[] info = null;
        try {
            info = td.getTiffInfo();
        } catch (IOException e) {
            imageLen = mm.core().getImageWidth() * mm.core().getImageHeight();
        }
        if (info != null) {
            imageLen = info[0].width * info[0].height;
        }
        assert imageLen != 0;
        double requiredMemory = imageLen / (double) 1048576 * 2 * totalNbImages;
        double availiableMemory = Runtime.getRuntime().maxMemory() / 1048576;
        String lineSep = "<br>";
        if (requiredMemory > availiableMemory) {
            saveRam_ = true;
            summaryLabel_.setText(
                    "<html><body>Nb of time points: " + String.valueOf(timePointsNb) + lineSep +
                            "Nb of slices: " + String.valueOf(slicePerFrame) + lineSep +
                            "Nb of channels: " + String.valueOf(nbChannel) + lineSep +
                            "Total memory: <font color=#e74c3c>" + String.valueOf(requiredMemory) + "</font>Mb</body></html>");
        } else {
            saveRam_ = false;
            summaryLabel_.setText(
                    "<html><body>Nb of time points: " + String.valueOf(timePointsNb) + lineSep +
                            "Nb of slices: " + String.valueOf(slicePerFrame) + lineSep +
                            "Nb of channels: " + String.valueOf(nbChannel) + lineSep +
                            "Total memory: " + String.valueOf(requiredMemory) + "Mb</body></html>");
        }
    }

    /**
     *
     */
    private void updateMAARSFluoChParameters() {
        parameters_.setFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE, range.getText());
        parameters_.setFluoParameter(MaarsParameters.STEP, step.getText());
        parameters_.setFluoParameter(MaarsParameters.DO_ANALYSIS, String.valueOf(doAnalysis.isSelected()));
        parameters_.setFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES,
                String.valueOf(saveFlims.isSelected()));
        parameters_.setFluoParameter(MaarsParameters.TIME_INTERVAL, timeInterval.getText());
        parameters_.setDetectionChForMitosis(spindleChannel_);
        parameters_.setMinimumMitosisDuration(String.valueOf(mitosisDurationTf_.getText()));
        parameters_.setProjected(String.valueOf(projected_.isSelected()));
        List<String> existingChannels = parameters_.getAllChannels();
        ArrayList<String> channels = new ArrayList<>();
        for (HashMap chConfigHashMap : listChCompos_) {
            JCheckBox tmpChkBox = (JCheckBox) chConfigHashMap.get(USING);
            if (tmpChkBox.isSelected()) {
                JComboBox tmpChannelCombo = (JComboBox) chConfigHashMap.get(CHANNELS);
                String tmpChannel = (String) tmpChannelCombo.getSelectedItem();
                if (!existingChannels.contains(tmpChannel)) {
                    YesNoCancelDialog yesNoCancelDialog = new YesNoCancelDialog(null, "Channel not in config file", "add this channel " + tmpChannel + " into the config file?");
                    if (yesNoCancelDialog.yesPressed()) {
                        String shutterLabel = (String) JOptionPane.showInputDialog(this, "shutter for this channel ?", "Shutter configuration", JOptionPane.QUESTION_MESSAGE, null, mm.getShutterManager().getShutterDevices().toArray(), mm.getShutterManager().getShutterDevices().get(0));
                        String colorLabel = (String) JOptionPane.showInputDialog(this, "Color for this channel ?", "Color configuration", JOptionPane.QUESTION_MESSAGE, null, parameters_.availiableColors(), parameters_.availiableColors()[0]);
                        parameters_.addChannel(tmpChannel);
                        parameters_.setChShutter(tmpChannel, shutterLabel);
                        parameters_.setChColor(tmpChannel, colorLabel);
                    } else {
                        tmpChkBox.setSelected(false);
                        continue;
                    }
                }
                JFormattedTextField tmpTf = (JFormattedTextField) chConfigHashMap.get(MAXNBSPOT);
                parameters_.setChMaxNbSpot(tmpChannel, tmpTf.getText());
                tmpTf = (JFormattedTextField) chConfigHashMap.get(SPOTRADIUS);
                parameters_.setChSpotRaius(tmpChannel, tmpTf.getText());
                tmpTf = (JFormattedTextField) chConfigHashMap.get(QUALITY);
                parameters_.setChQuality(tmpChannel, tmpTf.getText());
                tmpTf = (JFormattedTextField) chConfigHashMap.get(CHEXPOSURE);
                parameters_.setChExposure(tmpChannel, tmpTf.getText());
                channels.add(tmpChannel);
            }
        }
        parameters_.setUsingChannels(String.join(",", channels));

    }

    /**
     * @param img image to detect spots on
     */
    private void testTrackmate(HashMap<String, Component> componentHashMap, ImagePlus img) {
        JFormattedTextField tmpTf = (JFormattedTextField) componentHashMap.get(SPOTRADIUS);
        double spotRadius = Double.parseDouble((String) tmpTf.getValue());
        tmpTf = (JFormattedTextField) componentHashMap.get(QUALITY);
        double quality = Double.parseDouble((String) tmpTf.getValue());
        ImagePlus zProjectedFluoImg = ImgUtils.zProject(img, img.getCalibration());
        MaarsTrackmate tmTest = new MaarsTrackmate(zProjectedFluoImg, spotRadius, quality);
        Model model = tmTest.doDetection(false);
        model.getSpots().setVisible(true);
        SelectionModel selectionModel = new SelectionModel(model);
        HyperStackDisplayer displayer = new HyperStackDisplayer(model, selectionModel, zProjectedFluoImg);
        IJ.run(zProjectedFluoImg, "Enhance Contrast", "saturated=0.35");
        displayer.render();
        displayer.refresh();
    }

    /**
     *
     */
    private void testTrackmate(HashMap<String, Component> componentHashMap) {
        String channelName = getSelectedChannel(componentHashMap);
        String pathToFluoDir = parameters_.getSavingPath() + File.separator + "X0_Y0_FLUO";
        String imgPath = pathToFluoDir + File.separator + channelName + "_1" + File.separator +
                channelName + "_1_MMStack_Pos0.ome.tif";
        ImagePlus img = null;
        if (FileUtils.exists(imgPath)) {
            img = IJ.openImage(imgPath);
        } else if(FileUtils.getShortestTiffName(
                  parameters_.getSavingPath() + File.separator + "X0_Y0_FLUO") != null){
             img = IJ.getImage();
        } else {
            img = MAARS_mda.acquireImagePlus(mm,
                    "/Users/tongli/Desktop/untitled folder/AcqSettings_bf.txt",
                    pathToFluoDir, "FLUO");
//         img = acquireTestImg(componentHashMap);
        }
        testTrackmate(componentHashMap, img);
    }

    /**
     * @return get the image
     */
//   private ImagePlus acquireTestImg(HashMap<String, Component> componentHashMap) {
//      double zRange = Double.parseDouble(parameters_.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
//      double zStep = Double.parseDouble(parameters_.getFluoParameter(MaarsParameters.STEP));
//
//      MaarsParameters testParam = parameters_.duplicate();
//      String channelName = getSelectedChannel(componentHashMap);
//      testParam.setUsingChannels(channelName);
//      testParam.setFluoParameter(MaarsParameters.TIME_LIMIT, "0");
//      testParam.setFluoParameter(MaarsParameters.TIME_INTERVAL, this.timeInterval.getText());
//      testParam.setFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES, "false");
//      FluoAcqSetting acq = new FluoAcqSetting(testParam);
//
//      SequenceSettings fluoAcqSetting = acq.configAcqSettings(acq.configChannels(channelName));
//      fluoAcqSetting.keepShutterOpenSlices = true;
//      AcquisitionWrapperEngine acqEng = mm.getAcquisitionEngine();
//      acqEng.setSequenceSettings(fluoAcqSetting);
//      acqEng.enableZSliceSetting(true);
//      acqEng.setSlices(-zRange / 2, zRange / 2, zStep, false);
//      acqEng.setChannelGroup(fluoAcqSetting.channelGroup);
//      java.util.List<Image> imageList = AcqLauncher.acquire(acqEng);
//      for (Image img : imageList){
//         mm.live().displayImage(img);
//      }
//      return ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);
//   }
    private String getSelectedChannel(HashMap<String, Component> componentHashMap) {
        JComboBox tmpCombo = (JComboBox) componentHashMap.get(CHANNELS);
        return (String) tmpCombo.getSelectedItem();
    }

    private void clearChannelSettings() {
        ch1Combo_.removeAllItems();
        ch2Combo_.removeAllItems();
        ch3Combo_.removeAllItems();
        maxNbSpotCh1Tf_.setText("");
        maxNbSpotCh2Tf_.setText("");
        maxNbSpotCh3Tf_.setText("");
        spotRadiusCh1Tf_.setText("");
        spotRadiusCh2Tf_.setText("");
        spotRadiusCh3Tf_.setText("");
        qualityCh1Tf_.setText("");
        qualityCh2Tf_.setText("");
        qualityCh3Tf_.setText("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == okFluoAnaParamButton) {
            for (HashMap chConfigHashMap : listChCompos_) {
                JRadioButton tmpBut = (JRadioButton) chConfigHashMap.get(SPINDLE);
                if (tmpBut.isSelected()) {
                    spindleChannel_ = tmpBut.getActionCommand();
                }
            }
            updateMAARSFluoChParameters();
            try {
                parameters_.save();
            } catch (IOException e1) {
                System.out.println("Can not save MAARS parameters_");
                e1.printStackTrace();
            }
            this.setVisible(false);
        } else if (src == configurationCombo_) {
            String selectedGroup = (String) configurationCombo_.getSelectedItem();
            parameters_.setChannelGroup(selectedGroup);
            clearChannelSettings();
            String[] newConfigs = mm.getCore().getAvailableConfigs(selectedGroup).toArray();
            for (String s : newConfigs) {
                ch1Combo_.addItem(s);
                ch2Combo_.addItem(s);
                ch3Combo_.addItem(s);
            }
        }
    }
}
