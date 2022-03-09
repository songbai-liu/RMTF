package etmo.metaheuristics;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import etmo.core.ProblemSet;
import etmo.core.SolutionSet;
import etmo.problems.Benchmarks_RMTF.*;
import etmo.problems.benchmarks_LSMOP.*;
import etmo.problems.benchmarks_LSMOP_V1.*;
import etmo.problems.benchmarks_ManytaskMOP.*;
import etmo.qualityIndicator.fastHypervolume.wfg.wfghvCalculator;
import etmo.util.Configuration;
import etmo.util.JMException;

public class getNodominatedPopulation {
	
	public static Logger logger_; // Logger object
	public static FileHandler fileHandler_; // FileHandler object
	
	public static etmo.qualityIndicator.util.MetricsUtil utils_;
	
	public static void printGD(String path,double[] GD){
	    try {
	      /* Open the file */
	      FileOutputStream fos   = new FileOutputStream(path)     ;
	      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
	      BufferedWriter bw      = new BufferedWriter(osw)        ;               
	      for (int i = 0; i < GD.length; i++) {  
	        bw.write(GD[i]+" ");
	        bw.newLine();        
	      }
	      
	      /* Close the file */
	      bw.close();
	    }catch (IOException e) {
	      Configuration.logger_.severe("Error acceding to the file");
	      e.printStackTrace();
	    }       
	} // printGD
	
	public static void main(String[] args) throws JMException,
	SecurityException, IOException, ClassNotFoundException {
		ProblemSet problemSet; // The problem to solve
		wfghvCalculator WFG; // Object to get quality indicators
		int runtimes=1;
	    for(int fun=7;fun<=7;fun++){
	    	double[] hvArray=new double[runtimes];
	    	if(fun == 1) {
	    		problemSet = RMTF1.getProblem();
	    	}else if(fun == 2) {
	    		problemSet = RMTF2.getProblem();
	    	}else if(fun == 3) {
	    		problemSet = RMTF3.getProblem();
	    	}else if(fun == 4) {
	    		problemSet = RMTF4.getProblem();
	    	}else if(fun == 5) {
	    		problemSet = RMTF5.getProblem();
	    	}else if(fun == 6) {
	    		problemSet = RMTF6.getProblem();
	    	}else if(fun == 7) {
	    		problemSet = RMTF7.getProblem();
	    	}else if(fun == 8) {
	    		problemSet = LSMOP8.getProblem();
	    	}else if(fun == 9) {
	    		problemSet = LSMOP9.getProblem();
	    	}else if(fun == 10) {
	    		problemSet = LSMOPV1.getProblem();
	    	}else if(fun == 11) {
	    		problemSet = LSMOPV2.getProblem();
	    	}else if(fun == 12) {
	    		problemSet = LSMOPV3.getProblem();
	    	}else if(fun == 13) {
	    		problemSet = LSMOPV4.getProblem();
	    	}else if(fun == 14) {
	    		problemSet = LSMOPV5.getProblem();
	    	}else if(fun == 15) {
	    		problemSet = LSMOPV6.getProblem();
	    	}else{
	    		problemSet = MATP1.getProblem();
	    	}
	    	utils_ = new etmo.qualityIndicator.util.MetricsUtil();
	    	int taskSize = problemSet.size();
	    	for(int i=0;i<taskSize;i++){
	    		int numObj = problemSet.get(i).getNumberOfObjectives();
		    	int numVar = problemSet.get(i).getNumberOfVariables();
		    	
		    	
		    	String[] algorithms = new String[16];
		    	algorithms[0] = "EMTET";
				algorithms[1] = "MOMFEA";
				algorithms[2] = "MOMFEAII";
				algorithms[3] = "MFEAAKT";
				algorithms[4] = "DRNEA";
				algorithms[5] = "NSGAII";
				algorithms[6] = "LMEA";
				algorithms[7] = "WOF";
				algorithms[8] = "LSMOF";
				algorithms[9] = "LMOCSO";
				algorithms[10] = "MOEAPSL";
				algorithms[11] = "SparseEA";
				String pName = problemSet.get(i).getName();
				//if (fun == 7) pName = "Sparse_PO";
				for(int a=11;a<=11;a++) {
					for(int t=0;t<runtimes;t++) {
						String path = "D:\\ResearchWork\\2021\\2021_P6_ETDAE\\Review1\\RMTF\\DRNEA";
						//String path = "H:\\2021_P6_ETDAE\\Data_Platemo_LSMOPM3";
						path += "\\"+algorithms[a]+"_"+ numObj +"Obj_"+ pName + "_" + numVar +"D_run"+(t+1)+".txt";
						//path += "\\"+algorithms[a]+"_"+ numObj +"Obj_"+ "Sparse_PO" + "_" + numVar +"D_run"+(t+1)+".txt";
						SolutionSet population = utils_.readNonDominatedSolutionSet(path);
						population.printObjectivesToFile("LMEA_"+problemSet.get(i).getNumberOfObjectives()+"Obj_"+
								problemSet.get(i).getName()+ "_" + problemSet.get(i).getNumberOfVariables() + "D_run"+t+".txt");
					}//for times
				}//for algorithm
	    	}//for task
	    }//for function
	}

}
