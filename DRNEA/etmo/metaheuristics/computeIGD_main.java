package etmo.metaheuristics;

import java.io.*;
import java.util.logging.*;

import etmo.core.ProblemSet;
import etmo.problems.benchmarks_ETMO.*;
import etmo.problems.benchmarks_LSMOP.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017.*;
import etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.Configuration;
import etmo.util.JMException;

public class computeIGD_main {
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
	    for(int fun=52;fun<=69;fun++){
	    	double[] IGDarray=new double[runtimes];
	    	if(fun == 1) {
	    		problemSet = CPLX1.getT1();
	    	}else if(fun == 2) {
	    		problemSet = CPLX1.getT2();
	    	}else if(fun == 3) {
	    		problemSet = CPLX2.getT1();
	    	}else if(fun == 4) {
	    		problemSet = CPLX2.getT2();
	    	}else if(fun == 5) {
	    		problemSet = CPLX3.getT1();
	    	}else if(fun == 6) {
	    		problemSet = CPLX3.getT2();
	    	}else if(fun == 7) {
	    		problemSet = CPLX4.getT1();
	    	}else if(fun == 8) {
	    		problemSet = CPLX4.getT2();
	    	}else if(fun == 9) {
	    		problemSet = CPLX5.getT1();
	    	}else if(fun == 10) {
	    		problemSet = CPLX5.getT2();
	    	}else if(fun == 11) {
	    		problemSet = CPLX6.getT1();
	    	}else if(fun == 12) {
	    		problemSet = CPLX6.getT2();
	    	}else if(fun == 13) {
	    		problemSet = CPLX7.getT1();
	    	}else if(fun == 14) {
	    		problemSet = CPLX7.getT2();
	    	}else if(fun == 15) {
	    		problemSet = CPLX8.getT1();
	    	}else if(fun == 16) {
	    		problemSet = CPLX8.getT2();
	    	}else if(fun == 17) {
	    		problemSet = CPLX9.getT1();
	    	}else if(fun == 18) {
	    		problemSet = CPLX9.getT2();
	    	}else if(fun == 19) {
	    		problemSet = CPLX10.getT1();
	    	}else if(fun == 20) {
	    		problemSet = CPLX10.getT2();
	    	}else if(fun == 21) {
	    		problemSet = ETMOF17.getT1();
	    	}else if(fun == 22) {
	    		problemSet = ETMOF17.getT2();
	    	}else if(fun == 23) {
	    		problemSet = ETMOF18.getT1();
	    	}else if(fun == 24) {
	    		problemSet = ETMOF18.getT2();
	    	}else if(fun == 25) {
	    		problemSet = ETMOF19.getT1();
	    	}else if(fun == 26) {
	    		problemSet = ETMOF19.getT2();
	    	}else if(fun == 27) {
	    		problemSet = ETMOF20.getT1();
	    	}else if(fun == 28) {
	    		problemSet = ETMOF20.getT2();
	    	}else if(fun == 29) {
	    		problemSet = ETMOF20.getT3();
	    	}else if(fun == 30) {
	    		problemSet = ETMOF21.getT1();
	    	}else if(fun == 31) {
	    		problemSet = ETMOF21.getT2();
	    	}else if(fun == 32) {
	    		problemSet = ETMOF21.getT3();
	    	}else if(fun == 33) {
	    		problemSet = ETMOF22.getT1();
	    	}else if(fun == 34) {
	    		problemSet = ETMOF22.getT2();
	    	}else if(fun == 35) {
	    		problemSet = ETMOF22.getT3();
	    	}else if(fun == 36) {
	    		problemSet = ETMOF23.getT1();
	    	}else if(fun == 37) {
	    		problemSet = ETMOF23.getT2();
	    	}else if(fun == 38) {
	    		problemSet = ETMOF24.getT1();
	    	}else if(fun == 39) {
	    		problemSet = ETMOF24.getT2();
	    	}else if(fun == 40) {
	    		problemSet = LSMOP1.getT1();
	    	}else if(fun == 41) {
	    		problemSet = LSMOP1.getT2();
	    	}else if(fun == 42) {
	    		problemSet = LSMOP1.getT3();
	    	}else if(fun == 43) {
	    		problemSet = LSMOP5.getT1();
	    	}else if(fun == 44) {
	    		problemSet = LSMOP5.getT2();
	    	}else if(fun == 45) {
	    		problemSet = LSMOP5.getT3();
	    	}else if(fun == 46) {
	    		problemSet = LSMOP8.getT1();
	    	}else if(fun == 47) {
	    		problemSet = LSMOP8.getT2();
	    	}else if(fun == 48) {
	    		problemSet = LSMOP8.getT3();
	    	}else if(fun == 49) {
	    		problemSet = LSMOP9.getT1();
	    	}else if(fun == 50) {
	    		problemSet = LSMOP9.getT2();
	    	}else if(fun == 51) {
	    		problemSet = LSMOP9.getT3();
	    	}else if(fun == 52) {
	    		problemSet = CIHS.getT1();
	    	}else if(fun == 53) {
	    		problemSet = CIHS.getT2();
	    	}else if(fun == 54) {
	    		problemSet = CILS.getT1();
	    	}else if(fun == 55) {
	    		problemSet = CILS.getT2();
	    	}else if(fun == 56) {
	    		problemSet = CIMS.getT1();
	    	}else if(fun == 57) {
	    		problemSet = CIMS.getT2();
	    	}else if(fun == 58) {
	    		problemSet = NIHS.getT1();
	    	}else if(fun == 59) {
	    		problemSet = NIHS.getT2();
	    	}else if(fun == 60) {
	    		problemSet = NILS.getT1();
	    	}else if(fun == 61) {
	    		problemSet = NILS.getT2();
	    	}else if(fun == 62) {
	    		problemSet = NIMS.getT1();
	    	}else if(fun == 63) {
	    		problemSet = NIMS.getT2();
	    	}else if(fun == 64) {
	    		problemSet = PIHS.getT1();
	    	}else if(fun == 65) {
	    		problemSet = PIHS.getT2();
	    	}else if(fun == 66) {
	    		problemSet = PILS.getT1();
	    	}else if(fun == 67) {
	    		problemSet = PILS.getT2();
	    	}else if(fun == 68) {
	    		problemSet = PIMS.getT1();
	    	}else if(fun == 69) {
	    		problemSet = PIMS.getT2();
	    	}else{
	    		problemSet = CIHS.getT1();
	    	}
	    	
	    	int numObj = problemSet.get(0).getNumberOfObjectives();
	    	int numVar = problemSet.get(0).getNumberOfVariables();
	    	
	    	
	    	String pf = "PF/StaticPF/" + problemSet.get(0).getHType() + "_" + numObj + "D.pf";
	    	indicators = new QualityIndicator(problemSet.get(0),pf);
	    	utils_ = new etmo.qualityIndicator.util.MetricsUtil();
	    	String[] algorithms = new String[16];
	    	algorithms[0] = "EMTET";
			algorithms[1] = "MOMFEA";
			algorithms[2] = "MOMFEAII";
			algorithms[3] = "MFEAAKT";
			algorithms[4] = "NSGAII";
			algorithms[5] = "MOEAPLS";
			algorithms[6] = "LMDAA0";
			algorithms[7] = "LMDAA1";
			algorithms[8] = "LMDAA2";
			algorithms[9] = "LMDAA3";
			algorithms[10] = "DRNET";
			algorithms[11] = "LMEA";
			algorithms[12] = "WOF";
			algorithms[13] = "LSMOF";
			algorithms[14] = "LMOCSO";
			algorithms[15] = "MOEAPSL";
			
			String pType;
			if(fun <= 20){
				pType = "OnCPLX";
			}else if(fun <= 39){
				pType = "OnETOMF";
			}else if(fun <= 51){
				pType = "OnLSMOP";
			}else{
				pType = "Other";
			}
			
			for(int a=10;a<=10;a++) {//LSMOP1_1
				String pName = problemSet.get(0).getName();
				//String x = pName.substring(6, 8);
				//if(a >= 11 && a <= 15){//LSMOP1_1
					//pName = pName.replace(x, "");
				//}
				
				for(int t=0;t<runtimes;t++) {
					String path = "D:\\CityU\\2021\\2021_P6_ETDAE\\Data_Jmetal_CEC2017";
					path += "\\"+algorithms[a]+"_"+ numObj +"Obj_"+ pName + "_" + numVar +"D_run"+(t+1)+".txt";
					double[][] population = utils_.readFront(path);
					IGDarray[t] = indicators.getIGD1(population);
				}//for times
				
				//if(a < 11 || a > 15){
					//pName = pName.replace(x, "");
				//}
				printGD(algorithms[a]+"_"+numObj+"Obj_"+pName+ "_" + numVar + "D_IGD.txt",IGDarray);
				double sumIGD = 0;
				for(int i=0;i<runtimes;i++){
				  sumIGD+=IGDarray[i];
				}
				System.out.println("avrIGD-fun"+fun+" = " + sumIGD/runtimes);
			}//for algorithm
	    }//for function
	}
	

}
