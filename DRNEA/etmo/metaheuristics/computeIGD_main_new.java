package etmo.metaheuristics;

import java.io.*;
import java.util.logging.*;

import etmo.core.ProblemSet;
import etmo.problems.benchmarks_ETMO.*;
import etmo.problems.benchmarks_LSMOP.*;
import etmo.problems.benchmarks_LSMOP_V1.*;
import etmo.problems.benchmarks_ManytaskMOP.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.Configuration;
import etmo.util.JMException;

public class computeIGD_main_new {
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
		QualityIndicator indicators; // Object to get quality indicators
		int runtimes=20;
	    for(int fun=1;fun<=9;fun++){
	    	double[] IGDarray=new double[runtimes];
	    	if(fun == 1) {
	    		problemSet = LSMOP1.getProblem();
	    	}else if(fun == 2) {
	    		problemSet = LSMOP2.getProblem();
	    	}else if(fun == 3) {
	    		problemSet = LSMOP3.getProblem();
	    	}else if(fun == 4) {
	    		problemSet = LSMOP4.getProblem();
	    	}else if(fun == 5) {
	    		problemSet = LSMOP5.getProblem();
	    	}else if(fun == 6) {
	    		problemSet = LSMOP6.getProblem();
	    	}else if(fun == 7) {
	    		problemSet = LSMOP7.getProblem();
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
		    	String pf = "PF/StaticPF/" + problemSet.get(i).getHType() + "_" + numObj + "D.pf";
		    	indicators = new QualityIndicator(problemSet.get(i),pf);
		    	
		    	String[] algorithms = new String[16];
		    	algorithms[0] = "EMTET";
				algorithms[1] = "MOMFEA";
				algorithms[2] = "MOMFEAII";
				algorithms[3] = "MFEAAKT";
				algorithms[4] = "NSGAII";
				algorithms[5] = "MOEAPLS";
				algorithms[6] = "DRNET0";
				algorithms[7] = "DRNET";
				algorithms[8] = "DRNET1";
				algorithms[9] = "DRNET2";
				algorithms[10] = "DRNET4";
				algorithms[11] = "MOEADVA";
				algorithms[12] = "WOF";
				algorithms[13] = "LSMOF";
				algorithms[14] = "LMOCSO";
				algorithms[15] = "MOEAPSL";
				String pName = problemSet.get(i).getName();
				//String x = pName.substring(6, 8);
				//pName = pName.replace(x, "");
				for(int a=6;a<=10;a++) {//LSMOP1_1  H:\2021_P6_ETDAE\Data_Jmetal_LSMOP
					for(int t=0;t<runtimes;t++) {
						String path = "H:\\2021_P6_ETDAE\\Data_Jmetal_LSMOPM3";
						//String path = "H:\\2021_P6_ETDAE\\Data_Platemo_LSMOPM3";
						path += "\\"+algorithms[a]+"_"+ numObj +"Obj_"+ pName + "_" + numVar +"D_run"+(t+1)+".txt";
						double[][] population = utils_.readFront(path);
						IGDarray[t] = indicators.getIGD1(population);
					}//for times
					
					printGD(algorithms[a]+"_"+numObj+"Obj_"+problemSet.get(i).getName()+ "_" + numVar + "D_IGD.txt",IGDarray);
					double sumIGD = 0;
					for(int t=0;t<runtimes;t++){
					  sumIGD+=IGDarray[t];
					}
					System.out.println("avrIGD-fun"+fun+" = " + sumIGD/runtimes);
				}//for algorithm
	    	}//for task
	    }//for function
	}
	

}
