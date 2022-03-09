package etmo.metaheuristics.emtet;

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
import etmo.problems.benchmarks_ETMO.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.JMException;

public class EMTET_main {
	public static void main(String args[]) throws IOException, JMException, ClassNotFoundException {
		ProblemSet problemSet; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		Operator selection;
		
		HashMap parameters; // Operator parameters
		
		int totalPro=11;
	    int pCase;
	    for(pCase=10;pCase<totalPro;pCase++){
	    	switch(pCase){
	    	case 0:
			    problemSet = CPLX1.getProblem();
				break;
			case 1:
				problemSet = CPLX2.getProblem();    
				break;
			case 2:
			    problemSet = CPLX3.getProblem();  
				break;				
			case 3:
			    problemSet = CPLX4.getProblem();  
				break;	
			case 4:
			    problemSet = CPLX5.getProblem();  
				break;					
			case 5:
				problemSet = CPLX6.getProblem();    
				break;	
			case 6:
			    problemSet = CPLX7.getProblem();    
				break;		
			case 7:
			    problemSet = CPLX8.getProblem();  //3 objective  
				break;				
			case 8:
			    problemSet = CPLX9.getProblem();  //3 objective, different dimension
				break;	
			case 9:
			    problemSet = CPLX10.getProblem();  //3 objective, different dimension
				break;
			case 10:
			    problemSet = CIHS.getProblem();
				break;
			case 11:
			    problemSet = PIMS.getProblem();
				break;
			case 12:
			    problemSet = NIMS.getProblem();
				break;
			default:
				problemSet = CIHS.getProblem();			
			}
			int taskNumber = problemSet.size();
			System.out.println("taskNumber = "+taskNumber);
			String[] pf = new String[taskNumber];
			for(int i=0;i<taskNumber;i++) {
				pf[i] = "PF/StaticPF/" + problemSet.get(i).getHType() + "_" + problemSet.get(i).getNumberOfObjectives() + "D.pf";
			}
			
			algorithm = new EMTET(problemSet);
			
			algorithm.setInputParameter("populationSize",100*taskNumber);
			algorithm.setInputParameter("maxEvaluations",100*taskNumber * 100);

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
			
			int times = 10;
			
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
					QualityIndicator indicator = new QualityIndicator(problemSet.get(i), pf[i]);
					if(resPopulation[i].size()==0)
						continue;
					resPopulation[i].printObjectivesToFile("EMTET2_"+problemSet.get(i).getNumberOfObjectives()+"Obj_"+
							problemSet.get(i).getName()+ "_" + problemSet.get(i).getNumberOfVariables() + "D_run"+t+".txt");
					igd =  indicator.getIGD(resPopulation[i]);
					System.out.print(form.format(igd) + "\t" );
					ave[i] += igd;
				}
				System.out.println("");		
			}
			    			
			System.out.println();
			for(int i=0;i<taskNumber;i++)		
				System.out.println("Average IGD for " + problemSet.get(i).getName()+ ": " + form.format(ave[i] / times));
	    }
	}
}
