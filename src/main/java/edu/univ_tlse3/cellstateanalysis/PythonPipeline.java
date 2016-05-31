package edu.univ_tlse3.cellstateanalysis;

import ij.IJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonPipeline {

    public static void getMitosisFiles(String acqDir, String channel, String calibration, String minimumPeriod, String interval) {

        ProcessBuilder probuilder;
        Process process;
        BufferedReader in;
        BufferedReader stdError;
        String[] cmd;
        if (System.getProperty("os.name").matches("(Windows)(.+)")) {
            cmd = new String[]{PythonPipeline.getPythonInConda(),
                    PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1)
                            + "AnalyzeMAARSOutput.py",
                    acqDir, channel, "-calibration", calibration, "-minimumPeriod", minimumPeriod, "-acq_interval", interval};
        } else {
            cmd = new String[]{PythonPipeline.getPythonInConda(),
                    PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                            + "AnalyzeMAARSOutput.py",
                    acqDir, channel, "-calibration", calibration, "-minimumPeriod", minimumPeriod, "-acq_interval", interval};
        }

        probuilder = new ProcessBuilder(cmd);
        try {
            process = probuilder.start();
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = in.readLine()) != null) {
                IJ.log(s);
            }
            while ((s = stdError.readLine()) != null) {
                IJ.log(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            IJ.log(e.getMessage());
        }
    }

    private static String getPythonInConda() {
        String osName = System.getProperty("os.name");
        String pythonPath = "";
        File condaDir;
        String condaDirPattern = "(\\w+)(conda)(\\w+)";
        String sep = File.separator;
        if (osName.equals("Linux")) {
            condaDir = new File(sep + "home" + sep + System.getProperty("user.name"));
            for (String dir : condaDir.list()) {
                if (dir.matches(condaDirPattern)) {
                    pythonPath = condaDir + sep + dir + sep + "bin" + sep + "python";
                }
            }
        } else if (osName.equals("Mac OS X")) {
            condaDir = new File(sep + "Users" + sep + System.getProperty("user.name"));
            for (String dir : condaDir.list()) {
                if (dir.matches(condaDirPattern)) {
                    pythonPath = condaDir + sep + dir + sep + "bin" + sep + "python";
                }
            }
        } else {
            condaDir = new File(sep + "C:" + sep + "Users" + sep + System.getProperty("user.name"));
            for (String dir : condaDir.list()) {
                if (dir.matches(condaDirPattern)) {
                    pythonPath = condaDir + sep + dir + sep + "python";
                }
            }
        }
        System.out.println(pythonPath);
        return pythonPath;
    }

    /*public static void main(String[] args) {
        PythonPipeline.getMitosisFiles("/Volumes/Macintosh/curioData/102/25-03-1/X0_Y0", "CFP", "0.1075",
                "200", "20");
    }*/
}