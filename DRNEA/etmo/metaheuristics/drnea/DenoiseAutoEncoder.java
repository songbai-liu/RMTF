package etmo.metaheuristics.drnea;

import etmo.core.Solution;
import etmo.core.SolutionSet;
import etmo.metaheuristics.drnea.BaseModel.DeepEncode;
import etmo.util.JMException;
import etmo.util.wrapper.XReal;

public class DenoiseAutoEncoder {
	
	int features;
	int feaAfterEncode;
	int layerNum;
	double learningRate;
	DeepEncode baseAE;
	
	
	public DenoiseAutoEncoder(int numVars, int reD, int layerNum, double rate) {
		features = numVars;
		feaAfterEncode = reD;
		this.layerNum = layerNum;
		learningRate = rate;
		baseAE = new DeepEncode(features,feaAfterEncode,layerNum,learningRate);
	}
	
	public DenoiseAutoEncoder(int numVars, int reD, int[] layerNum, double rate) {
		features = numVars;
		feaAfterEncode = reD;
		learningRate = rate;
		baseAE = new DeepEncode(features,feaAfterEncode,layerNum,learningRate);
	}
	
	public void getTrainingModel(SolutionSet trainingSet, int epochs) throws JMException {
		int size = trainingSet.size();
		double[][] inputs = new double[size][features]; 
		for(int i=0;i<size;i++) {
			XReal sol = new XReal(trainingSet.get(i));
			for(int j=0;j<features;j++) {
				inputs[i][j] = sol.getValue(j);
			}
		}
		baseAE.trainModel(inputs, epochs);
	}
	
	public void trainingModel(Solution individual, int epochs) throws JMException {
		double[] inputs = new double[features]; 
		XReal sol = new XReal(individual);
		for(int j=0;j<features;j++) {
			inputs[j] = sol.getValue(j);
		}
		baseAE.trainModel(inputs, epochs);
	}
	
	public void getTrainingModel(SolutionSet trainingSet, double p, int epochs) throws JMException {
		int size = trainingSet.size();
		double[][] inputs = new double[size][features]; 
		for(int i=0;i<size;i++) {
			XReal sol = new XReal(trainingSet.get(i));
			for(int j=0;j<features;j++) {
				inputs[i][j] = sol.getValue(j);
			}
		}
		baseAE.trainModel(inputs, p, epochs);
	}
	
	public double[] encode(Solution sol, int dim) throws JMException {
		double[] encodedSolution = new double[feaAfterEncode];
		XReal xsol = new XReal(sol);
		if(dim != features) {
			System.out.println("The variable-related dimensions of the input solution do not match the model at encode a single solution!");
			System.out.println("features = " + features + ", and the dimensions of input is: " + dim);
			System.exit(0);
		}else {
			double[] input = new double[features];
			for(int i=0;i<features;i++) { 
				input[i] = xsol.getValue(i); 
			}
			baseAE.computeEncodeData(input, encodedSolution);
		}
		return encodedSolution;
	}
	
	public double[][] encode(SolutionSet solSet,int dim) throws JMException{
		int size = solSet.size();
		double[][] encodedSet = new double[size][feaAfterEncode];
		for(int p=0;p<size;p++) {
			XReal xsol = new XReal(solSet.get(p));
			if(dim != features) {
				System.out.println("The variable-related dimensions of the input solution do not match the model at encode a solution set!");
				System.out.println("features = " + features + ", and the dimensions of input is: " + dim);
				System.exit(0);
			}else {
				double[] input = new double[features];
				for(int i=0;i<features;i++) { 
					input[i] = xsol.getValue(i); 
				}
				baseAE.computeEncodeData(input, encodedSet[p]);
			}
		}
		return encodedSet;
	}
	
	public double[] encode(double[] input) {
		double[] encodedSolution = new double[feaAfterEncode];
		if(input.length != features) {
			System.out.println("The variable-related dimensions of the input solution do not match the model at encode a array of input!");
			System.exit(0);
		}else {
			baseAE.computeEncodeData(input, encodedSolution);
		}
		return encodedSolution;
	}
	
	public double[] decode(double[] encodeData) {
		double[] decodedSolution = new double[features];
		if(encodeData.length != feaAfterEncode) {
			System.out.println("The dimensions of the input encoded data do not match the model at decode a arry of input!");
			System.exit(0);
		}else {
			baseAE.computeDecodeData(encodeData, decodedSolution);
		}
		return decodedSolution;
	}

}
