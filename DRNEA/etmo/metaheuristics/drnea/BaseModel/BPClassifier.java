package etmo.metaheuristics.drnea.BaseModel;

import java.util.Random;

public class BPClassifier {
	private double[][] layer;//Record the output of each neuron at each layer
	private double[][][] weight;//Record all weights of the entire neural network，[n][a][b]，n < The total number of layer-1
	private double[][] error;//errors
	private double rate;//learning rate
	private int[] numberOfLayer;
	
	public BPClassifier(int[] layernum, double r) {
		numberOfLayer = new int[layernum.length]; //Record the number of neurons in each layer
		for (int i = 0; i < layernum.length; i++) {
			numberOfLayer[i] = layernum[i];
		}
		
		init(numberOfLayer, r);
	}
	
	/*
	 * Randomly initializes the weights and bias
	 * The bias is recored in the last column of the weight matrix
	*/
	public void init(int[] numberOfLayer,double r) {
		rate=r;
		int n = numberOfLayer.length;//the number of layers of the auto-encoder
		layer = new double[n][];
		weight = new double[n-1][][];
		error = new double[n][];
		for(int i=0;i<n;++i) {
			layer[i] = new double[numberOfLayer[i]];
			error[i] = new double[numberOfLayer[i]];
			if(i < n-1) {
				weight[i] = new double[numberOfLayer[i]+1][numberOfLayer[i+1]];
				for(int j=0;j<numberOfLayer[i]+1;++j)
					for(int k=0;k<numberOfLayer[i+1];++k)
						weight[i][j][k] = Math.random();
			}
		}
	}
	
	/*
	 * Adopted Activation Function: sigmoid, s(x) = 1/(1+exp(-x))
	 * assume z is the weighted sum of each neuron
	 * the output of each neuron is s(z)
	*/
	public double[] computeOut(double[] inp) {
		for(int i=1;i<layer.length;++i) {//start from the first hidden layer, i.e., i = 1
			for(int k=0;k<layer[i].length;++k) {
				double z = weight[i-1][layer[i-1].length][k];//z is initialized as the bias
				for(int j=0;j<layer[i-1].length;++j) {
					layer[i-1][j] = i==1? inp[j]:layer[i-1][j];//initialization of the input layer
					z += weight[i-1][j][k]*layer[i-1][j]; //get the weighted sum
				}
				layer[i][k] = Function.sigmoid(z);
			}
		}
		return layer[layer.length-1];
	}
	
	/*
	 * The adopted loss function: Square Variance
	 * Starting from the second last layer 
	 * the error is calculated backwards 
	 * the weights and deviations are accordingly updated
	*/
	public void backPropagation(double[] target) {
		//Calculate the error of the last layer first
		int i = layer.length-1;//the last layer, i.e., the output layer
		for(int j=0;j<layer[i].length;++j)
			//here use (target[j]-layer[i][j]), so the new weights is updates by adding rate*error*layer 
			error[i][j] = layer[i][j]*(1-layer[i][j])*(target[j]-layer[i][j]); //here use (target[j]-layer[i][j]), so when update the 
		while(i-- > 0){//until to the fist layer, i.e., the input layer
			//The error and the weight are updated simultaneously
			for(int j=0;j<layer[i].length;++j) {
				double err = 0.0;
				for(int k=0;k<layer[i+1].length;++k) {
					err += weight[i][j][k]*error[i+1][k];
					weight[i][j][k] += rate*error[i+1][k]*layer[i][j];
					if(j == layer[i].length-1) //Adjust the bias
						weight[i][j+1][k] += rate*error[i+1][k];
				}
				error[i][j] = err*layer[i][j]*(1.0-layer[i][j]);
			}
		}
	}
	
	public void trainModel(double[][] inp, double[][] target) {
		for(int i=0;i<inp.length;++i) {
			computeOut(inp[i]);
			backPropagation(target[i]);
		}
	}
	
	public void trainModel(double [] inp, double[] target) {
		computeOut(inp);
		backPropagation(target);
	}

	public double[] getError(){
		return error[0];
	}
	public double testing(double[] inp, int t){
		double test_target;
		double[] output = computeOut(inp);
		double sum = 0.0;
		for(int i=0;i<output.length;i++){
			sum += Math.exp(output[i]);
		}
		test_target = Math.exp(output[t])/sum;
		return test_target;
	}
}
