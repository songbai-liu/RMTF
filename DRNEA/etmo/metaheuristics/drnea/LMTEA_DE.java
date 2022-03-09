package etmo.metaheuristics.drnea;

import etmo.core.Algorithm;
import etmo.core.Operator;
import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.core.Solution;
import etmo.core.SolutionSet;
import etmo.util.Distance;
import etmo.util.JMException;
import etmo.util.PseudoRandom;
import etmo.util.Ranking;
import etmo.util.comparators.CrowdingComparator;
import etmo.util.wrapper.XReal;

public class LMTEA_DE extends Algorithm {
	private int populationSize;
	
	private SolutionSet population;
	private SolutionSet offspringPopulation;
	private SolutionSet unionPopulation;
	
	Operator mutationOperator;
	Operator crossoverOperator;
	Operator selectionOperator;
	
	int evaluations;
	int maxEvaluations;
	
	Distance distance = new Distance();
		
	public LMTEA_DE(ProblemSet problemSet) {
		super(problemSet);
	}

	
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		
		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");
		
		// Initialize the variables
		evaluations = 0;
		//unionPopulation = new SolutionSet(2*populationSize);
		initPopulation();
		int numVars = problemSet_.get(0).getNumberOfVariables();
		double[] lowBounds = new double[numVars];
		double[] upBounds = new double[numVars];
		XReal sol = new XReal(population.get(0));
		for(int d=0;d<numVars;d++) {
			lowBounds[d] = sol.getLowerBound(d);
			upBounds[d] = sol.getUpperBound(d);
		}
		int reducedD = 20;
		if(numVars <= reducedD) {
			reducedD = numVars/2;
		}
		int layerNum = 3;
		int epochs = 20;
		double learningRate = 0.1;
		//Construct a Auto-Encoder based on deep neural networks
		DenoiseAutoEncoder DAE = new DenoiseAutoEncoder(numVars, reducedD, layerNum, learningRate);
		Ranking ranking0 = new Ranking(population);
		
		DAE.getTrainingModel(ranking0.getSubfront(0), 0.1, 2000);
		
		//Main loop: evolutionary of the population
		while (evaluations < maxEvaluations) {
			
			if(evaluations % 1000 == 0) {
				//get the training set of solutions
				Ranking ranking1 = new Ranking(population);
				SolutionSet trainingSet = ranking1.getSubfront(0);
				//Update the DAE by training
				DAE.getTrainingModel(trainingSet, epochs); 
			}
			// Create the offSpring solutionSet
			offspringPopulation = new SolutionSet(populationSize);
			Solution[] parents = new Solution[3];
			double[][] encodedParents = new double[3][reducedD]; 
			for (int i = 0; i < populationSize; i++) {
				// obtain parents
				parents[0] = population.get(i);
				int rdInt1 = PseudoRandom.randInt(0, populationSize-1);
				while(rdInt1 == i) {
					rdInt1 = PseudoRandom.randInt(0, populationSize-1);
				}
				int rdInt2 = PseudoRandom.randInt(0, populationSize-1);
				while(rdInt2 == i || rdInt2 == rdInt1) {
					rdInt2 = PseudoRandom.randInt(0, populationSize-1);
				}
				parents[1] = population.get(rdInt1);
				parents[2] = population.get(rdInt2);
				
				for(int j=0;j<3;j++) {
					encodedParents[j] = DAE.encode(parents[j],numVars);
				}
				
				double[] newEncode = new double[reducedD];
				for(int j=0;j<reducedD;j++) {
					newEncode[j] = encodedParents[0][j] + 1.0*(encodedParents[1][j] - encodedParents[2][j]);
					if(newEncode[j] < 0) {
						newEncode[j] = 0.00001;
					}
					if(newEncode[j] > 1) {
						newEncode[j] = 0.99999;
					}
				}
				double[] newDecode = DAE.decode(newEncode);
				Solution child = new Solution(parents[0]);
				XReal offspring = new XReal(child);
				double value;
				for(int var=0;var<numVars;var++) {
					value = newDecode[var]*(upBounds[var] - lowBounds[var]) + lowBounds[var];
					if(value < lowBounds[var]) {
						value = lowBounds[var];
					}
					if(value > upBounds[var]) {
						value = upBounds[var];
					}
					offspring.setValue(var, value);
				}
				mutationOperator.execute(child);
				problemSet_.get(0).evaluate(child);
				problemSet_.get(0).evaluateConstraints(child);
				offspringPopulation.add(child);
				evaluations += 1;
			} // for
			
			// Create the solutionSet union of solutionSet and offSpring
			unionPopulation = ((SolutionSet) population).union(offspringPopulation);
			// Ranking the union
			Ranking ranking = new Ranking(unionPopulation);

			int remain = populationSize;
			int index = 0;
			SolutionSet front = null;
			population.clear();

			// Obtain the next front
			front = ranking.getSubfront(index);

			while ((remain > 0) && (remain >= front.size())) {
				// Assign crowding distance to individuals
				distance.crowdingDistanceAssignment(front, problemSet_.get(0).getNumberOfObjectives());
				// Add the individuals of this front
				for (int k = 0; k < front.size(); k++) {
					population.add(front.get(k));
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
				distance.crowdingDistanceAssignment(front, problemSet_.get(0).getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (int k = 0; k < remain; k++) {
					population.add(front.get(k));
				} // for

				remain = 0;
			} // if
			
		}
		
		Ranking ranking = new Ranking(population);

		return ranking.getSubfront(0);
	}
	
	public void initPopulation() throws JMException, ClassNotFoundException {
		population = new SolutionSet(populationSize);
		for (int i = 0; i < populationSize; i++) {
			Solution newSolution = new Solution(problemSet_);
			problemSet_.get(0).evaluate(newSolution);
			problemSet_.get(0).evaluateConstraints(newSolution);
			evaluations++;
			population.add(newSolution);
		} // for
	} // initPopulation

}
