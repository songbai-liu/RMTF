package etmo.metaheuristics.emtet;

import Jama.Matrix;
import etmo.core.ProblemSet;
import etmo.core.Solution;
import etmo.core.SolutionSet;
import etmo.util.JMException;
import etmo.util.wrapper.XReal;

public class mDA {
	SolutionSet curr_pop;
	SolutionSet his_pop;
	int popSize;
	int dim, dim1, dim2;
	Matrix TM;//transfer matrix
	int sf;//Skill factor
	/*
	 * curr_pop and his_pop denote the current population and population from another domain.
	 * Both in the form of n*d matrix.
	 * n is the number of individual, and d is the variable dimension.
	 * They do not have to be with the same d. We assume they have the same n (same population size)
	 * his_bestSolution is the best solutions from one domain.
	*/
	public mDA(SolutionSet curr, SolutionSet his) {
		curr_pop = curr;
		his_pop = his;
		if(curr.size() != his.size()) {
			System.out.println("The number of solutions are different betwee P and Q");
			System.exit(0);
		}
		popSize = curr.size();
		dim1 = curr.get(0).numberOfVariables();
		dim2 = his.get(0).numberOfVariables();
		dim = Math.max(dim1, dim2);
		sf = curr.get(0).getSkillFactor();
	}
	
	public void getTansferMatrix() throws JMException {
		//Step1: Extract Solutions Sets and pad them
		double[][] xx = new double[popSize][dim+1];
		double[][] noise = new double[popSize][dim+1];
		for(int i=0;i<popSize;i++) {
			XReal cur = new XReal(curr_pop.get(i));
			XReal his = new XReal(his_pop.get(i));
			for(int j=0;j<dim;j++) {
				if(j < dim1) {
					xx[i][j] = cur.getValue(j);
				}else {
					xx[i][j] = 0.0;
				}
				if(j < dim2) {
					noise[i][j] = his.getValue(j);
				}else {
					noise[i][j] = 0.0;
				}
			}
			//add the augment
			xx[i][dim] = 1.0;
			noise[i][dim] = 1.0;
		}
	
		//Step2: Get the Matrix of the input and output and their transpose
		Matrix xxb = new Matrix(xx); //popSize*dim+1
		Matrix noiseb = new Matrix(noise);//popSize*dim+1
		Matrix xxbT = xxb.transpose();//dim+1*popSize
		Matrix noisebT = noiseb.transpose();//dim+1*popSize 
		Matrix Q = noisebT.times(noiseb);//dim+1*dim+1
		Matrix P = xxbT.times(noiseb);//dim+1*dim+1
		//Step3: get the transformation matrix
		double lambda = 0.00001;
		double[][] reg = new double[dim+1][dim+1];
		for(int i=0;i<dim+1;i++) {
			for(int j=0;j<dim+1;j++) {
				if(i == j && i != dim) {
					reg[i][j] = lambda;
				}else {
					reg[i][j] = 0.0;
				}
			}
		}
		Matrix regM = new Matrix(reg);
		Q = Q.plus(regM);
		Matrix M = P.times((Q).inverse());//dim+1*dim+1
		
		double[][] W = new double[dim][dim];
		for(int i=0;i<dim;i++) {
			for(int j=0;j<dim;j++) {
				W[i][j] = M.get(i, j);
			}
		}
		TM = new Matrix(W);//dim*dim
	}
	
	public SolutionSet transferOperator(SolutionSet hiskonwledge, ProblemSet pSet) throws JMException, ClassNotFoundException{
		int tranSize = hiskonwledge.size();
		SolutionSet transferredSet = new SolutionSet(tranSize);
		double[][] transfer = new double[tranSize][dim];
		for(int i=0;i<tranSize;i++) {
			XReal tran = new XReal(hiskonwledge.get(i));
			for(int j=0;j<dim;j++) {
				if(j < dim2) {
					transfer[i][j] = tran.getValue(j);
				}else {
					transfer[i][j] = 0.0;
				}
			}
		}
		Matrix trans = new Matrix(transfer);//tranSize*dim
		Matrix newSet = TM.times(trans.transpose());//dim*tranSize
		newSet = newSet.transpose();//tranSize*dim
		for(int i=0;i<tranSize;i++) {
			Solution newSolution = new Solution(pSet);
			XReal child = new XReal(newSolution);
			double value;
			for(int var=0;var<dim1;var++) {
				value = newSet.get(i, var);
				if(value > 1) value = 1;
				if(value < 0) value = 0;
				child.setValue(var, value); 
			}
			pSet.get(sf).evaluate(newSolution);
			pSet.get(sf).evaluateConstraints(newSolution);
			newSolution.setSkillFactor(sf);
			transferredSet.add(newSolution);
		}
		return transferredSet;
	}
}
