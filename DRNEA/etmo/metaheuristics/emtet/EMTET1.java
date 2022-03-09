// EMTET.java

package etmo.metaheuristics.emtet;

import etmo.core.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.Distance;
import etmo.util.JMException;
import etmo.util.PseudoRandom;
import etmo.util.Ranking;
import etmo.util.comparators.CrowdingComparator;
import etmo.util.comparators.ObjectiveComparator;

/**
 * Implementation of evolutionary multitasking via explicit transfer (EMTET) of knowledge
 * The paper is "Evolutionary Multitasking via Explicit Autoencoding"
 */

public class EMTET1 extends Algorithm {
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
	
	mDA[][][] models;
	
	int interval;//Interval of explicit solution transfer across tasks
	int transNo; //Number of solutions to be transferred across tasks
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public EMTET1(ProblemSet problemSet) {
		super(problemSet);
	//	System.out.println("sup: " + problemSet_.get(0).getHType());
	} // NSGAII

	public void initialization() throws JMException, ClassNotFoundException {
		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		// Initialize the variables
		int noTasks = problemSet_.size();
		if(noTasks != 2) {
			System.out.println("The number of tasks  does not meet the requirements of this algorithm.");
			System.exit(0);
		}
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
		
		interval = 10;
		transNo = 10;
		
		initializationM();
	}
	
	public void initializationM() throws JMException{
		int noTasks = problemSet_.size();
		models = new mDA[noTasks][][];
		SolutionSet curPop, hisPop;
		for(int i=0;i<noTasks;i++) {
			if(i == 0) {//mapping from OP2 to OP1 to get the transfer matrix M21
				curPop = population[0];//output of the DAE
				hisPop = population[1];//input of the DAE
			}else {//mapping from OP1 to OP2 to get the transfer matrix M12
				hisPop = population[0];
				curPop = population[1];
			}
			int nObj1 = hisPop.get(0).getNumberOfObjectives();
			models[i] = new mDA[nObj1][];
			for(int j=0;j<nObj1;j++) {
				//Sorting the input population, i.e., hisPop, based on the value of the j-th objective (Ascending order)
				hisPop.sort(new ObjectiveComparator(j));
				int nObj2 = curPop.get(0).getNumberOfObjectives();
				models[i][j] = new mDA[nObj2];
				for(int k=0;k<nObj2;k++) {
					//Sorting the input population, i.e., curPop, based on the value of the k-th objective (Ascending order)
					curPop.sort(new ObjectiveComparator(k));
					models[i][j][k] = new mDA(curPop, hisPop);
					models[i][j][k].getTansferMatrix();
				}
			}
		}
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
		while (evaluations < 0.5*maxEvaluations) {
			reproduction();
			environmetalSelection();
		} // while
		initializationM();
		while (evaluations < maxEvaluations) {
			reproduction(evaluations, maxEvaluations);
			environmetalSelection();
		} // while
		return ((SolutionSet) population[0]).union(population[1]);
	} // execute
	
	
	public void reproduction() throws JMException, ClassNotFoundException {
		for(int t=0;t<2;t++) {
			// Create the offSpring solutionSet
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize/4); i++) {
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
			} // for			
		}	
	}
	
	public void reproduction(int curE, int maxE) throws JMException, ClassNotFoundException {
		for(int t=0;t<2;t++) {
			if(evaluations/populationSize % interval == 0) {//Explicit knowledge transfer from other domain/task
				SolutionSet curPop, hisPop;
				if(t == 0) {
					curPop = population[0];
					hisPop = population[1];
				}else {
					curPop = population[1];
					hisPop = population[0];
				}
				int rd1 = PseudoRandom.randInt(0, hisPop.get(0).getNumberOfObjectives()-1);
				//Sorting the input population, i.e., hisPop, based on the value of the j-th objective (Ascending order)
				hisPop.sort(new ObjectiveComparator(rd1));
				//get the knowledge to be transfered
				SolutionSet knowledgeSet = new SolutionSet(transNo);
				for(int tr=0;tr<transNo;tr++) {
					knowledgeSet.add(hisPop.get(tr));
				}
				int rd2 = PseudoRandom.randInt(0, curPop.get(0).getNumberOfObjectives()-1);
				//generate the transferred solutions
				SolutionSet newSolutionSet = models[t][rd1][rd2].transferOperator(knowledgeSet, problemSet_);
				updateCurrentPopulation(curPop, newSolutionSet);
				evaluations += transNo;
			}
			// Create the offSpring solutionSet
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize/4); i++) {
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
		for(int t=0;t<2;t++) {
			// Create the solutionSet union of solutionSet and offSpring
			union[t] = ((SolutionSet) population[t]).union(offspringPopulation[t]);
			population[t].clear();
			offspringPopulation[t].clear();
			// Ranking the union
			Ranking ranking = new Ranking(union[t]);
			int remain = populationSize/2;
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
	
	public void updateCurrentPopulation(SolutionSet curPop, SolutionSet newSolutionSet) {
		SolutionSet unionSet = ((SolutionSet) curPop).union(newSolutionSet);
		int id = curPop.get(0).getSkillFactor();
		curPop.clear();
		// Ranking the union
		Ranking ranking = new Ranking(unionSet);
		int remain = populationSize/2;
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
	
} // NSGA-II
