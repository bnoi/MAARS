//package maars.main;
//
//import maars.display.SOCVisualizer;
//import maars.io.IOUtils;
//import mmcorej.CMMCore;
//import org.micromanager.internal.MMStudio;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.PrintStream;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.Future;
//
///**
// * Main MaarsOTFSeg program
// *
// * @author Tong LI, mail: tongli.bioinfo@gmail.com
// * @version Nov 21, 2015
// */
//public class MAARS implements Runnable {
//   private static boolean stop_ = false;
//   private PrintStream curr_err;
//   private PrintStream curr_out;
//   private MMStudio mm;
//   private CMMCore mmc;
//   private MaarsParameters parameters;
//   private SocSet socSet_;
//   private HashMap<String, SOCVisualizer> socVisualizerList_;
//   private CopyOnWriteArrayList<Map<String, Future>> tasksSet_;
//
//   /**
//    * * Constructor
//    *
//    * @param mm                MMStudio object (gui)
//    * @param mmc               CMMCore object (core)
//    * @param parameters        MaarsOTFSeg parameters object
//    * @param socVisualizerList list of set of cell visualizer
//    * @param tasksSet          tasks to be terminated
//    * @param socSet            list of set of cell
//    */
//   public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters, HashMap<String, SOCVisualizer> socVisualizerList,
//                CopyOnWriteArrayList<Map<String, Future>> tasksSet,
//                SocSet socSet) {
//      this.mmc = mmc;
//      this.parameters = parameters;
//      socSet_ = socSet;
//      this.mm = mm;
//      tasksSet_ = tasksSet;
//      socVisualizerList_ = socVisualizerList;
//   }
//
//   private void redirectLog(String savingPath) {
//      try {
//         PrintStream ps = new PrintStream(savingPath + File.separator + "FluoAnalysis.LOG");
//         curr_err = System.err;
//         curr_out = System.err;
//         System.setOut(ps);
//         System.setErr(ps);
//      } catch (FileNotFoundException e) {
//         IOUtils.printErrorToIJLog(e);
//      }
//   }
//
//   public void interrupt() {
//      stop_ = true;
//   }
//
//   @Override
//   public void run() {
////      int frame = 0;
////      // Start time
////      long start = System.currentTimeMillis();
////      parameters.setCalibration(String.valueOf(mm.getCachedPixelSizeUm()));
////      ArrayList<String> arrayChannels = new ArrayList<>();
////      Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));
////      String savingPath = FileUtils.convertPath(parameters.getSavingPath());
////      String segPath = savingPath + File.separator + Maars_Interface.SEG;
////      String fluoPath = savingPath + File.separator + Maars_Interface.FLUO;
////      //acquisition
////      Datastore fullFluoDs = null;
////      Datastore fullSegDs = null;
////      try {
////         fullSegDs = mm.data().createMultipageTIFFDatastore(segPath, true, true);
////         fullFluoDs = mm.data().createMultipageTIFFDatastore(fluoPath, true, true);
////      } catch (IOException e) {
////         e.printStackTrace();
////      }
////      Datastore segDs = null;
////      Datastore fluoDs = null;
////      ExecutorService es = Executors.newSingleThreadExecutor();
////      try {
////         segDs = es.submit(new MAARS_mda(
////               parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING))).get();
////      } catch (InterruptedException | ExecutionException e) {
////         IOUtils.printErrorToIJLog(e);
////      }
////      ArrayList<Image> segImgs = maars.mmUtils.ImgMMUtils.dsToSortedList(segDs, frame);
////      for (Image img: segImgs){
////         try {
////            fullSegDs.putImage(img);
////         } catch (DatastoreFrozenException | DatastoreRewriteException e) {
////            e.printStackTrace();
////         }
////      }
////      HashMap<String, ImagePlus[]> segImps = maars.mmUtils.ImgMMUtils.convertImages2Imp(segImgs,
////            segDs.getSummaryMetadata(), mm.getCore().getPixelSizeUm());
////
////      if (segImgs.size() ==0) {
////         IJ.log("No images acquired");
////         return;
////      }
////      MaarsSegmentation ms;
////      HashMap<String, MaarsSegmentation> arrayMs = new HashMap<>();
////      for (String posNb : segImps.keySet()) {
////         ImagePlus segImg = segImps.get(posNb)[0];
////         // --------------------------segmentation-----------------------------//
////         ms = new MaarsSegmentation(parameters, segImg, posNb);
////         arrayMs.put(posNb, ms);
////         try {
////            es.submit(ms).get();
////         } catch (InterruptedException | ExecutionException e) {
////            IOUtils.printErrorToIJLog(e);
////         }
////      }
////      es.shutdown();
////      try {
////         es.awaitTermination(3, TimeUnit.MINUTES);
////      } catch (InterruptedException e) {
////         IOUtils.printErrorToIJLog(e);
////      }
////      // from Roi initialize a set of cell
////      for (String pos : segImps.keySet()) {
////         SetOfCells soc = socSet_.getSoc(pos);
////         soc.reset();
////         soc.loadCells(savingPath + File.separator + parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX)
////               + Maars_Interface.SEGANALYSIS_SUFFIX);
////         soc.addRoiMeasurementIntoCells(arrayMs.get(pos).getRoiMeasurements());
////      }
////      // ----------------start acquisition and analysis --------//
////      redirectLog(savingPath);
////      Boolean do_analysis = Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.DO_ANALYSIS));
////      double fluoTimeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
////      ExecutorService es1 = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
////      if (parameters.useDynamic()) {
////         // being dynamic acquisition
////         double startTime = System.currentTimeMillis();
////         double timeLimit = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT)) * 60
////               * 1000;
////         while (System.currentTimeMillis() - startTime <= timeLimit) {
////            double beginAcq = System.currentTimeMillis();
////            if (stop_) {
////               es1.shutdownNow();
////               break;
////            }
////            // Section to acquire bf images between fluos, can be skipped
////            if (frame != 0) {
////               try {
////                  segDs = es1.submit(new MAARS_mda(parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING))).get();
////               } catch (InterruptedException | ExecutionException e) {
////                  IOUtils.printErrorToIJLog(e);
////               }
////               segImgs = maars.mmUtils.ImgMMUtils.dsToSortedList(segDs, frame);
////               for (Image img: segImgs){
////                  try {
////                     fullSegDs.putImage(img);
////                  } catch (DatastoreFrozenException | DatastoreRewriteException e) {
////                     e.printStackTrace();
////                  }
////               }
////               segImps = maars.mmUtils.ImgMMUtils.convertImages2Imp(segImgs,
////                     segDs.getSummaryMetadata(), mm.getCore().getPixelSizeUm());
//////               parameters.setSavingPath(savingPath + File.separator + BF + "_" + String.valueOf(frame + 1));
//////               for (Integer posNb : segImps.keySet()) {
//////                  ImagePlus segImg = segImps.get(posNb)[0];
//////                  //update saving path
////////                          parameters.setSavingPath(savingPath + File.separator + BF + "_"+posNb);
//////                  // --------------------------segmentation-----------------------------//
//////                  ms = new MaarsSegmentation(parameters, segImg, posNb);
//////                  try {
//////                     es1.submit(ms).get();
//////                  } catch (InterruptedException | ExecutionException e) {
//////                     IOUtils.printErrorToIJLog(e);
//////                  }
//////               }
////            }
////
////            Map<String, Future> channelsInFrame = new HashMap<>();
////            try {
////               fluoDs = es1.submit(new MAARS_mda(
////                     parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING))).get();
////            } catch (InterruptedException | ExecutionException e) {
////               IOUtils.printErrorToIJLog(e);
////            }
////            ArrayList<Image> fluoImgs = maars.mmUtils.ImgMMUtils.dsToSortedList(fluoDs, frame);
////            for (Image img: fluoImgs){
////               try {
////                  fullFluoDs.putImage(img);
////               } catch (DatastoreFrozenException | DatastoreRewriteException e) {
////                  e.printStackTrace();
////               }
////            }
////            HashMap<String, ImagePlus[]> fluoImps = maars.mmUtils.ImgMMUtils.convertImages2Imp(fluoImgs,
////                  fluoDs.getSummaryMetadata(), mm.getCore().getPixelSizeUm());
////            for (String posNb : fluoImps.keySet()) {
////               for (ImagePlus chImp:fluoImps.get(posNb)){
////                  //TODO
////                  if (do_analysis) {
////                     String channel = chImp.getTitle();
////                     Future future2 = es1.submit(new FluoAnalyzer(chImp, chImp.getCalibration(), (DefaultSetOfCells) socSet_.getSoc(posNb), channel,
////                           Integer.parseInt(parameters.getChMaxNbSpot(channel)),
////                           Double.parseDouble(parameters.getChSpotRaius(channel)),
////                           Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizerList_.get(posNb), parameters.useDynamic()));
////                     channelsInFrame.put(channel, future2);
////                  }
////               }
////            }
////            tasksSet_.add(channelsInFrame);
////            frame++;
////            double acqTook = System.currentTimeMillis() - beginAcq;
////            if (fluoTimeInterval > acqTook) {
////               try {
////                  Thread.sleep((long) (fluoTimeInterval - acqTook));
////               } catch (InterruptedException e) {
////                  IOUtils.printErrorToIJLog(e);
////               }
////            } else {
////               IJ.log("Attention : acquisition before took longer than " + fluoTimeInterval
////                     / 1000 + " s : " + acqTook);
////            }
////         }
////         IJ.log("Acquisition Done, proceeding to post-analysis");
////      } else {
////         // being static acquisition
////         Map<String, Future> channelsInFrame = new HashMap<>();
////         try {
////            fluoDs = es1.submit(new MAARS_mda(
////                  parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING))).get();
////         } catch (InterruptedException | ExecutionException e) {
////            IOUtils.printErrorToIJLog(e);
////         }
////         ArrayList<Image> fluoImgs = maars.mmUtils.ImgMMUtils.dsToSortedList(fluoDs, frame);
////         for (Image img: fluoImgs){
////            try {
////               fullFluoDs.putImage(img);
////            } catch (DatastoreFrozenException | DatastoreRewriteException e) {
////               e.printStackTrace();
////            }
////         }
////         HashMap<String, ImagePlus[]> fluoImps = maars.mmUtils.ImgMMUtils.convertImages2Imp(fluoImgs,
////               fluoDs.getSummaryMetadata(), mm.getCore().getPixelSizeUm());
////         for (String posNb : fluoImps.keySet()) {
////            for (ImagePlus chImp:fluoImps.get(posNb)){
////               String channel = chImp.getTitle();
////               if (do_analysis) {
////                  Future future2 = es1.submit(new FluoAnalyzer(chImp, chImp.getCalibration(), (DefaultSetOfCells) socSet_.getSoc(posNb), channel,
////                        Integer.parseInt(parameters.getChMaxNbSpot(channel)),
////                        Double.parseDouble(parameters.getChSpotRaius(channel)),
////                        Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizerList_.get(posNb), parameters.useDynamic()));
////                  channelsInFrame.put(channel, future2);
////               }
////            }
////         }
////         tasksSet_.add(channelsInFrame);
////      }
////      parameters.setSavingPath(savingPath);
////      try {
////         SummaryMetadata.SummaryMetadataBuilder segBuilder = segDs.getSummaryMetadata().copy();
////         segBuilder.prefix(Maars_Interface.SEG);
////         segBuilder.waitInterval(fluoTimeInterval);
////         segBuilder.intendedDimensions(segDs.getSummaryMetadata().getIntendedDimensions().copy().time(frame).build());
////         fullSegDs.setSummaryMetadata(segBuilder.build());
////         SummaryMetadata.SummaryMetadataBuilder fluoBuilder = fluoDs.getSummaryMetadata().copy();
////         fluoBuilder.prefix(Maars_Interface.FLUO);
////         fluoBuilder.waitInterval(fluoTimeInterval);
////         fluoBuilder.intendedDimensions(fluoDs.getSummaryMetadata().getIntendedDimensions().copy().time(frame).build());
////         fullFluoDs.setSummaryMetadata(fluoBuilder.build());
////      } catch (DatastoreFrozenException | DatastoreRewriteException e) {
////         e.printStackTrace();
////      }
////      fullFluoDs.save(Datastore.SaveMode.MULTIPAGE_TIFF, fluoPath);
////      fullSegDs.save(Datastore.SaveMode.MULTIPAGE_TIFF, segPath);
////      fullSegDs.freeze();
////      fullFluoDs.freeze();
////      fullSegDs.close();
////      fullFluoDs.close();
////      es1.shutdown();
////      try {
////         es1.awaitTermination(60, TimeUnit.MINUTES);
////      } catch (InterruptedException e) {
////         e.printStackTrace();
////      }
////      Maars_Interface.waitAllTaskToFinish(tasksSet_);
////      for (String pos : segImps.keySet()) {
////         DefaultSetOfCells soc = (DefaultSetOfCells) socSet_.getSoc(pos);
////         if (do_analysis && !stop_) {
////            long startWriting = System.currentTimeMillis();
////            ImagePlus mergedImg = IJ.openImage(fluoPath + File.separator + Maars_Interface.FLUO + "_MMStack_"+pos+".ome.tif");
////            mergedImg.getCalibration().frameInterval = fluoTimeInterval / 1000;
////            IOUtils.saveAll(soc, mergedImg, savingPath, parameters.useDynamic(), arrayChannels, pos);
////            if (parameters.useDynamic()) {
////               if (IJ.isWindows()) {
////                  savingPath = FileUtils.convertPathToLinuxType(segPath);
////               }
////               Maars_Interface.analyzeMitosisDynamic(soc, parameters);
////            }
////            ReportingUtils.logMessage("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
////                  + " sec for writing results");
////         }
//////                RemoteNotification.mailNotify("tongli.bioinfo@gmail.com");
////         mmc.setAutoShutter(true);
////         System.setErr(curr_err);
////         System.setOut(curr_out);
////         soc.reset();
////      }
////      IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
////      IJ.showMessage("MaarsOTFSeg: Done!");
////      MaarsMainDialog.okMainDialogButton.setEnabled(true);
//   }
//}
