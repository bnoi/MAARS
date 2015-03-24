package fiji.plugin.maars.maarslib;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;

import java.awt.*;
import java.util.Vector;
import java.util.Properties;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.text.*;
import ij.plugin.MeasurementsWriter;
import ij.plugin.Straightener;
import ij.util.Tools;
import ij.macro.Interpreter;

public class testAnalyzer implements PlugInFilter, Measurements {
	ImagePlus imp;
	int measurements;
	ResultsTable rt;

	public testAnalyzer(ImagePlus imp, int measurements, ResultsTable rt) {
		this.imp = imp;
		this.measurements = measurements;
		if (rt == null) {
			rt = new ResultsTable();
		}
		rt.setPrecision((systemMeasurements & SCIENTIFIC_NOTATION) != 0 ? -precision
				: precision);
		this.rt = rt;
	}

	public void measure() {
		String lastHdr = rt.getColumnHeading(ResultsTable.LAST_HEADING);
		if (lastHdr == null || lastHdr.charAt(0) != 'S') {
			if (!reset())
				return;
		}
		firstParticle = lastParticle = 0;
		Roi roi = imp.getRoi();
		if (roi != null && roi.getType() == Roi.POINT) {
//			measurePoint(roi);
			return;
		}
		if (roi != null && roi.isLine()) {
//			measureLength(roi);
			return;
		}
		if (roi != null && roi.getType() == Roi.ANGLE) {
//			measureAngle(roi);
			return;
		}
		ImageStatistics stats;
//		if (isRedirectImage()) {
//			stats = getRedirectStats(measurements, roi);
			if (stats == null)
				return;
//		} else
//			stats = imp.getStatistics(measurements);
//		if (!IJ.isResultsWindow() && IJ.getInstance() != null)
//			reset();
//		saveResults(stats, roi);
	}
}
