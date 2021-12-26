package etmo.problems.base.realWorld;

import etmo.core.Problem;
import etmo.core.Solution;
import etmo.util.JMException;

public class Training_DNN extends Problem {
	
	String lossType_;
	String regularizationType_;
	String activationType_;
	int nHidden1_; //Size of first hidden layer
	int nHidden2_; //Size of second hidden layer
	int featureSize_;
	int sampleSize_;
	double[][] trainIn_;//Input of training set
	double[] trainLabel_;//Labels of training set
	//double[][] testIn_;//Input of test set
	//double[] testLabel_;//Labels of test set

	public Training_DNN(double[][] input, double[] label, int nHidden1, int nHidden2) {
		
		lossType_ = "MSE";
		regularizationType_ = "L1";
		activationType_ = "Sigmoid";
		
		trainIn_ = input;
		trainLabel_ = label;
		
		numberOfObjectives_ = 2;
		nHidden1_ = nHidden1;
		nHidden2_ = nHidden2;
		
		featureSize_ = trainIn_[0].length;
		sampleSize_ = trainIn_.length;
		
		numberOfVariables_ =  (featureSize_+1)*nHidden1_ + (nHidden1_+1)*nHidden2_ + (nHidden2_+1)*1;
	
		upperLimit_ = new double[numberOfVariables_];
		lowerLimit_ = new double[numberOfVariables_];

		for (int var = 0; var < numberOfVariables_; var++) {
			lowerLimit_[var] = -1.0;
			upperLimit_[var] = 1.0;
		} // for
		
	}
	
	public void evaluate(Solution solution) throws JMException {
		double vars[] = scaleVariables(solution);
		
		double[] f = new double[numberOfObjectives_];
		f[0] = f[1] = 0.0;
		
		double[][] W1 = new double[featureSize_+1][nHidden1_];
		double[][] W2 = new double[nHidden1_+1][nHidden2_];
		double[] W3 = new double[nHidden2_+1];
		int t = 0;
		for(int i=0; i<featureSize_+1; i++) {
			for(int j=0; j<nHidden1_; j++) {
				W1[i][j] = vars[t];
				t++;
			}
		}
		for(int i=0; i<nHidden1_+1; i++) {
			for(int j=0; j<nHidden2_; j++) {
				W2[i][j] = vars[t];
				t++;
			}
		}
		
		for(int i=0; i<nHidden2_+1; i++) {
			W3[i] = vars[t];
			t++;
		}
		
		double[][] Y1 = new double [sampleSize_][nHidden1_];
		for(int i=0; i<sampleSize_; i++) {
			Y1[i] = computingOutputHidden(trainIn_[i], W1, nHidden1_);
		}
		
		double[][] Y2 = new double [sampleSize_][nHidden2_];
		for(int i=0; i<sampleSize_; i++) {
			Y2[i] = computingOutputHidden(Y1[i], W2, nHidden2_);
		}
		
		
		double[] Z = new double [sampleSize_];
		for(int i=0; i<sampleSize_; i++) {
			Z[i] = computingOutput(Y2[i], W3);
		}
		
		f[0] = computingComplexity(vars);//controlling the effective complexity of the neural network by including a regularization term
		f[1] = computingMeanError(Z); //Mean error in neural network training
		
		solution.setObjective(startObjPos_ + 0, f[0]); //Weight decay: Laplace regularizer
		solution.setObjective(startObjPos_ + 1, f[1]); //Training loss: Mean squared error (MSE)
	}
	
	public double[] computingOutputHidden(double[] input, double[][] W, int nHidden) {
		double[] hiddenOuts = new double[nHidden];
		for(int i=0; i<nHidden; i++) {
			hiddenOuts[i] = 1.0*W[0][i];
			for(int d=0; d<input.length; d++) {
				hiddenOuts[i] += input[d]*W[d+1][i]; 
			}
			
			if(activationType_.equalsIgnoreCase("Sigmoid")) {
				hiddenOuts[i] = ActivationFunction.sigmoid(hiddenOuts[i]);
			}else if(activationType_.equalsIgnoreCase("Tanh")) {
				hiddenOuts[i] = ActivationFunction.tanh(hiddenOuts[i]);
			}else if(activationType_.equalsIgnoreCase("Gaussian")) {
				hiddenOuts[i] = ActivationFunction.gaussian(hiddenOuts[i]);
			}else if(activationType_.equalsIgnoreCase("SoftSign")) {
				hiddenOuts[i] = ActivationFunction.softSign(hiddenOuts[i]);
			}else if(activationType_.equalsIgnoreCase("DecayingSineUnit")) {
				hiddenOuts[i] = ActivationFunction.decayingSineUnit(hiddenOuts[i]);
			}else {
				System.out.println("Error: activation function type " + activationType_ + " is not considered in our Test");
				System.exit(0);
			}
		}
		
		return hiddenOuts;
	}
	
	public double computingOutput(double[] hiddenOut, double[] W) {
		int dim = hiddenOut.length;
		double outPuts = 0.0;
		for(int i=0; i<dim; i++) {
			outPuts += hiddenOut[i]*W[i];
		}
		outPuts += 1.0*W[dim];
		outPuts = ActivationFunction.sigmoid(outPuts);
		return outPuts;
	}
	
	public double computingComplexity(double[] vars) {
		double complexity = 0.0;
		
		if(regularizationType_.equalsIgnoreCase("L1")) {
			for (int i = 0; i < numberOfVariables_; i++)//Weight decay in neural network training: Sum of the absolute weights
				complexity += Math.abs(vars[i])/numberOfVariables_; 
		}else if(regularizationType_.equalsIgnoreCase("L2")) {
			for (int i = 0; i < numberOfVariables_; i++)//consists of the squared sum of all the parameters in the neural network
				complexity += vars[i]*vars[i]/numberOfVariables_; 
			complexity = Math.sqrt(complexity);
		}else {
			System.out.println("Error: regularization type " + regularizationType_ + " is not considered in our Test");
			System.exit(0);
		}
		return complexity;
	}
	
	public double computingMeanError(double[] output) {//Cost: Based on Mean squared error (MSE)
		
		double cost = 0.0;
		if(lossType_.equalsIgnoreCase("MSE")) {
			cost = LossFunction.mse(output, trainLabel_);
		}else if(lossType_.equalsIgnoreCase("MAE")) {
			cost = LossFunction.mae(output, trainLabel_);
		}else if(lossType_.equalsIgnoreCase("RMSE")) {
			cost = LossFunction.rmse(output, trainLabel_);
		}else {
			System.out.println("Error: loss function type " + lossType_ + " is not considered in our Test");
			System.exit(0);
		}
		
		return cost;
	}
	
	public void setLossType(String lossType) {
		lossType_ = lossType;
	}
	
	public void setRegularizationType(String regularizationType) {
		regularizationType_ = regularizationType;
	}
	
	public void setActivationType(String activationType) {
		activationType_ = activationType;
	}

	public void dynamicEvaluate(Solution solution, int currentGeneration) throws JMException {
		
	}

}
