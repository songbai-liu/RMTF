package etmo.problems.Benchmarks_RMTF;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.realWorld.Training_DNN;
import etmo.qualityIndicator.util.MetricsUtil;

public class RMTF2 {
	
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
		String dataSetName = "RaisinGrains";//ConnectionistBenchSonar
		double[][] dataSet = utils_.readFront("DataSet/" + dataSetName + ".txt");
		int sampleSize = dataSet.length;
		int featureDim = dataSet[0].length-1;
		//Data preprocessing: Standardization
		double[][] standDataSet = utils_.getStandardizationDataSet(dataSet, featureDim);

		//Split the dataSet into training set and testing set
		int trainingSize = (int)(sampleSize*0.8);
		int testingSize = sampleSize - trainingSize;
		double[][] trainIn = utils_.extractDataSet(standDataSet, 0, trainingSize, 0, featureDim);
		double[] trainLabel = utils_.extractColumnVector(standDataSet, 0, trainingSize, featureDim);
		double[][] testIn = utils_.extractDataSet(standDataSet, trainingSize, sampleSize, 0, featureDim);
		double[] testLabel = utils_.extractColumnVector(standDataSet, trainingSize, sampleSize, featureDim);
		
		Training_DNN prob = new Training_DNN(trainIn, trainLabel, 50, 20);
		prob.setLossType("RMSE");	
		prob.setActivationType("Tanh");
		prob.setRegularizationType("L1");
		
		((Problem)prob).setName("RMTF2_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		String dataSetName = "RiceOsmancikCammeo";
		double[][] dataSet = utils_.readFront("DataSet/" + dataSetName + ".txt");
		int sampleSize = dataSet.length;
		int featureDim = dataSet[0].length-1;
		//Data preprocessing: Standardization
		double[][] standDataSet = utils_.getStandardizationDataSet(dataSet, featureDim);

		//Split the dataSet into training set and testing set
		int trainingSize = (int)(sampleSize*0.5);
		int testingSize = sampleSize - trainingSize;
		double[][] trainIn = utils_.extractDataSet(standDataSet, 0, trainingSize, 0, featureDim);
		double[] trainLabel = utils_.extractColumnVector(standDataSet, 0, trainingSize, featureDim);
		double[][] testIn = utils_.extractDataSet(standDataSet, trainingSize, sampleSize, 0, featureDim);
		double[] testLabel = utils_.extractColumnVector(standDataSet, trainingSize, sampleSize, featureDim);
		
		Training_DNN prob = new Training_DNN(trainIn, trainLabel, 50, 20);
		prob.setLossType("MAE");	
		prob.setActivationType("DecayingSineUnit");
		prob.setRegularizationType("L2");
		
		((Problem)prob).setName("RMTF2_2");
		
		problemSet.add(prob);
		return problemSet;
	}
}
