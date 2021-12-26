package etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.*;
import etmo.problems.base.staticBase.IO;
import etmo.problems.base.staticBase.MMDTLZ;
import etmo.problems.base.staticBase.MMZDT;


public class PIMS {
	
	public static ProblemSet getProblem() throws IOException {
		ProblemSet ps1 = getT1();
		ProblemSet ps2 = getT2();
		ProblemSet problemSet = new ProblemSet(2);

		problemSet.add(ps1.get(0));
		problemSet.add(ps2.get(0));
		return problemSet;

	}
	
	public static ProblemSet getT(int tsk) throws IOException {
		if(tsk == 0){
			return getT1();
		}else{
			return getT2();
		}
		
	}
	
	
	public static ProblemSet getT1() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMDTLZ prob = new MMDTLZ(2, 50, 1, 0,1);
		prob.setGType("F171");
		
		double[] shiftValues = IO.readShiftValuesFromFile("MData/CEC2017/benchmark_5/S_PIMS_1.txt");
		prob.setShiftValues(shiftValues);
		double[][] matrix = IO.readMatrixFromFile("MData/CEC2017/benchmark_5/M_PIMS_1.txt");
		prob.setRotationMatrix(matrix);	
		
	    ((Problem)prob).setName("PIMS_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMZDT prob = new MMZDT(50, 1, 0,1);
		prob.setGType("F174");
		prob.setHType("concave");
		
		double[][] matrix = IO.readMatrixFromFile("MData/CEC2017/benchmark_5/M_PIMS_2.txt");
		prob.setRotationMatrix(matrix);	
		
		((Problem)prob).setName("PIMS_2");
		
		problemSet.add(prob);
		return problemSet;
	}
}
