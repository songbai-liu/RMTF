package etmo.problems.base.realWorld;

public class LossFunction {
	
	public static double mse(double[] output, double[] label) {//Mean Squared Error
		double loss = 0;
		int size = output.length;
		for(int i=0; i<size; i++) {
			loss += (label[i] - output[i])*(label[i] - output[i]);
		}
		loss = loss/size;
		return loss;
	}
	
	public static double mae(double[] output, double[] label) {//Mean Absolute Error
		double loss = 0;
		int size = output.length;
		for(int i=0; i<size; i++) {
			loss += Math.abs(label[i] - output[i]);
		}
		loss = loss/size;
		return loss;
	}
	
	public static double rmse(double[] output, double[] label) {//Root Mean Squared Error
		double loss = 0;
		int size = output.length;
		for(int i=0; i<size; i++) {
			loss += (label[i] - output[i])*(label[i] - output[i]);
		}
		loss = Math.sqrt(loss/size);
		return loss;
	}
}
