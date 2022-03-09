package etmo.metaheuristics.drnea.BaseModel;

import Jama.Matrix;

import java.io.FileWriter;
import java.io.IOException;

public class PCAMain {

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated catch block
        PCA pca = new PCA();
        //Get a sample set
        double[][] primaryArray = { { 100, 2, 3, 4, 1, 2, 32, 2 }, { 1, 2, 31, 52, 1, 2, 32, 2 },
                { 1, 2, 32, 2, 1, 2, 31, 52 }, { 1, 2, 32, 2, 1, 2, 30, 52 } };
        System.out.println("--------------------------------------------");
        double[][] averageArray = pca.changeAverageToZero(primaryArray);
        System.out.println("--------------------------------------------");
        System.out.println("The data after the mean is zero: ");
        System.out.println(averageArray.length + "rowsï¼?"
                + averageArray[0].length + "columns");

        System.out.println("---------------------------------------------");
        System.out.println("Covariance matrix: ");
        double[][] varMatrix = pca.getVarianceMatrix(averageArray);

        System.out.println("--------------------------------------------");
        System.out.println("Eigenvalue matrix: ");
        double[][] eigenvalueMatrix = pca.getEigenvalueMatrix(varMatrix);

        System.out.println("--------------------------------------------");
        System.out.println("Eigenvector matrix: ");
        double[][] eigenVectorMatrix = pca.getEigenVectorMatrix(varMatrix);

        System.out.println("--------------------------------------------");
        Matrix principalMatrix = pca.getPrincipalComponent(primaryArray, eigenvalueMatrix, eigenVectorMatrix);
        System.out.println("Principal component matrix: ");
//        principalMatrix.print(6, 3);

        System.out.println("--------------------------------------------");
        System.out.println("The dimensionality reduction matrix: ");
        Matrix resultMatrix = pca.getResult(primaryArray, principalMatrix);
//        resultMatrix.print(6, 3);
        int c = resultMatrix.getColumnDimension(); //The number of columns
        int r = resultMatrix.getRowDimension();//The number of rows
        System.out.println(resultMatrix.getRowDimension() + "," + resultMatrix.getColumnDimension());
    }
}