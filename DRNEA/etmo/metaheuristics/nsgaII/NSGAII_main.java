package etmo.metaheuristics.nsgaII;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import etmo.core.*;
import etmo.operators.crossover.CrossoverFactory;
import etmo.operators.mutation.MutationFactory;
import etmo.operators.selection.SelectionFactory;
import etmo.problems.benchmarks_ETMO.*;
import etmo.problems.benchmarks_LSMOP.LSMOP1;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.JMException;
import etmo.util.Ranking;

public class NSGAII_main {
	public static void main(String args[]) throws IOException, JMException, ClassNotFoundException {
		ProblemSet problemSet; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		Operator selection;

		HashMap parameters; // Operator parameters
		
		int totalPro=22;
	    int pCase;
		
	    for(pCase=20;pCase<totalPro;pCase++){
	    	switch(pCase){
			case 0:
			    problemSet = CPLX1.getT1();
				break;
			case 1:
				problemSet = CPLX1.getT2();    
				break;
			case 2:
			    problemSet = CPLX2.getT1();
				break;
			case 3:
				problemSet = CPLX2.getT2();    
				break;
			case 4:
			    problemSet = CPLX3.getT1();
				break;
			case 5:
				problemSet = CPLX3.getT2();    
				break;
			case 6:
			    problemSet = CPLX4.getT1();
				break;
			case 7:
				problemSet = CPLX4.getT2();    
				break;
			case 8:
			    problemSet = CPLX5.getT1();
				break;
			case 9:
				problemSet = CPLX5.getT2();    
				break;
			case 10:
			    problemSet = CPLX6.getT1();
				break;
			case 11:
				problemSet = CPLX6.getT2();    
				break;
			case 12:
			    problemSet = CPLX7.getT1();
				break;
			case 13:
				problemSet = CPLX7.getT2();    
				break;
			case 14:
			    problemSet = CPLX8.getT1();
				break;
			case 15:
				problemSet = CPLX8.getT2();    
				break;
			case 16:
			    problemSet = CPLX9.getT1();
				break;
			case 17:
				problemSet = CPLX9.getT2();    
				break;
			case 18:
			    problemSet = CPLX10.getT1();
				break;
			case 19:
				problemSet = CPLX10.getT2();    
				break;
			case 20:
				problemSet = CIHS.getT1();    
				break;
			case 21:
				problemSet = CIHS.getT2();    
				break;
			default:
				problemSet = CIHS.getT2();			
			}
	    	
	    	int taskNumber = problemSet.size();
	    	System.out.println("taskNumber = "+taskNumber);
	    	
	    	algorithm = new NSGAII(problemSet);
	    	String pf = "PF/StaticPF/" + problemSet.get(0).getHType() + "_" + problemSet.get(0).getNumberOfObjectives() + "D.pf";
	    	algorithm.setInputParameter("populationSize", 100);
			algorithm.setInputParameter("maxEvaluations", 100 * 300);
	
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
		    parameters = null ;
		    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;  
		    
			// Add the operators to the algorithm
			algorithm.addOperator("crossover", crossover);
			algorithm.addOperator("mutation", mutation);
			algorithm.addOperator("selection", selection);
	
			System.out.println("RunID\t" + "IGD for " + problemSet.get(0).getName());
			DecimalFormat form = new DecimalFormat("#.####E0");
			QualityIndicator indicator = new QualityIndicator(problemSet.get(0), pf);
			int times = 1;
			double aveIGD = 0;
			long Execution_time=0;
			for (int i = 1; i <= times; i++) {
				long initTime = System.currentTimeMillis();
				SolutionSet population = algorithm.execute();
				Execution_time += (System.currentTimeMillis() - initTime);
				Ranking ranking = new Ranking(population);
				population = ranking.getSubfront(0);
				population.printObjectivesToFile("NSGAII_"+problemSet.get(0).getNumberOfObjectives()+"Obj_"+
					                             problemSet.get(0).getName()+ "_" + problemSet.get(0).getNumberOfVariables() + "D_run"+i+".txt");
				double igd = indicator.getIGD(population);
				aveIGD += igd;
				System.out.println(i + "\t" + form.format(igd));
			} 	
			System.out.println("Total execution time: " + (double)Execution_time/1000 + "s");
			System.out.println("IGD_"+problemSet.get(0).getNumberOfObjectives()+"Obj_"+ problemSet.get(0).getName() + "_"
					            + problemSet.get(0).getNumberOfVariables() + "D" + " = " + form.format(aveIGD / times));
			System.out.println();
			
	    }
	}	

}
