package etmo.metaheuristics;

import java.io.*;
import java.util.logging.*;

import etmo.core.ProblemSet;
import etmo.problems.benchmarks_ETMO.*;
import etmo.problems.benchmarks_LSMOP.*;
import etmo.problems.benchmarks_ManytaskMOP.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.Configuration;
import etmo.util.JMException;

public class computeIGD_matp {
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
	    for(int fun=1;fun<=10;fun++){
	    	double[] IGDarray=new double[runtimes];
	    	if(fun == 1) {
	    		problemSet = MATP1.getProblem();
	    	}else if(fun == 2) {
	    		problemSet = MATP2.getProblem();
	    	}else if(fun == 3) {
	    		problemSet = MATP3.getProblem();
	    	}else if(fun == 4) {
	    		problemSet = MATP4.getProblem();
	    	}else if(fun == 5) {
	    		problemSet = MATP5.getProblem();
	    	}else if(fun == 6) {
	    		problemSet = MATP6.getProblem();
	    	}else if(fun == 7) {
	    		problemSet = MATP7.getProblem();
	    	}else if(fun == 8) {
	    		problemSet = MATP8.getProblem();
	    	}else if(fun == 9) {
	    		problemSet = MATP9.getProblem();
	    	}else if(fun == 10) {
	    		problemSet = MATP10.getProblem();
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
				algorithms[6] = "LMDAA2";
				algorithms[7] = "LMDAA2";
				algorithms[8] = "LMDAA3";
				algorithms[9] = "LMDAA4";
				algorithms[10] = "MLMOEA5";
				algorithms[11] = "LMEA";
				algorithms[12] = "WOF";
				algorithms[13] = "LSMOF";
				algorithms[14] = "LMOCSO";
				algorithms[15] = "MOEAPSL";
				String pName = problemSet.get(i).getName();
				for(int a=8;a<=8;a++) {//LSMOP1_1
					for(int t=0;t<runtimes;t++) {
						String path = "D:\\CityU\\2021\\2021_P6_ETDAE\\Data_Jmetal_MATP";
						path += "\\"+algorithms[a]+"_"+ numObj +"Obj_"+ pName + "_" + numVar +"D_run"+(t+1)+".txt";
						double[][] population = utils_.readFront(path);
						IGDarray[t] = indicators.getIGD1(population);
					}//for times
					
					//if(a < 11 || a > 15){
						//pName = pName.replace(x, "");
					//}
					printGD(algorithms[a]+"_"+numObj+"Obj_"+pName+ "_" + numVar + "D_IGD.txt",IGDarray);
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
