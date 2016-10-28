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

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.internal.MMStudio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
   private JCheckBox saveFlims;
   private JCheckBox doAnalysis;
   private JButton test1;
   private JButton test2;
   private JButton test3;
   private JButton okFluoAnaParamButton;
   private JComboBox channel1Combo;
   private JComboBox channel2Combo;
   private JComboBox channel3Combo;
   private JComboBox configurationCombo_;
   private JCheckBox useChannel1_;
   private JCheckBox useChannel2_;
   private JCheckBox useChannel3_;
   private static Integer TOTAL_PARAMETERS = 4;
   private ArrayList<JPanel> chPanels = new ArrayList<JPanel>();

   /**
    * Constructor :
    *
    * @param parameters : parameters_ displayed in dialog
    */
   public MaarsFluoAnalysisDialog(MMStudio mm, MaarsParameters parameters) {

      // set up this dialog
      this.mm = mm;
      this.parameters_ = parameters;
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      // this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      this.setTitle("MAARS - Fluorescent Analysis parameters_");
      this.setBackground(Color.WHITE);
      this.setLayout(new GridLayout(0, 1));
      this.setMinimumSize(new Dimension(250, 500));
      Color labelColor = Color.ORANGE;

      // Movie parameters_ label

      Label fluoMovieLabel = new Label("Movie parameters_", Label.CENTER);
      fluoMovieLabel.setBackground(labelColor);

      //

      JPanel fluoRangePanel = new JPanel(new GridLayout(1, 2));
      JLabel rangeTitle = new JLabel("Range (micron) : ", SwingConstants.CENTER);
      int fieldLength = 8;
      range = new JTextField(parameters_.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE), fieldLength);
      fluoRangePanel.add(rangeTitle);
      fluoRangePanel.add(range);

      //

      JPanel fluoStepPanel = new JPanel(new GridLayout(1, 2));
      JLabel stepTitle = new JLabel("Step (micron) : ", SwingConstants.CENTER);
      step = new JTextField(parameters_.getFluoParameter(MaarsParameters.STEP), fieldLength);
      fluoStepPanel.add(stepTitle);
      fluoStepPanel.add(step);

      //

      JPanel timeIntervalPanel = new JPanel(new GridLayout(1, 2));
      JLabel timeIntervalTitle = new JLabel("Time Interval (ms) : ", SwingConstants.CENTER);
      timeInterval = new JTextField(parameters_.getFluoParameter(MaarsParameters.TIME_INTERVAL), fieldLength);
      timeIntervalPanel.add(timeIntervalTitle);
      timeIntervalPanel.add(timeInterval);
      timeInterval.addKeyListener(new KeyListener() {
         @Override
         public void keyTyped(KeyEvent e) {
         }

         @Override
         public void keyReleased(KeyEvent e) {
            if (Double.parseDouble(timeInterval.getText()) < 10000) {
               doAnalysis.setSelected(false);
               doAnalysis.setEnabled(false);
            } else {
               doAnalysis.setSelected(true);
               doAnalysis.setEnabled(true);
            }
         }

         @Override
         public void keyPressed(KeyEvent e) {
         }
      });

      //

      JPanel checkBoxPanel = new JPanel(new GridLayout(1, 0));
      saveFlims = new JCheckBox("Save Movies",
              Boolean.parseBoolean(parameters_.getFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES)));
      doAnalysis = new JCheckBox("Do Analysis", true);
      checkBoxPanel.add(saveFlims);
      checkBoxPanel.add(doAnalysis);

      //

      Label fluoAnaParamLabel = new Label("Spot identification parameter(s)", Label.CENTER);
      fluoAnaParamLabel.setBackground(labelColor);

      //

      JPanel channelTitlePanel = new JPanel(new GridLayout(1, 0));
      JLabel channelCheckTitle = new JLabel("Channels", SwingConstants.CENTER);
      JLabel fluoChannelsTitle = new JLabel("Config", SwingConstants.CENTER);
      JLabel maxNbSpotTitle = new JLabel("Max # of spot", SwingConstants.CENTER);
      JLabel spotRaiusTitle = new JLabel("Spot Radius", SwingConstants.CENTER);
      JLabel qualityTitle = new JLabel("Quality", SwingConstants.CENTER);
      channelTitlePanel.add(channelCheckTitle);
      channelTitlePanel.add(fluoChannelsTitle);
      channelTitlePanel.add(maxNbSpotTitle);
      channelTitlePanel.add(spotRaiusTitle);
      channelTitlePanel.add(qualityTitle);
      channelTitlePanel.add(new JLabel());

      //

      channel1Panel = new JPanel(new GridLayout(1, 0));

      channel1Combo = new JComboBox();
      channel1Combo.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            updateChSetting(channel1Panel);
         }
      });
      maxNumberSpotCh1Tf = new JFormattedTextField(Integer.class);
      spotRadiusCh1Tf = new JFormattedTextField(Double.class);
      qualityCh1Tf = new JFormattedTextField(Double.class);
      test1 = new JButton("test");
      test1.setEnabled(false);
      test1.addActionListener(this);
      useChannel1_ = new JCheckBox("",true);
      useChannel1_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            if (useChannel1_.isSelected()){
               enableChannelPanel(channel1Panel, true);
            }else{
               enableChannelPanel(channel1Panel, false);
            }
         }
      });
      channel1Panel.add(useChannel1_);
      channel1Panel.add(channel1Combo);
      channel1Panel.add(maxNumberSpotCh1Tf);
      channel1Panel.add(spotRadiusCh1Tf);
      channel1Panel.add(qualityCh1Tf);
      channel1Panel.add(test1);
      chPanels.add(channel1Panel);
      //

      channel2Panel = new JPanel(new GridLayout(1, 0));

      channel2Combo = new JComboBox();
      channel2Combo.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            updateChSetting(channel2Panel);
         }
      });
      maxNumberSpotCh2Tf = new JFormattedTextField(Integer.class);
      spotRadiusCh2Tf = new JFormattedTextField(Double.class);
      qualityCh2Tf = new JFormattedTextField(Double.class);
      test2 = new JButton("test");
      test2.setEnabled(false);
      test2.addActionListener(this);
      maxNumberSpotCh2Tf.setText("");
      spotRadiusCh2Tf.setText("");
      qualityCh2Tf.setText("");
      useChannel2_ = new JCheckBox("",true);
      useChannel2_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            if (useChannel2_.isSelected()){
               enableChannelPanel(channel2Panel, true);
            }else{
               enableChannelPanel(channel2Panel, false);
            }
         }
      });
      channel2Panel.add(useChannel2_);
      channel2Panel.add(channel2Combo);
      channel2Panel.add(maxNumberSpotCh2Tf);
      channel2Panel.add(spotRadiusCh2Tf);
      channel2Panel.add(qualityCh2Tf);
      channel2Panel.add(test2);
      chPanels.add(channel2Panel);

      //

      channel3Panel = new JPanel(new GridLayout(1, 0));
      channel3Combo = new JComboBox();
      channel3Combo.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            updateChSetting(channel3Panel);
         }
      });
      maxNumberSpotCh3Tf = new JFormattedTextField(Integer.class);
      spotRadiusCh3Tf = new JFormattedTextField(Double.class);
      qualityCh3Tf = new JFormattedTextField(Double.class);
      test3 = new JButton("test");
      test3.setEnabled(false);
      test3.addActionListener(this);
      maxNumberSpotCh3Tf.setText("");
      spotRadiusCh3Tf.setText("");
      qualityCh3Tf.setText("");
      useChannel3_ = new JCheckBox("",true);
      useChannel3_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            if (useChannel3_.isSelected()){
               enableChannelPanel(channel3Panel, true);
            }else{
               enableChannelPanel(channel3Panel, false);
            }
         }
      });
      channel3Panel.add(useChannel3_);
      channel3Panel.add(channel3Combo);
      channel3Panel.add(maxNumberSpotCh3Tf);
      channel3Panel.add(spotRadiusCh3Tf);
      channel3Panel.add(qualityCh3Tf);
      channel3Panel.add(test3);
      chPanels.add(channel3Panel);

      //

      okFluoAnaParamButton = new JButton("OK");
      okFluoAnaParamButton.addActionListener(this);

      //


      JPanel configurationGroupPanel = new JPanel();
      configurationGroupPanel.setLayout(new BorderLayout(1,2));

      Label configurationGroupLabel = new Label("Configuration Group", Label.CENTER);
      configurationGroupLabel.setBackground(Color.lightGray);

      configurationCombo_ = new JComboBox(mm.getCore().getAvailableConfigGroups().toArray());
      configurationCombo_.addActionListener(this);
      configurationCombo_.setSelectedItem(parameters_.getChannelGroup());
      configurationGroupPanel.add(configurationGroupLabel, BorderLayout.NORTH);
      configurationGroupPanel.add(configurationCombo_, BorderLayout.SOUTH);

      //


      String channelsString = parameters_.getUsingChannels();
      String[] arrayChannels = channelsString.split(",", -1);
      for (int i = 0; i<arrayChannels.length; i++){
         JComboBox tmpChannelCombo = (JComboBox) chPanels.get(i).getComponent(1);
         tmpChannelCombo.setSelectedItem(arrayChannels[i]);
      }

      //


      JPanel mainPanel = new JPanel();
      mainPanel.setBackground(Color.WHITE);
      mainPanel.setLayout(new GridLayout(0, 1));
      mainPanel.setMinimumSize(new Dimension(250, 500));
      mainPanel.add(fluoMovieLabel);
      mainPanel.add(fluoRangePanel);
      mainPanel.add(fluoStepPanel);
      mainPanel.add(timeIntervalPanel);
      mainPanel.add(checkBoxPanel);
      mainPanel.add(fluoAnaParamLabel);
      mainPanel.add(configurationGroupPanel);
      mainPanel.add(channelTitlePanel);
      mainPanel.add(channel1Panel);
      mainPanel.add(channel2Panel);
      mainPanel.add(channel3Panel);
      mainPanel.add(okFluoAnaParamButton);
      this.add(mainPanel);

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
      for (Element element : parameters_.getAllChannels()){
         if (ch != null && ch.equals(element.getName())){
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
    * @param jp   the panel which contains informations about corresponding panel
    * @param enable  enable or not
    */
   private void enableChannelPanel(JPanel jp, Boolean enable){
      if (enable) {
         for (int i = 1; i <= TOTAL_PARAMETERS+1; i++){
            jp.getComponent(i).setEnabled(true);
         }
      }else{
         for (int i = 1; i <= TOTAL_PARAMETERS+1; i++){
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
    * retrieve parameters_ from interface and update parameter object, then
    * return selected channels
    *
    * @return channels : list of selected object
    */
   private void updateMAARSFluoChParameters() {
      ArrayList<String> channels = new ArrayList<String>();
      for (JPanel p : chPanels){
         JCheckBox tmpChkBox = (JCheckBox) p.getComponent(0);
         if (tmpChkBox.isSelected()){
            JComboBox tmpChannelCombo = (JComboBox) p.getComponent(1);
            String tmpChannel = (String) tmpChannelCombo.getSelectedItem();
            JFormattedTextField tmpTf = (JFormattedTextField) p.getComponent(2);
            parameters_.setChMaxNbSpot(tmpChannel, tmpTf.getText());
            tmpTf = (JFormattedTextField) p.getComponent(3);
            parameters_.setChSpotRaius(tmpChannel, tmpTf.getText());
            tmpTf = (JFormattedTextField) p.getComponent(4);
            parameters_.setChQuality(tmpChannel, tmpTf.getText());
            channels.add(tmpChannel);
         }
      }
      parameters_.setUsingChannels(StringUtils.join(channels.toArray(), ","));

   }

   /**
    *
    * @param jp the panel which contains informations about corresponding panel
    * @param img image to detect spots on
    */
   private void testTrackmate(JPanel jp, ImagePlus img) {
      JFormattedTextField tmpTf = (JFormattedTextField) jp.getComponent(2);
      double spotRadius = Double.parseDouble((String) tmpTf.getValue());
      tmpTf = (JFormattedTextField) jp.getComponent(3);
      double quality = Double.parseDouble((String) tmpTf.getValue());
      // ImagePlus img = IJ.getImage().duplicate();
      img.show();
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
      String imgPath = parameters_.getSavingPath() + File.separator + "X0_Y0_FLUO" + File.separator + "0_"
              + channelName + "_1" + File.separator + "0_" + channelName + "_1_MMStack_Pos0.ome.tif";
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
      return AcqLauncher.acquire(acqEng);
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
         parameters_.setFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE, range.getText());
         parameters_.setFluoParameter(MaarsParameters.STEP, step.getText());
         parameters_.setFluoParameter(MaarsParameters.DO_ANALYSIS, String.valueOf(doAnalysis.isSelected()));
         parameters_.setFluoParameter(MaarsParameters.SAVE_FLUORESCENT_MOVIES,
                 String.valueOf(saveFlims.isSelected()));
         parameters_.setFluoParameter(MaarsParameters.TIME_INTERVAL, timeInterval.getText());
         updateMAARSFluoChParameters();
         try {
            parameters_.save();
         } catch (IOException e1) {
            System.out.println("Can not save MAARS parameters_");
            e1.printStackTrace();
         }
         this.setVisible(false);
      } else if (src == test1) {
         testTrackmate(channel1Panel);
      } else if (src == test2) {
         testTrackmate(channel2Panel);
      } else if (src == test3) {
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
