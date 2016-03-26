package com.anysoftkeyboard.touchmodels;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.simple.SimpleMatrix;

public class GPOffsetModel implements TouchOffsetModel {

    private double mAlpha;
    private double mBeta;
    private double mGamma;
    private double mDelta;
    private double mVariance;

    private SimpleMatrix m_input;
    private SimpleMatrix m_covariance;

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

        // TODO: Find a better way!
        train(OffsetTrainingData.RAW);
    }

    protected SimpleMatrix calculateCovariance(SimpleMatrix m1, SimpleMatrix m2) {
        int n1 = m1.numRows();
        int n2 = m2.numRows();

        SimpleMatrix kernel = new SimpleMatrix(n1, n2);

        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                SimpleMatrix v1 = m1.extractVector(true, i);
                SimpleMatrix v2 = m2.extractVector(true, j);
                SimpleMatrix diff = v1.minus(v2);
                double d = diff.mult(diff.transpose()).elementSum();
                double s = v1.transpose().mult(v2).get(0, 0);
                kernel.set(i, j, mBeta * (mAlpha * s + (1.0 - mAlpha) * Math.exp(-mGamma * d)));
            }
        }

        SimpleMatrix covariance = new SimpleMatrix(n1 * 2, n2 * 2);

        covariance.insertIntoThis(0, 0, kernel);
        covariance.insertIntoThis(n1, 0, kernel.scale(mDelta));
        covariance.insertIntoThis(0, n2, kernel.scale(mDelta));
        covariance.insertIntoThis(n1, n2, kernel);

        return covariance;
    }

    protected void train(double[][] data) {

        SimpleMatrix temp = new SimpleMatrix(data);

        // Stack up all of the locations into a single vector because
        // it is easier to process in one-dimensional regression.

        SimpleMatrix targets = new SimpleMatrix(temp.numRows() * 2, 1);

        targets.insertIntoThis(0, 0, temp.extractVector(false, 2));
        targets.insertIntoThis(temp.numRows(), 0, temp.extractVector(false, 3));

        // Store the touch locations.

        m_input = temp.extractMatrix(0, temp.numRows(), 0, 2);

        // Calculate the full covariance matrix using
        // the user specified covariance function.

        SimpleMatrix cov = calculateCovariance(m_input, m_input);

        // Add some additive noise to overcome possible problems
        // resulting from trying to map very similar input values
        // to different intended touch positions.

        cov = cov.plus(SimpleMatrix.identity(cov.numRows()).scale(mVariance));

        // Invert the covariance matrix.

        DenseMatrix64F covMatrix = cov.getMatrix();
        LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.symmPosDef(covMatrix.getNumRows());
        solver.setA(covMatrix);
        DenseMatrix64F result = new DenseMatrix64F(covMatrix.getNumRows(), covMatrix.getNumCols());
        solver.invert(result);

        // And finally store the covariance matrix.

        m_covariance = SimpleMatrix.wrap(result).mult(targets);
    }

    private double[] predict(double[] data) {

        SimpleMatrix input = new SimpleMatrix(1, data.length);
        input.setRow(0, 0, data);

        // Calculate the covariance matrix.

        SimpleMatrix cov = calculateCovariance(input, m_input);

        // Calculate the GP prediction mean.

        SimpleMatrix result = cov.mult(m_covariance);

        // Return the predicted offsets.

        return new double[]{result.get(0, 0), result.get(1, 0)};
    }

    private int mOffsetX = -1;
    private int mOffsetY = -1;
    private int mLastX = -1;
    private int mLastY = -1;

    /** Store the array to reduce GC overhead. */
    private final double[] mData = new double[2];

    private void calculateOffset(int x, int y) {

        // We are caching the last values to save some processing
        // time. Usually the same values are used twice or more.

        if (mLastX == x && mLastY == y) {
            return;
        }

        mData[0] = (double)x;
        mData[1] = (double)y;

        double[] result = predict(mData);

        mOffsetX = (int)Math.round(result[0]);
        mOffsetY = (int)Math.round(result[1]);

        mLastX = x;
        mLastY = y;
    }

    @Override
    public int getOffsetX(int x, int y) {
        calculateOffset(x, y);
        return mOffsetX;
    }

    @Override
    public int getOffsetY(int x, int y) {
        calculateOffset(x, y);
        return mOffsetY;
    }
}
