package edu.univ_tlse3.maars;

import edu.univ_tlse3.acquisition.MAARS_mda;
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
import ij.plugin.Duplicator;
import ij.plugin.frame.RoiManager;
import mmcorej.CMMCore;
import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * Main MAARS program
 *
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
public class MAARS implements Runnable {
    public boolean skipAllRestFrames = false;
    private PrintStream curr_err;
    private PrintStream curr_out;
    private MMStudio mm;
    private CMMCore mmc;
    private MaarsParameters parameters;
    private SetOfCells soc_;
    private SOCVisualizer socVisualizer_;
    private CopyOnWriteArrayList<Map<String, Future>> tasksSet_;
    private static String BF = "BF";
    private static String FLUO = "FLUO";

    /**
     * Constructor
     *
     * @param mm            MMStudio object (gui)
     * @param mmc           CMMCore object (core)
     * @param parameters    MAARS parameters object
     * @param socVisualizer set of cell visualizer
     */
    public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters, SOCVisualizer socVisualizer,
                 CopyOnWriteArrayList<Map<String, Future>> tasksSet,
                 SetOfCells soc) {
        this.mmc = mmc;
        this.parameters = parameters;
        soc_ = soc;
        this.mm = mm;
        tasksSet_ = tasksSet;
        socVisualizer_ = socVisualizer;
    }

    static void serializeSoc(String pathToFluoDir, SetOfCells soc) {
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

    static void saveAll(SetOfCells soc, ImagePlus mergedImg, String pathToFluoDir,
                        Boolean useDynamic, ArrayList<String> arrayChannels) {
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
            mergedImg.setRoi(cell.getCellShapeRoi());
            for (int j = 1; j <= mergedImg.getNChannels(); j++) {
                ImagePlus croppedImg = new Duplicator().run(mergedImg, j, j, 1, mergedImg.getNSlices(),
                        1, mergedImg.getNFrames());
                IJ.run(croppedImg, "Grays", "");
                imgSaver.saveImgs(croppedImg, i, arrayChannels.get(j - 1), false);
            }
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
        PositionList pl = new PositionList();
        try {
            if (FileUtils.exists(parameters.getPathToPositionList())) {
                pl.load(parameters.getPathToPositionList());
            }else{
                String xyStage = mmc.getXYStageDevice();
                String zStage = mmc.getFocusDevice();
                MultiStagePosition currentPos = new MultiStagePosition(xyStage,mm.getCachedXPosition(),mm.getCachedYPosition(),
                        zStage,mm.getCachedZPosition());
                pl.addPosition(currentPos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double fluoTimeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
        //prepare executor for image analysis
        for (int i = 0; i < pl.getNumberOfPositions(); i++) {
            soc_.reset();
            if (skipAllRestFrames) {
                break;
            }
            try {
                mm.core().setXYPosition(pl.getPosition(i).getX(),pl.getPosition(i).getY());
                mmc.waitForDevice(mmc.getXYStageDevice());
            } catch (Exception e) {
                IJ.error("Can't set XY stage devie");
                IOUtils.printErrorToIJLog(e);

            }
            String xPos = String.valueOf(Math.round(pl.getPosition(i).getX()));
            String yPos = String.valueOf(Math.round(pl.getPosition(i).getY()));
            IJ.log("Current position : X : " + xPos + " Y : " + yPos);

            String savingPath = FileUtils.convertPath(parameters.getSavingPath());
            //update saving path
            parameters.setSavingPath(savingPath + File.separator +BF + "_1");
            //acquisition
            ImagePlus segImg = MAARS_mda.acquireImagePlus(mm,
                    parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING),
                    savingPath, BF);

            // --------------------------segmentation-----------------------------//
            MaarsSegmentation ms = new MaarsSegmentation(parameters, segImg);
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.execute(ms);
            es.shutdown();
            try {
                es.awaitTermination(60,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            parameters.setSavingPath(savingPath);
//            if (ms.roiDetected()) {
            if (true) {
                String pathToFluoDir = savingPath + File.separator +FLUO + "_1";
                parameters.setSavingPath(pathToFluoDir);
                // from Roi initialize a set of cell
                soc_.reset();
                soc_.loadCells(savingPath + File.separator +BF + "_1");
                soc_.setRoiMeasurementIntoCells(ms.getRoiMeasurements());

                // ----------------start acquisition and analysis --------//
//                FluoAcqSetting fluoAcq = new FluoAcqSetting(parameters);
                try {
                    PrintStream ps = new PrintStream(savingPath + File.separator +BF + "_1" + File.separator + "CellStateAnalysis.LOG");
                    curr_err = System.err;
                    curr_out = System.err;
                    System.setOut(ps);
                    System.setErr(ps);
                } catch (FileNotFoundException e) {
                    IOUtils.printErrorToIJLog(e);
                }
                int frame = 0;
                Boolean do_analysis = Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.DO_ANALYSIS));

                ExecutorService es1 = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                if (parameters.useDynamic()) {
                    // being dynamic acquisition
                    double startTime = System.currentTimeMillis();
                    double timeLimit = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT)) * 60
                            * 1000;
                    while (System.currentTimeMillis() - startTime <= timeLimit) {
                        double beginAcq = System.currentTimeMillis();
                        Map<String, Future> channelsInFrame = new HashMap<>();
                        if (frame != 0){
                            MAARS_mda.acquireImagePlus(mm,
                                    parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING),
                                    savingPath, BF);
                            parameters.setSavingPath(savingPath + File.separator + BF+"_"+String.valueOf(frame+1));
                            ms = new MaarsSegmentation(parameters, segImg);
                            ExecutorService es2 = Executors.newSingleThreadExecutor();
                            es2.execute(ms);
                            es2.shutdown();
                            try {
                                es2.awaitTermination(60,TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        ImagePlus fluoImage = MAARS_mda.acquireImagePlus(mm,
                                parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING),
                                savingPath, FLUO);
                        //TODO
//                        if (do_analysis) {
//                            Future future = es1.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc_, channel,
//                                    Integer.parseInt(parameters.getChMaxNbSpot(channel)),
//                                    Double.parseDouble(parameters.getChSpotRaius(channel)),
//                                    Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_, parameters.useDynamic()));
//                            channelsInFrame.put(channel, future);
//                        }
                        tasksSet_.add(channelsInFrame);
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
                        if (skipAllRestFrames) {
                            break;
                        }
                    }
                    IJ.log("Acquisition Done, proceeding to post-analysis");
                } else {
                    // being static acquisition
                    for (String channel : arrayChannels) {
                        Map<String, Future> channelsInFrame = new HashMap<>();
                        ImagePlus fluoImage = MAARS_mda.acquireImagePlus(mm,
                                parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING),
                                savingPath, FLUO);
//                        if (do_analysis) {
//                            future = es_.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc_, channel,
//                                    Integer.parseInt(parameters.getChMaxNbSpot(channel)),
//                                    Double.parseDouble(parameters.getChSpotRaius(channel)),
//                                    Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_, parameters.useDynamic()));
//                            channelsInFrame.put(channel, future);
//                        }
                        fluoImage = null;
                        tasksSet_.add(channelsInFrame);
                        if (skipAllRestFrames) {
                            break;
                        }
                    }
                }
                es1.shutdown();
                try {
                    es1.awaitTermination(60,TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MaarsMainDialog.waitAllTaskToFinish(tasksSet_);
                if (!skipAllRestFrames) {
                    RoiManager.getInstance().reset();
                    RoiManager.getInstance().close();
                    if (soc_.size() != 0 && do_analysis) {
                        long startWriting = System.currentTimeMillis();
                        ImagePlus mergedImg = ImgUtils.loadFullFluoImgs(pathToFluoDir);
                        mergedImg.getCalibration().frameInterval = fluoTimeInterval / 1000;
                        MAARS.saveAll(soc_, mergedImg, pathToFluoDir, parameters.useDynamic(), arrayChannels);
                        if (parameters.useDynamic()) {
                            if (IJ.isWindows()) {
                                savingPath = FileUtils.convertPathToLinuxType(savingPath + File.separator +BF + "_1");
                            }
                            MAARS.analyzeMitosisDynamic(soc_, parameters, savingPath, true);
                        }
                        ReportingUtils.logMessage("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                                + " sec for writing results");
                        mergedImg = null;
                    } else if (soc_.size() == 0) {
                        try {
                            org.apache.commons.io.FileUtils.deleteDirectory(new File(savingPath + File.separator +BF + "_1"));
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
