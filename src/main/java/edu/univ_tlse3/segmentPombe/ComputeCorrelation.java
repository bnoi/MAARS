package edu.univ_tlse3.segmentPombe;

public class ComputeCorrelation {
    private float[] iz_;
    private float zf;
    private float sigma;
    private int direction;
    private int N_ = 101; // precision parameter
    private double h_;
    private int first_ind_;
    private double[] ind_d_ = new double[N_];
    private int[] ind_i_ = new int[N_];
    private double[] smooth_ponderation_ = new double[N_];

    // it is -1 for image with cell boundaries be black then white
    // it is 1 for image with cell boundaries be white then black

    /**
     * f is the function which is going to be integrated on z : Iz[z] * (z - zf)
     * * np.exp(-(zf - z) ** 2 / (2 * sigma ** 2)) CorrelationPixVal f = new
     * CorrelationPixVal(iz, zf, sigma);
     * @param zf        focus plan
     * @param sigma     typical cell size
     * @param direction -1 or 1
     */
    public ComputeCorrelation(float zf, float sigma, int direction) {
        this.zf = zf;
        this.sigma = sigma;
        this.direction = direction;
    }

    public void preCalculateParameters(int first_ind, int last_ind) {
        first_ind_ = first_ind;
        h_ = (last_ind - first_ind) / (double) N_; // step size


        for (int i = 1; i <= ind_d_.length - 1; i += 1) {
            ind_d_[i] = h_ * i;
        }

        for (int i = 1; i <= ind_d_.length - 1; i += 1) {
            ind_i_[i] = (int) ind_d_[i];
        }

        for (int i = 1; i <= ind_d_.length - 1; i += 1) {
            smooth_ponderation_[i] = direction * (ind_d_[i] - zf) *
                    Math.exp(-Math.pow(zf - ind_d_[i], 2) / (2 * Math.pow(sigma, 2)));
        }
    }

    /**********************************************************************
     * Standard normal distribution density function. Replace with any
     * sufficiently smooth function.
     **********************************************************************/
    private double f(int index) {
        return iz_[ind_i_[index]] * smooth_ponderation_[index];
    }

    /**********************************************************************
     * Integrate f from a to b using Simpson's rule. Increase N for more
     * precision.
     * @param iz    z dimension pixel values of a 2d x y plan
     * @return      double integrate number
     **********************************************************************
     */
    public double integrate(float[] iz) {
        iz_ = iz;
        // 1/3 terms
        double sum = f(first_ind_) + f(N_ - 1);

        // 4/3 terms
        for (int i = 1; i < N_; i += 2) {
            sum += 4.0 * f(first_ind_ + i);
        }

        // 2/3 terms
        for (int i = 2; i < N_ - 1; i += 2) {
            sum += 2.0 * f(first_ind_ + i);
        }

        return sum * h_ / 3.0;
    }
}