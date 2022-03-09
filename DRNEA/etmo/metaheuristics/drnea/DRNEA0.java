// LMOEA/DAA.java

package etmo.metaheuristics.drnea;

import etmo.core.*;
import etmo.util.Distance;
import etmo.util.JMException;
import etmo.util.PseudoRandom;
import etmo.util.Ranking;
import etmo.util.archive.SPEA2DensityArchive;
import etmo.util.comparators.CrowdingComparator;
import etmo.util.comparators.DominanceComparator;
import etmo.util.comparators.ObjectiveComparator;
import etmo.util.wrapper.XReal;

/**
 * Implementation of LMEA/DAA
 */
public class DRNEA0 extends Algorithm {
	int populationSize;
	int maxEvaluations;
	int evaluations;
	SolutionSet[] population;
	SolutionSet[] offspringPopulation;
	SolutionSet[] union;
	SolutionSet[] archive;
	Operator mutationOperator;
	Operator crossoverOperator;
	Operator crossover1;
	Operator selectionOperator;

	Distance distance = new Distance();
	
	int noTasks;
	int[] numVars;
	int[] numObjs;
	
	DenoiseAutoEncoder[] DAE;
	int epochs;
	int numCodes;
	double learningRate;
	Discriminator[] DIS;
	
	double[][] upBounds;
	double[][] lowBounds;
	
	double[] productivity;
	int interval;//Interval of explicit solution transfer across tasks
	int transNo; //Number of solutions to be transferred across tasks
	DominanceComparator comparatorD;
	int iter;
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public DRNEA0(ProblemSet problemSet){
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
		archive = new SPEA2DensityArchive[noTasks];
		for(int t=0;t<noTasks;t++) {
			productivity[t] = 0.8;
			population[t] = new SolutionSet(populationSize/noTasks);
			archive[t] = new SPEA2DensityArchive(populationSize/noTasks);
			// Create the initial solutionSet
			Solution newSolution;
			for (int j = 0; j < populationSize/noTasks; j++) {
				newSolution = new Solution(problemSet_);
				problemSet_.get(t).evaluate(newSolution);
				problemSet_.get(t).evaluateConstraints(newSolution);
				newSolution.setSkillFactor(t);
				population[t].add(newSolution);
				archive[t].add(newSolution);
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
		crossover1 = operators_.get("crossover1");
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
			DAE[t] = new DenoiseAutoEncoder(problemSet_.getMaxDimension(), numCodes, 3, learningRate);
		}
		
		int numLayers = 3;
		int[] layerNum = new int[numLayers];
		layerNum[0] = numCodes;
		layerNum[numLayers-1] = noTasks;
		int divisor = (numCodes-noTasks)/(numLayers-1);
		/*
		 * if(divisor < 1){ System.out.
		 * println("There are too few coding or too many layers, please replan the network!"
		 * ); System.exit(0); }
		 */
		for(int l=1;l<numLayers-1;l++){
			layerNum[l] = layerNum[l-1] - divisor;
		}
		
		DIS = new Discriminator[noTasks];
		for(int t=0;t<noTasks;t++) {
			DIS[t] = new Discriminator(layerNum, learningRate);
		}
		
		interval = 10;
		transNo = populationSize/noTasks/20;
		comparatorD = new DominanceComparator();
		iter = 0;
	}
	
	public void getTraningModels() throws JMException {
		int features = problemSet_.getMaxDimension();
		for(int t=0;t<noTasks;t++) {
			//incrementally training the Auto-Encoders based on neural networks for each task
			Ranking ranking = new Ranking(population[t]);
			SolutionSet front = ranking.getSubfront(0);
			for(int p=0;p<front.size();p++) {
				Solution sol = front.get(p);
				XReal xSol = new XReal(sol);
				if(numVars[t] < features) {
					for(int j=numVars[t];j<features;j++) {
						xSol.setValue(j, 0);
					}
				}
				double[] inputs = new double[features];
				for(int j=0;j<features;j++) {
					inputs[j] = xSol.getValue(j);
				}
				DAE[t].baseAE.trainDecoder(inputs);
				double[] encodes = DAE[t].encode(sol,features);
				double[] targets = new double[noTasks];
				for(int tt=0; tt<noTasks; tt++){
					if(tt == t) targets[tt] = 1.0;
					else targets[tt] = 0.0;
				}
				DIS[t].trainingModel(encodes, targets);
				double[] cError = DIS[t].classifier.getError();
				double[] dError = DAE[t].baseAE.getDecoderError();
				double[] error = new double[numCodes];
				for(int c=0;c<numCodes;c++) {
					error[c] = dError[c] - cError[c];
				}
				DAE[t].baseAE.trainEncoder(error);
			}
			
			for(int ot=0;ot<noTasks;ot++) {
				if(ot != t) {
					Ranking oranking = new Ranking(population[ot]);
					SolutionSet ofront = oranking.getSubfront(0);
					for(int p=0;p<ofront.size();p++) {
						Solution sol = ofront.get(p);
						XReal xSol = new XReal(sol);
						if(numVars[ot] < features) {
							for(int j=numVars[ot];j<features;j++) {
								xSol.setValue(j, 0);
							}
						}
						double[] encodes = DAE[t].encode(sol,features);
						double[] targets = new double[noTasks];
						for(int tt=0; tt<noTasks; tt++){
							targets[tt] = 0.0;
						}
						DIS[t].trainingModel(encodes, targets);
						double[] cError = DIS[t].classifier.getError();
						double[] error = new double[numCodes];
						for(int c=0;c<numCodes;c++) {
							error[c] = -cError[c];
						}
						DAE[t].baseAE.trainEncoder(error);
					}
				}
			}
		}
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
			getTraningModels();
			reproduction();
			environmetalSelection();
			iter++;
		} // while
		SolutionSet U = population[0];
		for(int i=1;i<noTasks;i++) {
			U = ((SolutionSet) U).union(population[i]);
		}
		return U; 
	} // execute
	
	public void reproduction() throws JMException, ClassNotFoundException {
		for(int t=0;t<noTasks;t++) {
			if(iter % interval == 0) {//Explicit knowledge transfer from other domain/task
				//get the knowledge to be transfered
				SolutionSet knowledgeSet = new SolutionSet(transNo);
				for(int tr=0;tr<transNo;tr++) {
					int rd = PseudoRandom.randInt(0, noTasks-1);
					while(rd == t) {
						rd = PseudoRandom.randInt(0, noTasks-1);
					}
					int r1 = PseudoRandom.randInt(0, (populationSize/noTasks)-1);
					int r2 = PseudoRandom.randInt(0, (populationSize/noTasks)-1);
					while(r2 == r1) {
						r2 = PseudoRandom.randInt(0, (populationSize/noTasks)-1);
					}
					Solution so1 = population[rd].get(r1);
					Solution so2 = population[rd].get(r2);
					int flag = comparatorD.compare(so1, so2);
					double[] encode = null;
					if(flag < 0) {
						XReal xSol = new XReal(so1);
						if(numVars[rd] < problemSet_.getMaxDimension()) {
							for(int j=numVars[rd];j<problemSet_.getMaxDimension();j++) {
								xSol.setValue(j, 0.0);
							}
						}
						encode = encodeWithDAE(so1,t);
					}else {
						XReal xSol = new XReal(so2);
						if(numVars[rd] < problemSet_.getMaxDimension()) {
							for(int j = numVars[rd];j < problemSet_.getMaxDimension();j++) {
								xSol.setValue(j, 0.0);
							}
						}
						encode = encodeWithDAE(so2,t);
					}
					Solution child = decodeWithDAE(encode, t);
					problemSet_.get(t).evaluate(child);
					problemSet_.get(t).evaluateConstraints(child);
					knowledgeSet.add(child);
				}
				//generate the transferred solutions
				updateCurrentPopulation(population[t], knowledgeSet);
				evaluations += transNo;
			}
			// Create the offSpring solutionSet
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize/noTasks/2); i++) {
				// obtain parents
				parents[0] = (Solution) selectionOperator.execute(population[t]);
				parents[1] = (Solution) selectionOperator.execute(population[t]);
				if (PseudoRandom.randDouble() < 0.5) {
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					problemSet_.get(t).evaluate(offSpring[0]);
					problemSet_.get(t).evaluateConstraints(offSpring[0]);
					problemSet_.get(t).evaluate(offSpring[1]);
					problemSet_.get(t).evaluateConstraints(offSpring[1]);
					offspringPopulation[t].add(offSpring[0]);
					offspringPopulation[t].add(offSpring[1]);
					archive[t].add(offSpring[0]);
					archive[t].add(offSpring[1]);
				} else{
					Solution[] offSpring = new Solution[2];
					for(int c=0;c<2;c++){
						int size = archive[t].size();
						if(size > 3) {
							Solution[] exParents = new Solution[3];
							exParents[2] = parents[c];
							int rdInt1 = PseudoRandom.randInt(0, size-1);
							int rdInt2 = PseudoRandom.randInt(0, size-1);
							while(rdInt1 == rdInt2) {
								rdInt2 = PseudoRandom.randInt(0, size-1);
							}
							exParents[0] = archive[t].get(rdInt1);
							exParents[1] = archive[t].get(rdInt2);
							offSpring[c] = (Solution) crossover1.execute(new Object[] {
									parents[c], exParents});
						}else {
							offSpring[c] = new Solution(parents[c]);
						}
						mutationOperator.execute(offSpring[c]);
						offSpring[c].setSkillFactor(t);
						problemSet_.get(t).evaluate(offSpring[c]);
						problemSet_.get(t).evaluateConstraints(offSpring[c]);
						offspringPopulation[t].add(offSpring[c]);
						archive[t].add(offSpring[c]);
					}
				}// if
				evaluations += 2;
			} // for			
		}	
	}
	
	public double[][] encodeWithDAE(Solution[] parents,int task) throws JMException {
		double[][] encodedParents = new double[parents.length][];
		for(int i=0;i<parents.length;i++) {
			XReal xsol = new XReal(parents[i]);
			encodedParents[i] = DAE[task].encode(parents[i],problemSet_.getMaxDimension());
		}
		return encodedParents;
	}
	
	public double[] encodeWithDAE(Solution parent,int task) throws JMException {
		double[] encodedParents;
		XReal xsol = new XReal(parent);
		
		encodedParents = DAE[task].encode(parent, problemSet_.getMaxDimension());
		
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
	
	public void updateCurrentPopulation(SolutionSet curPop, SolutionSet newSolutionSet) {
		SolutionSet unionSet = ((SolutionSet) curPop).union(newSolutionSet);
		int id = curPop.get(0).getSkillFactor();
		curPop.clear();
		// Ranking the union
		Ranking ranking = new Ranking(unionSet);
		int remain = populationSize/noTasks;
		int index = 0;
		SolutionSet front = null;
		// Obtain the next front
		front = ranking.getSubfront(index);
		while ((remain > 0) && (remain >= front.size())) {
			// Assign crowding distance to individuals
			distance.crowdingDistanceAssignment(front, problemSet_.get(id).getNumberOfObjectives());
			// Add the individuals of this front
			for (int k = 0; k < front.size(); k++) {
				curPop.add(front.get(k));
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
			distance.crowdingDistanceAssignment(front, problemSet_.get(id).getNumberOfObjectives());
			front.sort(new CrowdingComparator());
			for (int k = 0; k < remain; k++) {
				curPop.add(front.get(k));
			} // for
			remain = 0;
		} // if			
	}
} //MLMOEA
