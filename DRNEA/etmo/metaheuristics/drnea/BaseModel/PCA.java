package etmo.metaheuristics.drnea.BaseModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import Jama.Matrix;

/*
 * Steps of PCA:
 * 1)Raw data is organized into matrix X with n rows m columns;
 * 2)Feature centralization.That is, the mean of each dimension is subtracted from the data in each dimension, so that the mean of each dimension is 0;
 * 3)Find the covariance matrix;
 * 4)The eigenvalues and corresponding eigenvectors of the covariance matrix are obtained;
 * 5)The eigenvectors are arranged in rows according to the value of corresponding eigenvalues, and the first k rows are taken to form matrix P;
 * 6)Y=PX is the result after dimension reduction to k dimension.
 */
public class PCA {
    
	private static final double threshold = 0.95;//Eigenvalue threshold

    /**
     * 
     * Feature centralization
     * 
     * @param primary
     * Raw two-dimensional array matrix
     * @return averageArray: Centralized matrix
     */
    public double[][] changeAverageToZero(double[][] primary) {
        int n = primary.length;
        int m = primary[0].length;
        double[] sum = new double[m];
        double[] average = new double[m];
        double[][] averageArray = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sum[i] += primary[j][i];
            }
            average[i] = sum[i] / n;
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                averageArray[j][i] = primary[j][i] - average[i];
            }
        }
        return averageArray;
    }

    /**
     * 
     * Calculate the covariance matrix
     * 
     * @param matrix
     * Centralized matrix
     * @return result: Covariance matrix
     */
    public double[][] getVarianceMatrix(double[][] matrix) {
        int n = matrix.length;// rows
        int m = matrix[0].length;// columns
        double[][] result = new double[m][m];// Covariance matrix
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                double temp = 0;
                for (int k = 0; k < n; k++) {
                    temp += matrix[k][i] * matrix[k][j];
                }
                result[i][j] = temp / (n - 1);
            }
        }
        return result;
    }

    /**
     * Find the eigenvalue matrix
     * 
     * @param matrix
     * Covariance matrix
     * @return result: A two-dimensional array matrix of eigenvalues of vectors
     */
    public double[][] getEigenvalueMatrix(double[][] matrix) {
        Matrix A = new Matrix(matrix);
        //The diagonal matrix is composed of eigenvalues. obtain the eigenvalues via eig() method
        // A.eig().getD().print(10, 6);
        double[][] result = A.eig().getD().getArray();
        return result;
    }

    /**
     * Standardized matrix (eigenvector matrix)
     * 
     * @param matrix
     * Eigenvalue matrix
     * @return result: A normalized two-dimensional array matrix
     */
    public double[][] getEigenVectorMatrix(double[][] matrix) {
        Matrix A = new Matrix(matrix);
//      A.eig().getV().print(6, 2);
        double[][] result = A.eig().getV().getArray();
        return result;
    }

    /**
     *Looking for principal components
     * 
     * @param prinmaryArray
     * Raw two-dimensional array array
     * @param eigenvalue
     * A two-dimensional array of eigenvalues
     * @param eigenVectors
     * A two-dimensional array of eigenvectors
     * @return principalMatrix: Principal component matrix
     */
    public Matrix getPrincipalComponent(double[][] primaryArray,
            double[][] eigenvalue, double[][] eigenVectors) {
        Matrix A = new Matrix(eigenVectors);// Define an eigenvector matrix
        double[][] tEigenVectors = A.transpose().getArray();// The eigenvector transpose
        Map<Integer, double[]> principalMap = new HashMap<Integer, double[]>();// key=Principal component eigenvalue，value=corresponding to the eigenvector
        TreeMap<Double, double[]> eigenMap = new TreeMap<Double, double[]>(
                Collections.reverseOrder());// key=eigenvalue，value=eigenvector；Initialize to flip sort，Sort the map in descending order of key values
        double total = 0;// Stores the sum of eigenvalues
        int index = 0, n = eigenvalue.length;
        double[] eigenvalueArray = new double[n];// Put the elements on the diagonal of the eigenvalue matrix into the array EigenvalueArray
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    eigenvalueArray[index] = eigenvalue[i][j];
            }
            index++;
        }

        for (int i = 0; i < tEigenVectors.length; i++) {
            double[] value = new double[tEigenVectors[0].length];
            value = tEigenVectors[i];
            eigenMap.put(eigenvalueArray[i], value);
        }

        // Sum of features
        for (int i = 0; i < n; i++) {
            total += eigenvalueArray[i];
        }
        // Pick out the first few principal components
        double temp = 0;
        int principalComponentNum = 0;// The number of principal components
        List<Double> plist = new ArrayList<Double>();//Principal component eigenvalue
        for (double key : eigenMap.keySet()) {
            if (temp / total <= threshold) {
                temp += key;
                plist.add(key);
                principalComponentNum++;
            }
        }
        System.out.println("\n" + "The current threshold: " + threshold);
        System.out.println("Number of principal components obtained: " + principalComponentNum + "\n");

        // Enter data into the principal component map
        for (int i = 0; i < plist.size(); i++) {
            if (eigenMap.containsKey(plist.get(i))) {
                principalMap.put(i, eigenMap.get(plist.get(i)));
            }
        }

        // Store the value of the map in a two-dimensional array
        double[][] principalArray = new double[principalMap.size()][];
        Iterator<Entry<Integer, double[]>> it = principalMap.entrySet()
                .iterator();
        for (int i = 0; it.hasNext(); i++) {
            principalArray[i] = it.next().getValue();
        }

        Matrix principalMatrix = new Matrix(principalArray);

        return principalMatrix;
    }

    /**
     * Matrix multiplication
     * 
     * @param primary
     * Primitive two-dimensional array
     * 
     * @param matrix
     * Principal component matrix
     * 
     * @return result: The result matrix
     */
    public Matrix getResult(double[][] primary, Matrix matrix) {
        Matrix primaryMatrix = new Matrix(primary);
        Matrix result = primaryMatrix.times(matrix.transpose());
        return result;
    }
}