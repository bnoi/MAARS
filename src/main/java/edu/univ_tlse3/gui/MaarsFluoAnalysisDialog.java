package edu.univ_tlse3.gui;

import edu.univ_tlse3.acquisition.AcqLauncher;
import edu.univ_tlse3.acquisition.FluoAcqSetting;
import edu.univ_tlse3.cellstateanalysis.MaarsTrackmate;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.ImgUtils;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;

import ij.IJ;
import ij.ImagePlus;

import ij.gui.YesNoCancelDialog;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Class to create and display a dialog to get parameters_ of the fluorescent
 * analysis (detection and measurement of mitotic spindle)
 *
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 */
class MaarsFluoAnalysisDialog extends JDialog implements ActionListener {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private MMStudio mm;
   private MaarsParameters parameters_;
   private JTextField range;
   private JTextField step;
   private JTextField timeInterval;
   private JPanel channel1Panel;
   private JPanel channel2Panel;
   private JPanel channel3Panel;
   private JFormattedTextField maxNumberSpotCh1Tf;
   private JFormattedTextField spotRadiusCh1Tf;
   private JFormattedTextField qualityCh1Tf;
   private JFormattedTextField maxNumberSpotCh2Tf;
   private JFormattedTextField spotRadiusCh2Tf;
   private JFormattedTextField qualityCh2Tf;
   private JFormattedTextField maxNumberSpotCh3Tf;
   private JFormattedTextField spotRadiusCh3Tf;
   private JFormattedTextField qualityCh3Tf;
   private JFormattedTextField mitosisDurationTf_;
   private JCheckBox saveFlims;
   private JCheckBox doAnalysis;
   private JButton test1;
   private JButton test2;
   private JButton test3;
   private JButton okFluoAnaParamButton;
   private JComboBox<String> channel1Combo;
   private JComboBox<String> channel2Combo;
   private JComboBox<String> channel3Combo;
   private JComboBox<String> configurationCombo_;
   private JCheckBox useChannel1_;
   private JCheckBox useChannel2_;
   private JCheckBox useChannel3_;
   private static Integer TOTAL_PARAMETERS = 5;
   private ArrayList<JPanel> chPanels = new ArrayList<>();
   private String spindleChannel_ = null;

   /**
    *
    * @param maarsMainFrame MAARS main frame
    * @param mm             mmstudio object
    * @param parameters : parameters_ displayed in dialog
    */
   MaarsFluoAnalysisDialog(JFrame maarsMainFrame, MMStudio mm, MaarsParameters parameters) {
      super(maarsMainFrame);
      String channelsString = parameters.getUsingChannels();
      String[] arrayChannels = channelsString.split(",", -1);
      String originSpindleChannel = parameters.getDetectionChForMitosis();
      // set up this dialog
      this.mm = mm;
      parameters_ = parameters;
      setModalityType(ModalityType.DOCUMENT_MODAL);
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      setTitle("MAARS - Fluorescent Analysis Parameters");
      setLayout(new BorderLayout());
      setMinimumSize(new Dimension(750, 500));
      setSize(750,500);

      // Movie parameters_ label

      JPanel movieParaPanel = new JPanel(new GridLayout(3,1));
      movieParaPanel.setBorder(BorderFactory.createTitledBorder("Movie parameters"));
      add(movieParaPanel, BorderLayout.PAGE_START);

      //

      JPanel fluoRangePanel = new JPanel(new GridLayout(1, 1));
      fluoRangePanel.setBorder(BorderFactory.createTitledBorder("Z Range (micron) : "));
      int fieldLength = 8;
      range = new JTextField(parameters_.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE), fieldLength);
      fluoRangePanel.add(range);
      movieParaPanel.add(fluoRangePanel);

      //

      JPanel fluoStepPanel = new JPanel(new GridLayout(1, 1));
      fluoStepPanel.setBorder(BorderFactory.createTitledBorder("Z Step (micron) : "));
      step = new JTextField(parameters_.getFluoParameter(MaarsParameters.STEP), fieldLength);
      fluoStepPanel.add(step);
      movieParaPanel.add(fluoStepPanel);

      //

      JPanel timeIntervalPanel = new JPanel(new GridLayout(1, 1));
      timeIntervalPanel.setBorder(BorderFactory.createTitledBorder("Time Interval (ms) : "));
      timeInterval = new JTextField(parameters_.getFluoParameter(MaarsParameters.TIME_INTERVAL), fieldLength);
      timeIntervalPanel.add(timeInterval);
      timeInterval.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent e) {
            updateDoAnalysisButton();
         }
      });
      movieParaPanel.add(timeIntervalPanel);

      //

      JPanel fluoAnaParamLabel = new JPanel(new GridLayout(5,1));
      fluoAnaParamLabel.setBorder(BorderFactory.createTitledBorder("Fluo-acquisition parameters"));
      add(fluoAnaParamLabel, BorderLayout.CENTER);

      //

      JPanel channelTitlePanel = new JPanel(new GridLayout(1, 0));
      JPanel tmpPanel = new JPanel(new GridLayout(1, 0));
      tmpPanel.add(new JLabel());
      channelTitlePanel.add(tmpPanel);
      JLabel channelCheckTitle = new JLabel("Using", SwingConstants.CENTER);
      tmpPanel.add(channelCheckTitle);
      channelTitlePanel.add(tmpPanel);
      tmpPanel = new JPanel(new GridLayout(1, 0));
      JLabel fluoChannelsTitle = new JLabel("Channels", SwingConstants.CENTER);
      tmpPanel.add(fluoChannelsTitle);
      channelTitlePanel.add(tmpPanel);
      tmpPanel = new JPanel(new GridLayout(1, 0));
      JLabel maxNbSpotTitle = new JLabel("Max # of spot", SwingConstants.CENTER);
      tmpPanel.add(maxNbSpotTitle);
      channelTitlePanel.add(tmpPanel);
      tmpPanel = new JPanel(new GridLayout(1, 0));
      JLabel spotRaiusTitle = new JLabel("Spot Radius", SwingConstants.CENTER);
      tmpPanel.add(spotRaiusTitle);
      channelTitlePanel.add(tmpPanel);
      tmpPanel = new JPanel(new GridLayout(1, 0));
      JLabel qualityTitle = new JLabel("Quality", SwingConstants.CENTER);
      tmpPanel.add(qualityTitle);
      channelTitlePanel.add(tmpPanel);
      tmpPanel = new JPanel(new GridLayout(1, 0));
      JLabel exposureTitle = new JLabel("Exposure", SwingConstants.CENTER);
      tmpPanel.add(exposureTitle);
      channelTitlePanel.add(tmpPanel);
      tmpPanel = new JPanel(new GridLayout(1, 0));
      JLabel testButTitle = new JLabel("test detection", SwingConstants.CENTER);
      tmpPanel.add(testButTitle);
      channelTitlePanel.add(tmpPanel);
      tmpPanel = new JPanel(new GridLayout(1, 0));
      JLabel spindleChannel = new JLabel("Spindle ?", SwingConstants.CENTER);
      tmpPanel.add(spindleChannel);
      channelTitlePanel.add(tmpPanel);

      //

      channel1Panel = new JPanel(new GridLayout(1, 0));
      channel1Combo = new JComboBox<>();
      channel1Combo.addActionListener(actionEvent -> updateChSetting(channel1Panel));
      maxNumberSpotCh1Tf = new JFormattedTextField(Integer.class);
      spotRadiusCh1Tf = new JFormattedTextField(Double.class);
      qualityCh1Tf = new JFormattedTextField(Double.class);
      JFormattedTextField exposureCh1Tf_ = new JFormattedTextField(Integer.class);
      test1 = new JButton("test");
      test1.setEnabled(false);
      test1.addActionListener(this);
      useChannel1_ = new JCheckBox("",true);
      useChannel1_.addItemListener(itemEvent -> {
         if (useChannel1_.isSelected()){
            enableChannelPanel(channel1Panel, true);
         }else{
            enableChannelPanel(channel1Panel, false);
         }
      });
       final JRadioButton ch1Button = new JRadioButton("");
      channel1Panel.add(useChannel1_,0);
      channel1Panel.add(channel1Combo);
      channel1Panel.add(maxNumberSpotCh1Tf);
      channel1Panel.add(spotRadiusCh1Tf);
      channel1Panel.add(qualityCh1Tf);
      channel1Panel.add(exposureCh1Tf_);
      channel1Panel.add(test1);
       channel1Panel.add(ch1Button);
      chPanels.add(channel1Panel);

      //

      channel2Panel = new JPanel(new GridLayout(1, 0));

      channel2Combo = new JComboBox<>();
      channel2Combo.addActionListener(actionEvent -> updateChSetting(channel2Panel));
      maxNumberSpotCh2Tf = new JFormattedTextField(Integer.class);
      spotRadiusCh2Tf = new JFormattedTextField(Double.class);
      qualityCh2Tf = new JFormattedTextField(Double.class);
      JFormattedTextField exposureCh2Tf_ = new JFormattedTextField(Integer.class);
      test2 = new JButton("test");
      test2.addActionListener(this);
      useChannel2_ = new JCheckBox("", true);
      useChannel2_.addItemListener(itemEvent -> {
         if (useChannel2_.isSelected()){
            enableChannelPanel(channel2Panel, true);
         }else{
            enableChannelPanel(channel2Panel, false);
         }
      });
       final JRadioButton ch2Button = new JRadioButton("");
      ch2Button.setActionCommand((String) channel2Combo.getSelectedItem());
      channel2Panel.add(useChannel2_,0);
      channel2Panel.add(channel2Combo);
      channel2Panel.add(maxNumberSpotCh2Tf);
      channel2Panel.add(spotRadiusCh2Tf);
      channel2Panel.add(qualityCh2Tf);
      channel2Panel.add(exposureCh2Tf_);
      channel2Panel.add(test2);
       channel2Panel.add(ch2Button);
      chPanels.add(channel2Panel);

      //

      channel3Panel = new JPanel(new GridLayout(1, 0));
      channel3Combo = new JComboBox<>();
      channel3Combo.addActionListener(actionEvent -> updateChSetting(channel3Panel));
      maxNumberSpotCh3Tf = new JFormattedTextField(Integer.class);
      spotRadiusCh3Tf = new JFormattedTextField(Double.class);
      qualityCh3Tf = new JFormattedTextField(Double.class);
      JFormattedTextField exposureCh3Tf_ = new JFormattedTextField(Integer.class);
      test3 = new JButton("test");
      test3.addActionListener(this);
      maxNumberSpotCh3Tf.setText("");
      spotRadiusCh3Tf.setText("");
      qualityCh3Tf.setText("");
      useChannel3_ = new JCheckBox("", true);
      useChannel3_.addItemListener(itemEvent -> {
         if (useChannel3_.isSelected()){
            enableChannelPanel(channel3Panel, true);
         }else{
            enableChannelPanel(channel3Panel, false);
         }
      });
       final JRadioButton ch3Button = new JRadioButton("");
      channel3Panel.add(useChannel3_,0);
      channel3Panel.add(channel3Combo);
      channel3Panel.add(maxNumberSpotCh3Tf);
      channel3Panel.add(spotRadiusCh3Tf);
      channel3Panel.add(qualityCh3Tf);
      channel3Panel.add(exposureCh3Tf_);
      channel3Panel.add(test3);
      channel3Panel.add(ch3Button);
      chPanels.add(channel3Panel);

       //

      ButtonGroup group = new ButtonGroup();
      group.add(ch1Button);
      group.add(ch2Button);
      group.add(ch3Button);

      //

      JPanel configurationGroupPanel = new JPanel(new GridLayout(1,1));
      configurationGroupPanel.setBorder(BorderFactory.createTitledBorder("Configuration Group"));
      configurationCombo_ = new JComboBox<>(mm.getCore().getAvailableConfigGroups().toArray());
      configurationCombo_.addActionListener(this);
      configurationCombo_.setSelectedItem(parameters_.getChannelGroup());
      configurationGroupPanel.add(configurationCombo_);

      //

      JPanel mitosisDurationPanel = new JPanel(new GridLayout(1,1));
      mitosisDurationPanel.setBorder(BorderFactory.createTitledBorder("Minimum mitosis duration (s)"));
      mitosisDurationTf_ = new JFormattedTextField(Integer.class);
      mitosisDurationTf_.setText(parameters.getMinimumMitosisDuration());
      mitosisDurationPanel.add(mitosisDurationTf_);

      //

      saveFlims = new JCheckBox("Save Movies",
              Boolean.parseBoolean(parameters_.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES)));
      doAnalysis = new JCheckBox("Do Analysis", true);
      updateDoAnalysisButton();
      JPanel optionPanel = new JPanel(new GridLayout(1,4));
      optionPanel.add(saveFlims);
      optionPanel.add(doAnalysis);
      optionPanel.add(configurationGroupPanel);
      optionPanel.add(mitosisDurationPanel);

      //

      fluoAnaParamLabel.add(optionPanel);
      fluoAnaParamLabel.add(channelTitlePanel);
      fluoAnaParamLabel.add(channel1Panel);
      fluoAnaParamLabel.add(channel2Panel);
      fluoAnaParamLabel.add(channel3Panel);

      //

      okFluoAnaParamButton = new JButton("OK");
      okFluoAnaParamButton.addActionListener(this);
      add(okFluoAnaParamButton, BorderLayout.PAGE_END);

      //

      int j = 0;
      for (JPanel chPanel : chPanels) {
         JCheckBox tmpChkbox = (JCheckBox) chPanel.getComponent(0);
         if (j < arrayChannels.length) {
            JComboBox tmpChannelCombo = (JComboBox) chPanel.getComponent(1);
            tmpChannelCombo.setSelectedItem(arrayChannels[j]);
            tmpChkbox.setSelected(true);
            if (originSpindleChannel.equals(arrayChannels[j])) {
               JRadioButton tmpRadio = (JRadioButton) chPanel.getComponent(7);
               tmpRadio.setSelected(true);
            }
            j += 1;
         } else {
            tmpChkbox.setSelected(false);
         }
      }

      ch1Button.addActionListener(e -> ch1Button.setActionCommand((String) channel1Combo.getSelectedItem()));
      ch2Button.addActionListener(e -> ch2Button.setActionCommand((String) channel2Combo.getSelectedItem()));
      ch3Button.addActionListener(e -> ch3Button.setActionCommand((String) channel3Combo.getSelectedItem()));
      ch1Button.setActionCommand((String) channel1Combo.getSelectedItem());
      ch2Button.setActionCommand((String) channel2Combo.getSelectedItem());
      ch3Button.setActionCommand((String) channel3Combo.getSelectedItem());

      //

      this.pack();
      this.setVisible(true);
   }

   /**
    *
    * @param jp   the panel which contains informations about corresponding panel
    * @param ch   channel name
    */
   private void setChPanelValue(JPanel jp, String ch) {
      for (String channel : parameters_.getAllChannels()){
         if (ch != null && ch.equals(channel)){
             JCheckBox tmpChk = (JCheckBox) jp.getComponent(0);
             if (tmpChk.isSelected()){
                 JButton tmpButton = (JButton) jp.getComponent(TOTAL_PARAMETERS+1);
                 tmpButton.setEnabled(true);
             }
            JFormattedTextField tmpTf = (JFormattedTextField) jp.getComponent(2);
            tmpTf.setValue(parameters_.getChMaxNbSpot(ch));
            tmpTf = (JFormattedTextField) jp.getComponent(3);
            tmpTf.setValue(parameters_.getChSpotRaius(ch));
            tmpTf = (JFormattedTextField) jp.getComponent(4);
            tmpTf.setValue(parameters_.getChQuality(ch));
            tmpTf = (JFormattedTextField) jp.getComponent(5);
            tmpTf.setValue(parameters_.getChExposure(ch));
            JComboBox tmpCombo = (JComboBox)jp.getComponent(1);
            String tmpCh = (String) tmpCombo.getSelectedItem();
            if (tmpCh!=null && !tmpCh.equals(ch)){
               tmpCombo.setSelectedItem(ch);
            }
         }
      }
   }

   /**
    *
    */
   private void updateDoAnalysisButton(){
      if (Double.parseDouble(timeInterval.getText()) < 10000) {
         doAnalysis.setSelected(false);
         doAnalysis.setEnabled(false);
      } else {
         doAnalysis.setSelected(true);
         doAnalysis.setEnabled(true);
      }
   }

   /**
    *
    * @param jp   the panel which contains informations about corresponding panel
    * @param enable  enable or not
    */
   private void enableChannelPanel(JPanel jp, Boolean enable){
      if (enable) {
         for (int i = 1; i <= TOTAL_PARAMETERS+2; i++){
            jp.getComponent(i).setEnabled(true);
         }
      }else{
         for (int i = 1; i <= TOTAL_PARAMETERS+2; i++){
            jp.getComponent(i).setEnabled(false);
         }
      }

   }

   /**
    *
    * @param jp   the panel which contains informations about corresponding panel
    */
   private void updateChSetting(JPanel jp){
      JComboBox tmpCombo = (JComboBox) jp.getComponent(1);
      String selectedChannel = (String) tmpCombo.getSelectedItem();
      setChPanelValue(jp, selectedChannel);
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
      List<String> existingChannels = parameters_.getAllChannels();
      ArrayList<String> channels = new ArrayList<>();
      for (JPanel p : chPanels){
         JCheckBox tmpChkBox = (JCheckBox) p.getComponent(0);
         if (tmpChkBox.isSelected()){
            JComboBox tmpChannelCombo = (JComboBox) p.getComponent(1);
            String tmpChannel = (String) tmpChannelCombo.getSelectedItem();
            if (!existingChannels.contains(tmpChannel)){
               YesNoCancelDialog yesNoCancelDialog = new YesNoCancelDialog(null, "Channel not in config file", "add this channel " + tmpChannel + " into the config file?");
               if (yesNoCancelDialog.yesPressed()){
                  String shutterLabel = (String) JOptionPane.showInputDialog(this, "shutter for this channel ?","Shutter configuration", JOptionPane.QUESTION_MESSAGE, null,mm.getShutterManager().getShutterDevices().toArray() ,mm.getShutterManager().getShutterDevices().get(0));
                  String colorLabel = (String) JOptionPane.showInputDialog(this, "Color for this channel ?","Color configuration", JOptionPane.QUESTION_MESSAGE, null,parameters_.availiableColors(),parameters_.availiableColors()[0]);
                  parameters_.addChannel(tmpChannel);
                  parameters_.setChShutter(tmpChannel, shutterLabel);
                  parameters_.setChColor(tmpChannel, colorLabel);
               }else{
                  tmpChkBox.setSelected(false);
                  continue;
               }
            }
            JFormattedTextField tmpTf = (JFormattedTextField) p.getComponent(2);
            parameters_.setChMaxNbSpot(tmpChannel, tmpTf.getText());
            tmpTf = (JFormattedTextField) p.getComponent(3);
            parameters_.setChSpotRaius(tmpChannel, tmpTf.getText());
            tmpTf = (JFormattedTextField) p.getComponent(4);
            parameters_.setChQuality(tmpChannel, tmpTf.getText());
            tmpTf = (JFormattedTextField) p.getComponent(5);
            parameters_.setChExposure(tmpChannel, tmpTf.getText());
            channels.add(tmpChannel);
         }
      }
      parameters_.setUsingChannels(String.join(",", channels));

   }

   /**
    *
    * @param jp the panel which contains informations about corresponding panel
    * @param img image to detect spots on
    */
   private void testTrackmate(JPanel jp, ImagePlus img) {
      JFormattedTextField tmpTf = (JFormattedTextField) jp.getComponent(3);
      double spotRadius = Double.parseDouble((String) tmpTf.getValue());
      tmpTf = (JFormattedTextField) jp.getComponent(4);
      double quality = Double.parseDouble((String) tmpTf.getValue());
      ImagePlus zProjectedFluoImg = ImgUtils.zProject(img);
      zProjectedFluoImg.setCalibration(img.getCalibration());
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
    * @param jp the panel which contains informations about corresponding panel
    */
   private void testTrackmate(JPanel jp) {
      String channelName = getSelectedChannel(jp);
      String imgPath = parameters_.getSavingPath() + File.separator + "X0_Y0_FLUO" + File.separator
              + channelName + "_1" + File.separator + channelName + "_1_MMStack_Pos0.ome.tif";
      if (FileUtils.exists(imgPath)) {
         testTrackmate(jp, IJ.openImage(imgPath));
      } else {
         testTrackmate(jp, acquireTestImg(jp));
      }
   }

   /**
    *
    * @param jp the panel which contains informations about corresponding panel
    * @return get the image
    */
   private ImagePlus acquireTestImg(JPanel jp) {
      double zRange = Double.parseDouble(parameters_.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
      double zStep = Double.parseDouble(parameters_.getFluoParameter(MaarsParameters.STEP));

      MaarsParameters testParam = parameters_.duplicate();
      String channelName = getSelectedChannel(jp);
      testParam.setUsingChannels(channelName);
      testParam.setFluoParameter(MaarsParameters.TIME_LIMIT, "0");
      testParam.setFluoParameter(MaarsParameters.TIME_INTERVAL, this.timeInterval.getText());
      testParam.setFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES, "false");
      FluoAcqSetting acq = new FluoAcqSetting(testParam);

      SequenceSettings fluoAcqSetting = acq.configAcqSettings(acq.configChannels(channelName));
      fluoAcqSetting.keepShutterOpenSlices = true;
      AcquisitionWrapperEngine acqEng = mm.getAcquisitionEngine();
      acqEng.setSequenceSettings(fluoAcqSetting);
      acqEng.enableZSliceSetting(true);
      acqEng.setSlices(-zRange / 2, zRange / 2, zStep, false);
      acqEng.setChannelGroup(fluoAcqSetting.channelGroup);
      java.util.List<Image> imageList = AcqLauncher.acquire(acqEng);
      for (Image img : imageList){
         mm.live().displayImage(img);
      }
      return ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);
   }

   private String getSelectedChannel(JPanel jp) {
      JComboBox tmpCombo = (JComboBox) jp.getComponent(1);
      return (String) tmpCombo.getSelectedItem();
   }

   private void clearChannelSettings(){
      channel1Combo.removeAllItems();
      channel2Combo.removeAllItems();
      channel3Combo.removeAllItems();
      maxNumberSpotCh1Tf.setText("");
      spotRadiusCh1Tf.setText("");
      qualityCh1Tf.setText("");
      maxNumberSpotCh2Tf.setText("");
      spotRadiusCh2Tf.setText("");
      qualityCh2Tf.setText("");
      maxNumberSpotCh3Tf.setText("");
      spotRadiusCh3Tf.setText("");
      qualityCh3Tf.setText("");
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if (src == okFluoAnaParamButton) {
         for (JPanel jp : chPanels){
            JRadioButton tmpBut = (JRadioButton) jp.getComponent(TOTAL_PARAMETERS + 2);
            if (tmpBut.isSelected()){
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
      } else if (src == test1) {
         updateMAARSFluoChParameters();
         testTrackmate(channel1Panel);
      } else if (src == test2) {
         updateMAARSFluoChParameters();
         testTrackmate(channel2Panel);
      } else if (src == test3) {
         updateMAARSFluoChParameters();
         testTrackmate(channel3Panel);
      } else if (src == configurationCombo_) {
         String selectedGroup= (String) configurationCombo_.getSelectedItem();
         parameters_.setChannelGroup(selectedGroup);
         clearChannelSettings();
         String[] newConfigs = mm.getCore().getAvailableConfigs(selectedGroup).toArray();
         for (String s : newConfigs){
            channel3Combo.addItem(s);
            channel1Combo.addItem(s);
            channel2Combo.addItem(s);
         }
      }
   }
}
