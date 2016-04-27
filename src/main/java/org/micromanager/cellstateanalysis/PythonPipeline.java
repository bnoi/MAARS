package org.micromanager.cellstateanalysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonPipeline {

	public PythonPipeline() {
	}

	public static void getMitosisFiles(String acqDir, String channel, String calibration, String totalNbOfCell,
			String gap_tolerance, String elongat_trend, String minimumPeriod, String interval) {
		// TODO find a way to call python with packages
		String[] cmd = new String[] { "/home/tong/miniconda3/bin/python",
				PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath()
						+ "getMitosisFiles.py",
				acqDir, channel, "-calibration", calibration, "-totalNbOfCell", totalNbOfCell, "-gap_tolerance",
				gap_tolerance, "-elongating_trend", elongat_trend, "-minimumPeriod", minimumPeriod, "-acq_interval",
				interval };
		ProcessBuilder probuilder = new ProcessBuilder(cmd);
		try {
			Process process = probuilder.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String s = null;
			while ((s = in.readLine()) != null) {
				System.out.println(s);
			}
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public static void main(String[] args) {
//		PythonPipeline.getMitosisFiles("/home/tong/Documents/movies/102/60x/25-03-1/X0_Y0", "CFP", "0.1075", "1200",
//				"0.3", "0.6", "200", "20");
//	}
}