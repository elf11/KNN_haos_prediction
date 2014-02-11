package knn;

import java.util.Vector;


public class NDimVec {
	public Vector<Double> dataSet;
	public double aTol;
	public double rTol;
	
	/*
	 * Cream un vector de vectori
	 * 
	 */
	public NDimVec(Vector<Double> data, int start, int tDly, int maxDim, double rTol, double aTol) {
		this.dataSet = new Vector<Double>();
		this.aTol = aTol;
		this.rTol = rTol;
		
		if (tDly == 1) {
			for (int i = start; i < start + maxDim; i++) {
				this.dataSet.add(data.get(i));
			}
		} else {
			for (int i = start; i < start + maxDim * tDly + 1; i += tDly) {
				this.dataSet.add(data.get(i));
			}
		}
	}

}
