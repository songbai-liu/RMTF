package etmo.metaheuristics.drnea.BaseModel;

import java.util.Arrays;
import java.util.Random;

import etmo.util.PseudoRandom;

/*
 * Basic Implementation based on the BP Neural Network:
 * Activation Function: Sigmoid
 * Loss Function: Square Variance
 * This program in the presence of three iteration variables:
 * i indicates the number of layers;
 * j represents a neuron in a certain layer 
 * k is a neuron in the next layer of that j belong to 
*/ 
public class BPEncode implements IbpBase{
	private double[][] layer;//Record the output of each neuron at each layer
	private double[][][] weight;//Record all weights of the entire neural network，[n][a][b]，n < The total number of layer-1
	private double[][] error;//errors
	private double rate;//learning rate
	private int[] numberOfLayer;
	private int encodedSize;
	private int featureSize;
	
	/*
	 * Then encode phase:
	 * features indicates the number of inputs, i.e., the dimensions of the original data space
	 * feaAfterEncode indicates the dimensions of the compressed space
	 * layerNum is the number of layers
	 * r is the learning rate
	*/
	public BPEncode(int features,int feaAfterEncode,int layerNum,double r) {
		encodedSize = feaAfterEncode;
		featureSize = features;
		if(layerNum%2==0) ++layerNum; //The total number of layers of the auto-encoder is odd
		numberOfLayer = new int[layerNum]; //Record the number of neurons in each layer
		numberOfLayer[0] = numberOfLayer[layerNum-1] = features; //the input and output with the same dimension to features (input layer and output layer)
		numberOfLayer[layerNum/2] = feaAfterEncode; //The middle hidden layer is the final compression space
		int step = (features - feaAfterEncode) / (layerNum/2); //to determine the number of neurons in other hidden layers
		for (int i = 1; i < layerNum/2; i++) {
			numberOfLayer[i] = numberOfLayer[i-1] - step;
			numberOfLayer[layerNum-1-i] = numberOfLayer[layerNum-i] - step;
		}
		//System.out.println(Arrays.toString(numberOfLayer));
		init(numberOfLayer, r);
		//reInit(r);
	}
	
	
	public BPEncode(int features,int feaAfterEncode,int[] layerNum,double r) {
		encodedSize = feaAfterEncode;
		featureSize = features;
		numberOfLayer = layerNum;
		//System.out.println(Arrays.toString(numberOfLayer));
		init(numberOfLayer, r);
		//reInit(r);
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
	
	public void reInit(double r) {
		rate=r;
		for(int i=0;i<numberOfLayer.length;++i) {
			layer[i] = new double[numberOfLayer[i]];
			error[i] = new double[numberOfLayer[i]];
			if(i < numberOfLayer.length-1) {
				weight[i] = new double[numberOfLayer[i]+1][numberOfLayer[i+1]];
				for(int j=0;j<numberOfLayer[i]+1;++j)
					for(int k=0;k<numberOfLayer[i+1];++k)
						if(j == numberOfLayer[i]) weight[i][j][k] = Math.random();
						else weight[i][j][k] = 1.0/numberOfLayer[i];
			}
		}
	}
	
	public void reInit() {
		for(int i=0;i<numberOfLayer.length;++i) {
			layer[i] = new double[numberOfLayer[i]];
			error[i] = new double[numberOfLayer[i]];
			if(i < numberOfLayer.length-1) {
				weight[i] = new double[numberOfLayer[i]+1][numberOfLayer[i+1]];
				for(int j=0;j<numberOfLayer[i]+1;++j)
					for(int k=0;k<numberOfLayer[i+1];++k)
						weight[i][j][k] = Math.random();
			}
		}
	}

	/*
	 * Adopted Activation Function：sigmoid, s(x) = 1/(1+exp(-x))
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
			error[i][j] = layer[i][j]*(1-layer[i][j])*(target[j]-layer[i][j]); 
		while(i-- > 0){//until to the fist layer, i.e., the input layer
			//The error and the weight are updated simultaneously
			for(int j=0;j<layer[i].length;++j) {
				double err = 0.0;
				for(int k=0;k<layer[i+1].length;++k) {
					err += weight[i][j][k]*error[i+1][k];
					weight[i][j][k] += rate*error[i+1][k]*layer[i][j];
					//if(weight[i][j][k] < 0) weight[i][j][k] = 0;
					//if(weight[i][j][k] > 1) weight[i][j][k] = 1;
					
					if(j == layer[i].length-1) { //Adjust the bias
						weight[i][j+1][k] += rate*error[i+1][k];
						//if(weight[i][j+1][k] < 0) weight[i][j+1][k] = 0;
						//if(weight[i][j+1][k] > 1) weight[i][j+1][k] = 1;
					}
				}
				error[i][j] = err*layer[i][j]*(1.0-layer[i][j]);
			}
		}
	}
	
	/*
	 * The adopted loss function: Square Variance
	 * Starting from the second last layer 
	 * the error is calculated backwards 
	 * the weights and deviations are accordingly updated
	*/
	public void backPropagation_Encoder(double[] errors) {
		//Calculate the error of the last layer first
		int i = layer.length/2;//the code layer
		for(int j=0;j<layer[i].length;++j) 
			error[i][j] = errors[j]; 
		while(i-- > 0){//until to the fist layer, i.e., the input layer
			//The error and the weight are updated simultaneously
			for(int j=0;j<layer[i].length;++j) {
				double err = 0.0;
				for(int k=0;k<layer[i+1].length;++k) {
					err += weight[i][j][k]*error[i+1][k];
					weight[i][j][k] += rate*error[i+1][k]*layer[i][j];
					
					if(j == layer[i].length-1) { //Adjust the bias
						weight[i][j+1][k] += rate*error[i+1][k];
					}
				}
				error[i][j] = err*layer[i][j]*(1.0-layer[i][j]);
			}
		}
	}
	
	public void backPropagation_Decoder(double[] target) {
		//Calculate the error of the last layer first
		int i = layer.length-1;//the last layer, i.e., the output layer
		for(int j=0;j<layer[i].length;++j)
			//here use (target[j]-layer[i][j]), so the new weights is updates by adding rate*error*layer 
			error[i][j] = layer[i][j]*(1-layer[i][j])*(target[j]-layer[i][j]); 
		while(i-- > layer.length/2){//until to the representation layer, i.e., the code layer
			//The error and the weight are updated simultaneously
			for(int j=0;j<layer[i].length;++j) {
				double err = 0.0;
				for(int k=0;k<layer[i+1].length;++k) {
					err += weight[i][j][k]*error[i+1][k];
					weight[i][j][k] += rate*error[i+1][k]*layer[i][j];
					
					if(j == layer[i].length-1) { //Adjust the bias
						weight[i][j+1][k] += rate*error[i+1][k];
					}
				}
				error[i][j] = err*layer[i][j]*(1.0-layer[i][j]);
			}
		}		
	}
	
	/*
	 * Training this auto-encoder model by input a set of samples: inp
	 * Given a acceptable error range: p
	*/
	public void trainModel(double[][] inp,double p,int epochs) {
		int xn=0;
		double tmp;
		do {
			//Add noise to the input
			for(int i=0;i<inp.length;++i) {
				double[] nInp = new double[inp[i].length];
				for(int j=0;j<inp[i].length;j++) {
					if(PseudoRandom.randDouble() < 0.5) {
						nInp[j] = inp[i][j] + PseudoRandom.randDouble(-0.1,0.1)*(inp[i][j]+0.00001);
					}
				}
				computeOut(nInp);
				backPropagation(inp[i]);
			}
			tmp = reportModel(inp, p);
			++xn;
			if(xn % 100 == 0) {
				System.out.println("Iterative training " + (xn) + " times. The current accuracy of the model is�? " + tmp);
				p += 0.01; //Progressively increase the value of p,
			}  
		} while (tmp < 1.0-p && xn < epochs);
	}
	
	public void trainModel(double[][] inp,int epochs) {
		double probability = 0.1;
		double distributionIndex_ = 20.0;
		double eta_m_ = 20.0;
		double rnd, delta1, delta2, mut_pow, deltaq;
		double y, yl, yu, val, xy;
		yl = 0.0;
		yu = 1.0;
		for(int t=0;t<epochs;t++) {
			//in each epoch, the input data is updated via the polynomial mutation (PM) with some probability
			for(int i=0;i<inp.length;++i) {
				double[] nInp = new double[inp[i].length];
				for(int j=0;j<inp[i].length;j++) {
					y = inp[i][j];
					if (PseudoRandom.randDouble() <= probability) {
						delta1 = (y - yl) / (yu - yl);
						delta2 = (yu - y) / (yu - yl);
						mut_pow = 1.0 / (eta_m_ + 1.0);
						rnd = PseudoRandom.randDouble();
						if (rnd <= 0.5) {
							xy = 1.0 - delta1;
							val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, (distributionIndex_ + 1.0)));
							deltaq = java.lang.Math.pow(val, mut_pow) - 1.0;
						}else {
							xy = 1.0 - delta2;
							val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (java.lang.Math.pow(xy, (distributionIndex_ + 1.0)));
							deltaq = 1.0 - (java.lang.Math.pow(val, mut_pow));
						}
						y = y + deltaq * (yu - yl);
						if (y < yl)
							y = yl;
						if (y > yu)
							y = yu;
					}
					nInp[j] = y;
				}
				computeOut(nInp);
				backPropagation(inp[i]);
			}
		}
	}
	
	public void trainModel(double[] inp,int epochs) {
		double probability = 0.1;
		double distributionIndex_ = 20.0;
		double eta_m_ = 20.0;
		double rnd, delta1, delta2, mut_pow, deltaq;
		double y, yl, yu, val, xy;
		yl = 0.0;
		yu = 1.0;
		for(int t=0;t<epochs;t++) {
			//in each epoch, the input data is updated via the polynomial mutation (PM) with some probability
			double[] nInp = new double[inp.length];
			for(int j=0;j<inp.length;j++) {
				y = inp[j];
				if (PseudoRandom.randDouble() <= probability) {
					delta1 = (y - yl) / (yu - yl);
					delta2 = (yu - y) / (yu - yl);
					mut_pow = 1.0 / (eta_m_ + 1.0);
					rnd = PseudoRandom.randDouble();
					if (rnd <= 0.5) {
						xy = 1.0 - delta1;
						val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, (distributionIndex_ + 1.0)));
						deltaq = java.lang.Math.pow(val, mut_pow) - 1.0;
					}else {
						xy = 1.0 - delta2;
						val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (java.lang.Math.pow(xy, (distributionIndex_ + 1.0)));
						deltaq = 1.0 - (java.lang.Math.pow(val, mut_pow));
					}
					y = y + deltaq * (yu - yl);
					if (y < yl)
						y = yl;
					if (y > yu)
						y = yu;
				}
				nInp[j] = y;
			}
			computeOut(nInp);
			backPropagation(inp);
		}
	}
	
	public void trainModel(double[][] inp) {
		for(int i=0;i<inp.length;++i) {
			computeOut(inp[i]);
			backPropagation(inp[i]);
		}
	}
	
	public void trainEncoder(double[] errors) {
		backPropagation_Encoder(errors);
	}
	
	public void trainDecoder(double[] inp) {
		double probability = 0.1;
		double distributionIndex_ = 20.0;
		double eta_m_ = 20.0;
		double rnd, delta1, delta2, mut_pow, deltaq;
		double y, yl, yu, val, xy;
		yl = 0.0;
		yu = 1.0;
		
		double[] nInp = new double[inp.length];
		for(int j=0;j<inp.length;j++) {
			y = inp[j];
			if (PseudoRandom.randDouble() <= probability) {
				delta1 = (y - yl) / (yu - yl);
				delta2 = (yu - y) / (yu - yl);
				mut_pow = 1.0 / (eta_m_ + 1.0);
				rnd = PseudoRandom.randDouble();
				if (rnd <= 0.5) {
					xy = 1.0 - delta1;
					val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, (distributionIndex_ + 1.0)));
					deltaq = java.lang.Math.pow(val, mut_pow) - 1.0;
				}else {
					xy = 1.0 - delta2;
					val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (java.lang.Math.pow(xy, (distributionIndex_ + 1.0)));
					deltaq = 1.0 - (java.lang.Math.pow(val, mut_pow));
				}
				y = y + deltaq * (yu - yl);
				if (y < yl)
					y = yl;
				if (y > yu)
					y = yu;
			}
			nInp[j] = y;
		}
		computeOut(nInp);	
		backPropagation_Decoder(inp);
	}
	
	
	public void computeEncodeData(double[] inp,double[] result) {
		//computeOut(inp);
		for(int i=1;i<=layer.length/2;++i) {
			for(int k=0;k<layer[i].length;++k) {
				double z = weight[i-1][layer[i-1].length][k];
				for(int j=0;j<layer[i-1].length;++j) {
					layer[i-1][j] = i==1? inp[j]:layer[i-1][j];
					z += weight[i-1][j][k]*layer[i-1][j];
				}
				layer[i][k] = Function.sigmoid(z);
			}
		}
		for(int i=0;i<result.length;++i) 
			result[i] = layer[layer.length/2][i];
	}
	
	public void computeDecodeData(double[] encodeData,double[] result) {
		for(int i=layer.length/2+1;i<layer.length;++i) {
			for(int k=0;k<layer[i].length;++k) {
				double z = weight[i-1][layer[i-1].length][k];
				for(int j=0;j<layer[i-1].length;++j) {
					layer[i-1][j] = i==1+layer.length/2? encodeData[j]:layer[i-1][j];
					z += weight[i-1][j][k]*layer[i-1][j];
				}
				layer[i][k] = Function.sigmoid(z);
			}
		}
		for(int i=0;i<result.length;++i) 
			result[i] = layer[layer.length-1][i];
	}
	
	public void computeEncodeDataSet(double[][] inp,double[][] dataSet) {
		for(int i=0;i<inp.length;++i)
			computeEncodeData(inp[i], dataSet[i]);
	}
	public double reportModel(double[][] inp,double p) {
		int count=0;
		for(int i=0;i<inp.length;++i) {
			double[] result = computeOut(inp[i]);
			//System.out.println(Arrays.toString(inp[i])+"\t"+Arrays.toString(result));
			//System.out.println(Arrays.toString(result));
			if(compare(result, inp[i], p)) ++count;
		}
		return count/(double)inp.length;
	}
	
	//If the mean squared error (MSE) is less than p, it's correct
	private boolean compare(double[] a1,double[] a2,double p) {
		double err = 0.0;
		for(int i=0;i<a1.length;++i)
			err += Math.pow(a1[i]-a2[i],2.0);
		//err *= 0.5;
		err *= 1.0/a1.length;
		if(err < p) return true;
		else return false;
	}
	
	public double[][][] getWeight(){
		return weight;
	}
	
	public int getEncodedSize() {
		return this.encodedSize;
	}
	
	public int getFeatureSize() {
		return this.featureSize;
	}
	
	public double[] getDecoderError(){
		return error[layer.length/2];
	}
}
