package etmo.problems.Benchmarks_RMTF;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.realWorld.Training_DNN;
import etmo.qualityIndicator.util.MetricsUtil;

public class RMTF4 {
	
	public static MetricsUtil utils_ = new MetricsUtil();
	
	public static ProblemSet getProblem() throws IOException {
		ProblemSet ps1 = getT1();
		ProblemSet ps2 = getT2();
		ProblemSet ps3 = getT3();
		ProblemSet problemSet = new ProblemSet(3);

		problemSet.add(ps1.get(0));
		problemSet.add(ps2.get(0));
		problemSet.add(ps3.get(0));
		return problemSet;
	}
	
	public static ProblemSet getT1() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		//Load the dataSet
		String dataSetName = "WaveForm01";
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
		
		Training_DNN prob = new Training_DNN(trainIn, trainLabel, 20, 10);
		prob.setLossType("MSE");	
		prob.setActivationType("SoftSign");
		prob.setRegularizationType("L1");
		
		((Problem)prob).setName("RMTF4_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		String dataSetName = "WaveForm02";
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
		
		Training_DNN prob = new Training_DNN(trainIn, trainLabel, 20, 10);
		prob.setLossType("MAE");	
		prob.setActivationType("SoftSign");
		prob.setRegularizationType("L1");
		
		((Problem)prob).setName("RMTF4_2");
		
		problemSet.add(prob);
		return problemSet;
	}
	
	public static ProblemSet getT3() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		String dataSetName = "WaveForm12";
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
		
		Training_DNN prob = new Training_DNN(trainIn, trainLabel, 20, 10);
		prob.setLossType("RMSE");	
		prob.setActivationType("SoftSign");
		prob.setRegularizationType("L1");
		
		((Problem)prob).setName("RMTF4_3");
		
		problemSet.add(prob);
		return problemSet;
	}
}

