package etmo.problems.benchmarks_MultiTaskMOP.mtmop_CEC2019;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.*;
import etmo.problems.base.staticBase.MMLZ;


public class CPLX3 {
	
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
		
		MMLZ prob = new MMLZ(2, 256, -1.0,1.0);
		prob.setGType("F192");
		prob.setHType("convex");
			
		((Problem)prob).setName("CPLX3_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLZ prob = new MMLZ(2, 256, -1.0,1.0);
		prob.setGType("F196");
		prob.setHType("convex");

		
		((Problem)prob).setName("CPLX3_2");
		
		problemSet.add(prob);
		return problemSet;
	}
}
