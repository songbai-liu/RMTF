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

public class LMTEA_SBX extends Algorithm {
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
		
	public LMTEA_SBX(ProblemSet problemSet) {
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
		
		DAE.getTrainingModel(ranking0.getSubfront(0), 0.3, 1000);
		
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
			Solution[] parents = new Solution[2];
			double[][] encodedParents = new double[2][reducedD]; 
			for (int i = 0; i < populationSize/2; i++) {
				// obtain parents
				parents[0] = (Solution) selectionOperator.execute(population);
				parents[1] = (Solution) selectionOperator.execute(population);
				for(int j=0;j<2;j++) {
					encodedParents[j] = DAE.encode(parents[j],numVars);
				}
				
				double[][] newEncode = doCrossover(0.9,encodedParents);
				
				double[] newDecode0 = DAE.decode(newEncode[0]);
				double[] newDecode1 = DAE.decode(newEncode[1]);
				Solution child0 = new Solution(parents[0]);
				Solution child1 = new Solution(parents[1]);
				XReal offspring0 = new XReal(child0);
				XReal offspring1 = new XReal(child1);
				double value;
				for(int var=0;var<numVars;var++) {
					value = newDecode0[var]*(upBounds[var] - lowBounds[var]) + lowBounds[var];
					if(value < lowBounds[var]) {
						value = lowBounds[var];
					}
					if(value > upBounds[var]) {
						value = upBounds[var];
					}
					offspring0.setValue(var, value);
					value = newDecode1[var]*(upBounds[var] - lowBounds[var]) + lowBounds[var];
					if(value < lowBounds[var]) {
						value = lowBounds[var];
					}
					if(value > upBounds[var]) {
						value = upBounds[var];
					}
					offspring1.setValue(var, value);
				}
				mutationOperator.execute(child0);
				problemSet_.get(0).evaluate(child0);
				problemSet_.get(0).evaluateConstraints(child0);
				offspringPopulation.add(child0);
				mutationOperator.execute(child1);
				problemSet_.get(0).evaluate(child1);
				problemSet_.get(0).evaluateConstraints(child1);
				offspringPopulation.add(child1);
				evaluations += 2;
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


}
