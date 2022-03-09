package etmo.metaheuristics.drnea.BaseModel;

public interface IbpBase {//Framework of a deep neural network (DNN)
	public abstract void init(int[] numberOfLayer,double r);//initialization of the parameters
	public abstract double[] computeOut(double[] inp);//Calculate the output layer by layer
	public abstract void backPropagation(double[] target);//Reverse the error calculation and update the weights
}
