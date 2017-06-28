package maars.headless;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;
import maars.gui.MaarsFluoAnalysisDialog;
import maars.gui.MaarsSegmentationDialog;
import maars.io.IOUtils;
import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import maars.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main of MAARS without GUI configuration of MAARS parameters
 * Created by tongli on 28/04/2017.
 */
public class GuiFreeRun implements PlugIn {
   public static MaarsParameters loadMaarsParameters(String configFileName, String rootDir) {
      if (rootDir == null) {
         JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new File("."));
         chooser.setDialogTitle("Directory of MAARS folder");
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            rootDir = String.valueOf(chooser.getSelectedFile());
         } else {
            IJ.error("No folder Selected");
         }
      }
      MaarsParameters parameters;
      InputStream inStream;
      if (FileUtils.exists(rootDir + File.separator + configFileName)) {
         try {
            inStream = new FileInputStream(rootDir + File.separator + configFileName);
            parameters = new MaarsParameters(inStream);
            return parameters;
         } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
         }
         return null;

      } else {
         inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
         parameters = new MaarsParameters(inStream);
         parameters.setSavingPath(rootDir);
         new MaarsSegmentationDialog(parameters, null);
         Maars_Interface.post_segmentation(parameters);
         MaarsFluoAnalysisDialog fluoAnalysisDialog = new MaarsFluoAnalysisDialog(parameters);
         return fluoAnalysisDialog.getParameters();
      }
   }

   private static MaarsParameters loadMaarsParameters(String configFileName) {
      return loadMaarsParameters(configFileName, null);
   }

   private static void createDialog(){
      JDialog dialog = new JDialog();
      dialog.setMinimumSize(new Dimension(400,300));
      dialog.setTitle("Choose directories to analyze");
      dialog.setLayout(new GridLayout(0,1));
      String[] methods = new String[]{"Segmentation", "FluoAnalysis"};
      JComboBox<String> methdoComBox = new JComboBox<>(methods);
      dialog.add(methdoComBox);
      dialog.add(new JLabel("MAARS config file name"));
      JFormattedTextField maarConfigTf = new JFormattedTextField(String.class);
      maarConfigTf.setText("maars_config.xml");
      dialog.add(maarConfigTf);
      dialog.add(new JLabel("Path(s) to folder of MAARS"));
      ArrayList<JTextField> pathToFolderTfs = new ArrayList<>();
      for (JTextField tf:pathToFolderTfs){
         dialog.add(tf);
      }
      JButton okbut = new JButton("Ok");
      JButton addBut = new JButton("add");
      int fInd = 4;
      addBut.addActionListener(o1->{
         pathToFolderTfs.add(new JTextField("",20));
         int counter = fInd;
         for (JTextField tf:pathToFolderTfs){
            dialog.add(tf, counter);
            counter+=1;
         }
         dialog.validate();
      });
      JButton clearBut = new JButton("clear");
      clearBut.addActionListener(o->{
         for (JTextField tf:pathToFolderTfs){
            dialog.remove(tf);
         }
         pathToFolderTfs.clear();
         pathToFolderTfs.add(new JTextField("",20));
         for (JTextField tf:pathToFolderTfs){
            dialog.add(tf,fInd);
         }
         dialog.validate();
      });
      JButton removeBut = new JButton("remove");
      removeBut.addActionListener(o2->{
         for (JTextField tf:pathToFolderTfs){
            dialog.remove(tf);
         }
         pathToFolderTfs.remove(pathToFolderTfs.size()-1);
         for (JTextField tf:pathToFolderTfs){
            dialog.add(tf,fInd);
         }
         dialog.validate();
      });
      JButton validBut = new JButton("validate");
      validBut.addActionListener(o4->{
         for (JTextField tf:pathToFolderTfs) {
            if (!validateMaarsDir(tf.getText(), maarConfigTf.getText())) {
               okbut.setEnabled(false);
               tf.setForeground(Color.RED);
               tf.validate();
            } else {
               okbut.setEnabled(true);
               tf.setForeground(Color.BLACK);
               tf.validate();
            }
         }
      });
      JPanel butPanel = new JPanel();
      butPanel.add(addBut);
      butPanel.add(removeBut);
      butPanel.add(clearBut);
      butPanel.add(validBut);
      dialog.add(butPanel);
      okbut.setEnabled(false);
      okbut.addActionListener(o3->{
         okbut.setEnabled(false);
         if (!validatePaths(pathToFolderTfs, maarConfigTf.getText())){
            IJ.error("Invalid MAARS folder.");
         }else{
            for (JTextField tf:pathToFolderTfs) {
               MaarsParameters parameters = loadMaarsParameters(maarConfigTf.getText(), tf.getText());
               parameters.setSavingPath(tf.getText());
               String[] imgNames = Maars_Interface.getBfImgs(parameters);
               String[] posNbs = Maars_Interface.getPosNbs(imgNames);
               if (methdoComBox.getSelectedIndex() == 0) {
                  Maars_Interface.post_segmentation(parameters, imgNames, posNbs);
               }else{
                  Maars_Interface.post_fluoAnalysis(posNbs, tf.getText(), parameters);
               }
               IJ.showMessage(methods[methdoComBox.getSelectedIndex()] + " done");
            }
         }
      });
      dialog.add(okbut);
      dialog.setVisible(true);
   }

   static boolean validatePaths(ArrayList<JTextField> listTfs, String configName){
      for (JTextField tf : listTfs){
         if (!validateMaarsDir(tf.getText(), configName)){
            tf.setForeground(Color.RED);
            tf.validate();
            return false;
         }else{
            tf.setForeground(Color.BLACK);
            tf.validate();
         }
      }
      return true;
   }

   static boolean validateMaarsDir(String path, String configName){
      String root = path + File.separator;
      return FileUtils.exists(path) && FileUtils.exists(root + configName) &&
            FileUtils.exists(root  + Maars_Interface.SEG) && FileUtils.exists(root  + Maars_Interface.FLUO);
   }

   public static void main(String[] args) {
      new ImageJ();
      Maars_Interface.copyDeps();
      createDialog();


//      //executeAnalysis(fluoAnalysisDialog.getParameters());
//      DefaultSetOfCells soc = new DefaultSetOfCells(0);
//      MAARS.analyzeMitosisDynamic(soc,parameters,dir + "/BF_1");

//        for (String s : new File(".").list()){
//            System.out.println(s);
//        }

//        //        chooser.setAcceptAllFileFilterUsed(false);
//        FileNameExtensionFilter posListFilter = new FileNameExtensionFilter(
//                "MM acquisition setting file ", "txt");
//        chooser.setFileFilter(posListFilter);

//        concatenatedImg.setProperty("Info", im.getInfoProperty());

//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(IJ.openImage("/Volumes/Macintosh/curioData/new_format/27C_3_MMStack_102_1.ome.tif",1).getInfoProperty());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        ArrayList<String> arrayChannels = new ArrayList<>();
//        try {
//
//            for (int i=0; i<jsonObject.getJSONArray("ChNames").length(); i++){
//                arrayChannels.add(jsonObject.getJSONArray("ChNames").getString(i));
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        System.out.println(arrayChannels);
////        ArrayList<String> arrayChannels = (ArrayList) map.get("ChNames");
//        int totalChannel = MAARSNoAcq.extractFromOMEmetadata(jsonObject, "channel");
//        int totalSlice = MAARSNoAcq.extractFromOMEmetadata(jsonObject, "z");
//        int totalFrame = MAARSNoAcq.extractFromOMEmetadata(jsonObject, "time");
////               totalPosition = (int) ((Map)map.get("IntendedDimensions")).get("position");
//
//        IJ.log("Re-stack image : channel " + totalChannel +", slice " + totalSlice + ", frame " + totalFrame);

//        String jsonTxt = null;
//        try {
//            InputStream is = new FileInputStream("/Volumes/Macintosh/cloudDrives/Dropbox/AcqSettings_fluo.txt");
//            jsonTxt = org.apache.commons.IOUtils.toString(is);
//            System.out.println(jsonTxt);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(jsonTxt);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            System.out.println(((JSONObject)((JSONArray)jsonObject.get("channels")).get(0)).get("config"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


//        TmXmlReader reader = new TmXmlReader(new File(
//                "/Volumes/Macintosh/curioData/102/60x/26-10-1/X0_Y0_FLUO/spots/1_GFP.xml"));
//        Model model = reader.getModel();
//        System.out.println(reader.getModel().set);


//        ImagePlus imp = IJ.openImage("/Volumes/Macintosh/curioData/MAARSdata/102/16-06-1/X0_Y0_FLUO/CFP_2/CFP_2_MMStack_Pos0.ome.tif");
//        imp.show();
//        Model model = new Model();
//        model.setLogger(Logger.IJ_LOGGER);
//        Settings settings = new Settings();
//        settings.setFrom(imp);
//        settings.detectorFactory = new LogDetectorFactory<>();
//        settings.detectorSettings = new HashMap<>();
//        settings.detectorSettings.put("DO_SUBPIXEL_LOCALIZATION", true);
//        settings.detectorSettings.put("RADIUS", 0.25);
//        settings.detectorSettings.put("TARGET_CHANNEL", 1);
//        settings.detectorSettings.put("THRESHOLD", 0.);
//        settings.detectorSettings.put("DO_MEDIAN_FILTERING", true);
//
//        FeatureFilter filter1 = new FeatureFilter("QUALITY", 5, true);
//        settings.addSpotFilter(filter1);
//
//        settings.trackerFactory = new SparseLAPTrackerFactory();
//        settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
//        settings.trackerSettings.put("ALLOW_TRACK_SPLITTING", true);
//        settings.trackerSettings.put("ALLOW_TRACK_MERGING", true);
//
//        settings.addTrackAnalyzer(new TrackDurationAnalyzer());
//
//        FeatureFilter filter2 = new FeatureFilter("TRACK_DISPLACEMENT", 2, true);
//        settings.addTrackFilter(filter2);
//
//        TrackMate trackmate = new TrackMate(model, settings);
//
//        Boolean ok = trackmate.checkInput();
//        if (!ok) {
//            IJ.log(trackmate.getErrorMessage());
//        }
//
//        ok = trackmate.process();
//        if (!ok) {
//            IJ.log(trackmate.getErrorMessage());
//        }
//
//        SelectionModel selectionModel = new SelectionModel(model);
//        HyperStackDisplayer displayer = new  HyperStackDisplayer(model, selectionModel, imp);
//        displayer.render();
//        displayer.refresh();

   }

   @Override
   public void run(String s) {
      Maars_Interface.copyDeps();
      createDialog();
//        byte[] encoded = new byte[0];
//        try {
//            encoded = Files.readAllBytes(Paths.get("/home/tong/Desktop/new_mda/AcqSettings_bf.txt"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        PositionList pl = new PositionList();
//        try {
//            pl.load("/home/tong/Desktop/new_mda/PositionList.pos");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(pl.getPosition(0).getX());
   }
}
