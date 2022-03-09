package etmo.metaheuristics.drnea.BaseModel;

import java.util.ArrayList;

public class DeepAutoEncode {
	private ArrayList<BPEncode> stackDEA = new ArrayList<BPEncode>();
	private int modelSize;
	public DeepAutoEncode(int[] size, double r) {
		int n = size.length;
		System.out.println(n);
		for(int i=0;i<n-1;++i) {
			BPEncode bpEncode = new BPEncode(size[i], size[i+1], 3, r);
			stackDEA.add(bpEncode);
			System.out.println("\tThis is the "+(i+1)+"-th submodel.");
		}
		modelSize = stackDEA.size();
	}
	
	/*
	 * Training this auto-encoder model by input a set of samples: input
	*/
	public void trainModel(double[][] inp) {
		double[][] in = inp;
        int numModels =  stackDEA.size();
		for(int i=0;i<numModels;++i) {
			BPEncode currentModel = stackDEA.get(i);
			currentModel.trainModel(in);
			if(i != (numModels-1)) {
				int features_next = currentModel.getEncodedSize();
				double[][] tmp = new double[in.length][features_next];
				currentModel.computeEncodeDataSet(in, tmp);
				in = tmp;
			}
			
		}
	}
	
	public double[][] encoding(double[] inp){
		double[] in = inp;
		int numModels =  stackDEA.size();
		double[][] result = new double[numModels][];
		for(int i=0;i<numModels;++i) {
			BPEncode currentModel = stackDEA.get(i);
			result[i] = new double[currentModel.getEncodedSize()];
			currentModel.computeEncodeData(in,result[i]);
			if(i != (numModels-1)) {
				in  = result[i];
			}
		}
		return result;
	}
	
	public double[] decoding(double[] code, int id) {
		if(id > stackDEA.size()) {
			System.out.println("The input parameter id = " + id + " is beyond the size of the model");
			System.exit(0);
		}
		if(code.length != stackDEA.get(id).getEncodedSize()) {
			System.out.println("The length of the input code is not consistent with the data dimensions encoded by the model");
			System.exit(0);
		}
		double[] currentCode = code;
		double[] decode=null;
		for(int i=id; i>=0; i--) {
			BPEncode currentModel = stackDEA.get(i);
			decode = new double[currentModel.getFeatureSize()];
			currentModel.computeDecodeData(currentCode, decode);
			currentCode = decode;
		}
		return decode;
	}
	
	public int getModelSize() {
		return this.modelSize;
	}
}
