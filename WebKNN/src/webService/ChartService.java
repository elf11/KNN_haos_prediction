package webService;

import java.awt.Color;
import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ServletEncoder;

import knn.*;
/**
 * Servlet implementation class ChartService
 * 
 * Clasa ce apeleaza metodele din package-ul knn, metode de calculare a spatiului fazelor 
 * si de predictie a valorilor pentru o time series si intoarce utilizatorului un grafic ce afiseaza
 * diferentele dintre valorile prezise si valorile ce se doreau.
 */

public class ChartService extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public String fileName;
	public Chart chartProps;
	  
	public String getParameter(HttpServletRequest req, String parameter) throws ServletException {
	    String value = req.getParameter(parameter);
	    if (isEmptyOrNull(value)) {
	      throw new ServletException("Parameter " + parameter + " not found");
	    }
	    return value.trim();
	}

	public String getParameter(HttpServletRequest req, String parameter, String defaultValue) {
	    String value = req.getParameter(parameter);
	    if (isEmptyOrNull(value)) {
	      value = defaultValue;
	    }
	    return value.trim();
	}

	public boolean isEmptyOrNull(String value) {
	    return value == null || value.trim().length() == 0;
	  }
	

	@SuppressWarnings("unchecked")
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		String fileNameParam = request.getParameter("file");
		
		if (!isEmptyOrNull(fileNameParam)){
			fileName = fileNameParam;
		} else{
			fileName = Upload.DEFAULT_PATH; 
		}
		
		File inputFile = new File(fileName);
		if (!inputFile.exists()){
			response.getWriter().write("<html><body>" + "Eroare. Verificati daca exista fisierul /tmp/req.txt" + "</body></html>");
			return;
		}
		
		int k, tDly;
		double meanSquaredError = 0;
		
		String tDlyParam = request.getParameter("tDly");
		if (!isEmptyOrNull(tDlyParam)){
			tDly = Integer.parseInt(tDlyParam);
		} else{
			tDly = 5;
		}
		
		String kParam = request.getParameter("k");
		if (!isEmptyOrNull(kParam)){
			k = Integer.parseInt(kParam);
		} else{
			k = 5;
		}
		
		chartProps = new Chart(750,300);
		chartProps.setTitle("Time series prediction using false nearest neighbors");
		chartProps.setXAxisTitle("Time series");
		chartProps.setYAxisTitle("Value of prediction/corect value");
		
		Scanner scan;
		File file = new File(fileName);
		
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
		
		double rTol = 10;
		double aTol = 2;
		int maxDim = 10;
		
		MainKNN mKNN = new MainKNN();
		
		int emDim = MainKNN.embeddingDim(dataSet, maxDim, tDly, rTol, aTol) - 1;
		
//		int emDim = embeddingDim(dataSet, maxDim, tDly, rTol, aTol);
		
		System.out.println(emDim);
		
		Vector<Double> predicted = MainKNN.predictVector(trainSet, testSet, testSet.size(), tDly, emDim, k);
		
		for (int i = 0; i < testSet.size(); i++) {
			meanSquaredError += Math.pow((predicted.get(i) - testSet.get(i)), 2);
		}
		
		meanSquaredError = meanSquaredError / testSet.size();
		
		Collection<Number> valuesOnX;
		Collection<Number> valuesOnY;
		
		
		valuesOnX = new Vector<Number>();
		valuesOnY = new Vector<Number>();
		
		for (int i = 0; i < testSet.size(); i ++){
			valuesOnX.add(i);
			valuesOnY.add(testSet.get(i));
		}
		
		chartProps.setBackgroundColor(new Color(173,206,250));
		chartProps.setLinesColor(Color.RED);
		
		chartProps.addSeries("Test Set ", valuesOnX, valuesOnY);

		valuesOnX = new Vector<Number>();
		valuesOnY = new Vector<Number>();
		
		for (int i = 0; i < predicted.size(); i ++){
			valuesOnX.add(i);
			valuesOnY.add(predicted.get(i));
		}
		chartProps.addSeries("Preddicted Set", valuesOnX, valuesOnY);
		
		if (chartProps != null){
			response.setContentType("image/png");
			ServletOutputStream out = response.getOutputStream();
			
			ServletEncoder.streamPNG(out, chartProps);
			
			out.close();		
		}
	}

}
