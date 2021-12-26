package etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2017;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.*;
import etmo.problems.base.staticBase.IO;
import etmo.problems.base.staticBase.MMDTLZ;
import etmo.problems.base.staticBase.MMZDT;


public class NILS {
	
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
		
		MMDTLZ prob = new MMDTLZ(3, 25, 1, -50,50);
		prob.setGType("F176");
		
		double[] shiftValues = IO.readShiftValuesFromFile("MData/CEC2017/benchmark_9/S_NILS_1.txt");
		prob.setShiftValues(shiftValues);
	    ((Problem)prob).setName("NILS_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMZDT prob = new MMZDT(50, 2, -100,100);
		prob.setGType("F175");
		prob.setHType("concave");
		
		((Problem)prob).setName("NILS_2");
		
		problemSet.add(prob);
		return problemSet;
	}
}
