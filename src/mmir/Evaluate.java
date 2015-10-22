package mmir;

import java.io.IOException;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrServerException;

public class Evaluate {
	
	public static double lambda = 1.0;
	
	public static void main(String[] args){
		
	}
	
	public static void train(){
		
	}
	
	public static void test(){
		
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
