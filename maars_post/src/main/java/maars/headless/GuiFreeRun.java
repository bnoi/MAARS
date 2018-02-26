package maars.headless;

import fiji.Debug;
public class GuiFreeRun  {
   public static void main(String[] args) {
      Debug.run("Script...", "");
   }
}


//package maars.headless;
//
//import ij.IJ;
//import ij.ImageJ;
//import ij.plugin.PlugIn;
//import maars.gui.MaarsFluoAnalysisDialog;
//import maars.gui.MaarsSegmentationDialog;
//import maars.io.IOUtils;
//import maars.main.MaarsParameters;
//import maars.main.Maars_Interface;
//import maars.utils.FileUtils;
//
//import javax.swing.*;
//import java.awt.*;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * The main of MAARS without GUI configuration of MAARS parameters
// * Created by tongli on 28/04/2017.
// */
//public class GuiFreeRun implements PlugIn, Runnable {
//   public static MaarsParameters loadMaarsParameters(String configFileName, String rootDir) {
//      if (rootDir == null) {
//         JFileChooser chooser = new JFileChooser();
//         chooser.setCurrentDirectory(new File("."));
//         chooser.setDialogTitle("Directory of MAARS folder");
//         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//            rootDir = String.valueOf(chooser.getSelectedFile());
//         } else {
//            IJ.error("No folder Selected");
//         }
//      }
//      MaarsParameters parameters;
//      InputStream inStream;
//      if (FileUtils.exists(rootDir + File.separator + configFileName)) {
//         try {
//            inStream = new FileInputStream(rootDir + File.separator + configFileName);
//            parameters = new MaarsParameters(inStream);
//            return parameters;
//         } catch (FileNotFoundException e) {
//            IOUtils.printErrorToIJLog(e);
//         }
//         return null;
//
//      } else {
//         inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
//         parameters = new MaarsParameters(inStream);
//         parameters.setSavingPath(rootDir);
//         new MaarsSegmentationDialog(parameters, null);
//         Maars_Interface.post_segmentation(parameters);
//         MaarsFluoAnalysisDialog fluoAnalysisDialog = new MaarsFluoAnalysisDialog(parameters);
//         return fluoAnalysisDialog.getParameters();
//      }
//   }
//
//   private static void createDialog(){
//      JDialog dialog = new JDialog();
//      dialog.setMinimumSize(new Dimension(400,300));
//      dialog.setTitle("Choose directories to analyze");
//      dialog.setLayout(new GridLayout(0,1));
//      String[] methods = new String[]{"Segmentation", "FluoAnalysis"};
//      JComboBox<String> methdoComBox = new JComboBox<>(methods);
//      dialog.add(methdoComBox);
//      dialog.add(new JLabel("Path to MAARS config file"));
//      JFormattedTextField maarConfigTf = new JFormattedTextField(String.class);
//      maarConfigTf.setText("maars_config.xml");
//      dialog.add(maarConfigTf);
//      dialog.add(new JLabel("Path(s) to folder of MAARS"));
//      ArrayList<JTextField> pathToFolderTfs = new ArrayList<>();
//      for (JTextField tf:pathToFolderTfs){
//         dialog.add(tf);
//      }
//      JButton okbut = new JButton("Ok");
//      JButton addBut = new JButton("add");
//      int fInd = 4;
//      addBut.addActionListener(o1->{
//         pathToFolderTfs.add(new JTextField("",20));
//         int counter = fInd;
//         for (JTextField tf:pathToFolderTfs){
//            dialog.add(tf, counter);
//            counter+=1;
//         }
//         dialog.validate();
//      });
//      JButton clearBut = new JButton("clear");
//      clearBut.addActionListener(o->{
//         for (JTextField tf:pathToFolderTfs){
//            dialog.remove(tf);
//         }
//         pathToFolderTfs.clear();
//         pathToFolderTfs.add(new JTextField("",20));
//         for (JTextField tf:pathToFolderTfs){
//            dialog.add(tf,fInd);
//         }
//         dialog.validate();
//      });
//      JButton removeBut = new JButton("remove");
//      removeBut.addActionListener(o2->{
//         for (JTextField tf:pathToFolderTfs){
//            dialog.remove(tf);
//         }
//         pathToFolderTfs.remove(pathToFolderTfs.size()-1);
//         for (JTextField tf:pathToFolderTfs){
//            dialog.add(tf,fInd);
//         }
//         dialog.validate();
//      });
//      JButton validBut = new JButton("validate");
//      validBut.addActionListener(o4->{
//         for (JTextField tf:pathToFolderTfs) {
//            if (!isOkToProcess(tf.getText(), maarConfigTf.getText())) {
//               okbut.setEnabled(false);
//               tf.setForeground(Color.RED);
//               tf.validate();
//            } else {
//               okbut.setEnabled(true);
//               tf.setForeground(Color.BLACK);
//               tf.validate();
//            }
//         }
//      });
//      JPanel butPanel = new JPanel();
//      butPanel.add(addBut);
//      butPanel.add(removeBut);
//      butPanel.add(clearBut);
//      butPanel.add(validBut);
//      dialog.add(butPanel);
//      okbut.setEnabled(false);
//      okbut.addActionListener(o3->{
//         okbut.setEnabled(false);
//         if (!validatePaths(pathToFolderTfs, maarConfigTf.getText())){
//            IJ.error("Invalid MAARS folder.");
//         }else{
//            int selection = 0;
//            MaarsParameters parameters = null;
//            if (methdoComBox.getSelectedIndex() == 1) {
//               selection = 1;
//            }else if(methdoComBox.getSelectedIndex() == 2){
//               selection = 2;
//            } else{
//               try {
//                  parameters = new MaarsParameters(
//                        new FileInputStream(maarConfigTf.getText()));
//               } catch (FileNotFoundException e) {
//                  e.printStackTrace();
//               }
//            }
//            ExecutorService es = Executors.newSingleThreadExecutor();
//            for (JTextField tf:pathToFolderTfs) {
//
//               String currentFolder = tf.getText();
//               String[] split = currentFolder.split("/");
//               int endIndex = currentFolder.lastIndexOf("/");
//               String savingRoot = null;
//               if (endIndex != -1) {
//                  savingRoot = currentFolder.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
//               }
//               if (parameters==null) {
//                  try {
//                     parameters = new MaarsParameters(
//                           new FileInputStream(savingRoot + File.separator + MaarsParameters.DEFAULT_CONFIG_NAME));
//                  } catch (FileNotFoundException e) {
//                     e.printStackTrace();
//                  }
//               }
//               parameters.setSavingPath(savingRoot);
//               if (selection==0){
//                  parameters.setSegmentationParameter(MaarsParameters.SEG_PREFIX, split[split.length-1]);
//                  parameters.save(savingRoot);
//               } else if (selection==1){
//                  parameters.setFluoParameter(MaarsParameters.FLUO_PREFIX, split[split.length-1]);
//                  parameters.save(savingRoot);
//               }
//               if (selection==0) {
//                  Maars_Interface.post_segmentation(parameters);
//               }else if(selection == 1){
//                  es.execute(new MaarsFluoAnalysis(parameters));
//               }
////               IJ.showMessage(methods[methdoComBox.getSelectedIndex()] + " done");
//            }
//            es.shutdown();
//         }
//      });
//      dialog.add(okbut);
//      dialog.setVisible(true);
//   }
//
//   static boolean validatePaths(ArrayList<JTextField> listTfs, String configName){
//      for (JTextField tf : listTfs){
//         if (!isOkToProcess(tf.getText(), configName)){
//            tf.setForeground(Color.RED);
//            tf.validate();
//            return false;
//         }else{
//            tf.setForeground(Color.BLACK);
//            tf.validate();
//         }
//      }
//      return true;
//   }
//
//   static boolean isOkToProcess(String tiffFolder, String configName){
//      return FileUtils.exists(tiffFolder) && FileUtils.exists(configName);
////            && FileUtils.containsTiffFile(tiffFolder);
//   }
//}
