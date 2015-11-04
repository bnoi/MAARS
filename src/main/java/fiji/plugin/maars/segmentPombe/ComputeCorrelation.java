package fiji.plugin.maars.segmentPombe;

public class ComputeCorrelation {
	private float[] iz;
	private float zf;
	private float sigma;
	private int direction;

	// it is -1 for image with cell boundaries be black then white
	// it is 1 for image with cell boundaries be white then black

	/*
	 * f is the function which is going to be integrated on z : Iz[z] * (z - zf)
	 * * np.exp(-(zf - z) ** 2 / (2 * sigma ** 2)) CorrelationPixVal f = new
	 * CorrelationPixVal(iz, zf, sigma);
	 */

	public ComputeCorrelation(float[] iz, float zf, float sigma, int direction) {
		this.iz = iz;
		this.zf = zf;
		this.sigma = sigma;
		this.direction = direction;
	}

	/**********************************************************************
	 * Standard normal distribution density function. Replace with any
	 * sufficiently smooth function.
	 **********************************************************************/
	public double f(double z) {
		return iz[(int) z] * direction * (z - zf)
				* Math.exp(-Math.pow(zf - z, 2) / (2 * Math.pow(sigma, 2)));
	}

	/**********************************************************************
	 * Integrate f from a to b using Simpson's rule. Increase N for more
	 * precision.
	 **********************************************************************/
	public double integrate(double a, double b) {
		int N = 100; // precision parameter
		double h = (b - a) / (N - 1); // step size

		// 1/3 terms
		double sum = 1.0 / 3.0 * (f(a) + f(b));

		// 4/3 terms
		for (int i = 1; i < N - 1; i += 2) {
			double x = a + h * i;
			sum += 4.0 / 3.0 * f(x);
		}

		// 2/3 terms
		for (int i = 2; i < N - 1; i += 2) {
			double x = a + h * i;
			sum += 2.0 / 3.0 * f(x);
		}

		return sum * h;
	}
}
