package etmo.metaheuristics.momfea;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import etmo.util.comparators.LocationComparator;
import etmo.core.Algorithm;
import etmo.core.Operator;
import etmo.core.ProblemSet;
import etmo.core.Solution;
import etmo.core.SolutionSet;


import etmo.operators.crossover.CrossoverFactory;
import etmo.operators.mutation.MutationFactory;
import etmo.operators.selection.SelectionFactory;
import etmo.problems.Benchmarks_RMTF.*;
import etmo.problems.benchmarks_ETMO.*;
import etmo.problems.benchmarks_LSMOP.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.JMException;

public class MOMFEAII_main_real {
	public static void main(String args[]) throws IOException, JMException, ClassNotFoundException {
		ProblemSet problemSet; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		Operator selection;
		
		HashMap parameters; // Operator parameters
		
		int totalPro=21;
	    int pCase;
	    for(pCase=6;pCase<7;pCase++){
	    	switch(pCase){
			case 0:
				problemSet = RMTF1.getProblem();
				break;
			case 1:
				problemSet = RMTF2.getProblem();    
				break;
			case 2:
			    problemSet = RMTF3.getProblem();  
				break;				
			case 3:
			    problemSet = RMTF4.getProblem();  
				break;	
			case 4:
			    problemSet = RMTF5.getProblem();  
				break;					
			case 5:
				problemSet = RMTF6.getProblem();    
				break;	
			case 6:
			    problemSet = RMTF7.getProblem();    
				break;		
			case 7:
			    problemSet = CPLX8.getProblem(); 
				break;				
			case 8:
			    problemSet = CPLX9.getProblem(); 
				break;	
			case 9:
			    problemSet = CPLX10.getProblem();
				break;
			default:
				problemSet = RMTF1.getProblem();			
			}
	    	
	    	int taskNumber = problemSet.size();
			System.out.println("taskNumber = "+taskNumber);
			
			algorithm = new MOMFEAII(problemSet);
			
			algorithm.setInputParameter("populationSize",50*taskNumber);
			algorithm.setInputParameter("maxEvaluations",50*taskNumber * 200);
			algorithm.setInputParameter("rmp", 0.9);

			parameters = new HashMap();
			parameters.put("probability", 0.9);
			parameters.put("distributionIndex", 20.0);
			crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

			// Mutation operator
			parameters = new HashMap();
			parameters.put("probability", 1.0 / problemSet.getMaxDimension());
			parameters.put("distributionIndex", 20.0);
			mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

			// Selection Operator
		    parameters = new HashMap() ; 
		    parameters.put("comparator", new LocationComparator());
		    selection = SelectionFactory.getSelectionOperator("BinaryTournament",
					parameters);
				
			// Add the operators to the algorithm
			algorithm.addOperator("crossover", crossover);
			algorithm.addOperator("mutation", mutation);
			algorithm.addOperator("selection", selection);
			
			DecimalFormat form = new DecimalFormat("#.####E0");
			
			System.out.println("RunID\t" + "IGD for "+problemSet.get(0).getName()+" to "+problemSet.get(taskNumber-1).getName());
			
			int times = 3;
			
			double ave[] = new double[taskNumber];
			for (int t = 1; t <= times; t++) {	
				SolutionSet population = algorithm.execute();
				SolutionSet[] resPopulation = new SolutionSet[problemSet.size()];
				for (int i = 0; i < problemSet.size(); i++)
					resPopulation[i] = new SolutionSet();

				for (int i = 0; i < population.size(); i++) {
					Solution sol = population.get(i);

					int pid = sol.getSkillFactor();

					int start = problemSet.get(pid).getStartObjPos();
					int end = problemSet.get(pid).getEndObjPos();

					Solution newSolution = new Solution(end - start + 1);

					for (int k = start; k <= end; k++)
						newSolution.setObjective(k - start, sol.getObjective(k));

					resPopulation[pid].add(newSolution);
				}
				
				double igd;
				System.out.print(t + "\t");
				for(int i=0;i<taskNumber;i++){
					if(resPopulation[i].size()==0)
						continue;
					resPopulation[i].printObjectivesToFile("MOMFEAII_"+problemSet.get(i).getNumberOfObjectives()+"Obj_"+
							problemSet.get(i).getName()+ "_" + problemSet.get(i).getNumberOfVariables() + "D_run"+t+".txt");
				}
				System.out.println("");		
			}
			    			
			System.out.println();
			for(int i=0;i<taskNumber;i++)		
				System.out.println("Average IGD for " + problemSet.get(i).getName()+ ": " + form.format(ave[i] / times));
	    }
	}
}
