package etmo.metaheuristics.mfakt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import etmo.problems.benchmarks_ETMO.ETMOF17;
import etmo.problems.benchmarks_ETMO.ETMOF18;
import etmo.problems.benchmarks_ETMO.ETMOF19;
import etmo.problems.benchmarks_ETMO.ETMOF20;
import etmo.problems.benchmarks_ETMO.ETMOF21;
import etmo.problems.benchmarks_ETMO.ETMOF22;
import etmo.problems.benchmarks_ETMO.ETMOF23;
import etmo.problems.benchmarks_ETMO.ETMOF24;
import etmo.problems.benchmarks_LSMOP.LSMOP1;
import etmo.problems.benchmarks_LSMOP.LSMOP2;
import etmo.problems.benchmarks_LSMOP.LSMOP3;
import etmo.problems.benchmarks_LSMOP.LSMOP4;
import etmo.problems.benchmarks_LSMOP.LSMOP5;
import etmo.problems.benchmarks_LSMOP.LSMOP6;
import etmo.problems.benchmarks_LSMOP.LSMOP7;
import etmo.problems.benchmarks_LSMOP.LSMOP8;
import etmo.problems.benchmarks_LSMOP.LSMOP9;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX1;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX10;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX2;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX3;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX4;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX5;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX6;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX7;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX8;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.CPLX9;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.JMException;
import etmo.util.Ranking;

public class AKT_main {
	public static void main(String args[]) throws IOException, JMException, ClassNotFoundException {
		ProblemSet problemSet; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		
		Operator crossover; // Crossover operator
		Operator crossover1;
		Operator mutation; // Mutation operator
		Operator selection;
		
		HashMap parameters; // Operator parameters	
					
	    int totalPro=18,totalcx=8;
	    int pCase,cxNum;	    
	    
	    for(cxNum=7;cxNum<totalcx;cxNum++){	    
			for(pCase=18;pCase<27;pCase++){	
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
				    problemSet = CPLX8.getProblem(); 
					break;				
				case 8:
				    problemSet = CPLX9.getProblem();
					break;	
				case 9:
				    problemSet = CPLX10.getProblem();
					break;
				case 10:
				    problemSet = ETMOF17.getProblem();
					break;
				case 11:
				    problemSet = ETMOF18.getProblem();
					break;
				case 12:
				    problemSet = ETMOF19.getProblem();
					break;
				case 13:
				    problemSet = ETMOF20.getProblem();
					break;
				case 14:
				    problemSet = ETMOF21.getProblem();
					break;
				case 15:
				    problemSet = ETMOF22.getProblem();
					break;
				case 16:
				    problemSet = ETMOF23.getProblem();
					break;
				case 17:
				    problemSet = ETMOF24.getProblem();
					break;
				case 18:
				    problemSet = LSMOP1.getProblem();
					break;
				case 19:
				    problemSet = LSMOP2.getProblem();
					break;
				case 20:
				    problemSet = LSMOP3.getProblem();
					break;
				case 21:
				    problemSet = LSMOP4.getProblem();
					break;
				case 22:
				    problemSet = LSMOP5.getProblem();
					break;
				case 23:
				    problemSet = LSMOP6.getProblem();
					break;
				case 24:
				    problemSet = LSMOP7.getProblem();
					break;
				case 25:
				    problemSet = LSMOP8.getProblem();
					break;
				case 26:
				    problemSet = LSMOP9.getProblem();
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
				
				algorithm = new MFEAAKT(problemSet);	
				
				algorithm.setInputParameter("populationSize",150*taskNumber);
				algorithm.setInputParameter("maxEvaluations",150*taskNumber * 1000);
				algorithm.setInputParameter("rmp", 0.9);
		
				parameters = new HashMap();
				parameters.put("probability", 0.9);
				parameters.put("distributionIndex", 20.0);
				
				crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);		
				
				switch(cxNum){
				case 0:
					crossover1 = CrossoverFactory.getCrossoverOperator("SpCrossover", parameters);
					break;
				case 1:
					crossover1 = CrossoverFactory.getCrossoverOperator("TpCrossover", parameters); //Two point
					break;
				case 2:
					crossover1 = CrossoverFactory.getCrossoverOperator("UfCrossover", parameters);//Uniform
					break;				
				case 3:
					crossover1 = CrossoverFactory.getCrossoverOperator("AriCrossover", parameters);//Arithmetical
					break;	
				case 4:
					crossover1 = CrossoverFactory.getCrossoverOperator("GeoCrossover", parameters);//Geometrical
					break;						
				case 5:
					crossover1 = CrossoverFactory.getCrossoverOperator("BLX03Crossover", parameters);//BLX-0.3
					break;					
				case 6:
					crossover1 = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);
					break;	
				case 7:
					crossover1 = CrossoverFactory.getCrossoverOperator("NewCrossover", parameters);
					break;		
				default:
					crossover1 = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);		
				}				
				
				
				// Mutation operator
				parameters = new HashMap();
				parameters.put("probability", 1.0 / problemSet.getMaxDimension());
				parameters.put("distributionIndex", 20.0);
				mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);
		
				// Selection Operator
			    parameters = new HashMap() ; 
			    parameters.put("comparator", new LocationComparator()) ;
			    selection = SelectionFactory.getSelectionOperator("BinaryTournament",
						parameters);
				
				
				// Add the operators to the algorithm
			    algorithm.addOperator("crossover1", crossover1);
				algorithm.addOperator("crossover", crossover);
				algorithm.addOperator("mutation", mutation);
				algorithm.addOperator("selection", selection);
																
				DecimalFormat form = new DecimalFormat("#.####E0");
				
				System.out.println("RunID\t" + "IGD for "+problemSet.get(0).getName()+" to "+problemSet.get(taskNumber-1).getName());
				//System.out.println(crossover1.getName());
				int times = 10;
				long Execuion_time=0;
				//===========================================================================================										
				double ave[] = new double[taskNumber];
				
				for (int t = 1; t <= times; t++) {
					long initTime = System.currentTimeMillis();
					SolutionSet population = algorithm.execute();
					Execuion_time += (System.currentTimeMillis() - initTime);
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
						resPopulation[i].printObjectivesToFile("MFEAAKT_"+problemSet.get(i).getNumberOfObjectives()+"Obj_"+
								problemSet.get(i).getName()+ "_" + problemSet.get(i).getNumberOfVariables() + "D_run"+t+".txt");
						igd =  indicator.getIGD(resPopulation[i]);
						//System.out.print(form.format(igd) + "\t" );
						ave[i] += igd;
					}
					//System.out.println("");		
				}
				System.out.println();
				System.out.println("Average Runtime for " + "LSMOP" + (pCase-17) + ": " + form.format(Execuion_time / times));    			
				//for(int i=0;i<taskNumber;i++) {
					//System.out.println("Average IGD for " + problemSet.get(i).getName()+ ": " + form.format(ave[i] / times));
					
				//}		
					
					
			}
	    }
		
	}
}
