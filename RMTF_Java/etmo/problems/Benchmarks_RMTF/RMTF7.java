package etmo.problems.Benchmarks_RMTF;

import java.io.IOException;

import Jama.Matrix;
import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.realWorld.PortfolioOpt;
import etmo.problems.base.realWorld.computeCovarianceMatrix;
import etmo.qualityIndicator.util.MetricsUtil;

public class RMTF7 {
	
	public static MetricsUtil utils_ = new MetricsUtil();
	
	public static ProblemSet getProblem() throws IOException {
		
		ProblemSet ps1 = getT1();
		ProblemSet ps2 = getT2();
		ProblemSet problemSet = new ProblemSet(2);

		problemSet.add(ps1.get(0));
		problemSet.add(ps2.get(0));
		return problemSet;

	}
	
	public static ProblemSet getT1() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		//Load the dataSet
		String dataSetName = "PoData1";
		double[][] dataSet = utils_.readFront("DataSet/" + dataSetName + ".txt");
		int instruments = dataSet.length;
		int len = dataSet[0].length;
		Matrix Yield = new Matrix(instruments, len-1);
		for(int i=0;i<instruments;i++) {
			for(int j=0;j<len-1;j++) {
				double value = Math.log(dataSet[i][j+1]) - Math.log(dataSet[i][j]);
				Yield.set(i, j, value); 
			}
		}
		Matrix Risk = computeCovarianceMatrix.covarianceMatrix(Yield.transpose());
		
		PortfolioOpt prob = new PortfolioOpt(Yield, Risk);
		
		((Problem)prob).setName("RMTF7_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		String dataSetName = "PoData2";
		double[][] dataSet = utils_.readFront("DataSet/" + dataSetName + ".txt");
		
		int instruments = dataSet.length;
		int len = dataSet[0].length;
		Matrix Yield = new Matrix(instruments, len-1);
		for(int i=0;i<instruments;i++) {
			for(int j=0;j<len-1;j++) {
				double value = Math.log(dataSet[i][j+1]) - Math.log(dataSet[i][j]);
				Yield.set(i, j, value); 
			}
		}
		Matrix Risk = computeCovarianceMatrix.covarianceMatrix(Yield.transpose());
		
		PortfolioOpt prob = new PortfolioOpt(Yield, Risk);
		
		((Problem)prob).setName("RMTF7_2");
		
		problemSet.add(prob);
		return problemSet;
	}
}
