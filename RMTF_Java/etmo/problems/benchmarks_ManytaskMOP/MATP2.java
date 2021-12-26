package etmo.problems.benchmarks_ManytaskMOP;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.*;
import etmo.problems.base.staticBase.IO;
import etmo.problems.base.staticBase.MMDTLZ;


public class MATP2 {
	
	public static ProblemSet getProblem() throws IOException {
		
		int taskNumber=6;
		
		ProblemSet problemSet = new ProblemSet(taskNumber);
		
		for(int i=0;i<taskNumber;i++)
			problemSet.add(getT(i).get(0));
		
		return problemSet;

	}
	
	
	public static ProblemSet getT(int taskID) throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMDTLZ prob = new MMDTLZ(2, 50, 1, -50,50);
		prob.setGType("FM2");
				
		
		double[][] matrix = IO.readMatrixFromFile("MData/ManyTask/benchmark_2/matrix_"+(taskID+1));
		
		double shiftValues[] = IO.readShiftValuesFromFile("MData/ManyTask/benchmark_2/bias_"+(taskID+1));
		
		prob.setRotationMatrix(matrix);
		prob.setShiftValues(shiftValues);		
		
		((Problem)prob).setName("MATP2_"+(taskID+1));
		
		problemSet.add(prob);
		
		return problemSet;
	}
		
}
