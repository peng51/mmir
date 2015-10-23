package mmir;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;

import weka.classifiers.functions.Logistic;

public class Evaluate {
	
	public static double lambda = 1.0;
	public static Logistic logis = new Logistic();
	
	public static void main(String[] args) throws IOException{
		//initIndexing("data/base/google-tw.txt");
		allEval("data/query/google-query.txt");
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
			//String[] path = line.split("\t")[0].split(".jpg")[0].split("/");
			words.add(line.split("\t")[1]);
			queries.add(line.split("\t")[2]);
			//System.out.println(words.get(words.size() - 1));
		}
		br.close();
		for(int i = 0; i < words.size(); i += 2){
			train(words.get(i), queries.get(i));
			//test(words.get(i + 1), queries.get(i + 1));
			//break;
		}
	}
	
	// for each category/landmark, train the lambda and logistic regression classifier
	public static void train(String word, String query){
		//word = word.substring(0,  word.length() - 1);
		System.out.println("training query: " + word);
		try {
			//List<Entry> ires = Search.query(query, 100);
			String s = word.replaceAll("-", " ");
			System.out.println(s);
			List<Entry> tres = Search.query(s, 1000);
			//for(Entry e : tres){
				//System.out.println(e.id + " " + e.score);
			//}
			// calculate average precision of text-only and image-only
			//double iap = getAP(ires, word);
			double tap = getAP(tres, word);
			//System.out.println("image retrieval ap = " + iap);
			System.out.println("text retrieval ap = " + tap);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// for each category/landmark, test the multi-modal fusion and single-modality retrieval
	public static void test(String word, String query){
		word = word.substring(0,  word.length() - 1);
		System.out.println("training query: " + word);
		try {
			List<Entry> ires = Search.query(query, 100);
			List<Entry> tres = Search.query(word, 100);
			//for(Entry e : tres){
				//System.out.println(e.id + " " + e.score);
				//imap.entrySet();
			//}
			
			// calculate average precision of text-only, image-only, liner rule, maximum rule and logistic regression
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double getAP(List<Entry> res, String target){
		// recall problem, relevant_docs bug
		double ap = 0.0;
		int relevant_detected = 0;
		int relevant_docs = 0;
		for(int i = 0; i < res.size(); i++){
			if(res.get(i).id.contains(target))
				relevant_docs++;
		}
		//System.out.println("relevant_docs = " + relevant_docs);
		for(int i = 0; i < res.size(); i++){
			int relevance = 0;
			if(res.get(i).id.contains(target)){
				relevance = 1;
				relevant_detected++;
			}
			double precision = ((double) relevant_detected) / (i + 1);
			//System.out.println("precision = " + precision + ", relevance = " + relevance);
			ap += ( precision * relevance ) / (double)relevant_docs;
		}
		//System.out.println(ap);
		return ap;
	}
}
