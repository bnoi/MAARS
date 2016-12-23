package edu.univ_tlse3.maars;

import edu.univ_tlse3.acquisition.AcqLauncher;
import edu.univ_tlse3.acquisition.FluoAcqSetting;
import edu.univ_tlse3.acquisition.SegAcqSetting;
import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.cellstateanalysis.PythonPipeline;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.gui.MaarsMainDialog;
import edu.univ_tlse3.resultSaver.MAARSGeometrySaver;
import edu.univ_tlse3.resultSaver.MAARSImgSaver;
import edu.univ_tlse3.resultSaver.MAARSSpotsSaver;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.ImgUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import mmcorej.CMMCore;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Main MAARS program
 *
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
public class MAARS implements Runnable {
    private PrintStream curr_err;
    private PrintStream curr_out;
    private MMStudio mm;
    private CMMCore mmc;
    private MaarsParameters parameters;
    private SetOfCells soc_;
    private SOCVisualizer socVisualizer_;
    private ExecutorService es_;
    public boolean skipAllRestFrames = false;
    private CopyOnWriteArrayList<Map<String, Future>> tasksSet_;

    /**
     * Constructor
     *
     * @param mm         MMStudio object (gui)
     * @param mmc        CMMCore object (core)
     * @param parameters MAARS parameters object
     * @param socVisualizer set of cell visualizer
     * @param es executer service of MAARS
     */
    public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters, SOCVisualizer socVisualizer,
                 ExecutorService es, CopyOnWriteArrayList<Map<String, Future>> tasksSet,
                 SetOfCells soc) {
        this.mmc = mmc;
        this.parameters = parameters;
        soc_ = soc;
        this.mm = mm;
        tasksSet_ = tasksSet;
        socVisualizer_ = socVisualizer;
        es_ = es;
    }

    static void saveAll(SetOfCells soc, ImagePlus mergedImg, String pathToFluoDir,
                        Boolean splitChannel) {
        IJ.log("Saving information of each cell");
        MAARSSpotsSaver spotSaver = new MAARSSpotsSaver(pathToFluoDir);
        MAARSGeometrySaver geoSaver = new MAARSGeometrySaver(pathToFluoDir);
        MAARSImgSaver imgSaver = new MAARSImgSaver(pathToFluoDir, mergedImg);
        HashMap<String, ImagePlus> croppedImgSet;
        //TODO only save the potential the mitotic cells
//        ArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
//         for (int i : cellIndex) {
//             Cell cell = soc.getCell(i);
        for (Cell cell : soc){
             geoSaver.save(cell);
             spotSaver.save(cell);
             croppedImgSet = ImgUtils.cropMergedImpWithRois(cell, mergedImg, splitChannel);
             imgSaver.saveCroppedImgs(croppedImgSet, cell.getCellNumber());
         }
        File f = new File(pathToFluoDir + "SetOfCell.serialize");
        ObjectOutputStream objOut = null;

        try {
            objOut = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(f)));
            objOut.writeObject(soc);
            objOut.flush();

            IJ.log("Set of cel object is serialized.");
        }catch(IOException i) {
            ReportingUtils.logError(i.getMessage());
        }finally {
            if (objOut != null){
                try{
                    objOut.close();
                } catch (IOException e) {
                    IJ.error(e.toString());;
                }
            }
        }
//      if (croppedImgSet != null) {
//         imgSaver.exportChannelBtf(splitChannel, croppedImgSet.keySet());
//      }
    }

    private static void showChromLaggingCells(String pathToSegDir,
                                              SetOfCells soc,
                                              Boolean splitChannel) {
        if (FileUtils.exists(pathToSegDir + "_MITOSIS")) {
            String[] listAcqNames = new File(pathToSegDir + "_MITOSIS" + File.separator + "figs" + File.separator)
                    .list();
            String pattern = "(\\d+)(_slopChangePoints_)(\\d+)(_)(\\d+)(.png)";
            ImagePlus merotelyImp;
            PrintWriter out = null;
            try {
                out = new PrintWriter(pathToSegDir + "_MITOSIS" + File.separator + "laggingCells.txt");
            } catch (FileNotFoundException e) {
                IJ.error(e.toString());;
            }
            assert listAcqNames != null;
            for (String acqName : listAcqNames) {
                if (Pattern.matches(pattern, acqName)) {
                    String[] splitName = acqName.split("_", -1);
                    int cellNb = Integer.parseInt(splitName[0]);
                    int anaBOnsetFrame = Integer.parseInt(
                            splitName[splitName.length - 1].substring(0, splitName[splitName.length - 1].length() - 4));
                    Cell cell = soc.getCell(cellNb);
                    cell.setAnaBOnsetFrame(anaBOnsetFrame);
                    ArrayList<Integer> spotInBtwnFrames = cell.getSpotInBtwnFrames();
                    if (spotInBtwnFrames.size() > 0) {
                        Collections.sort(spotInBtwnFrames);
                        if (spotInBtwnFrames.get(spotInBtwnFrames.size() - 1) - anaBOnsetFrame > 1) {
                            out.println(cellNb + "_last_" + spotInBtwnFrames.get(spotInBtwnFrames.size() - 1) + "_onset_" + anaBOnsetFrame);
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss")
                                    .format(Calendar.getInstance().getTime());
                            // IJ.log(timeStamp + " : cell " + cellNb + "_" +
                            // abnormalStateTimes * this.fluotimeInterval / 1000 + " s.");
                            if (splitChannel) {
                                merotelyImp = IJ.openImage(pathToSegDir + "_MITOSIS" + File.separator + "croppedImgs"
                                        + File.separator + cellNb + "_GFP.tif");
                                merotelyImp.show();
                            } else {
                                merotelyImp = IJ.openImage(pathToSegDir + "_MITOSIS" + File.separator + "croppedImgs"
                                        + File.separator + cellNb + "_merged.tif");
                                merotelyImp.show();
                            }
                        }
                    }
                }
            }
            assert out != null;
            out.close();
        }
    }

    static void analyzeMitosisDynamic(SetOfCells soc, MaarsParameters parameters,
                                      Boolean splitChannel, String pathToSegDir, Boolean showChromLagging) {
        // TODO need to find a place for there metadata maybe in images
        IJ.log("Start python analysis");
        ArrayList<String> script = PythonPipeline.getPythonScript(pathToSegDir, parameters.getDetectionChForMitosis(),
                "0.1075", parameters.getMinimumMitosisDuration(),
                String.valueOf((Math.round(Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000))));
        PythonPipeline.savePythonScript(script);
        IJ.log("Script generated");
        PythonPipeline.runPythonScript();
        if (showChromLagging) {
            MAARS.showChromLaggingCells(pathToSegDir, soc, splitChannel);
        }
    }

    @Override
    public void run() {
        // Start time
        long start = System.currentTimeMillis();
        // Set XY stage device
        try {
            mmc.setOriginXY(mmc.getXYStageDevice());
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        ArrayList<String> arrayChannels = new ArrayList<>();
        Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));

        // Acquisition path arrangement
        ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);
        double fluoTimeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
        //prepare executor for image analysis
        for (int i = 0; i < explo.length(); i++) {
            if (skipAllRestFrames) {
                break;
            }
            try {
                mm.core().setXYPosition(explo.getX(i), explo.getY(i));
                mmc.waitForDevice(mmc.getXYStageDevice());
            } catch (Exception e) {
                IJ.error("Can't set XY stage devie");
                IJ.error(e.toString());;
            }
            String xPos = String.valueOf(Math.round(explo.getX(i)));
            String yPos = String.valueOf(Math.round(explo.getY(i)));
            IJ.log("Current position : X_" + xPos + " Y_" + yPos);
            String original_folder = FileUtils.convertPath(parameters.getSavingPath());
            String pathToSegDir = FileUtils
                    .convertPath(original_folder + File.separator + "X" + xPos + "_Y" + yPos);
            //update saving path
            parameters.setSavingPath(pathToSegDir);
//            autofocus(mm, mmc);

            //acquisition
            SegAcqSetting segAcq = new SegAcqSetting(parameters);
            ArrayList<ChannelSpec> channelSpecs = segAcq.configChannels();
            SequenceSettings acqSettings = segAcq.configAcqSettings(channelSpecs);
            FileUtils.createFolder(acqSettings.root);

            IJ.log("Acquire bright field image...");
            AcquisitionWrapperEngine acqEng = segAcq.buildSegAcqEngine(acqSettings, mm);
            List<Image>  imageList = AcqLauncher.acquire(acqEng);
            ImagePlus segImg = ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);

            // --------------------------segmentation-----------------------------//
            MaarsSegmentation ms = new MaarsSegmentation(parameters,segImg);
            Future future = es_.submit(ms);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                IJ.error(e.toString());;
            }
            parameters.setSavingPath(original_folder);
            if (ms.roiDetected()) {
                String pathToFluoDir = pathToSegDir + "_FLUO" + File.separator;
                parameters.setSavingPath(pathToFluoDir);
                FileUtils.createFolder(pathToFluoDir);
                // from Roi initialize a set of cell
                soc_.reset();
                soc_.loadCells(pathToSegDir);
                soc_.setRoiMeasurementIntoCells(ms.getRoiMeasurements());

                // ----------------start acquisition and analysis --------//
                FluoAcqSetting fluoAcq = new FluoAcqSetting(parameters);
                try {
                    PrintStream ps = new PrintStream(pathToSegDir + File.separator + "CellStateAnalysis.LOG");
                    curr_err = System.err;
                    curr_out = System.err;
                    System.setOut(ps);
                    System.setErr(ps);
                } catch (FileNotFoundException e) {
                    IJ.error(e.toString());;
                }
                int frame = 0;
                Boolean do_analysis = Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.DO_ANALYSIS));

                if (parameters.useDynamic()) {
                    // being dynamic acquisition
                    double startTime = System.currentTimeMillis();
                    double timeLimit = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT)) * 60
                            * 1000;
                    while (System.currentTimeMillis() - startTime <= timeLimit) {
                        double beginAcq = System.currentTimeMillis();
                        Map<String, Future> channelsInFrame = new HashMap<>();
                        for (String channel : arrayChannels) {
                            if (mm.live().getIsLiveModeOn()){
                                mm.live().setLiveMode(false);
                            }
                            String focusDevice = mmc.getFocusDevice();
                            double currrentFocus = Double.MIN_VALUE;
                            try {
                                currrentFocus = mmc.getPosition(focusDevice);
                            } catch (Exception e) {
                                IJ.error(e.toString());;
                            }
                            SequenceSettings fluoAcqSetting = fluoAcq.configAcqSettings(fluoAcq.configChannels(channel));
                            if (currrentFocus != Double.MIN_VALUE) {
                                acqEng = fluoAcq.buildFluoAcqEngine(fluoAcqSetting, mm, currrentFocus);
                            }else{
                                IJ.error("Focus position is infinite negative.");
                            }
                            imageList = AcqLauncher.acquire(acqEng);
                            ImagePlus fluoImage = ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);
                            if (do_analysis) {
                                future = es_.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc_, channel,
                                        Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                                        Double.parseDouble(parameters.getChSpotRaius(channel)),
                                        Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_, parameters.useDynamic(), es_));
                                channelsInFrame.put(channel, future);
                            }
                            tasksSet_.add(channelsInFrame);
                            for (Image img : imageList){
                                mm.live().displayImage(img);
                            }
                        }
                        frame++;
                        double acqTook = System.currentTimeMillis() - beginAcq;
                        System.out.println(String.valueOf(acqTook));
                        if (fluoTimeInterval > acqTook) {
                            try {
                                Thread.sleep((long) (fluoTimeInterval - acqTook));
                            } catch (InterruptedException e) {
                                IJ.error(e.toString());;
                            }
                        } else {
                            IJ.log("Attention : acquisition before took longer than " + fluoTimeInterval
                                    / 1000 + " s : " + acqTook);
                        }
                        if (skipAllRestFrames){
                            break;
                        }
                    }
                    IJ.log("Acquisition Done, proceeding to post-analysis");
                } else {
                    // being static acquisition
                    for (String channel : arrayChannels) {
                        Map<String, Future> channelsInFrame = new HashMap<>();
                        String focusDevice = mmc.getFocusDevice();
                        double currrentFocus = Double.MIN_VALUE;
                        try {
                            currrentFocus = mmc.getPosition(focusDevice);
                        } catch (Exception e) {
                            IJ.error(e.toString());;
                        }
                        SequenceSettings fluoAcqSetting = fluoAcq.configAcqSettings(fluoAcq.configChannels(channel));
                        if (currrentFocus != Double.MIN_VALUE) {
                            acqEng = fluoAcq.buildFluoAcqEngine(fluoAcqSetting, mm, currrentFocus);
                        }else{
                            IJ.error("Focus position is infinite negative.");
                        }
                        imageList = AcqLauncher.acquire(acqEng);
                        ImagePlus fluoImage = ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);
                        if (do_analysis) {
                            future = es_.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc_, channel,
                                    Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                                    Double.parseDouble(parameters.getChSpotRaius(channel)),
                                    Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_, parameters.useDynamic(),
                                    es_));
                            channelsInFrame.put(channel, future);
                        }
                        tasksSet_.add(channelsInFrame);
                        if (skipAllRestFrames){
                            break;
                        }
                    }
                }
                MaarsMainDialog.waitAllTaskToFinish(tasksSet_);
                if (!skipAllRestFrames) {
                    RoiManager.getInstance().reset();
                    RoiManager.getInstance().close();
                    if (soc_.size() != 0 && do_analysis) {
                        long startWriting = System.currentTimeMillis();
                        Boolean splitChannel = true;
                        ImagePlus mergedImg = ImgUtils.loadFullFluoImgs(pathToFluoDir);
                        mergedImg.getCalibration().frameInterval = fluoTimeInterval / 1000;
                        MAARS.saveAll(soc_, mergedImg, pathToFluoDir, splitChannel);
                        if (parameters.useDynamic()) {
                            if (IJ.isWindows()) {
                                pathToSegDir = FileUtils.convertPathToLinuxType(pathToSegDir);
                            }
                            MAARS.analyzeMitosisDynamic(soc_, parameters,
                                    splitChannel, pathToSegDir, true);
                        }
                        ReportingUtils.logMessage("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                                + " sec for writing results");
                    }else if (soc_.size() == 0){
                        try {
                            org.apache.commons.io.FileUtils.deleteDirectory(new File(pathToSegDir));
                        } catch (IOException e) {
                            IJ.error(e.toString());;
                        }
                    }
//                RemoteNotification.mailNotify("tongli.bioinfo@gmail.com");
                }
            }
        }
        mmc.setAutoShutter(true);
        System.setErr(curr_err);
        System.setOut(curr_out);
        if (!skipAllRestFrames) {
            IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
        }
    }
}
