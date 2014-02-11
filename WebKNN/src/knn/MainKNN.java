package knn;

import java.util.*;
import java.io.*;

public class MainKNN {
	
	static final double rTol = 10;
	static final double aTol = 2;
	
	/*
	 * Calculul deviatiei standard
	 */
	public static double standardDev(Vector<Double> dataSet) {
		double standardDev = 0.0;
		double sum = 0.0;
		
		for (int i = 0; i < dataSet.size(); i++) {
			sum += dataSet.get(i);
		}
		
		sum /= dataSet.size();
		
		for (int i = 0; i < dataSet.size(); i++) {
			standardDev += Math.pow((dataSet.get(i) - sum), 2);
		}
		
		standardDev /= dataSet.size();
		standardDev = Math.sqrt(standardDev);
		return standardDev;
	}
	
	/*
	 * Calculul distantei euclidien intre vectori de tipul NDimVec
	 * vectori n dimensionali de vectori (adica matrice)
	 * 
	 */
	public static double euclidDistance(NDimVec data1, NDimVec data2, int size) {
		double euclid = 0.0;
		
		for (int i = 0; i < size; i++) {
			euclid += Math.pow((data1.dataSet.get(i) - data2.dataSet.get(i)), 2);
		}
		
		euclid = Math.sqrt(euclid);
		
		return euclid;
	}
	
	/*
	 * Phase Space reconstruction
	 * 
	 * pentru fiecare dimensiune se construiesc vectori de acea dimensiune
	 * reprezentand secvente de elemente aflate pe pozitii consecutive
	 * in seria initiala
	 * 
	 * data - time series
	 * maxDim - embedding dimension
	 * tDly - time delay
	 */
	public static Vector<NDimVec> phaseReconstruction(Vector<Double> data, int maxDim, int tDly) {
		
		Vector<NDimVec> phaseVec = new Vector<NDimVec>();
	
		if (tDly == 1) {
			for (int i = 0; i < data.size() - maxDim; i++) {
				phaseVec.add(new NDimVec(data, i, tDly, maxDim, rTol, aTol));
			}
		} else {
			for (int i = 0; i < data.size() - maxDim * tDly; i++) {
				phaseVec.add(new NDimVec(data, i, tDly, maxDim, rTol, aTol));
			}
		}
		
		return phaseVec;
	}
	
	/*
	 * Calculam minimum embedding dimension pentru acest spatiu vectorial
	 * data - time series
	 * maxDim - maximum embedding dimension
	 * tDly - time delay
	 * rTol -
	 * aTol -
	 */
	public static int embeddingDim(Vector<Double> data, int maxDim, int tDly, double rTol, double aTol) {
		
		int emDim, minPos, minPos2, ind, minFNN, numFNN, size;
		double stdDev, dist, diff, R, minVal, minVal2;
		Vector<NDimVec> phaseSpace; //vectorul de vectori n dimensionali din acest spatiu
		Vector<Double> distances; // vectorul de distante dintre vectorii din spatiul acesta
		
		stdDev = standardDev(data);
//		System.out.println("standard deviation is " + stdDev);
		emDim = maxDim;
		minFNN = Integer.MAX_VALUE; // numarul de false nearest neighbors minim pentru care  aflam embedding dimension
		
		for (int i = 1; i <= maxDim; i++) {
			/*
			 * pentru fiecare i intre 1 si dimensiunea maxima aflam
			 * phase space-ul format din vectori
			 * Pentru fiecare vector din acest phase space aflam distantele euclidiene dintre
			 * vectorul respectiv si ceilalti vectori din spatiu
			 * Selectam 2 cei mai buni vectori (= care au distanta euclidiana minima) si calculam fractia
			 * [(minVal1^2 - minVal2^2)/minVal2^2]^(1/2)
			 */
			phaseSpace = phaseReconstruction(data, i, tDly);
			ind = -1;
			numFNN = 0;
			
			for (NDimVec phaseVec : phaseSpace) {
				ind += 1;
				distances = new Vector<Double>();
				minVal = Double.MAX_VALUE;
				minPos = -1;
				size = phaseVec.dataSet.size();
				
				for (int j = 0; j < phaseSpace.size(); j++) {
					dist = euclidDistance(phaseVec, phaseSpace.get(j), size);
					distances.add(dist);
					if (minVal > dist) {
						minVal = dist;
						minPos = j;
					}
				}
				
				minVal2 = Double.MAX_VALUE;
				minPos2 = -1;
				for (int j = 0; j < distances.size(); j++) {
					if (minVal2 > distances.get(j) && j != minPos) {
						minVal2 = distances.get(j);
						minPos2 = j;
					}
				}
				
				minPos = minPos2;
				diff = Math.abs(data.get(ind + i) - data.get(minPos + i));
				R = Math.sqrt(Math.pow(diff, 2) + Math.pow(minVal2, 2));
				
				if ((diff / minVal2) > rTol || (R / stdDev) > aTol) {
					numFNN += 1;
				}
			}
			
			if (minFNN > numFNN) {
				minFNN = numFNN;
				emDim = i;
			}
		}
		return emDim;
	}
	
	
	//==========================================================
	/*
	 * Metoda de calcul a k nearest neighbours.
	 * Avem o clasa KNeighbours, unde definim vectori N dimensionali
	 * si distantele dintre acestia. Functia getKNeighbours calculeaza
	 * K cei mai apropiati vectori de vectorul vecInit trimis ca parametru.
	 * 
	 */
	public int compareNeighbours(KNeighbours n1, KNeighbours n2) {
		if ((n1.dist - n2.dist) < 0)
			return -1;
		if ((n1.dist - n2.dist) > 0)
			return 1;
		return 0;
	}
	
	public static void sort(Vector<KNeighbours> neighbours) {
	    Collections.sort(neighbours, new Comparator<KNeighbours>() {
	        @Override
	        public int compare(KNeighbours o1, KNeighbours o2) {
	        	return Double.compare(o1.dist, o2.dist);
	        }           
	    });
	}
	
	
	public static Vector<NDimVec> getKNeighbours(NDimVec vecInit, Vector<NDimVec> vecs, int k) {
		
		Vector<NDimVec> kNeighs = new Vector<NDimVec>();
		Vector<KNeighbours> neighbours = new Vector<KNeighbours>();
		double dist;
		int size = vecInit.dataSet.size();
		
		for (NDimVec vecNeigh : vecs) {
			dist = euclidDistance(vecInit, vecNeigh, size);
			neighbours.add(new KNeighbours(vecNeigh, dist));
		}
		
		sort(neighbours);
		
		for (int i = 0; i < k; i++) {
			kNeighs.add(neighbours.get(i).vals);
		}
		
		return kNeighs;
	}
	
	//==========================================================
	/*
	 * Metode de predictie a urmatoarelor valori din serie.
	 * Functia predictNextVal() calculeaza o noua valoare pornind
	 * de la valoarea curenta.
	 * 
	 */
	public static double predictNextVal(Vector<Double> data, int tDly, int maxDim, int k) {
		
		double nextVal = 0.0, dist, invDist, norm = 0.0;
		Vector<Double> invDistances = new Vector<Double>();
		NDimVec curr = new NDimVec(data, (data.size() - tDly * maxDim - 1), tDly, maxDim, rTol, aTol);
		Vector<NDimVec> vecs = phaseReconstruction(data, maxDim + 1, tDly);
		Vector<NDimVec> neighbours = getKNeighbours(curr, vecs, k);
		
		int size = curr.dataSet.size();
		
		for (NDimVec neigh : neighbours) {
			dist = euclidDistance(curr, neigh, size);
			
			if (dist == 0) {
				invDist = Double.MAX_VALUE;
			} else {
				invDist = 1 / dist;
			}
			
			invDistances.add(invDist);
			norm += invDist;
		}
		
		for (int i = 0; i < k; i++) {
			nextVal += invDistances.get(i) * neighbours.get(i).dataSet.get(neighbours.get(i).dataSet.size() - 1);
		}
		
		// normalizam valoarea
		nextVal = nextVal / norm;
		
		return nextVal;
	}
	
	/*
	 * Predictie pentru un intreg vector utilizand functia de predictie
	 * pentru o singura valoare.
	 *
	 */
	public static Vector<Double> predictVector(Vector<Double> trainSet, Vector<Double> testSet, int numVals, int tDly, int maxDim, int k) {
		
		Vector<Double> predictedVec = new Vector<Double>();
		double nextVal;
		
		for (int i = 0; i < numVals; i++) {
			nextVal = predictNextVal(trainSet, tDly, maxDim, k);
			trainSet.add(testSet.get(i));
			predictedVec.add(nextVal);
		}
		
		return predictedVec;
	}

	public static void main(String args[]) {
		/*
		 * Citim valorile din fisierul de test si le adaugam intr-un vector de valori.
		 */
		Scanner scan;
		File file = new File("/home/elf/Desktop/PSKNN_NICULAESCU_Oana_341C1/psteste/VIX_Feb2_2004_13_12_2010.txt");
		
		// vectorul de valori citite
		Vector<Double> dataSet = new Vector<Double>();
		// vectorul de valori folosite pe post de trainingSet
		Vector<Double> trainSet = new Vector<Double>();
		// vectorul de valori folosite pe post de test
		Vector<Double> testSet = new Vector<Double>();
		
		try {
			scan = new Scanner(file);
			
			while (scan.hasNextDouble()) {
				dataSet.add(scan.nextDouble());
			}
		} catch(FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		/*
		 *  daca am ajuns pana aici, atunci citirea setului de date s-a facut corect si putem trece la
		 *  popularea seturilor de training si de test
		 */
		int middle = (int)dataSet.size() / 2;
		for (int i = 0; i < dataSet.size(); i++) {
			if (i < middle) {
				trainSet.add(dataSet.get(i));
			} else {
				testSet.add(dataSet.get(i));
			}
		}
		
		int maxDim = 10;
		int tDly = 5;
		int k = 5;
		double meanSquaredError = 0.0;
		int emDim = embeddingDim(dataSet, maxDim, tDly, rTol, aTol) - 1;
		
//		int emDim = embeddingDim(dataSet, maxDim, tDly, rTol, aTol);
		
		System.out.println(emDim);
		
		Vector<Double> values = predictVector(trainSet, testSet, testSet.size(), tDly, emDim, k);
		
		for (int i = 0; i < testSet.size(); i++) {
			meanSquaredError += Math.pow((values.get(i) - testSet.get(i)), 2);
		}
		
		meanSquaredError = meanSquaredError / testSet.size();
		
//		for (int i = 0; i < testSet.size(); i++) {
//			System.out.println("Valoare de test: " + testSet.get(i) + " - valoare prezisa: " + values.get(i));
//		}
		
		System.out.println(meanSquaredError);
		
	}
	
}
