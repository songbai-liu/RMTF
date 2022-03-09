package etmo.metaheuristics.drnea;

import etmo.core.Solution;
import etmo.core.SolutionSet;
import etmo.metaheuristics.drnea.BaseModel.DeepAutoEncode;
import etmo.util.JMException;
import etmo.util.PseudoRandom;
import etmo.util.wrapper.XReal;

public class StackAutoEncoder {
	int featureSize;
	double learningRate;
	DeepAutoEncode DAE;
	
	public StackAutoEncoder(int[] size, double r) {
		learningRate = r;
		DAE = new DeepAutoEncode(size, r);
		featureSize = size[0];
	}
	
	public void getTrainingModel(SolutionSet trainingSet, int epochs) throws JMException {
		double probability = 0.1;
		double distributionIndex_ = 20.0;
		double eta_m_ = 20.0;
		double rnd, delta1, delta2, mut_pow, deltaq;
		double y, yl, yu, val, xy;
		yl = 0.0;
		yu = 1.0;
		int size = trainingSet.size();
		double[][] inputs = new double[size][featureSize];
		for(int iter=0;iter<epochs;iter++) {
			//in each epoch, the input data is updated via the polynomial mutation (PM) with some probability
			for(int i=0;i<size;i++) {
				XReal sol = new XReal(trainingSet.get(i));
				for(int j=0;j<featureSize;j++) {
					y = sol.getValue(j);
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
					inputs[i][j] = y;
				}
			}
			DAE.trainModel(inputs);
		}
		
	}
	
	public double[][] encoding(Solution sol) throws JMException {
		XReal xsol = new XReal(sol);
		double[] input = new double[featureSize];
		for(int i=0;i<featureSize;i++) { 
			input[i] = xsol.getValue(i); 
		}
		return DAE.encoding(input);
	}
	
	public double[] decoding(double[] code, int id) throws JMException {
		return DAE.decoding(code, id);
	}
}
