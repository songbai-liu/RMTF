package etmo.metaheuristics.drnea;

import etmo.metaheuristics.drnea.BaseModel.BPClassifier;
import etmo.util.JMException;

public class Discriminator {
	
	int[] layerNum;
	double learningRate;
	BPClassifier classifier;
	
	public Discriminator(int[] layerNum, double rate){
		this.layerNum = layerNum;
		this.learningRate = rate;
		classifier = new BPClassifier(layerNum, rate);
	}
	
	public void getTrainingModel(double[][] input, double[][] target) throws JMException {
		classifier.trainModel(input, target);
	}
	
	public void trainingModel(double[] input, double[] target) throws JMException {
		classifier.trainModel(input, target);
	}
	
	public double[] computeTarget(double[] inp){
		return classifier.computeOut(inp);
	}
}
