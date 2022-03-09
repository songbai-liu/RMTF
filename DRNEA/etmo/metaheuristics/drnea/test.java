package etmo.metaheuristics.drnea;

import java.util.*;

public class test {
	public static void main(String args[]) {
		// Instantiates Random object
		Random ran = new Random();
		//By using nextGuassian() method is to return double Gaussian pseudo-random value with mean 0.0 and SD 1.0 by using Random Value Generator
		for(int i=0;i<100;i++) {
			double val = ran.nextGaussian()*0.1;
		    // Display val
			System.out.println("ran.nextGaussian(): " + val); 
		}
		
	}
}
