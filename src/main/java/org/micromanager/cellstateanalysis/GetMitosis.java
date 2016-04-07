package org.micromanager.cellstateanalysis;

import org.apache.commons.math3.util.FastMath;
import org.micromanager.utils.FileUtils;

public class GetMitosis {

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
}
