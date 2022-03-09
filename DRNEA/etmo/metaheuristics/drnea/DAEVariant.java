package etmo.metaheuristics.drnea;

import etmo.core.Solution;
import etmo.core.SolutionSet;
import etmo.metaheuristics.deepAE.BaseAE.*;
import etmo.util.JMException;
import etmo.util.PseudoRandom;
import etmo.util.wrapper.XReal;

public class DAEVariant {
	
	int features;
	int feaAfterEncode;
	int layerNum;
	double learningRate;
	BPAutoEncode baseAE;
	
	
	public DAEVariant (int numVars, int reD, int layerNum, double rate) {
		features = numVars;
		feaAfterEncode = reD;
		this.layerNum = layerNum;
		learningRate = rate;
		baseAE = new BPAutoEncode(features,feaAfterEncode,layerNum,learningRate);
	}
	
	public void getTrainingModel(SolutionSet inputSet, SolutionSet targetOutputSet, int epochs) throws JMException {
		int size = inputSet.size();
		int tSize = targetOutputSet.size();
		double[][] inputs = new double[size][features];
		double[][] targets = new double[size][features];
		for(int i=0;i<size;i++) {
			XReal sol = new XReal(inputSet.get(i));
			XReal tar = new XReal(targetOutputSet.get(i));
			for(int j=0;j<features;j++) {
				inputs[i][j] = sol.getValue(j);
				targets[i][j] = tar.getValue(j);
			}
		}
		baseAE.trainModel(inputs, targets, epochs);
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
