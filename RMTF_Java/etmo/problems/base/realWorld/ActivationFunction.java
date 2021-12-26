package etmo.problems.base.realWorld;

public class ActivationFunction {
	
	public static double sigmoid(double z) {//Logistic, sigmoid, or soft step
		double f = 0;
		f = 1.0/(1+Math.exp(-z));
		return f;
	}
	
	public static double tanh(double z) {//Hyperbolic tangent (tanh)
		double f = 0;
		f = (Math.exp(z) - Math.exp(-z))/(Math.exp(z) + Math.exp(-z));
		return f;
	}
	
	public static double gaussian(double z) {
		double f = 0;
		f = Math.exp(-z*z);
		return f;
	}
	
	public static double softSign(double z) {
		double f = 0;
		f = z/(1+Math.abs(z));
		return f;
	}
	
	public static double decayingSineUnit(double z) {
		double f = 0;
		double sinc1 = 1.0;
		if(z-Math.PI == 0) sinc1 = 1.0;
		else sinc1 = Math.sin(z-Math.PI)/(z-Math.PI);
		
		double sinc2 = 1.0;
		if(z+Math.PI == 0) sinc2 = 1.0;
		else sinc2 = Math.sin(z+Math.PI)/(z+Math.PI);
		
		f = 0.5*Math.PI*(sinc1 - sinc2);
		
		return f;
	}

}
