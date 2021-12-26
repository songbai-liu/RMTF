package etmo.problems.benchmarks_LSMOP_V1;

import java.io.IOException;

import etmo.core.Problem;
import etmo.core.ProblemSet;
import etmo.problems.base.staticBase.IO;
import etmo.problems.base.staticBase.MMLSMOP;

public class LSMOPV4 {

	public static ProblemSet getProblem() throws IOException {
		ProblemSet ps1 = getT1();
		ProblemSet ps2 = getT2();
		ProblemSet ps3 = getT3();
		ProblemSet ps4 = getT4();
		ProblemSet ps5 = getT5();
		ProblemSet ps6 = getT6();
		ProblemSet problemSet = new ProblemSet(6);

		problemSet.add(ps1.get(0));
		problemSet.add(ps2.get(0));
		problemSet.add(ps3.get(0));
		problemSet.add(ps4.get(0));
		problemSet.add(ps5.get(0));
		problemSet.add(ps6.get(0));
		return problemSet;

	}
	
	public static ProblemSet getT1() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(3, 512);
		prob.setHType("lineoid"); //Shape Function
		prob.setGType("lsmop1"); //Landscape Function

		((Problem)prob).setName("LSMOPV4_1");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT2() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(3, 512);
		prob.setHType("lineoid"); //Shape Function
		prob.setGType("lsmop2"); //Landscape Function

		((Problem)prob).setName("LSMOPV4_2");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT3() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(3, 512);
		prob.setHType("lineoid"); //Shape Function
		prob.setGType("lsmop4"); //Landscape Function

		((Problem)prob).setName("LSMOPV4_3");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT4() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(3, 512);
		prob.setHType("sphere"); //Shape Function
		prob.setGType("lsmop5"); //Landscape Function

		((Problem)prob).setName("LSMOPV4_4");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT5() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(3, 512);
		prob.setHType("sphere"); //Shape Function
		prob.setGType("lsmop8"); //Landscape Function

		((Problem)prob).setName("LSMOPV4_5");
		
		problemSet.add(prob);
		
		return problemSet;
	}
	
	public static ProblemSet getT6() throws IOException {
		ProblemSet problemSet = new ProblemSet(1);
		
		MMLSMOP prob = new MMLSMOP(3, 512);
		prob.setHType("disconnect"); //Shape Function
		prob.setGType("lsmop9"); //Landscape Function

		((Problem)prob).setName("LSMOPV4_6");
		
		problemSet.add(prob);
		
		return problemSet;
	}
}
