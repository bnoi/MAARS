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
import edu.univ_tlse3.utils.IOUtils;
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

    static void serializeSoc(String pathToFluoDir, SetOfCells soc){
        File f = new File(pathToFluoDir + "SetOfCell.serialize");
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(f)));
            objOut.writeObject(soc);
            objOut.flush();

            IJ.log("Set of cel object is serialized.");
        } catch (IOException i) {
            ReportingUtils.logError(i.getMessage());
        } finally {
            if (objOut != null) {
                try {
                    objOut.close();
                } catch (IOException e) {
                    IOUtils.printErrorToIJLog(e);
                }
            }
        }
    }

    static void saveAll(SetOfCells soc, String pathToFluoDir, Boolean useDynamic) {
        IJ.log("Saving information of each cell");
        MAARSSpotsSaver spotSaver = new MAARSSpotsSaver(pathToFluoDir);
        MAARSGeometrySaver geoSaver = new MAARSGeometrySaver(pathToFluoDir);
        //TODO
        CopyOnWriteArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
        for (int i : cellIndex) {
            Cell c = soc.getCell(i);
            geoSaver.save(c);
            spotSaver.save(c);
        }
        if (useDynamic) {
            serializeSoc(pathToFluoDir, soc);
        }
    }

    static void saveAll(SetOfCells soc, ImagePlus mergedImg, String pathToFluoDir, Boolean useDynamic, ArrayList<String> arrayChannels, int frameSize) {
        IJ.log("Saving information of each cell on disk");
        MAARSSpotsSaver spotSaver = new MAARSSpotsSaver(pathToFluoDir);
        MAARSGeometrySaver geoSaver = new MAARSGeometrySaver(pathToFluoDir);
        MAARSImgSaver imgSaver = new MAARSImgSaver(pathToFluoDir);
//        TODO
        CopyOnWriteArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
         for (int i : cellIndex) {
             Cell cell = soc.getCell(i);
//        for (Cell cell : soc){
             geoSaver.save(cell);
             spotSaver.save(cell);
             ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cell.getCellShapeRoi());
            ImagePlus hyperImg = ImgUtils.reshapeStack(croppedImg, arrayChannels.size(), frameSize);
            imgSaver.saveSplitImgs(hyperImg, i, arrayChannels);
         }
         if (useDynamic) {
             serializeSoc(pathToFluoDir, soc);
         }
    }

    private static void showChromLaggingCells(String pathToSegDir,
                                              SetOfCells soc) {
        if (FileUtils.exists(pathToSegDir + "_MITOSIS")) {
            String[] listAcqNames = new File(pathToSegDir + "_MITOSIS" + File.separator + "figs" + File.separator)
                    .list();
            String pattern = "(\\d+)(_slopChangePoints_)(\\d+)(_)(\\d+)(.png)";
            PrintWriter out = null;
            try {
                out = new PrintWriter(pathToSegDir + "_MITOSIS" + File.separator + "abnormalCells.txt");
            } catch (FileNotFoundException e) {
                IOUtils.printErrorToIJLog(e);
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
                            assert out != null;
                            out.println("Lagging :" + cellNb + "_last_" + spotInBtwnFrames.get(spotInBtwnFrames.size() - 1) + "_onset_" + anaBOnsetFrame);
                            IJ.log("Lagging :" + cellNb + "_last_" + spotInBtwnFrames.get(spotInBtwnFrames.size() - 1) + "_onset_" + anaBOnsetFrame);
                            IJ.openImage(pathToSegDir + "_MITOSIS" + File.separator + "croppedImgs"
                                    + File.separator + cellNb + "_GFP.tif").show();
                        }
                    }
                    //TODO to show unaligned cell
                    if (cell.unalignedSpotFrames().size() > 0) {
                        IJ.log("Unaligned : Cell " + cellNb + " detected with unaligned kinetochore(s)");
                        out.println("Unaligned : Cell " + cellNb + " detected with unaligned kinetochore(s)");

//                        if (splitChannel) {
//                            IJ.openImage(pathToSegDir + "_MITOSIS" + File.separator + "croppedImgs"
//                                    + File.separator + cellNb + "_GFP.tif").show();
//                        } else {
//                            IJ.openImage(pathToSegDir + "_MITOSIS" + File.separator + "croppedImgs"
//                                    + File.separator + cellNb + "_merged.tif").show();
//                        }
                    }
                }
            }
            assert out != null;
            out.close();
            IJ.log("lagging detection finished");
        }
    }

    static void analyzeMitosisDynamic(SetOfCells soc, MaarsParameters parameters, String pathToSegDir, Boolean showChromLagging) {
        // TODO need to find a place for the metadata, maybe in images
        IJ.log("Start python analysis");
        ArrayList<String> script = PythonPipeline.getPythonScript(pathToSegDir, parameters.getDetectionChForMitosis(),
                parameters.getCalibration(), parameters.getMinimumMitosisDuration(),
                String.valueOf((Math.round(Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000))));
        PythonPipeline.savePythonScript(script);
        IJ.log("Script generated");
        PythonPipeline.runPythonScript();
        if (showChromLagging) {
            MAARS.showChromLaggingCells(pathToSegDir, soc);
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
        parameters.setCalibration(String.valueOf(mm.getCachedPixelSizeUm()));

        ArrayList<String> arrayChannels = new ArrayList<>();
        Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));

        // Acquisition path arrangement
        ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);
        double fluoTimeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
        //prepare executor for image analysis
        for (int i = 0; i < explo.length(); i++) {
            soc_.reset();
            if (skipAllRestFrames) {
                break;
            }
            try {
                mm.core().setXYPosition(explo.getX(i), explo.getY(i));
                mmc.waitForDevice(mmc.getXYStageDevice());
            } catch (Exception e) {
                IJ.error("Can't set XY stage devie");
                IOUtils.printErrorToIJLog(e);

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
                IOUtils.printErrorToIJLog(e);
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
                    IOUtils.printErrorToIJLog(e);
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
                                IOUtils.printErrorToIJLog(e);
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
                                        Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_, parameters.useDynamic()));
                                channelsInFrame.put(channel, future);
                            }
                            fluoImage = null;
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
                                IOUtils.printErrorToIJLog(e);
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
                            IOUtils.printErrorToIJLog(e);
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
                                    Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_, parameters.useDynamic()));
                            channelsInFrame.put(channel, future);
                        }
                        fluoImage = null;
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
                        ImagePlus mergedImg = ImgUtils.loadFullFluoImgs(pathToFluoDir);
                        mergedImg.getCalibration().frameInterval = fluoTimeInterval / 1000;
                        MAARS.saveAll(soc_, mergedImg, pathToFluoDir, parameters.useDynamic(), arrayChannels, frame);
                        if (parameters.useDynamic()) {
                            if (IJ.isWindows()) {
                                pathToSegDir = FileUtils.convertPathToLinuxType(pathToSegDir);
                            }
                            MAARS.analyzeMitosisDynamic(soc_, parameters, pathToSegDir, true);
                        }
                        ReportingUtils.logMessage("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                                + " sec for writing results");
                        mergedImg = null;
                    }else if (soc_.size() == 0){
                        try {
                            org.apache.commons.io.FileUtils.deleteDirectory(new File(pathToSegDir));
                        } catch (IOException e) {
                            IOUtils.printErrorToIJLog(e);
                        }
                    }
//                RemoteNotification.mailNotify("tongli.bioinfo@gmail.com");
                }
            }
        }
        mmc.setAutoShutter(true);
        System.setErr(curr_err);
        System.setOut(curr_out);
        soc_.reset();
        soc_ = null;
        if (!skipAllRestFrames) {
            IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
        }
        System.gc();
        IJ.showMessage("MAARS: Done!");
    }
}
