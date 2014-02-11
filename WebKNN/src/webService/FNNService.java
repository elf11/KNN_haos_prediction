package webService;

import java.io.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import knn.*;

/**
 * Servlet implementation class FNNService
 * 
 * Clasa ce formeaza si trimite inapoi un JSON ca raspuns la apelul unui serviciu.
 * Face acelasi lucru ca si clasa ChartService fara a afisa grafic raspunsul insa.
 */

public class FNNService extends HttpServlet {
	
	public static final long serialVersionUID = 1L;
	public String fileName;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FNNService() {
        super();
    }
    
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fileParam = request.getParameter("file_name");
		request.getParameter(" ");
		
		if (!isEmptyOrNull(fileParam)) {
			fileName = fileParam;
		}
		
		System.out.println("file name is " + fileName);
		
		File file = new File(fileName);
		if (!file.exists()) {
			response.getWriter().write(ResultJSON.errorJSONString());
			return;
		}
		
		Scanner scan;
		int k, tDly;
		
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
		
		double rTol = 10;
		double aTol = 2;
		int maxDim = 10;
		
		MainKNN mKNN = new MainKNN();
		
		int emDim = MainKNN.embeddingDim(dataSet, maxDim, tDly, rTol, aTol) - 1;
		
//		int emDim = embeddingDim(dataSet, maxDim, tDly, rTol, aTol);
		
		System.out.println(emDim);
		
		Vector<Double> predicted = MainKNN.predictVector(trainSet, testSet, testSet.size(), tDly, emDim, k);
		
		double meanSquaredError = 0;
		for (int i = 0; i < testSet.size(); i++) {
			meanSquaredError  += Math.pow((predicted.get(i) - testSet.get(i)), 2);
		}
		
		meanSquaredError = meanSquaredError / testSet.size();
		
		response.setContentType("application/json");
		
		ResultJSON responseJS = new ResultJSON(meanSquaredError, predicted, testSet);
		
		PrintWriter writer = response.getWriter();
		writer.print(responseJS.toJSONString());
		writer.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
