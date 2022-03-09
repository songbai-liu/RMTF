// NSGAII.java

package etmo.metaheuristics.nsgaII;

import etmo.core.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.Distance;
import etmo.util.JMException;
import etmo.util.PseudoRandom;
import etmo.util.Ranking;
import etmo.util.comparators.CrowdingComparator;
import etmo.util.comparators.ObjectiveComparator;

/**
 * Implementation of NSGAII Similar to the MOMFEA Framework
 */

public class NSGAII_New extends Algorithm {
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
	
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public NSGAII_New(ProblemSet problemSet) {
		super(problemSet);
	//	System.out.println("sup: " + problemSet_.get(0).getHType());
	} // NSGAII

	public void initialization() throws JMException, ClassNotFoundException {
		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		// Initialize the variables
		noTasks = problemSet_.size();
	
		population = new SolutionSet[noTasks];
		offspringPopulation = new SolutionSet[noTasks];
		union = new SolutionSet[noTasks];
		for(int i=0;i<noTasks;i++) {
			population[i] = new SolutionSet(populationSize/noTasks);
			// Create the initial solutionSet
			Solution newSolution;
			for (int j = 0; j < populationSize/noTasks; j++) {
				newSolution = new Solution(problemSet_);
				problemSet_.get(i).evaluate(newSolution);
				problemSet_.get(i).evaluateConstraints(newSolution);
				evaluations++;
				newSolution.setSkillFactor(i);
				population[i].add(newSolution);
			} // for		
			offspringPopulation[i] = new SolutionSet(populationSize/noTasks);
			union[i] = new SolutionSet(2*populationSize/noTasks);
		}
		evaluations = 0;
		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");
	}
	/**
	 * Runs the NSGA-II algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws JMException
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException {		
		initialization();
		// Generations
		while (evaluations < maxEvaluations) {
			reproduction(evaluations, maxEvaluations);
			environmetalSelection();
		} // while
		SolutionSet U = population[0];
		for(int i=1;i<noTasks;i++) {
			U = ((SolutionSet) U).union(population[i]);
		}
		return U; 
	} // execute
	
	public void reproduction(int curE, int maxE) throws JMException, ClassNotFoundException {
		for(int t=0;t<noTasks;t++) {
			// Create the offSpring solutionSet
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize/noTasks/2); i++) {
				if (evaluations < maxEvaluations) {
					// obtain parents
					parents[0] = (Solution) selectionOperator.execute(population[t]);
					parents[1] = (Solution) selectionOperator.execute(population[t]);
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					problemSet_.get(t).evaluate(offSpring[0]);
					problemSet_.get(t).evaluateConstraints(offSpring[0]);
					problemSet_.get(t).evaluate(offSpring[1]);
					problemSet_.get(t).evaluateConstraints(offSpring[1]);
					offspringPopulation[t].add(offSpring[0]);
					offspringPopulation[t].add(offSpring[1]);
					evaluations += 2;
				} // if
			} // for			
		}	
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
} // NSGA-II
