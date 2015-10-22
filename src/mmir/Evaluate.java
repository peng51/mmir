package mmir;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrServerException;

import weka.classifiers.functions.Logistic;

public class Evaluate {
	
	public static double lambda = 1.0;
	public static Logistic logis = new Logistic();
	
	public static void main(String[] args) throws IOException{
		initIndexing("data/");
		allEval("data/query/google_query.txt");
	}
	
	public static void initIndexing(String filename){
		try {
			Indexing.index(filename);
		} catch (IOException | SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void allEval(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		ArrayList<String> queries = new ArrayList<String>();
		ArrayList<String> words = new ArrayList<String>();
		String line;
		while((line = br.readLine()) != null){
			String[] path = line.split("\t")[0].split(".jpg")[0].split("/");
			words.add(path[path.length - 1]);
			queries.add(line.split("\t")[1]);
			System.out.println(words.get(words.size() - 1));
		}
		br.close();
		for(int i = 0; i < words.size(); i += 2){
			train(words.get(i), queries.get(i));
			test(words.get(i + 1), queries.get(i + 1));
		}
	}
	
	public static void train(String word, String query){
		
	}
	
	public static void test(String word, String query){
		
	}
	
	public static HashMap<String, Double> getImageRetrieval(String imageBase, String imageQuery){
		HashMap<String, Double> map = new HashMap<String, Double>();
		try {
			Indexing.index(imageBase);
			Search.search(imageQuery);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return map;
	}
	
	public static HashMap<String, Double> getTextRetrieval(String textBase, String textQuery){
		HashMap<String, Double> map = new HashMap<String, Double>();
		
		return map;
	}
	
	public static double getAP(String[] rankedList, String target){
		double ap = 0.0;
		int relevant_detected = 0;
		int relevant_docs = 0;
		for(int i = 0; i<rankedList.length; i++){
			if(rankedList[i].contains(target))
				relevant_docs++;
		}
		for(int i = 0; i<rankedList.length; i++){
			int relevance = 0;
			if(rankedList[i].contains(target))
				relevance = 1;
				relevant_detected++;
			double precision = ((double) relevant_detected) / (i + 1);
			ap += ( precision * relevance ) / relevant_docs;
		}
		System.out.println(ap);
		return ap;
	}
}
