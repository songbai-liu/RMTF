// LMOEA/DAA.java

package etmo.metaheuristics.drnea;

import etmo.core.*;
import etmo.util.Distance;
import etmo.util.JMException;
import etmo.util.PseudoRandom;
import etmo.util.Ranking;
import etmo.util.comparators.CrowdingComparator;
import etmo.util.wrapper.XReal;

/**
 * Implementation of LMEA/DAA
 */

public class LMEA_DAA extends Algorithm {
	int populationSize;
	int maxEvaluations;
	int evaluations;
	SolutionSet[] population;
	SolutionSet[] offspringPopulation;
	SolutionSet[] union;
	
	Operator mutationOperator;
	Operator crossoverOperator;
	Operator selectionOperator;

	Distance distance = new Distance();
	
	int noTasks;
	int[] numVars;
	int[] numObjs;
	
	DenoiseAutoEncoder[] DAE;
	int epochs;
	int numCodes;
	double learningRate;
	Discriminator DIS;
	
	double[][] upBounds;
	double[][] lowBounds;
	
	double[] productivity;
	
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public LMEA_DAA(ProblemSet problemSet) {
		super(problemSet);
	//	System.out.println("sup: " + problemSet_.get(0).getHType());
	} // MLMOEA

	public void initialization() throws JMException, ClassNotFoundException {
		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		// Initialize the variables
		noTasks = problemSet_.size();
		population = new SolutionSet[noTasks];
		offspringPopulation = new SolutionSet[noTasks];
		union = new SolutionSet[noTasks];
		productivity = new double[noTasks];
		for(int t=0;t<noTasks;t++) {
			productivity[t] = 0.8;
			population[t] = new SolutionSet(populationSize/noTasks);
			// Create the initial solutionSet
			Solution newSolution;
			for (int j = 0; j < populationSize/noTasks; j++) {
				newSolution = new Solution(problemSet_);
				problemSet_.get(t).evaluate(newSolution);
				problemSet_.get(t).evaluateConstraints(newSolution);
				newSolution.setSkillFactor(t);
				population[t].add(newSolution);
			} // for	
			//offspringPopulation[t] = new SolutionSet(populationSize/noTasks);
			offspringPopulation[t] = new SolutionSet();
			//union[t] = new SolutionSet(2*populationSize/noTasks);
			union[t] = new SolutionSet();
		}
		evaluations = 0;
		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");
		
		numVars = new int[noTasks];
		numObjs = new int[noTasks];
		
		upBounds = new double[noTasks][];
		lowBounds = new double[noTasks][];

		for(int t=0;t<noTasks;t++) {
			numVars[t] = problemSet_.get(t).getNumberOfVariables();
			numObjs[t] = problemSet_.get(t).getNumberOfObjectives();
			upBounds[t] = new double[numVars[t]];
			lowBounds[t] = new double[numVars[t]];
			for(int var=0;var<numVars[t];var++) {
				upBounds[t][var] = problemSet_.getUnifiedUpperLimit();
				lowBounds[t][var] = problemSet_.getUnifiedLowerLimit();
			}
		}
		//Construct the Auto-Encoders based on neural networks for each task
		DAE = new DenoiseAutoEncoder[noTasks];
		numCodes = 10;
		learningRate = 0.1;
		for(int t=0;t<noTasks;t++) {
			DAE[t] = new DenoiseAutoEncoder(numVars[t], numCodes, 4, learningRate);
		}
		
		int numLayers = 3;
		int[] layerNum = new int[numLayers];
		layerNum[0] = numCodes;
		layerNum[numLayers-1] = noTasks;
		int divisor = (numCodes-noTasks)/(numLayers-1);
		if(divisor < 1){
			System.out.println("There are too few coding or too many layers, please replan the network!");
			System.exit(0);
		}
		for(int l=1;l<numLayers-1;l++){
			layerNum[l] = layerNum[l-1] - divisor;
		}
		DIS = new Discriminator(layerNum, learningRate);
		
		epochs = 100;
	}
	
	/**
	 * Runs the MLMOEA algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws JMException
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException {		
		initialization();
		// Generations
		while (evaluations < maxEvaluations) {

			for(int t=0;t<noTasks;t++) {
				Ranking ranking1 = new Ranking(population[t]);
				//incrementally training the Auto-Encoders based on neural networks for each task
				DAE[t].getTrainingModel(ranking1.getSubfront(0), epochs);
				double[][] encodes = DAE[t].encode(ranking1.getSubfront(0),numVars[t]);
				double[][] targets = new double[encodes.length][noTasks];
				for(int i=0; i<encodes.length; i++){
					for(int tt=0; tt<noTasks; tt++){
						if(tt == t) targets[i][tt] = 1.0;
						else targets[i][tt] = 0.0;
					}
				}
				DIS.getTrainingModel(encodes, targets);
			}

			reproduction();
			environmetalSelection();
			
		} // while
		SolutionSet U = population[0];
		for(int i=1;i<noTasks;i++) {
			U = ((SolutionSet) U).union(population[i]);
		}
		return U; 
	} // execute
	
	public void reproduction() throws JMException, ClassNotFoundException {
		for(int t=0;t<noTasks;t++) {
			// Create the offSpring solutionSet
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize/noTasks/2); i++) {
				// obtain parents
				parents[0] = (Solution) selectionOperator.execute(population[t]);
				parents[1] = (Solution) selectionOperator.execute(population[t]);
				if(evaluations < 0.5*maxEvaluations) {
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					problemSet_.get(t).evaluate(offSpring[0]);
					problemSet_.get(t).evaluateConstraints(offSpring[0]);
					problemSet_.get(t).evaluate(offSpring[1]);
					problemSet_.get(t).evaluateConstraints(offSpring[1]);
					offSpring[0].setGenerateType(1);
					offSpring[1].setGenerateType(1);
					offspringPopulation[t].add(offSpring[0]);
					offspringPopulation[t].add(offSpring[1]);
				}else{
					double rd;
					int target;
					double[][] encoder = new double[2][];
					//Encode via the related DAE models
					encoder = encodeWithDAE(parents,t);
					//Searching in the transfered subspace
					double[][] newEncoder = doCrossover(0.9, encoder);
					doMutation(newEncoder);
					for(int c=0;c<2;c++){
						rd = PseudoRandom.randDouble();
						if(rd < 0.9){
							double[] tar = DIS.computeTarget(newEncoder[c]);
							int pT = 0;
							double maxV = tar[0];
							for(int tt=1;tt<noTasks;tt++){
								if(maxV < tar[tt]){
									maxV = tar[tt];
									pT = tt;
								}
							}
							target = pT;
						}else{
							target = t;
						}
						//Decode via the related DAE models
						Solution offSpring = decodeWithDAE(newEncoder[c],target);
						//Evaluation
						problemSet_.get(target).evaluate(offSpring);
						problemSet_.get(target).evaluateConstraints(offSpring);
						offspringPopulation[target].add(offSpring);
					}
				}
				evaluations += 2;
			} // for			
		}	
	}
	
	public double[][] encodeWithDAE(Solution[] parents,int task) throws JMException {
		double[][] encodedParents = new double[parents.length][];
		for(int i=0;i<parents.length;i++) {
			XReal xsol = new XReal(parents[i]);
			encodedParents[i] = DAE[task].encode(parents[i],numVars[task]);
		}
		return encodedParents;
	}
	
	public double[] encodeWithDAE(Solution parent,int task) throws JMException {
		double[] encodedParents;
		XReal xsol = new XReal(parent);
		
		encodedParents = DAE[task].encode(parent, numVars[task]);
		
		return encodedParents;
	}
	
	public Solution decodeWithDAE(double[] encoder, int task) throws JMException, ClassNotFoundException {
		Solution offspring = new Solution(problemSet_);;
		XReal child = new XReal(offspring);
		double[] newDecode = DAE[task].decode(encoder);
		double value;
		for(int var=0;var<numVars[task];var++) {
			value = newDecode[var]*(upBounds[task][var] - lowBounds[task][var]) + lowBounds[task][var];
			if(value < lowBounds[task][var]) {
				value = lowBounds[task][var];
			}
			if(value > upBounds[task][var]) {
				value = upBounds[task][var];
			}
			child.setValue(var, value);
		}
		offspring.setSkillFactor(task);
		return offspring;
	}
	
	public Solution[] decodeWithDAE(double[][] encoder, int task) throws JMException, ClassNotFoundException {
		Solution[] offspring = new Solution[2];
		XReal[] child = new XReal[2];
		for(int i=0;i<2;i++) {
			offspring[i] = new Solution(problemSet_);
			offspring[i].setSkillFactor(task);
			child[i] = new XReal(offspring[i]);
			double[] newDecode = DAE[task].decode(encoder[i]);
			double value;
			for(int var=0;var<numVars[task];var++) {
				value = newDecode[var]*(upBounds[task][var] - lowBounds[task][var]) + lowBounds[task][var];
				if(value < lowBounds[task][var]) {
					value = lowBounds[task][var];
				}
				if(value > upBounds[task][var]) {
					value = upBounds[task][var];
				}
				child[i].setValue(var, value);
			}
		}
		return offspring;
	}

	public void environmetalSelection() {
		for(int t=0;t<noTasks;t++) {
			// Create the solutionSet union of solutionSet and offSpring
			union[t] = ((SolutionSet) population[t]).union(offspringPopulation[t]);
			population[t].clear();
			offspringPopulation[t].clear();
			// Ranking the union
			Ranking ranking = new Ranking(union[t]);
			int remain = populationSize/noTasks;
			int index = 0;
			SolutionSet front = null;
			// Obtain the next front
			front = ranking.getSubfront(index);
			while ((remain > 0) && (remain >= front.size())) {
				// Assign crowding distance to individuals
				distance.crowdingDistanceAssignment(front, problemSet_.get(t).getNumberOfObjectives());
				// Add the individuals of this front
				for (int k = 0; k < front.size(); k++) {
					population[t].add(front.get(k));
				} // for

				// Decrement remain
				remain = remain - front.size();

				// Obtain the next front
				index++;
				if (remain > 0) {
					front = ranking.getSubfront(index);
				} // if
			} // while
			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) { // front contains individuals to insert
				distance.crowdingDistanceAssignment(front, problemSet_.get(t).getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (int k = 0; k < remain; k++) {
					population[t].add(front.get(k));
				} // for
				remain = 0;
			} // if
		}	
	}//environmetalSelection	
	
	public double[][] doCrossover(double probability, double[][] encodedParents) throws JMException {

		double[][] offs = new double[2][];
		/**
		 * EPS defines the minimum difference allowed between real values
		 */
		double EPS = 1.0e-14;
		int i;
		double rand;
		double y1, y2, yL, yu;
		double c1, c2;
		double alpha, beta, betaq;
		double valueX1, valueX2;
		double[] x1 = encodedParents[0];
		double[] x2 = encodedParents[1];
		int dim = encodedParents[0].length;
		double distributionIndex_ = 20.0;
		offs[0] = new double[dim];
		offs[1] = new double[dim];

		if (PseudoRandom.randDouble() <= probability) {
			for (i = 0; i < dim; i++) {
				valueX1 = x1[i];
				valueX2 = x2[i];
				if (PseudoRandom.randDouble() <= 0.5) {
					if (java.lang.Math.abs(valueX1 - valueX2) > EPS) {

						if (valueX1 < valueX2) {
							y1 = valueX1;
							y2 = valueX2;
						} else {
							y1 = valueX2;
							y2 = valueX1;
						} // if

						yL = 0.0;
						yu = 1.0;
						rand = PseudoRandom.randDouble();
						beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));					
						
						alpha = 2.0 - java.lang.Math.pow(beta, -(distributionIndex_ + 1.0));

						if (rand <= (1.0 / alpha)) {
							betaq = java.lang.Math.pow((rand * alpha), (1.0 / (distributionIndex_ + 1.0)));
						} else {
							betaq = java.lang.Math.pow((1.0 / (2.0 - rand * alpha)),
									(1.0 / (distributionIndex_ + 1.0)));
						} // if
						if(Double.isNaN(betaq))
							System.out.println(java.lang.Math.pow(0.1,1.0/2.0));
						c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));

						beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
						alpha = 2.0 - java.lang.Math.pow(beta, -(distributionIndex_ + 1.0));

						if (rand <= (1.0 / alpha)) {
							betaq = java.lang.Math.pow((rand * alpha), (1.0 / (distributionIndex_ + 1.0)));
						} else {
							betaq = java.lang.Math.pow((1.0 / (2.0 - rand * alpha)),
									(1.0 / (distributionIndex_ + 1.0)));
						} // if

						c2 = 0.5 * ((y1 + y2) + betaq * (y2 - y1));

						if(Double.isNaN(c2))
							System.out.println(c2);
						if (c1 < yL)
							c1 = yL;

						if (c2 < yL)
							c2 = yL;

						if (c1 > yu)
							c1 = yu;

						if (c2 > yu)
							c2 = yu;

						if (PseudoRandom.randDouble() <= 0.5) {
							offs[0][i] = c2;
							offs[1][i] = c1;
						} else {
							offs[0][i] = c1;
							offs[1][i] = c2;
						} // if
					} else {
						offs[0][i] = valueX1;
						offs[1][i] = valueX2;
					} // if
				} else {
					offs[0][i] = valueX2;
					offs[1][i] = valueX1;
				} // if
			} // if
		} // if

		return offs;
	} // doCrossover
	
	public void doMutation(double[][] newEncoder) throws JMException { 
		for(int i=0;i<newEncoder.length;i++) {
			double probability = 1.0/newEncoder[i].length;
			if(newEncoder[i].length == 1) {
				probability = 0.2;
			}
			double rnd, delta1, delta2, mut_pow, deltaq;
			double y, yl, yu, val, xy;
			double[] x = newEncoder[i];
			for (int var = 0; var < x.length; var++) {
				if (PseudoRandom.randDouble() <= probability) {
					y = x[var];
					yl = 0.0;
					yu = 1.0;
					delta1 = (y - yl) / (yu - yl);
					delta2 = (yu - y) / (yu - yl);
					rnd = PseudoRandom.randDouble();
					mut_pow = 1.0 / (20.0 + 1.0);
					if (rnd <= 0.5) {
						xy = 1.0 - delta1;
						val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, (20.0 + 1.0)));
						deltaq = java.lang.Math.pow(val, mut_pow) - 1.0;
					} else {
						xy = 1.0 - delta2;
						val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (java.lang.Math.pow(xy, (20.0 + 1.0)));
						deltaq = 1.0 - (java.lang.Math.pow(val, mut_pow));
					}
					y = y + deltaq * (yu - yl);
					if (y < yl)
						y = yl;
					if (y > yu)
						y = yu;
					x[var] = y;
				}
			} // for
		}
	} // doMutation
} //MLMOEA
