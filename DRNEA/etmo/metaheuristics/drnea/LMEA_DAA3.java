// LMOEA/DAA.java

package etmo.metaheuristics.drnea;

import java.util.ArrayList;
import java.util.List;

import etmo.core.*;
import etmo.util.Distance;
import etmo.util.JMException;
import etmo.util.PORanking;
import etmo.util.PseudoRandom;
import etmo.util.Ranking;
import etmo.util.archive.SPEA2DensityArchive;
import etmo.util.comparators.CrowdingComparator;
import etmo.util.comparators.LocationComparator;
import etmo.util.wrapper.XReal;

/**
 * Implementation of LMEA/DAA
 */

public class LMEA_DAA3 extends Algorithm {
	int populationSize;
	int maxEvaluations;
	int evaluations;
	SolutionSet[] archive;
	SolutionSet conPopulation;
	SolutionSet offspringPopulation;
	SolutionSet union;
	
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
	Discriminator DIS;
	
	double[][] upBounds;
	double[][] lowBounds;
	
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public LMEA_DAA3(ProblemSet problemSet) {
		super(problemSet);
	//	System.out.println("sup: " + problemSet_.get(0).getHType());
	} // MLMOEA
	
	void initPopulation() throws JMException, ClassNotFoundException {
		conPopulation = new SolutionSet(populationSize);
		for(int t=0;t<noTasks;t++) {
			Solution newSolution;
			for (int j = 0; j < populationSize/noTasks; j++) {
				newSolution = new Solution(problemSet_);
				problemSet_.get(t).evaluate(newSolution);
				problemSet_.get(t).evaluateConstraints(newSolution);
				/*
				 * the skill factor of this new solution is the one task, amongst all other tasks in a K-factorial environment,
				 * with which the solution is associated. 
				*/
				newSolution.setSkillFactor(t);
				conPopulation.add(newSolution);
				archive[t].add(newSolution);
			} // for	
		}
		//The scalar fitness of each solution in a multitasking environment
		assignFitness(conPopulation);
	} // initPopulation

	public void initialization() throws JMException, ClassNotFoundException {
		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		// Initialize the variables
		noTasks = problemSet_.size();
		archive = new SPEA2DensityArchive[noTasks];
		offspringPopulation = new SolutionSet(populationSize);
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
			archive[t] = new SPEA2DensityArchive(populationSize/noTasks);
			numVars[t] = problemSet_.get(t).getNumberOfVariables();
			numObjs[t] = problemSet_.get(t).getNumberOfObjectives();
			upBounds[t] = new double[numVars[t]];
			lowBounds[t] = new double[numVars[t]];
			for(int var=0;var<numVars[t];var++) {
				upBounds[t][var] = problemSet_.getUnifiedUpperLimit();
				lowBounds[t][var] = problemSet_.getUnifiedLowerLimit();
			}
		}
		
		initPopulation();
		
		//Construct the Auto-Encoders based on neural networks for each task
		DAE = new DenoiseAutoEncoder[noTasks];
		numCodes = 10;
		learningRate = 0.1;
		for(int t=0;t<noTasks;t++) {
			DAE[t] = new DenoiseAutoEncoder(numVars[t], numCodes, 3, learningRate);
		}
		
		int numLayers = 4;
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
		
		epochs = 1;
		getTraningModels();
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
			
			if(evaluations < 0.5*maxEvaluations) {//stage 1
				reproduction1(0.1);
			}else {//stage 2
				reproduction2(0.5);
			}
			getNextPopulation();
			
		} // while
		return conPopulation; 
	} // execute
	
	public void getTraningModels() throws JMException {
		for(int t=0;t<noTasks;t++) {
			//incrementally training the Auto-Encoders based on neural networks for each task
			DAE[t].getTrainingModel(archive[t], epochs);
			double[][] encodes = DAE[t].encode(archive[t],numVars[t]);
			double[][] targets = new double[encodes.length][noTasks];
			for(int i=0; i<encodes.length; i++){
				for(int tt=0; tt<noTasks; tt++){
					if(tt == t) targets[i][tt] = 1.0;
					else targets[i][tt] = 0.0;
				}
				DIS.trainingModel(encodes[i], targets[i]);
				double[] error = DIS.classifier.getError();
				DAE[t].baseAE.trainEncoder(error);
			}
		}
	}
	
	public void tranning(Solution sol, int task) throws JMException {
		DAE[task].trainingModel(sol, epochs);
		double[] encodes = DAE[task].encode(sol,numVars[task]);
		double[] targets = new double[noTasks];
		for(int tt=0; tt<noTasks; tt++){
			if(tt == task) targets[tt] = 1.0;
			else targets[tt] = 0.0;
		}
		DIS.trainingModel(encodes, targets);
		double[] error = DIS.classifier.getError();
		DAE[task].baseAE.trainEncoder(error);
	}
	
	public void reproduction1(double rmp) throws JMException, ClassNotFoundException {
		Solution[] parents = new Solution[2];
		for (int i = 0; i < (populationSize / 2); i++) {
			parents[0] = (Solution) selectionOperator.execute(conPopulation);
			parents[1] = (Solution) selectionOperator.execute(conPopulation);			
			int[] sfs = new int[2];
			sfs[0] = parents[0].getSkillFactor();
			sfs[1] = parents[1].getSkillFactor();
			double rand = PseudoRandom.randDouble();
			Solution[] offSpring;
			if (sfs[0] == sfs[1] || rand < rmp) {
				offSpring = (Solution[]) crossoverOperator.execute(parents);
				mutationOperator.execute(offSpring[0]);
				mutationOperator.execute(offSpring[1]);				
				int p0 = PseudoRandom.randInt(0, 1);
				int p1 = PseudoRandom.randInt(0, 1);
			    //int p1 = 1 - p0;
				offSpring[0].setSkillFactor(sfs[p0]);
				offSpring[1].setSkillFactor(sfs[p1]);				
				resetObjectives(offSpring[0]);
				resetObjectives(offSpring[1]);
				problemSet_.get(sfs[p0]).evaluate(offSpring[0]);
				problemSet_.get(sfs[p1]).evaluate(offSpring[1]);
				problemSet_.get(sfs[p0]).evaluateConstraints(offSpring[0]);
				problemSet_.get(sfs[p1]).evaluateConstraints(offSpring[1]);
				boolean flag0 = archive[sfs[p0]].add(offSpring[0]);
				if(flag0) tranning(offSpring[0], sfs[p0]);
				boolean flag1 = archive[sfs[p1]].add(offSpring[1]);
				if(flag1) tranning(offSpring[1], sfs[p1]);
			} else {
				offSpring = new Solution[2];
				for(int c=0;c<2;c++){
					int size = archive[sfs[c]].size();
					if(size > 3) {
						Solution[] exParents = new Solution[3];
						exParents[2] = parents[c];
						int rdInt1 = PseudoRandom.randInt(0, size-1);
						int rdInt2 = PseudoRandom.randInt(0, size-1);
						while(rdInt1 == rdInt2) {
							rdInt2 = PseudoRandom.randInt(0, size-1);
						}
						exParents[0] = archive[sfs[c]].get(rdInt1);
						exParents[1] = archive[sfs[c]].get(rdInt2);
						offSpring[c] = (Solution) crossover1.execute(new Object[] {
								parents[c], exParents});
					}else {
						offSpring[c] = new Solution(parents[c]);
					}
					mutationOperator.execute(offSpring[c]);
					offSpring[c].setSkillFactor(sfs[c]);
					problemSet_.get(sfs[c]).evaluate(offSpring[c]);
					problemSet_.get(sfs[c]).evaluateConstraints(offSpring[c]);
					boolean flag = archive[sfs[c]].add(offSpring[c]);
					if(flag) tranning(offSpring[c], sfs[c]);
				}	
			}
			
			offspringPopulation.add(offSpring[0]);
			offspringPopulation.add(offSpring[1]);
			evaluations += 2;
		}						
	}
	
	public void reproduction2(double rmp) throws JMException, ClassNotFoundException {
		Solution[] parents = new Solution[2];
		for (int i = 0; i < (populationSize / 2); i++) {
			parents[0] = (Solution) selectionOperator.execute(conPopulation);
			parents[1] = (Solution) selectionOperator.execute(conPopulation);			
			int[] sfs = new int[2];
			sfs[0] = parents[0].getSkillFactor();
			sfs[1] = parents[1].getSkillFactor();
			double rand = PseudoRandom.randDouble();
			Solution[] offSpring;
			offSpring = new Solution[2];
			if (sfs[0] == sfs[1] || rand < rmp) {
				double rd;
				double[][] encoder = new double[2][];
				//Encode via the related DAE models
				encoder[0] = encodeWithDAE(parents[0],sfs[0]);
				encoder[1] = encodeWithDAE(parents[1],sfs[1]);
				//Searching in the transfered subspace
				double[][] newEncoder = doCrossover(0.9, encoder);
				doMutation(newEncoder);
				for(int c=0;c<2;c++){
					rd = PseudoRandom.randDouble();
					double[] tar = DIS.computeTarget(newEncoder[c]);
					int pT = 0;
					double maxV = tar[0];
					for(int tt=1;tt<noTasks;tt++){
						if(maxV < tar[tt]){
							maxV = tar[tt];
							pT = tt;
						}
					}
					if(rd > maxV){
						int pc = PseudoRandom.randInt(0, 1);
						pT = sfs[pc];
					}
					//Decode via the related DAE models
					offSpring[c] = decodeWithDAE(newEncoder[c],pT);
					resetObjectives(offSpring[c]);
					//Evaluation
					problemSet_.get(pT).evaluate(offSpring[c]);
					problemSet_.get(pT).evaluateConstraints(offSpring[c]);
					archive[pT].add(offSpring[c]);
					boolean flag = archive[pT].add(offSpring[c]);
					if(flag) tranning(offSpring[c], pT);
				}
			}else {
				double[][] childCode = new double[2][];
				for(int c=0;c<2;c++){
					double[][] encoder = new double[3][];
					encoder[2] = encodeWithDAE(parents[c],sfs[c]);
					int size = archive[sfs[c]].size();
					if(size > 3) {
						Solution[] exParents = new Solution[3];
						exParents[2] = parents[c];
						int rdInt1 = PseudoRandom.randInt(0, size-1);
						int rdInt2 = PseudoRandom.randInt(0, size-1);
						while(rdInt1 == rdInt2) {
							rdInt2 = PseudoRandom.randInt(0, size-1);
						}
						exParents[0] = archive[sfs[c]].get(rdInt1);
						exParents[1] = archive[sfs[c]].get(rdInt2);
						encoder[0] = encodeWithDAE(exParents[0],sfs[c]);
						encoder[1] = encodeWithDAE(exParents[1],sfs[c]);
						childCode[c] = doCrossover1(encoder);
					}else {
						childCode[c] = encodeWithDAE(parents[c],sfs[c]);
					}
				}
				doMutation(childCode);
				for(int c=0;c<2;c++){
					double rd = PseudoRandom.randDouble();
					double[] tar = DIS.computeTarget(childCode[c]);
					int pT = 0;
					double maxV = tar[0];
					for(int tt=1;tt<noTasks;tt++){
						if(maxV < tar[tt]){
							maxV = tar[tt];
							pT = tt;
						}
					}
					if(rd > maxV){
						pT = sfs[c];
					}
					
					offSpring[c] = decodeWithDAE(childCode[c],pT);
					problemSet_.get(pT).evaluate(offSpring[c]);
					problemSet_.get(pT).evaluateConstraints(offSpring[c]);
					archive[pT].add(offSpring[c]);
					boolean flag = archive[pT].add(offSpring[c]);
					if(flag) tranning(offSpring[c], pT);
				}
			}
			
			offspringPopulation.add(offSpring[0]);
			offspringPopulation.add(offSpring[1]);
			evaluations += 2;
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

	void getNextPopulation() {
		union = conPopulation.union(offspringPopulation);
		//long tmpAFTime = System.currentTimeMillis();
		assignFitness(union);
		//long endTime = System.currentTimeMillis();
		union.sort(new LocationComparator());
		conPopulation.clear();
		offspringPopulation.clear();
		for (int i = 0; i < populationSize; i++)
			conPopulation.add(union.get(i));
	}	
	
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
				probability = 0.1;
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
	
	public double[] doCrossover1(double[][] encodedParents) throws JMException {
		int dim = encodedParents[0].length;
		double[] off = new double[dim];
		for(int d=0;d<dim;d++) {
			double value = encodedParents[2][d];
			if(PseudoRandom.randDouble() <= 0.5) {
				value = encodedParents[2][d] + 0.5 * (encodedParents[0][d] - encodedParents[1][d]);
				if(value < 0.0) value = 0.0;
				if(value > 1.0) value = 1.0;
			}
			off[d] = value;
		}
		return off;
	} // doCrossover
	
	//The scalar fitness of each solution of the population in a multitasking environment is given.
	public void assignFitness(SolutionSet pop) {
		for (int i = 0; i < pop.size(); i++)
			pop.get(i).setLocation(Integer.MAX_VALUE);
		for (int i = 0; i < problemSet_.size(); i++)
			rankSolutionOnTask(pop, i);
	}
	
	public void rankSolutionOnTask(SolutionSet pop, int taskId) {
		int start = problemSet_.get(taskId).getStartObjPos();
		int end = problemSet_.get(taskId).getEndObjPos();		
		boolean selec[] = new boolean[problemSet_.getTotalNumberOfObjs()];		
		for (int i = 0; i < selec.length; i++) {
			if (i < start || i > end)
				selec[i] = false;
			else
				selec[i] = true;
		}
		//Fast non-dominated Ranking as in NSGA-II, dividing pop into multiple subsets: S0,S1,...
		PORanking pr = new PORanking(pop, selec);
		int loc = 0;
		for (int i = 0; i < pr.getNumberOfSubfronts(); i++) {
			SolutionSet front = pr.getSubfront(i);
			distance.crowdingDistanceAssignment(front, problemSet_.getTotalNumberOfObjs(), selec);
			front.sort(new CrowdingComparator());
			for (int j = 0; j < front.size(); j++) {
				if (loc < front.get(j).getLocation())
					front.get(j).setLocation(loc);
				loc++;
			}
		}
	}
	public void resetObjectives(Solution sol) {
		for (int i = 0; i < sol.getNumberOfObjectives(); i++)
			sol.setObjective(i, Double.POSITIVE_INFINITY);
	}
	
	public void tailoringArchive() {
		for(int t=0;t<noTasks;t++) {
			tailoring(archive[t],t);
		}
	}
	
	public void tailoring(SolutionSet arc, int t) {
		double[] zideal = new double[numObjs[t]];
		for(int i=0;i<numObjs[t];i++) {
			zideal[i] = arc.get(0).getObjective(i);
			for(int p=1;p<arc.size();p++) {
				double z = arc.get(p).getObjective(i);
				if(zideal[i] < z) {
					zideal[i] = z;
				}
			}
		}
		double[] dis2Ideal = new double[arc.size()];
		for(int p=0;p<arc.size();p++) {
			dis2Ideal[p] = 0.0;
			for(int i=0;i<numObjs[t];i++) {
				dis2Ideal[p] += (arc.get(p).getObjective(i) - zideal[i])*(arc.get(p).getObjective(i) - zideal[i]);
			}
			dis2Ideal[p] = Math.sqrt(dis2Ideal[p]);
		}
		
		int[][] indexs = new int[numObjs[t]][arc.size()];
		for(int p=0;p<arc.size();p++) {
			int ss = 0;
			for(int i=0;i<numObjs[t];i++) {
				double angle = Math.acos(Math.abs((arc.get(p).getObjective(i) - zideal[i])/(dis2Ideal[p])));
				if(angle < Math.PI/2/100) {
					indexs[i][ss] = p;
					break;
				}
			}
		}
	}
	
	
} //MLMOEA
