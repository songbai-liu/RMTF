package etmo.problems.benchmarks_LSMOP;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.staticBase.IO;
import etmo.problems.base.staticBase.MMLSMOP;

public class LSMOP7 {
	public static int m = 3;
	public static ProblemSet getProblem() throws IOException {
		ProblemSet ps1 = getT1();
		ProblemSet ps2 = getT2();
		ProblemSet ps3 = getT3();
		ProblemSet problemSet = new ProblemSet(3);

		problemSet.add(ps1.get(0));
		problemSet.add(ps2.get(0));
		problemSet.add(ps3.get(0));
		return problemSet;

	}
	
	
	public static ProblemSet getT1() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(m, 512);
		if(m == 2){
			prob.setHType("circle"); //Shape Function
		}else{
			prob.setHType("sphere"); //Shape Function
		}
		prob.setGType("lsmop7"); //Landscape Function

		((Problem)prob).setName("LSMOP7_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(m, 1024);
		if(m == 2){
			prob.setHType("circle"); //Shape Function
		}else{
			prob.setHType("sphere"); //Shape Function
		}
		prob.setGType("lsmop7"); //Landscape Function

		((Problem)prob).setName("LSMOP7_2");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT3() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(m, 2048);
		if(m == 2){
			prob.setHType("circle"); //Shape Function
		}else{
			prob.setHType("sphere"); //Shape Function
		}
		prob.setGType("lsmop7"); //Landscape Function

		((Problem)prob).setName("LSMOP7_3");
		
		problemSet.add(prob);
		
		return problemSet;
	}
}
