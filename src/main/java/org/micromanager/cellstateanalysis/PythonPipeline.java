package org.micromanager.cellstateanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import ij.IJ;

public class PythonPipeline {

	public static void getMitosisFiles(String acqDir, String channel, String calibration, String gap_tolerance,
			String elongat_trend, String minimumPeriod, String interval) {

		ProcessBuilder probuilder = null;
		Process process = null;
		BufferedReader in = null;
		BufferedReader stdError = null;
		// TODO find a way to call python with packages
		String[] cmd = new String[] { PythonPipeline.getPythonInConda(),
				PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath()
						+ "AnalyzeMAARSOutput.py",
				acqDir, channel, "-calibration", calibration, "-gap_tolerance", gap_tolerance, "-elongating_trend",
				elongat_trend, "-minimumPeriod", minimumPeriod, "-acq_interval", interval };
		probuilder = new ProcessBuilder(cmd);
		try {
			process = probuilder.start();
			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String s = null;
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

	public static String getPythonInConda() {
		String osName = System.getProperty("os.name");
		String pythonPath = "";
		File condaDir = null;
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
		} else if (osName.equals("windows")) {

		}
		System.out.println(pythonPath);
		return pythonPath;
	}

	public static void main(String[] args) {
		PythonPipeline.getMitosisFiles("/Volumes/Macintosh/curioData/102/25-03-1/X0_Y0", "CFP", "0.1075", "0.3", "0.6", "200", "20");
		// PythonPipeline.getPythonInConda();
	}
}