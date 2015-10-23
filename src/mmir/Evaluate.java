package mmir;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class Evaluate {
	
	public static double lambda = 0;
	public static Logistic logis = new Logistic();
	public static HashMap<String, Integer> map = new HashMap<String, Integer>();
	public static ArrayList<Attribute> atts = new ArrayList<Attribute>();
	
	
	public static void main(String[] args) throws IOException{
		//initIndexing("data/base/google-vw-tw.txt");
		initAtts();
		initRecallCount("data/base/google-vw-tw.txt");
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
	
	public static void initAtts(){
		atts.add(new Attribute("imageScore"));
		atts.add(new Attribute("textScore"));
		ArrayList<String> classVal = new ArrayList<String>();
	    classVal.add("0");
	    classVal.add("1");
	    atts.add(new Attribute("@@class@@",classVal));
	    //atts.
	}
	
	public static void initRecallCount(String filename){
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line;
			while((line = br.readLine()) != null){
				String key = line.split("\t")[0].split("images.seq/")[1].split("/")[0];
				if(map.containsKey(key))
					map.put(key, map.get(key) + 1);
				else
					map.put(key, 1);
			}
			br.close();
			//for(String key : map.keySet())
			//	System.out.println(key + " " + map.get(key));
		} catch (IOException e) {
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
		int count = 0;
		double imap = 0.0, tmap = 0.0, lmap = 0.0, xmap = 0.0, gmap = 0.0;
		for(int i = 0; i < words.size(); i += 2){
			train(words.get(i + 1), queries.get(i + 1));
			System.out.println("lambda = " + lambda);
			List<Double> aps = test(words.get(i), queries.get(i));
			if(aps.size() >= 5){
				imap += aps.get(0);
				tmap += aps.get(1);
				lmap += aps.get(2);
				xmap += aps.get(3);
				gmap += aps.get(4);
				count++;
			}
			//break;
		}
		System.out.println("image map = " + (imap / count) + ", text map = " + (tmap / count));
		System.out.println("linear map = " + (lmap / count) + ", max map = " + (xmap / count) + ", log map = " + (gmap / count));
	}
	
	// for each category/landmark, train the lambda and logistic regression classifier
	public static List<Double> train(String word, String query){
		//word = word.substring(0,  word.length() - 1);
		System.out.println("training query: " + word);
		ArrayList<Double> aps = new ArrayList<Double>();
		try {
			List<Entry> ires = Search.query(query, 2000);
			String s = word.replaceAll("-", " ");
			//System.out.println(s);
			List<Entry> tres = Search.query(s, 2000);
			// calculate average precision of text-only and image-only
			double iap = getAP(ires, word);
			double tap = getAP(tres, word);
			System.out.println("image retrieval ap = " + iap);
			System.out.println("text retrieval ap = " + tap);
			aps.add(iap);
			aps.add(tap);
			lambda = iap / (iap + tap);
			trainLog(ires, tres, word);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aps;
	}
	
	public static void trainLog(List<Entry> ires, List<Entry> tres, String word){
		// initialize instances for training
		Instances ins = new Instances("logfusion", atts, 2000);
		// create instances from ires and tres
		ins.setClassIndex(2);
		Map<String, Float> imap = list2Map(ires);
		for(int i = 0; i < tres.size(); i++){
			Entry e = tres.get(i);
			if(imap.containsKey(e.id)){
				Instance in = new DenseInstance(3);
				in.setDataset(ins);
				in.setValue(0, imap.get(e.id));
				in.setValue(1, e.score);
				if(e.id.contains(word))
					in.setValue(2, "1");
				else
					in.setValue(2, "0");
				ins.add(in);
			}
			else{
				Instance in = new DenseInstance(ins.numAttributes());
				//System.out.println(ins.numAttributes());
				in.setDataset(ins);
				in.setValue(0, 0.0);
				//in.setValue(arg0, arg1);
				in.setValue(1, e.score);
				if(e.id.contains(word))
					in.setValue(2, "1");
				else
					in.setValue(2,"0");
				ins.add(in);
			}
		}
		for(String key: imap.keySet()){
			Instance in = new DenseInstance(3);
			in.setDataset(ins);
			in.setValue(0, imap.get(key));
			in.setValue(1, 0.0);
			if(key.contains(word))
				in.setValue(2, "1");
			else
				in.setValue(2, "0");
			ins.add(in);
		}
		
		try {
			logis.buildClassifier(ins);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// for each category/landmark, test the multi-modal fusion and single-modality retrieval
	public static List<Double> test(String word, String query){
		//word = word.substring(0,  word.length() - 1);
		System.out.println("testing query: " + word);
		ArrayList<Double> aps = new ArrayList<Double>();
		try {
			List<Entry> ires = Search.query(query, 2000);
			List<Entry> tres = Search.query(word.replaceAll("-", " "), 2000);
			// calculate average precision of text-only, image-only, liner rule, maximum rule and logistic regression
			List<Entry> linRes = linFusion(ires, tres);
			List<Entry> maxRes = maxFusion(ires, tres);
			List<Entry> logRes = logFusion(ires, tres);
			//for(int i = 0; i < logRes.size(); i++){
			//	System.out.println(logRes.get(i).id + " " + logRes.get(i).score);
			//}
			double iap = getAP(ires, word);
			double tap = getAP(tres, word);
			double lap = getAP(linRes, word);
			double xap = getAP(maxRes, word);
			double gap = getAP(logRes, word);
			System.out.println("image retrieval ap = " + iap);
			System.out.println("text retrieval ap = " + tap);
			System.out.println("linear fusion ap = " + lap);
			System.out.println("max fusion ap = " + xap);
			System.out.println("log fusion ap = " + gap);
			aps.add(iap);
			aps.add(tap);
			aps.add(lap);
			aps.add(xap);
			aps.add(gap);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aps;
	}
	
	public static List<Entry> linFusion(List<Entry> ires, List<Entry> tres){
		ArrayList<Entry> linRes = new ArrayList<Entry>();
		Map<String, Float> imap = list2Map(ires);
		for(int i = 0; i < tres.size(); i++){
			Entry e = tres.get(i);
			if(imap.containsKey(e.id)){
				Entry ne = new Entry(e.id, ((float)lambda * imap.get(e.id) + (1 - (float)lambda) * e.score));
				linRes.add(ne);
				imap.remove(e.id);
			}
			else{
				Entry ne = new Entry(e.id,  (1 - (float)lambda) * e.score);
				linRes.add(ne);
			}
		}
		for(String key: imap.keySet()){
			Entry ne =  new Entry(key,  (float)lambda * imap.get(key));
			linRes.add(ne);
		}
		Collections.sort(linRes, new Comparator<Entry>(){
			public int compare(Entry e1, Entry e2){
				if(e1.score < e2.score)
					return 1;
				else if (e1.score == e2.score)
					return 0;
				else
					return -1;
			}
		});
		return linRes;
	}
	
	public static List<Entry> maxFusion(List<Entry> ires, List<Entry> tres){
		ArrayList<Entry> maxRes = new ArrayList<Entry>();
		Map<String, Float> imap = list2Map(ires);
		for(int i = 0; i < tres.size(); i++){
			Entry e = tres.get(i);
			if(imap.containsKey(e.id)){
				Entry ne = new Entry(e.id, Math.max(imap.get(e.id), e.score));
				maxRes.add(ne);
				imap.remove(e.id);
			}
			else{
				Entry ne = new Entry(e.id, e.score);
				maxRes.add(ne);
			}
		}
		for(String key: imap.keySet()){
			Entry ne =  new Entry(key,  imap.get(key));
			maxRes.add(ne);
		}
		Collections.sort(maxRes, new Comparator<Entry>(){
			public int compare(Entry e1, Entry e2){
				if(e1.score < e2.score)
					return 1;
				else if (e1.score == e2.score)
					return 0;
				else
					return -1;
			}
		});
		return maxRes;
	}
	
	public static List<Entry> logFusion(List<Entry> ires, List<Entry> tres){
		ArrayList<Entry> logRes = new ArrayList<Entry>();
		Instances ins = new Instances("logfusion", atts, 2000);
		ins.setClassIndex(2);
		// initialize instances for training
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute("imageScore"));
		atts.add(new Attribute("textScore"));
		// create instances from ires and tres
		Map<String, Float> imap = list2Map(ires);
		try {
			for(int i = 0; i < tres.size(); i++){
				Entry e = tres.get(i);
				if(imap.containsKey(e.id)){
					Instance in = new DenseInstance(3);
					in.setDataset(ins);
					in.setValue(0, imap.get(e.id));
					in.setValue(1, e.score);
					double cls = logis.classifyInstance(in);
					double[] dist = logis.distributionForInstance(in);
					//System.out.println(dist[0] + " " + dist[1]);
					Entry ne = new Entry(e.id, (float)dist[1]);
					logRes.add(ne);
				}
				else{
					Instance in = new DenseInstance(2);
					in.setDataset(ins);
					in.setValue(0, 0.0);
					in.setValue(1, e.score);
					double cls = logis.classifyInstance(in);
					double[] dist = logis.distributionForInstance(in);
					//System.out.println(dist[0] + " " + dist[1]);
					Entry ne = new Entry(e.id, (float)dist[1]);
					logRes.add(ne);
				}
			}
			for(String key: imap.keySet()){
				Instance in = new DenseInstance(2);
				in.setDataset(ins);
				in.setValue(0, imap.get(key));
				in.setValue(1, 0.0);
				double cls = logis.classifyInstance(in);
				double[] dist = logis.distributionForInstance(in);
				//System.out.println(dist[0] + " " + dist[1]);
				Entry ne = new Entry(key, (float)dist[1]);
				logRes.add(ne);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Collections.sort(logRes, new Comparator<Entry>(){
			public int compare(Entry e1, Entry e2){
				if(e1.score < e2.score)
					return 1;
				else if (e1.score == e2.score)
					return 0;
				else
					return -1;
			}
		});
		return logRes;
	}
	
	public static Map<String, Float> list2Map(List<Entry> list){
		HashMap<String, Float> map = new HashMap<String, Float>();
		for(Entry e : list)
			map.put(e.id, e.score);
		return map;
	}
	
	public static double getAP(List<Entry> res, String target){
		// recall problem, relevant_docs bug
		double ap = 0.0;
		int relevant_detected = 0;
		int relevant_docs = map.get(target);
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
