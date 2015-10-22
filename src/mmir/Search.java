package mmir;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * Indexing and Searching runs locally using Solr
 */

public class Search {
	
	public static String urlString = "http://localhost:8983/solr";
	public static int numOfResults = 20;
	
	static HashMap<Integer, double[][]> botclusters = new HashMap<Integer, double[][]>();
	static final int max_number_botclusters = 200; //200 cache to store the bot level clusters
	//static int index_to_be_deleted = 0; // the  clusters to be deleted when hashmap is full
	
	public static void main(String[] args) throws IOException, SolrServerException{
		search("data/query/google-query.txt");
	}
	
	public static void search(String filename) throws IOException, SolrServerException{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		ArrayList<String> queries = new ArrayList<String>();
		ArrayList<String> words = new ArrayList<String>();
		String line;
		while((line = br.readLine()) != null){
			words.add(line.split("\t")[0]);
			queries.add(line.split("\t")[1]);
			//System.out.println(line);
		}
		br.close();
		HashMap<String, Double> results = query(queries.get(0), 20);
		//for(String s : results)
		//	System.out.println(s);
	}
	
	//query with customed number of results
	public static HashMap<String, Double> query(String s, int num_results) throws SolrServerException{//query and output results
		HashMap<String, Double> map = new HashMap<String, Double>();
		//query a numeric vector as a string
		HttpSolrServer server = new HttpSolrServer(urlString);
		// search
	    SolrQuery query = new SolrQuery();
	    //query.setQuery("includes:" + s);
	    query.set("q", "includes:" + s);
	    query.setRows(num_results);
	    query.set("fl","*,score");
	    // get results		
	    QueryResponse qresponse = server.query(query);
	    SolrDocumentList list = qresponse.getResults();
	    //String[] files = new String[list.size()];
	    for(int i = 0; i < list.size(); i++){
	    	//System.out.println(list.get(i).getFieldValue("id"));
	    	//files[i] = list.get(i).getFieldValue("id").toString();
	    	//System.out.println(list.get(i).getFieldValue("score"));
	    	map.put(list.get(i).getFieldValue("id").toString(), (Double) list.get(i).getFieldValue("score"));
	    }
	    
	    return map;
	}
	
	//call with a default number of results
	public static HashMap<String, Double> query(String s) throws SolrServerException{//query and output results
		
	    return query(s,Search.numOfResults);
	}
}