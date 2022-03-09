package etmo.metaheuristics.drnea.BaseModel;

import java.util.ArrayList;

/*
 * Basic Implementation based on the BP Neural Network:
 * Activation Function: Sigmoid
 * Loss Function: Square Variance
 * This program in the presence of three iteration variables:
 * i indicates the number of layers;
 * j represents a neuron in a certain layer 
 * k is a neuron in the next layer of that j belong to 
*/ 
public class BPEncodeMulti implements IbpBase{
	private double[][] layer;//Record the output of each neuron at each layer
	private double[][][] weight;//Record all weights of the entire neural network，[n][a][b]，n < The total number of layer-1
	private double[][] error;//errors
	private double rate;//learning rate
	
	
	public BPEncodeMulti(int features,int feaAfterEncode, double r) {
		int layerNum = 3;
		int[] numberOfLayer = new int[layerNum];
		numberOfLayer[0] = numberOfLayer[layerNum-1] = features;
		numberOfLayer[layerNum/2] = feaAfterEncode;
		init(numberOfLayer, r);
	}
	
	public BPEncodeMulti(ArrayList<BPEncodeMulti> models, double r) {
		int[] numberOfLayer = new int[models.size()*2 + 1];
		numberOfLayer[models.size()] = models.get(models.size()-1).getMinNodeNum();
		int n = numberOfLayer.length;//the number of layers of the auto-encoder
		rate = r;
		layer = new double[n][];
		weight = new double[n-1][][];
		error = new double[n][];
		for(int i=0;i<models.size();++i) {
			numberOfLayer[i] = numberOfLayer[n-1-i] = models.get(i).getMaxNodeNum();
			weight[i] = models.get(i).getZoomOutWeight();
			weight[weight.length-1-i] = models.get(i).getZoomInWeight();
		}
		for(int i=0;i<n;++i) {
			layer[i] = new double[numberOfLayer[i]];
			error[i] = new double[numberOfLayer[i]];
		}
	}
	
	public double[][] getZoomInWeight() {
		return weight[1];
	}
	
	public double[][] getZoomOutWeight() {
		return weight[0];
	}
	
	public int getMinNodeNum() {
		return layer[1].length;
	}
	
	public int getMaxNodeNum() {
		return layer[0].length;
	}
	
	/*
	 * Randomly initializes the weights and bias
	 * The bias is recored in the last column of the weight matrix
	*/
	public void init(int[] numberOfLayer,double r) {
		rate = r;
		int n = numberOfLayer.length;////the number of layers of the auto-encoder, here is fixed to 3
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
	 * Adopted Activation Function：sigmoid, s(x) = 1/(1+exp(-x))
	 * assume z is the weighted sum of each neuron
	 * the output of each neuron is s(z)
	*/
	public double[] computeOut(double[] inp) {
		for(int i=1;i<layer.length;++i) {//start from the first hidden layer, i.e., i = 1
			for(int k=0;k<layer[i].length;++k) {
				double z=weight[i-1][layer[i-1].length][k];//z is initialized as the bias
				for(int j=0;j<layer[i-1].length;++j) {
					layer[i-1][j] = i==1? inp[j]:layer[i-1][j];//initialization of the input layer
					z += weight[i-1][j][k]*layer[i-1][j]; //get the weighted sum
				}
				layer[i][k] = Function.sigmoid(z);
			}
		}
		return layer[layer.length-1];
	}

	public double[][] computeOut(double[][] inp) {
		double[][] result = new double[inp.length][];
		for(int i=0;i<inp.length;++i) 
			result[i] = computeOut(inp[i]);
		return result;
	}
	
	/*
	 * The adopted loss function: Square Variance
	 * Starting from the second last layer 
	 * the error is calculated backwards 
	 * the weights and deviations are accordingly updated
	*/
	public void backPropagation(double[] target) {
		//Calculate the error of the last layer first
		int i = layer.length-1;
		for(int j=0;j<layer[i].length;++j)
			error[i][j] = layer[i][j]*(1-layer[i][j])*(target[j]-layer[i][j]);
		while(i-->0){
			//The error and the weight are calculated simultaneously
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
	
	public void trainModel(double[][] inp,double p) {
		int xn=0;
		double tmp;
		while(true) {
			for(int i=0;i<inp.length;++i) {
				computeOut(inp[i]);
				backPropagation(inp[i]);
			}
			tmp = reportModel(inp, p);
			++xn;
			if(tmp > 1.0-p) return;
			if(xn % 10000==0) p += 0.01;
		}
	}
	
	public void trainModel(double[][] inp) {
		for(int i=0;i<inp.length;++i) {
			computeOut(inp[i]);
			backPropagation(inp[i]);
		}
	}
	
	public static ArrayList<BPEncodeMulti> getTrainModel(double[][] inp,double rate,double p) {
		ArrayList<BPEncodeMulti> result = new ArrayList<BPEncodeMulti>();
		int max = inp[0].length;
		int min = (int) Math.sqrt(max);
		min += (max-min)%4;
		int n = (max-min)/4;
		System.out.println(n);
		double[][] in = inp;
		for(int i=0;i<n;++i) {
			BPEncodeMulti bpEncodeMulti = new BPEncodeMulti(max-4*i, max-4*(i+1), rate);
			bpEncodeMulti.trainModel(in, p);
			System.out.print(bpEncodeMulti.reportModel(in, p));
			double[][] tmp = new double[in.length][max-4*i-4];
			bpEncodeMulti.computeEncodeDataSet(in, tmp);
			in = tmp;
			result.add(bpEncodeMulti);
			System.out.println("\tThis is the "+(i+1)+"-th submodel.");
		}
		return result;
	}
	
	
	public static ArrayList<BPEncodeMulti> getTrainModel(double[][] inp,double rate,double p, int[] size) {
		ArrayList<BPEncodeMulti> result = new ArrayList<BPEncodeMulti>();
		int n = size.length;
		System.out.println(n);
		double[][] in = inp;
		for(int i=0;i<n-1;++i) {
			BPEncodeMulti bpEncodeMulti = new BPEncodeMulti(size[i], size[i+1], rate);
			bpEncodeMulti.trainModel(in, p);
			System.out.print(bpEncodeMulti.reportModel(in, p));
			double[][] tmp = new double[in.length][size[i+1]];
			bpEncodeMulti.computeEncodeDataSet(in, tmp);
			in = tmp;
			result.add(bpEncodeMulti);
			System.out.println("\tThis is the "+(i+1)+"-th submodel.");
		}
		return result;
	}
	
	
	public static double repModel(ArrayList<BPEncodeMulti> model,double[][] inp,double p) {
		System.out.println(model.size());
		int count=0;
		for(double[] in : inp) {
			double[] a = in;
			for(int i=0;i<model.size();++i) {
				double[] tmp = new double[model.get(i).getMinNodeNum()];
				model.get(i).computeEncodeData(a, tmp);
				a = tmp;
			}
			for(int i=model.size()-1;i>=0;--i) 
				a = model.get(i).computeDecodeData(a);
			if(compare(a, in, p)) ++count;
		}
		return count/(double)inp.length;
	}
	
	public void computeEncodeData(double[] inp,double[] result) {
		computeOut(inp);
		for(int i=0;i<result.length;++i) 
			result[i] = layer[layer.length/2][i];
	}
	
	public void computeEncodeDataSet(double[][] inp,double[][] dataSet) {
		for(int i=0;i<inp.length;++i)
			computeEncodeData(inp[i], dataSet[i]);
	}
	
	public double[] computeDecodeData(double[] inp) {
		for(int i=layer.length/2+1;i<layer.length;++i) {
			for(int k=0;k<layer[i].length;++k) {
				double z = weight[i-1][layer[i-1].length][k];//The bias
				for(int j=0;j<layer[i-1].length;++j) {
					layer[i-1][j] = i==2? inp[j]:layer[i-1][j];//initialize the first input layer
					z += weight[i-1][j][k]*layer[i-1][j];
				}
				layer[i][k] = Function.sigmoid(z);
			}
		}
		return layer[layer.length-1];
	}
	
	public double[][] computeDecodeDataSet(double[][] inp) {
		double[][] result=new double[inp.length][];
		for(int i=0;i<result.length;++i) result[i]=computeDecodeData(inp[i]);
		return result;
	}
	
	public double reportModel(double[][] inp,double p) {
		int count=0;
		for(int i=0;i<inp.length;++i) {
			double[] result=computeOut(inp[i]);
			//System.out.println(Arrays.toString(inp[i])+"\t"+Arrays.toString(result));
			//System.out.println(Arrays.toString(result));
			if(compare(result, inp[i], p)) ++count;
		}
		return count/(double)inp.length;
	}
	
	//If the squared error is less than p, it's correct
	public static boolean compare(double[] a1,double[] a2,double p) {
		double err = 0.0;
		for(int i=0;i<a1.length;++i)
			err += Math.pow(a1[i]-a2[i],2.0);
		err /= a1.length;
		if(err < p) return true;
		else return false;
	}
}
