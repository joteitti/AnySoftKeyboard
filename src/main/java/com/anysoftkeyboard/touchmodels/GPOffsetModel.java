package com.anysoftkeyboard.touchmodels;

public class GPOffsetModel implements TouchOffsetModel {

    private double mAlpha;
    private double mBeta;
    private double mGamma;
    private double mDelta;
    private double mVariance;

    /**
     * Uses the default parameters.
     */
    public GPOffsetModel() {
        this(0.1, 5.0, 0.05, 0.9, 0.001);
    }

    /**
     * Creates Gaussian Process (GP) regression model that uses
     * the user defined parameters in the covariance function.
     * @param alpha Controls the relative influence of the linear and Gaussian terms.
     * @param beta ???
     * @param gamma Controls the length scale of the Gaussian.
     * @param delta Controls the strength of the dependence between x and y.
     * @param variance Additive Gaussian noise.
     */
    public GPOffsetModel(double alpha, double beta, double gamma, double  delta, double variance) {
        mAlpha = alpha;
        mBeta = beta;
        mGamma = gamma;
        mDelta = delta;
        mVariance = variance;
    }

    @Override
    public int getOffsetX(int x, int y) {
        return 10;
    }

    @Override
    public int getOffsetY(int x, int y) {
        return 10;
    }
}
