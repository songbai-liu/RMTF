package etmo.problems.benchmarks_ManytaskMOP;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.*;
import etmo.problems.base.staticBase.IO;
import etmo.problems.base.staticBase.MMZDT;


public class MATP8 {
	
	public static ProblemSet getProblem() throws IOException {
		
		int taskNumber=10;
		
		ProblemSet problemSet = new ProblemSet(taskNumber);
		
		for(int i=0;i<taskNumber;i++)
			problemSet.add(getT(i).get(0));
		
		return problemSet;

	}
	
	
	public static ProblemSet getT(int taskID) throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		MMZDT prob;
		
		switch(taskID%5){
		case 0:
			prob = new MMZDT(50, 1,  -100,100);
			prob.setGType("FM1");
			break;
		case 1:
			prob = new MMZDT(50, 1,  -50,50);
			prob.setGType("FM4");
			break;
		case 2:
			prob = new MMZDT(50, 1,  -50,50);
			prob.setGType("FM5");
			break;		
		case 3:
			prob = new MMZDT(50, 1,  -50,50);
			prob.setGType("FM2");
			break;
		case 4:
			prob = new MMZDT(50, 1, -0.5, 0.5);
			prob.setGType("FM6");
			break;					
		default:
			prob = new MMZDT(50, 1, -100,100);
		}   
		prob.setHType("convex");
				
		
		double[][] matrix = IO.readMatrixFromFile("MData/ManyTask/benchmark_8/matrix_"+(taskID+1));
		
		double shiftValues[] = IO.readShiftValuesFromFile("MData/ManyTask/benchmark_8/bias_"+(taskID+1));
		
		prob.setRotationMatrix(matrix);
		prob.setShiftValues(shiftValues);		
		
		((Problem)prob).setName("MATP8_"+(taskID+1));
		
		problemSet.add(prob);
		
		return problemSet;
	}
		
}
