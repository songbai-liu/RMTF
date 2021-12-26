package etmo.problems.base.staticBase;

import etmo.core.Problem;
import etmo.core.Solution;
import etmo.util.JMException;

public class MMLSMOP extends Problem {
	String gType_;
	String genType_;
	String linkageType_;
	
    int K;//Number of position-related variables
    int L;//Number of distance-related variables
    int nk;//Number of subcomponents in each variable group
    int[] sublen;//Number of Variables in each subcomponent
    int[] len;//Cumulative sum of lengths of variable groups
    double[] c;
   

	public MMLSMOP() {
		numberOfObjectives_ = 2;
	}

	public MMLSMOP(int numberOfObjectives, int numberOfVariables) {
		numberOfObjectives_ = numberOfObjectives;
		numberOfVariables_ = numberOfVariables;
	
		gType_ = "sphere";
		hType_ = "sphere";
		linkageType_ = "linear";
		
		K = numberOfObjectives - 1;;
		L = numberOfVariables_ - K;
		nk = 5;
		
		c = new double[numberOfObjectives_];
		c[0] = 3.8*0.1*(1.0-0.1);
		double sumC = c[0];
		for(int i = 1; i < numberOfObjectives_; i++){
			c[i] = 3.8*c[i-1]*(1.0-c[i-1]);
			sumC += c[i];
		}
		sublen = new int[numberOfObjectives_];
		for(int i = 0; i < numberOfObjectives_; i++){
			sublen[i] = (int)Math.floor((numberOfVariables_-numberOfObjectives_+1)*c[i]/sumC/nk);
		}
		len = new int[numberOfObjectives_+1]; 
		
		len[0] = 0;
		for(int i = 0; i < numberOfObjectives_; i++){
			len[i+1] = len[i] + nk*sublen[i];
		}
		
		upperLimit_ = new double[numberOfVariables_];
		lowerLimit_ = new double[numberOfVariables_];

		for (int var = 0; var < K; var++) {
			lowerLimit_[var] = 0.0;
			upperLimit_[var] = 1.0;
		} // for
		for (int var = K; var < numberOfVariables; var++) {
			lowerLimit_[var] = 0.0;
			upperLimit_[var] = 10.0;
		}

		shiftValues_ = new double[L];
		for (int i = 0; i < shiftValues_.length; i++)
			shiftValues_[i] = 0;

		rotationMatrix_ = new double[L][L];
		for (int i = 0; i < rotationMatrix_.length; i++) {
			for (int j = 0; j < rotationMatrix_.length; j++) {
				if (i != j)
					rotationMatrix_[i][j] = 0;
				else
					rotationMatrix_[i][j] = 1;
			}
		}
	}

	@Override
	public void evaluate(Solution solution) throws JMException {
		double vars[] = scaleVariables(solution);
		int dim = vars.length;
		double[] x = new double[dim];
		double[] f = new double[numberOfObjectives_];
		double[] xI = new double[numberOfObjectives_ - 1];
		for (int i = 0; i < numberOfObjectives_-1; i++) {
			x[i] = vars[i];
			xI[i] = x[i];
		}
		
		if(linkageType_.equalsIgnoreCase("linear")) {
			for(int i = numberOfObjectives_-1; i < dim; i++){
				x[i] = (1.0 + (i+1.0)/(dim))*vars[i] - 10.0*vars[0];
			}
		}else if(linkageType_.equalsIgnoreCase("nonlinear")) {
			for(int i = numberOfObjectives_-1; i < dim; i++){
				x[i] = (1.0 + Math.cos(0.5*Math.PI*((i+1.0)/(dim))))*vars[i] - 10.0*vars[0];
			}
		}else {
			System.out.println("Error: linkage type " + linkageType_ + " invalid");
			System.exit(0);
		}
		
		double[] gx = new double[numberOfObjectives_];
		for (int i = 0; i < numberOfObjectives_; i++) {
			gx[i] = 0.0;
			if(i%2 == 0){
				if(gType_.equalsIgnoreCase("lsmop1")) {
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<(len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1);t++){
							cg+=x[t]*x[t];
						}
						cg = cg/sublen[i];
						gx[i] += cg;
					}
				}else if(gType_.equalsIgnoreCase("lsmop2")) {
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						double cs = 1.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg += (x[t]*x[t])/4000;
							cs *= Math.cos(x[t]/Math.sqrt(t+1));
						}
						gx[i] += (cg - cs + 1)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop3")) {
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg += x[t]*x[t] + 10 - 10*Math.cos(2*Math.PI*x[t]);
						}
						gx[i] += cg/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop4")) {
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						double cs = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg += x[t]*x[t];
							cs += Math.cos(2*Math.PI*x[t]);
						}
						cg = -20*Math.exp(-0.2*Math.sqrt(cg/sublen[i]));
						cs = -1.0*Math.exp(cs/sublen[i]);
						gx[i] += (cg+cs+20+Math.E)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop5")){
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg+=x[t]*x[t];
						}
						cg = cg/sublen[i];
						gx[i] += cg;
					}
				}else if(gType_.equalsIgnoreCase("lsmop6")) {
					for(int j=0;j<nk;j++){
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						double cg = 0.0;
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-2;t++){
						   cg += 100*Math.pow((x[t]*x[t]-x[t+1]), 2) + (x[t]-1)*(x[t]-1);
						}
						gx[i] += (cg)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop7")){
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						double cs = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg += x[t]*x[t];
							cs += Math.cos(2*Math.PI*x[t]);
						}
						cg = -20*Math.exp(-0.2*Math.sqrt(cg/sublen[i]));
						cs = -1.0*Math.exp(cs/sublen[i]);
						gx[i] += (cg+cs+20+Math.E)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop8")){
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						double cs = 1.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg += (x[t]*x[t])/4000;
							cs *= Math.cos(x[t]/Math.sqrt(t+1));
						}
						gx[i] += (cg - cs + 1)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop9")){
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg+=x[t]*x[t];
						}
						cg = cg/sublen[i];
						gx[i] += cg;
					}
				}
			}else{
				if(gType_.equalsIgnoreCase("lsmop1")) {
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg+=x[t]*x[t];
						}
						cg = cg/sublen[i];
						gx[i] += cg;
					}
				}else if(gType_.equalsIgnoreCase("lsmop2")) {
					for(int j=0;j<nk;j++){
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						double maxg = x[t];
						for(t=t+1;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							if(maxg < x[t]){
								maxg = x[t];
							}
						}
						gx[i] += (maxg)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop3")) {
					for(int j=0;j<nk;j++){
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						double cg = 0.0;
						for(;t<(len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-2);t++){
						   cg += 100*Math.pow((x[t]*x[t]-x[t+1]), 2) + (x[t]-1)*(x[t]-1);
						}
						gx[i] += (cg)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop4")) {
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						double cs = 1.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg += (x[t]*x[t])/4000;
							cs *= Math.cos(x[t]/Math.sqrt(t+1));
						}
						gx[i] += (cg - cs + 1)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop5")) {
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg+=x[t]*x[t];
						}
						cg = cg/sublen[i];
						gx[i] += cg;
					}
				}else if(gType_.equalsIgnoreCase("lsmop6")){
					for(int j=0;j<nk;j++){
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						double maxg = x[t];
						for(t=t+1;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							if(maxg < x[t]){
								maxg = x[t];
							}
						}
						gx[i] += (maxg)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop7")){
					for(int j=0;j<nk;j++){
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						double cg = 0.0;
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-2;t++){
						   cg += 100*Math.pow((x[t]*x[t]-x[t+1]), 2) + (x[t]-1)*(x[t]-1);
						}
						gx[i] += (cg)/sublen[i];
					}
				}else if(gType_.equalsIgnoreCase("lsmop8")){
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg+=x[t]*x[t];
						}
						cg = cg/sublen[i];
						gx[i] += cg;
					}
				}else if(gType_.equalsIgnoreCase("lsmop9")){
					for(int j=0;j<nk;j++){
						double cg = 0.0;
						double cs = 0.0;
						int t = len[i]+numberOfObjectives_-1+j*sublen[i];
						for(;t<len[i]+numberOfObjectives_-1+(j+1)*sublen[i]-1;t++){
							cg += x[t]*x[t];
							cs += Math.cos(2*Math.PI*x[t]);
						}
						cg = -20*Math.exp(-0.2*Math.sqrt(cg/sublen[i]));
						cs = -1.0*Math.exp(cs/sublen[i]);
						gx[i] += (cg+cs+20+Math.E)/sublen[i];
					}
				}
			}
			gx[i] = gx[i]/nk;
		}
		
		double[] hx = evalH(xI,gx); 
		
		if(gType_.equalsIgnoreCase("lsmop1") || gType_.equalsIgnoreCase("lsmop2") || gType_.equalsIgnoreCase("lsmop3") || gType_.equalsIgnoreCase("lsmop4")) {
			for (int i = 0; i < numberOfObjectives_; i++) {
				f[i] = hx[i]*(1+gx[i]);
			}// for
		}else if(gType_.equalsIgnoreCase("lsmop5") || gType_.equalsIgnoreCase("lsmop6") || gType_.equalsIgnoreCase("lsmop7") || gType_.equalsIgnoreCase("lsmop8")) {
			for (int i = 0; i < numberOfObjectives_; i++) {
				if(i != numberOfObjectives_-1){
					f[i] = hx[i]*(1+gx[i]+gx[i+1]);
				}else{
					f[i] = hx[i]*(1+gx[i]);
				}
			}// for
		}else if(gType_.equalsIgnoreCase("lsmop9")) {
			// Calculate the value of f1,f2,f3,...,fM-1 (take acount of vectors
			// start at 0)
			System.arraycopy(x, 0, f, 0, numberOfObjectives_ - 1);

			double hx1 = 0.0;
			double hx2 = 0.0;
			for(int i=0;i<numberOfObjectives_;i++){
				hx1 += gx[i];
			}
			hx1 += 2;
			for(int i=0;i<numberOfObjectives_-1;i++){
				hx2 += (x[i]*(1+Math.sin(3*Math.PI*x[i])))/(hx1);
			}
			hx2 = numberOfObjectives_ - hx2;
			f[numberOfObjectives_ - 1] = (hx1) * hx2;
		}

		for (int i = 0; i < numberOfObjectives_; i++)
			solution.setObjective(startObjPos_ + i,f[i]);
	}

	

	double[] evalH(double[] xI, double[] gx) {
		double[] h = new double[numberOfObjectives_];
		if (hType_.equalsIgnoreCase("lineoid")){
			for (int i = 0; i < numberOfObjectives_; i++) {
				h[i] = 1.0;
				for (int j = 0; j < numberOfObjectives_ - (i + 1); j++)
					h[i] *= xI[j];
				if (i != 0) {
					int aux = numberOfObjectives_ - (i + 1);
					h[i] *= (1 - xI[aux]);
				} // if
			} // for
		}else if(hType_.equalsIgnoreCase("circle" ) || hType_.equalsIgnoreCase("sphere")){
			for (int i = 0; i < numberOfObjectives_; i++) {
				h[i] = 1.0;
				for (int j = 0; j < numberOfObjectives_ - (i + 1); j++)
					h[i] *= Math.cos(xI[j] * 0.5 * Math.PI);
				if (i != 0) {
					int aux = numberOfObjectives_ - (i + 1);
					h[i] *= Math.sin(xI[aux] * 0.5 * Math.PI);
				} // if
			} // for
		}else if(hType_.equalsIgnoreCase("disconnect")){
			h[numberOfObjectives_-1] = 0;
			for (int i = 0; i < numberOfObjectives_-1; i++) {
				h[i] = xI[i]/(1+gx[i]);
				h[numberOfObjectives_-1] += (xI[i]*(1 + Math.sin(3*Math.PI*xI[i])))/(2+gx[numberOfObjectives_-1]);
			} // for
			h[numberOfObjectives_-1] = (numberOfObjectives_ - h[numberOfObjectives_-1])*(2+gx[numberOfObjectives_-1])/(1+gx[numberOfObjectives_-1]);
		}else {
			System.out.println("Error: Shape function type " + hType_ + " invalid");
			System.exit(0);
		}
		return h;
	}

	public void setGType(String gType) {
		gType_ = gType;
	}
	
	public void setLinkageType(String gType) {
		linkageType_ = gType;
	}


	@Override
	public void dynamicEvaluate(Solution solution, int currentGeneration) throws JMException {
		
	}
}
