package fiji.plugin.maars.cellstateanalysis;

/**
 * Class allowing to do basic statistics on a dataset
 * 
 * @author marie
 *
 */
public class Statistics {
	double[] data;
	double size;

	public Statistics(double[] data) {
		this.data = data;
		size = data.length;
	}

	double getMean() {
		double sum = 0.0;
		for (double a : data)
			sum += a;
		return sum / size;
	}

	double getVariance() {
		double mean = getMean();
		double temp = 0;
		for (double a : data)
			temp += (mean - a) * (mean - a);
		return temp / size;
	}

	double getStdDev() {
		return Math.sqrt(getVariance());
	}
}
