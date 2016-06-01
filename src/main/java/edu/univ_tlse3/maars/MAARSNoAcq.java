package edu.univ_tlse3.maars;

import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.utils.FileUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq implements Runnable {
    private PrintStream curr_err;
    private PrintStream curr_out;
    private MaarsParameters parameters;
    private SetOfCells soc;
    private String rootDir;
    private ArrayList<String> arrayChannels = new ArrayList<String>();

    public MAARSNoAcq(MaarsParameters parameters) {
        this.parameters = parameters;
        rootDir = parameters.getSavingPath();
        this.soc = new SetOfCells();
    }

    private ArrayList<String[]> getAcqPositions() {
        ArrayList<String[]> acqPos = new ArrayList<String[]>();
        String[] listAcqNames = new File(rootDir).list();
        String pattern = "(X)(\\d+)(_)(Y)(\\d+)(_FLUO)";
        for (String acqName : listAcqNames) {
            if (Pattern.matches(pattern, acqName)) {
                acqPos.add(new String[]{acqName.split("_", -1)[0].substring(1),
                        acqName.split("_", -1)[1].substring(1)});
            }
        }
        return acqPos;
    }

    @Override
    public void run() {
        ExecutorService es = null;
        // Start time
        long start = System.currentTimeMillis();
        for (String[] pos : getAcqPositions()) {
            String xPos = pos[0];
            String yPos = pos[1];
            IJ.log("x : " + xPos + " y : " + yPos);
            String pathToSegDir = FileUtils.convertPath(rootDir + "/X" + xPos + "_Y" + yPos);
            String pathToFluoDir = pathToSegDir + "_FLUO/";
            String pathToSegMovie = FileUtils.convertPath(pathToSegDir + "/MMStack.ome.tif");
            ImagePlus segImg = null;
            try {
                segImg = IJ.openImage(pathToSegMovie);
            } catch (Exception e) {
                e.printStackTrace();
                IJ.error("Invalid path");
            }
            // --------------------------segmentation-----------------------------//
            MaarsSegmentation ms = new MaarsSegmentation(parameters);
            ms.segmentation(segImg, pathToSegDir);
            if (ms.roiDetected()) {
                soc.reset();
                // from Roi.zip initialize a set of cell
                soc.loadCells(pathToSegDir);
                // Get the focus slice of BF image
                soc.setRoiMeasurementIntoCells(ms.getRoiMeasurements());
                Calibration bfImgCal = null;
                if (segImg != null) {
                    bfImgCal = segImg.getCalibration();
                }
                // ----------------start acquisition and analysis --------//
                try {
                    PrintStream ps = new PrintStream(pathToSegDir + "/CellStateAnalysis.LOG");
                    curr_err = System.err;
                    curr_out = System.err;
                    System.setOut(ps);
                    System.setErr(ps);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String[] listAcqNames = new File(pathToFluoDir).list();
                String pattern = "(\\d+)(_)(\\w+)";
                ArrayList<Integer> arrayImgFrames = new ArrayList<Integer>();
                for (String acqName : listAcqNames) {
                    if (Pattern.matches(pattern, acqName)) {
                        String current_channel = acqName.split("_", -1)[1];
                        String current_frame = acqName.split("_", -1)[0];
                        if (!arrayChannels.contains(current_channel)) {
                            arrayChannels.add(current_channel);
                        }
                        if (!arrayImgFrames.contains(Integer.parseInt(current_frame))) {
                            arrayImgFrames.add(Integer.parseInt(current_frame));
                        }
                    }
                }
                Collections.sort(arrayImgFrames);
                int nThread = Runtime.getRuntime().availableProcessors();
                es = Executors.newFixedThreadPool(nThread);
                Future<FloatProcessor> future;
                ArrayList<Map<String, Future<FloatProcessor>>> futureSet = new ArrayList<Map<String, Future<FloatProcessor>>>();
                for (Integer arrayImgFrame : arrayImgFrames) {
                    Map<String, Future<FloatProcessor>> channelsInFrame = new HashMap<String, Future<FloatProcessor>>();
                    for (String channel : arrayChannels) {
                        int current_frame = arrayImgFrame;
                        IJ.log("Analysing channel " + channel + "_" + current_frame);
                        String pathToFluoMovie = pathToFluoDir + current_frame + "_" + channel + "/MMStack.ome.tif";
                        ImagePlus fluoImage = IJ.openImage(pathToFluoMovie);
                        future = es.submit(new FluoAnalyzer(fluoImage, bfImgCal, soc, channel,
                                Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                                Double.parseDouble(parameters.getChSpotRaius(channel)),
                                Double.parseDouble(parameters.getChQuality(channel)), current_frame));
                        channelsInFrame.put(channel, future);
                    }
                    futureSet.add(channelsInFrame);
                }
                assert segImg != null;
                ImageStack fluoStack = new ImageStack(segImg.getWidth(), segImg.getHeight());
                try {
                    for (Map<String, Future<FloatProcessor>> aFutureSet : futureSet) {
                        for (String channel : aFutureSet.keySet()) {
                            fluoStack.addSlice(channel, aFutureSet.get(channel).get());
                        }
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
                ImagePlus mergedImg = new ImagePlus("merged", fluoStack);
                if (segImg != null) {
                    mergedImg.setCalibration(segImg.getCalibration());
                }
                if (fluoStack != null) {
                    mergedImg.setT(fluoStack.getSize());
                }
                RoiManager.getInstance().reset();
                RoiManager.getInstance().close();
                double timeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
                if (soc.size() != 0) {
                    long startWriting = System.currentTimeMillis();
                    Boolean splitChannel = true;
                    mergedImg.getCalibration().frameInterval = timeInterval / 1000;
                    MAARS.saveAll(soc, mergedImg, pathToFluoDir, arrayChannels, splitChannel);
                    MAARS.analyzeMitosisDynamic(soc, parameters, splitChannel, pathToSegDir, true);
                    IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                            + " sec for writing results");
                }
            }
        }
        try {
            assert es != null;
            es.shutdown();
            es.awaitTermination(120, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.setErr(curr_err);
        System.setOut(curr_out);
        IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing");
    }
}
