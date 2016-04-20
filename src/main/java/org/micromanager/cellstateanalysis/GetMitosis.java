package org.micromanager.cellstateanalysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.math3.util.FastMath;
import org.micromanager.utils.FileUtils;

import util.opencsv.CSVReader;

public class GetMitosis {
	// TODO finish this version in JAVA
	public GetMitosis() {
	}

	public double distance(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return FastMath.sqrt(dx * dx + dy * dy);
	}

	public Boolean filesExist(String baseDir, int cellNb, String channel) {
		if (FileUtils.exists(baseDir + "/movie_X0_Y0_FLUO/features/" + cellNb + "_" + channel + ".csv")
				& FileUtils.exists(baseDir + "/movie_X0_Y0_FLUO/spots/" + cellNb + "_" + channel + ".xml")
				& FileUtils.exists(baseDir + "/movie_X0_Y0/BF_Results.csv")) {
			return true;
		} else {
			return false;
		}
	}

	public void loadROIsAnalaysis(String pathToBFResult) {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(pathToBFResult));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] nextLine;
		try {
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				for (String att : nextLine) {
					System.out.print(att + "\t");
				}
				System.out.println();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getMitosisWithPython(String acqDir, String channel) {
		// TODO find a way to call python with packages
		String[] cmd = new String[] { "/Users/tongli/miniconda3/bin/python",
				GetMitosis.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "getMitosisFiles.py",
				acqDir, channel };
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

	public static void main(String[] args) {
		GetMitosis.getMitosisWithPython("/Volumes/Macintosh/curioData/102/25-03-1/", "CFP");
	}
}