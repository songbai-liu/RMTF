package etmo.problems.base.realWorld;

import Jama.Matrix;

public class computeCovarianceMatrix {
	
	public static Matrix covarianceMatrix(Matrix matrix){
		
		int dim = matrix.getColumnDimension();
		Matrix covMatrix = new Matrix(dim, dim);//
		matrix = matrix.transpose();
		double[][] transInput = matrix.getArray();
		
		for(int i=0;i<dim;i++) {
			for(int j=0;j<i;j++) {
				double cov = covariance(transInput[i], transInput[j], true);
				covMatrix.set(i,j,cov);
				covMatrix.set(j,i,cov);
			}
			covMatrix.set(i,i,variance(transInput[i]));
		}

		return covMatrix;
	}
	
	public static double variance(final double[] values) {
	    double mean = mean(values);
        double var = Double.NaN;
        double len = values.length;
        if (len == 1) {
            var = 0.0;
        } else if (len > 1) {
            double accum = 0.0;
            double dev = 0.0;
            double accum2 = 0.0;
            for (int i = 0; i < len; i++) {
                dev = values[i] - mean;
                accum += dev * dev;
                accum2 += dev;
            }
            var = (accum - (accum2 * accum2 / len)) / (len - 1.0);
        }
        return var;
	}
	
	public static double covariance(double[] xArray, double[] yArray, boolean biasCorrected) {
        double result = 0d;
        int length = xArray.length;
        if (length != yArray.length) {
            System.out.println("Erro in computing the covariance!");
            System.exit(0);
        } else if (length < 2) {
        	System.out.println("Erro in computing the covariance!");
            System.exit(0);
        } else {
            double xMean = mean(xArray);
            double yMean = mean(yArray);
            for (int i = 0; i < length; i++) {
                double xDev = xArray[i] - xMean;
                double yDev = yArray[i] - yMean;
                result += (xDev * yDev - result) / (i + 1);
            }
        }
        return biasCorrected ? result * ((double) length / (double)(length - 1)) : result;
	}
	
	public static double mean(double[] array) {
		double mv = 0.0;
		double avg = 0.0;
		int len = array.length;
		for(int i=0;i<len;i++) {
			avg += array[i];
		}
		avg /= len;
		
		for(int i=0;i<len;i++) {
			mv += array[i] - avg;
		}
		
		mv = avg + (mv/len);
		
		return mv;
	}
}
