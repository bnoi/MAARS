package org.micromanager.cellstateanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import ij.IJ;

public class PythonPipeline {

	public static void getMitosisFiles(String acqDir, String channel, String calibration, String gap_tolerance,
			String elongat_trend, String minimumPeriod, String interval) {
		// TODO find a way to call python with packages
		// String[] cmd = new String[] { "/home/tong/miniconda3/bin/python",
		String[] cmd = new String[] {
				"C:" + File.separator + "Users" + File.separator + "NIKON-inver" + File.separator + "Anaconda3"
						+ File.separator + "python",
				PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1)
						+ "AnalyzeMAARSOutput.py",
				acqDir, channel, "-calibration", calibration, "-gap_tolerance", gap_tolerance, "-elongating_trend",
				elongat_trend, "-minimumPeriod", minimumPeriod, "-acq_interval", interval };
		ProcessBuilder probuilder = new ProcessBuilder(cmd);
		try {
			Process process = probuilder.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
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

//	public static void main(String[] args) {
//		PythonPipeline.getMitosisFiles("D:\\Data\\Tong\\102\\2\\X0_Y0", "CFP", "0.1075", "0.3", "0.6", "200", "20");
//	}
}