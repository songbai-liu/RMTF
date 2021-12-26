package etmo.problems.base.realWorld;

import Jama.Matrix;
import etmo.core.Problem;
import etmo.core.Solution;
import etmo.util.JMException;

public class PortfolioOpt extends Problem {
	
	double[][] Yield_;
	double[][] Risk_;
	int numberSamples_;
	
	public PortfolioOpt(Matrix Yield, Matrix Risk) {
		
		Yield_ = Yield.getArray();
		Risk_ = Risk.getArray();
		numberOfObjectives_ = 2;
		numberOfVariables_ = Risk.getColumnDimension();
		numberSamples_ = Yield.getColumnDimension();
		
		upperLimit_ = new double[numberOfVariables_];
		lowerLimit_ = new double[numberOfVariables_];

		for (int var = 0; var < numberOfVariables_; var++) {
			lowerLimit_[var] = -1.0;
			upperLimit_[var] = 1.0;
		} // for
	}

	public void evaluate(Solution solution) throws JMException {
		
		double vars[] = scaleVariables(solution);
		//Mandatory Constraint: the sum of the absolute values of all variables is 1
		double sum = 0.0;
		for(int i=0;i<numberOfVariables_;i++) {
			sum += Math.abs(vars[i]);
		}
		if(sum < 1.0) {
			sum = 1.0;
		}
		for(int i=0;i<numberOfVariables_;i++) {
			vars[i] = vars[i]/sum;
		}

		double[] f = new double[numberOfObjectives_];
		f[0] = f[1] = 0.0;
		
		double[] risk = new double[numberOfVariables_];
		for(int i=0;i<numberOfVariables_;i++) {
			risk[i] = 0.0;
			for(int j=0;j<numberOfVariables_;j++) {
				risk[i] += vars[j]*Risk_[j][i];
			}
		}
		for(int i=0;i<numberOfVariables_;i++) {
			f[0] += risk[i]*vars[i];
		}
		
		double[] yield = new double[numberSamples_];
		for(int i=0;i<numberSamples_;i++) {
			for(int j=0;j<numberOfVariables_;j++) {
				yield[i] += vars[j]*Yield_[j][i];
			}
			f[1] += yield[i];
		}
		f[1] = 1.0 - f[1];
		
		solution.setObjective(startObjPos_ + 0, f[0]); //Minimize the investment risk
		solution.setObjective(startObjPos_ + 1, f[1]); //Maximize the investment yield
	}

	public void dynamicEvaluate(Solution solution, int currentGeneration) throws JMException {
		
	}

}
