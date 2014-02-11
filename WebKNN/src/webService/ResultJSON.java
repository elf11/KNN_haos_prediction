package webService;

import java.util.*;

import org.json.simple.JSONObject;

/*
 * Clasa ce intoarce raspunsul intr-un format de JSON pentru
 * apelarea serviciului web de un clientul, altul decat browserul.
 */

public class ResultJSON {
	public double minEmDim;
	public double meanSquaredError;
	public Vector<Double> results;
	public Vector<Double> tests;
	
	public ResultJSON(double emDim, double mean, Vector<Double> res) {
		minEmDim = emDim;
		meanSquaredError = mean;
		results = res;
	}
	
	public ResultJSON(double mean, Vector<Double> res) {
		minEmDim = -1;
		meanSquaredError = mean;
		results = res;
	}
	
	public ResultJSON(double mean, Vector<Double> res, Vector<Double> testsIn) {
		minEmDim = -1;
		meanSquaredError = mean;
		results = res;
		tests = testsIn;
	}
	
	@SuppressWarnings("unchecked")
	public String toJSONString(){
		JSONObject jsonObject = new JSONObject();
		
		if (minEmDim != -1){
			jsonObject.put("min_embedding_dimension", minEmDim);
		}
		jsonObject.put("mean_squared_error", meanSquaredError);
		jsonObject.put("results", results);
		
		jsonObject.put("test_set", tests);
		
		return jsonObject.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String errorJSONString(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("error", "Verificati daca aveti un fisier valid si daca aveti permisiuni.");
		
		return jsonObject.toJSONString();
	}
	
	
}
