package edu.univ_tlse3.maars;

import edu.univ_tlse3.acquisition.AcqLauncher;
import edu.univ_tlse3.acquisition.FluoAcqSetting;
import edu.univ_tlse3.acquisition.SegAcqSetting;
import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.cellstateanalysis.PythonPipeline;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.resultSaver.MAARSGeometrySaver;
import edu.univ_tlse3.resultSaver.MAARSImgSaver;
import edu.univ_tlse3.resultSaver.MAARSSpotsSaver;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.ImgUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import mmcorej.CMMCore;
import org.micromanager.AutofocusPlugin;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.MMException;
import org.micromanager.internal.utils.ReportingUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private SetOfCells soc;
    private SOCVisualizer socVisualizer_;
    private ExecutorService es_;

    /**
     * Constructor
     *
     * @param mm         MMStudio object (gui)
     * @param mmc        CMMCore object (core)
     * @param parameters MAARS parameters object
     * @param socVisualizer set of cell visualizer
     * @param es executer service of MAARS
     */
    public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters, SOCVisualizer socVisualizer, ExecutorService es) {
        this.mmc = mmc;
        this.parameters = parameters;
        this.soc = new SetOfCells();
        this.mm = mm;
        socVisualizer_ = socVisualizer;
        es_ = es;
    }

    private static void mailNotify() {
        String to = "tongli.bioinfo@gmail.com";

        // Sender's email ID needs to be mentioned
        String from = "MAARS@univ-tlse3.fr";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", "smtps.univ-tlse3.fr");

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Analysis done!");

            // Now set the actual message
            message.setText("");

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    static void saveAll(SetOfCells soc, ImagePlus mergedImg, String pathToFluoDir,
                        Boolean splitChannel) {
        IJ.log("Saving information of each cell");
        MAARSSpotsSaver spotSaver = new MAARSSpotsSaver(pathToFluoDir);
        MAARSGeometrySaver geoSaver = new MAARSGeometrySaver(pathToFluoDir);
        MAARSImgSaver imgSaver = new MAARSImgSaver(pathToFluoDir, mergedImg);
        HashMap<String, ImagePlus> croppedImgSet = null;
        for (Cell cell : soc) {
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
                    e.printStackTrace();
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
                e.printStackTrace();
            }
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

    /**
     * A MAARS need specific autofocus process based on JAF(HP) sharpness
     * autofocus
     *
     * @param mm  MMStudio object (gui)
     * @param mmc CMMCore object (core)
     */
    public void autofocus(MMStudio mm, CMMCore mmc) {
        try {
            mmc.setShutterDevice(parameters.getChShutter(parameters.getSegmentationParameter(MaarsParameters.CHANNEL)));
        } catch (Exception e2) {
            IJ.error("Can't set BF channel for autofocusing");
            e2.printStackTrace();
        }
        double initialPosition = 0;
        String focusDevice = mmc.getFocusDevice();
        try {
            initialPosition = mmc.getPosition();
        } catch (Exception e) {
            IJ.error("Can't get current z level");
            e.printStackTrace();
        }

        // Get autofocus manager
        IJ.log("First autofocus");
        AutofocusPlugin autofocus = mm.getAutofocusManager().getAutofocusMethod();
        double firstPosition = 0;
        try {
            mmc.setShutterOpen(true);
            autofocus.fullFocus();
            mmc.waitForDevice(focusDevice);
            firstPosition = mmc.getPosition(mmc.getFocusDevice());
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        try {
            mmc.waitForDevice(focusDevice);
            mmc.setPosition(focusDevice, 2 * initialPosition - firstPosition);
        } catch (Exception e) {
            IJ.error("Can't set z position");
            e.printStackTrace();
        }

        IJ.log("Seconde autofocus");
        double secondPosition = 0;
        try {
            autofocus.fullFocus();
            mmc.waitForDevice(focusDevice);
            secondPosition = mmc.getPosition(mmc.getFocusDevice());
        } catch (MMException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mmc.waitForDevice(focusDevice);
            mmc.setPosition(focusDevice, (secondPosition + firstPosition) / 2);
        } catch (Exception e) {
            IJ.error("Can't set z position");
            e.printStackTrace();
        }
        try {
            mmc.setShutterOpen(false);
        } catch (Exception e) {
            e.printStackTrace();
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

        ArrayList<String> arrayChannels = new ArrayList<String>();
        Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));

        // Acquisition path arrangement
        ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);
        double fluoTimeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
        //prepare executor for image analysis
        for (int i = 0; i < explo.length(); i++) {
            try {
                mm.core().setXYPosition(explo.getX(i), explo.getY(i));
                mmc.waitForDevice(mmc.getXYStageDevice());
            } catch (Exception e) {
                IJ.error("Can't set XY stage devie");
                e.printStackTrace();
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
            MaarsSegmentation ms = new MaarsSegmentation(parameters);
            ms.segmentation(segImg);
            parameters.setSavingPath(original_folder);
            if (ms.roiDetected()) {
                String pathToFluoDir = pathToSegDir + "_FLUO" + File.separator;
                parameters.setSavingPath(pathToFluoDir);
                FileUtils.createFolder(pathToFluoDir);
                // from Roi initialize a set of cell
                soc.reset();
                soc.loadCells(pathToSegDir);
                soc.setRoiMeasurementIntoCells(ms.getRoiMeasurements());

                // ----------------start acquisition and analysis --------//
                FluoAcqSetting fluoAcq = new FluoAcqSetting(parameters);
                try {
                    PrintStream ps = new PrintStream(pathToSegDir + File.separator + "CellStateAnalysis.LOG");
                    curr_err = System.err;
                    curr_out = System.err;
                    System.setOut(ps);
                    System.setErr(ps);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int frame = 0;
                Boolean do_analysis = Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.DO_ANALYSIS));
                ArrayList<Map<String, Future>> futureSet = new ArrayList<Map<String, Future>>();
                Future future;
                if (parameters.useDynamic()) {
                    // being dynamic acquisition
                    double startTime = System.currentTimeMillis();
                    double timeLimit = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT)) * 60
                            * 1000;
                    while (System.currentTimeMillis() - startTime <= timeLimit) {
                        double beginAcq = System.currentTimeMillis();
                        Map<String, Future> channelsInFrame = new HashMap<String, Future>();
                        for (String channel : arrayChannels) {
                            if (mm.live().getIsLiveModeOn()){
                                mm.live().setSuspended(true);
                            }
                            SequenceSettings fluoAcqSetting = fluoAcq.configAcqSettings(fluoAcq.configChannels(channel));
                            acqEng = fluoAcq.buildFluoAcqEngine(fluoAcqSetting, mm);
                            imageList = AcqLauncher.acquire(acqEng);
                            ImagePlus fluoImage = ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);
                            if (do_analysis) {
                                future = es_.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc, channel,
                                        Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                                        Double.parseDouble(parameters.getChSpotRaius(channel)),
                                        Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_));
                                channelsInFrame.put(channel, future);
                            }
                            futureSet.add(channelsInFrame);
                            if (mm.live().getIsLiveModeOn()){
                                mm.live().setSuspended(false);
                            }
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
                                e.printStackTrace();
                            }
                        } else {
                            IJ.log("Attention : acquisition before took longer than " + fluoTimeInterval
                                    / 1000 + " s : " + acqTook);
                        }
                    }
                    IJ.log("Acquisition Done, proceeding to post-analysis");
                } else {
                    // being static acquisition
                    for (String channel : arrayChannels) {
                        Map<String, Future> channelsInFrame = new HashMap<String, Future>();
                        SequenceSettings fluoAcqSetting = fluoAcq.configAcqSettings(fluoAcq.configChannels(channel));
                        acqEng = fluoAcq.buildFluoAcqEngine(fluoAcqSetting, mm);
                        imageList = AcqLauncher.acquire(acqEng);
                        ImagePlus fluoImage = ImgUtils.convertImages2Imp(imageList, acqEng.getChannels().get(0).config);
                        if (do_analysis) {
                            future = es_.submit(new FluoAnalyzer(fluoImage, segImg.getCalibration(), soc, channel,
                                    Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                                    Double.parseDouble(parameters.getChSpotRaius(channel)),
                                    Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_));
                            channelsInFrame.put(channel, future);
                        }
                        futureSet.add(channelsInFrame);
                    }
                }
                for (Map<String, Future> aFutureSet : futureSet) {
                    for (String channel : aFutureSet.keySet()) {
                        try {
                            aFutureSet.get(channel).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }
                RoiManager.getInstance().reset();
                RoiManager.getInstance().close();
                if (soc.size() != 0 && do_analysis) {
                    long startWriting = System.currentTimeMillis();
                    Boolean splitChannel = true;
                    ImagePlus mergedImg = ImgUtils.loadFullFluoImgs(pathToFluoDir);
                    mergedImg.getCalibration().frameInterval = fluoTimeInterval / 1000;
                    MAARS.saveAll(soc, mergedImg, pathToFluoDir, splitChannel);
                    if (IJ.isWindows()){
                        pathToSegDir = FileUtils.convertPathToLinuxType(pathToSegDir);
                    }
                    MAARS.analyzeMitosisDynamic(soc, parameters,
                            splitChannel, pathToSegDir, true);
                    ReportingUtils.logMessage("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                            + " sec for writing results");
                }
                mailNotify();
            }
        }
        mmc.setAutoShutter(true);
        for (Runnable r  : es_.shutdownNow()){
            ReportingUtils.logMessage(r.toString());
        }
        System.setErr(curr_err);
        System.setOut(curr_out);
        IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing");
    }
}
