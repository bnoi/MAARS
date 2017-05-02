package edu.univ_tlse3.maars;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.gui.MaarsFluoAnalysisDialog;
import edu.univ_tlse3.gui.MaarsMainDialog;
import edu.univ_tlse3.resultSaver.MAARSImgSaver;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.IOUtils;
import edu.univ_tlse3.utils.ImgUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.frame.RoiManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq implements Runnable {
    public AtomicBoolean stop_ = new AtomicBoolean(false);
    private PrintStream curr_err;
    private PrintStream curr_out;
    private MaarsParameters parameters;
    private SetOfCells soc_;
    private String rootDir;
    private SOCVisualizer socVisualizer_;

    public MAARSNoAcq(MaarsParameters parameters, SOCVisualizer socVisualizer,
                      SetOfCells soc) {
        this.parameters = parameters;
        rootDir = parameters.getSavingPath();
        soc_ = soc;
        socVisualizer_ = socVisualizer;
    }

    private ArrayList<String[]> getAcqPositions() {
        ArrayList<String[]> acqPos = new ArrayList<>();
        String[] listAcqNames = new File(rootDir).list();
        String pattern = "(X)(\\d+)(_)(Y)(\\d+)(_FLUO)";
        assert listAcqNames != null;
        for (String acqName : listAcqNames) {
            if (Pattern.matches(pattern, acqName)) {
                acqPos.add(new String[]{acqName.split("_", -1)[0].substring(1),
                        acqName.split("_", -1)[1].substring(1)});
            }
        }
        return acqPos;
    }

    private static ArrayList<Integer> getFluoAcqStructure(String pathToFluoDir) {
        String[] listAcqNames = new File(pathToFluoDir).list();
        String pattern = "(\\w+)(_)(\\d+)";
        ArrayList<Integer> arrayImgFrames = new ArrayList<>();
        assert listAcqNames != null;
        for (String acqName : listAcqNames) {
            if (Pattern.matches(pattern, acqName)) {
                String current_frame = acqName.split("_", -1)[1];
                if (!arrayImgFrames.contains(Integer.parseInt(current_frame))) {
                    arrayImgFrames.add(Integer.parseInt(current_frame));
                }
            }
        }
        Collections.sort(arrayImgFrames);
        return arrayImgFrames;
    }

    private static ImagePlus prepareImgToSave(ImagePlus projectedImg, ImagePlus notProjected, String channel, int frame,
                                              Boolean projected) {
        ImagePlus imgToSave = projected ? projectedImg : notProjected;
        for (int i = 1; i <= imgToSave.getStack().getSize(); i++) {
            imgToSave.getStack().setSliceLabel(channel + "_" + frame, i);
        }
        return imgToSave;
    }

    public static Double extractFromOMEmetadata(Map<String, Object> omeData, String parameter){
        return (Double) ((Map) omeData.get("IntendedDimensions")).get(parameter);
    }

    private static ImagePlus loadImg(String pathToFluoImgsDir, String fluoTiffName){
        Concatenator concatenator = new Concatenator();
        concatenator.setIm5D(true);
        ImagePlus im = IJ.openImage(pathToFluoImgsDir + File.separator + fluoTiffName);
        IOUtils.writeToFile(pathToFluoImgsDir + File.separator + "metadata.txt", im.getProperties());
        String tifNameBase = fluoTiffName.split("\\.", -1)[0];
        IJ.run("Image Sequence...", "open=" + pathToFluoImgsDir + " file=" + tifNameBase + "_ sort");
        ImagePlus im2 = IJ.getImage();
        ImagePlus concatenatedImg = concatenator.concatenate(im, im2, false);
        concatenatedImg.setProperty("Info", im.getInfoProperty());
        return concatenatedImg;
    }

    private static ImagePlus processSplitImgs(String pathToFluoImgsDir, MaarsParameters parameters, SetOfCells soc,
                                              SOCVisualizer socVisualizer,CopyOnWriteArrayList<Map<String, Future>> tasksSet,
                                              boolean saveRam, AtomicBoolean stop){
        ArrayList<Integer> arrayImgFrames = getFluoAcqStructure(pathToFluoImgsDir);
        int totalFrame = arrayImgFrames.size();

        Concatenator concatenator = new Concatenator();
        concatenator.setIm5D(true);

        ArrayList<String> arrayChannels = new ArrayList<>();
        Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));

        ImagePlus concatenatedFluoImgs = null;
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (Integer current_frame : arrayImgFrames) {
            Map<String, Future> analysisTasks = new HashMap<>();
            for (String channel : arrayChannels) {
                IJ.log("Processing channel " + channel + "_" + current_frame);
                String pathToFluoMovie = pathToFluoImgsDir + channel + "_" + current_frame + "/" + channel + "_" + current_frame + "_MMStack_Pos0.ome.tif";
                ImagePlus currentFluoImage = IJ.openImage(pathToFluoMovie);
                ImagePlus zProjectedFluoImg = ImgUtils.zProject(currentFluoImage, currentFluoImage.getCalibration());
                Future future = es.submit(new FluoAnalyzer(zProjectedFluoImg.duplicate(), zProjectedFluoImg.getCalibration(),
                        soc, channel, Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                        Double.parseDouble(parameters.getChSpotRaius(channel)),
                        Double.parseDouble(parameters.getChQuality(channel)), current_frame, socVisualizer,
                        parameters.useDynamic()));
                analysisTasks.put(channel, future);
                ImagePlus imgToSave = prepareImgToSave(zProjectedFluoImg, currentFluoImage, channel, current_frame,
                        Boolean.parseBoolean(parameters.getProjected()));
                if (saveRam) {
                    IJ.log("Due to lack of RAM, MAARS will append cropped images frame by frame on disk (much slower)");
                    MAARSImgSaver imgSaver = new MAARSImgSaver(pathToFluoImgsDir);
                    //TODO
                    CopyOnWriteArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
                    for (int i : cellIndex) {
                        Cell c = soc.getCell(i);
//                     for (Cell c : soc_){
                        imgToSave.setRoi(c.getCellShapeRoi());
                        for (int j = 1; j <= imgToSave.getNChannels(); j++) {
                            ImagePlus croppedImg = new Duplicator().run(imgToSave, j, j, 1, imgToSave.getNSlices(),
                                    1, imgToSave.getNFrames());
                            imgSaver.saveImgs(croppedImg, i, channel, true);
                        }
                    }
                } else {
                    concatenatedFluoImgs = concatenatedFluoImgs == null ?
                            imgToSave : concatenator.concatenate(concatenatedFluoImgs, imgToSave, false);
                }
            }
            tasksSet.add(analysisTasks);
            if (stop.get()) {
                break;
            }
        }
        es.shutdown();
        return HyperStackConverter.toHyperStack(concatenatedFluoImgs, arrayChannels.size(),
                concatenatedFluoImgs.getStack().getSize() / arrayChannels.size() / totalFrame, totalFrame,
                "xyzct", "Grayscale");
    }

    private static ImagePlus processStackedImg(String pathToFluoImgsDir, String fluoTiffName,
                                               MaarsParameters parameters, SetOfCells soc, SOCVisualizer socVisualizer,
                                               CopyOnWriteArrayList<Map<String, Future>> tasksSet, AtomicBoolean stop){
        ImagePlus concatenatedFluoImgs = loadImg(pathToFluoImgsDir, fluoTiffName);

        Map<String, Object> map = new Gson().fromJson(concatenatedFluoImgs.getInfoProperty(),
                new TypeToken<HashMap<String, Object>>() {}.getType());

        ArrayList<String> arrayChannels = (ArrayList) map.get("ChNames");
        int totalChannel = extractFromOMEmetadata(map, "channel").intValue();
        int totalSlice = extractFromOMEmetadata(map, "z").intValue();
        int totalFrame = extractFromOMEmetadata(map, "time").intValue();
//               totalPosition = (int) ((Map)map.get("IntendedDimensions")).get("position");

        IJ.log("Re-stack image : channel " + totalChannel +", slice " + totalSlice + ", frame " + totalFrame);
        concatenatedFluoImgs = HyperStackConverter.toHyperStack(concatenatedFluoImgs, totalChannel, totalSlice, totalFrame
                , "xyzct", "Grayscale");
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 1; i <= totalFrame; i++) {
            Map<String, Future> chAnalysisTasks = new HashMap<>();
            for (int j = 1; j <= totalChannel; j++) {
                String channel = arrayChannels.get(j - 1);
                IJ.log("Processing channel " + channel + "_" + i);
                ImagePlus zProjectedFluoImg = ImgUtils.zProject(
                        new Duplicator().run(concatenatedFluoImgs, j, j, 1, totalSlice, i, i)
                        , concatenatedFluoImgs.getCalibration());
                Future future = es.submit(new FluoAnalyzer(zProjectedFluoImg.duplicate(), zProjectedFluoImg.getCalibration(),
                        soc, channel, Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                        Double.parseDouble(parameters.getChSpotRaius(channel)),
                        Double.parseDouble(parameters.getChQuality(channel)), i, socVisualizer,
                        parameters.useDynamic()));
                chAnalysisTasks.put(channel, future);
            }
            tasksSet.add(chAnalysisTasks);
            if (stop.get()) {
                break;
            }
        }
        if (Boolean.parseBoolean(parameters.getProjected())) {
            IJ.run(concatenatedFluoImgs, "Z Project...", "projection=[Max Intensity] all");
            return(IJ.getImage());
        }
        es.shutdown();
        return concatenatedFluoImgs;
    }

    @Override
    public void run() {
        // Start time
        long start = System.currentTimeMillis();
        for (String[] pos : getAcqPositions()) {
            if (stop_.get()) {
                break;
            }
            soc_.reset();
            String xPos = pos[0];
            String yPos = pos[1];
            IJ.log("x : " + xPos + " y : " + yPos);
            String pathToSegDir = FileUtils.convertPath(rootDir + "/X" + xPos + "_Y" + yPos);
            String pathToFluoDir = pathToSegDir + "_FLUO/";
            String pathToSegMovie = FileUtils.convertPath(pathToSegDir + "/_1/_1_MMStack_Pos0.ome.tif");
            //update saving path
            parameters.setSavingPath(pathToSegDir);
            Boolean skipSegmentation = Boolean.parseBoolean(parameters.getSkipSegmentation());
            ImagePlus segImg = null;
            try {
                segImg = IJ.openImage(pathToSegMovie);
                parameters.setCalibration(String.valueOf(segImg.getCalibration().pixelWidth));
            } catch (Exception e) {
                IOUtils.printErrorToIJLog(e);
            }
            // --------------------------segmentation-----------------------------//
            MaarsSegmentation ms = new MaarsSegmentation(parameters, segImg);
            if (!skipSegmentation) {
                ExecutorService es = Executors.newSingleThreadExecutor();
                es.execute(ms);
                es.shutdown();
                try {
                    es.awaitTermination(30,TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (ms.roiDetected()) {
                soc_.reset();
                // from Roi.zip initialize a set of cell
                soc_.loadCells(pathToSegDir);
                ResultsTable rt;
                if (skipSegmentation) {
                    IJ.open(pathToSegDir + File.separator + "BF_Results.csv");
                    rt = ResultsTable.getResultsTable();
                    ResultsTable.getResultsWindow().close(false);
                } else {
                    rt = ms.getRoiMeasurements();
                }
                soc_.setRoiMeasurementIntoCells(rt);

                // ----------------start acquisition and analysis --------//
                try {
                    PrintStream ps = new PrintStream(pathToSegDir + "/CellStateAnalysis.LOG");
                    curr_err = System.err;
                    curr_out = System.err;
                    System.setOut(ps);
                    System.setErr(ps);
                } catch (FileNotFoundException e) {
                    IOUtils.printErrorToIJLog(e);
                }

                Boolean saveRam_ = MaarsFluoAnalysisDialog.saveRam_;
                String fluoTiffName = FileUtils.getShortestTiffName(pathToFluoDir);

                CopyOnWriteArrayList<Map<String, Future>> tasksSet = new CopyOnWriteArrayList<>();
                ImagePlus concatenatedFluoImgs;
                if (fluoTiffName != null) {
                    concatenatedFluoImgs = processStackedImg(pathToFluoDir, fluoTiffName,
                            parameters, soc_, socVisualizer_, tasksSet,stop_);
                } else {
                    concatenatedFluoImgs = processSplitImgs(pathToFluoDir,parameters,soc_,
                            socVisualizer_,tasksSet, saveRam_,stop_);
                }
                concatenatedFluoImgs.getCalibration().frameInterval =
                        Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000;
                MaarsMainDialog.waitAllTaskToFinish(tasksSet);
                if (!stop_.get()) {
                    RoiManager.getInstance().reset();
                    RoiManager.getInstance().close();
                    if (soc_.size() != 0) {
                        long startWriting = System.currentTimeMillis();
                        if (saveRam_) {
                            MAARS.saveAll(soc_, pathToFluoDir, parameters.useDynamic());
                        } else {
                            concatenatedFluoImgs.show();
                            ArrayList<String> arrayChannels = new ArrayList<>();
                            Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));
                            MAARS.saveAll(soc_, concatenatedFluoImgs, pathToFluoDir, parameters.useDynamic(),
                                    arrayChannels);
                        }
                        IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                                + " sec for writing results");
                        if (parameters.useDynamic()) {
                            if (IJ.isWindows()) {
                                pathToSegDir = FileUtils.convertPathToLinuxType(pathToSegDir);
                            }
                            MAARS.analyzeMitosisDynamic(soc_, parameters, pathToSegDir, true);
                        }
                    } else if (soc_.size() == 0) {
                        try {
                            org.apache.commons.io.FileUtils.deleteDirectory(new File(pathToSegDir));
                        } catch (IOException e) {
                            IOUtils.printErrorToIJLog(e);
                        }
                    }
                }
            }
        }
        System.setErr(curr_err);
        System.setOut(curr_out);
        soc_.reset();
        soc_ = null;
        if (!stop_.get()) {
            IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
        }
        System.gc();
    }
}