package mmir;


import java.io.IOException;
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
	
	public static int featureSize = 128;
	
	public static int clusterNum = 100;
	public static int topclusterNum = 10;
	public static int botclusterNum = 10;
	public static String clusters = "test/cluster/clusters/";
	public static String terms = "data/features/frequency.txt";
	
	public static String urlString = "http://localhost:8983/solr";
	public static int numOfResults = 20;
	
	//cache 
	static double[][] topcluster = null;
	
	static HashMap<Integer, double[][]> botclusters = new HashMap<Integer, double[][]>();
	static final int max_number_botclusters = 200; //200 cache to store the bot level clusters
	//static int index_to_be_deleted = 0; // the  clusters to be deleted when hashmap is full
	
	public static void main(String[] args) throws IOException, SolrServerException{
		// run indexing
		//runIndexing("test/data/frequency.txt");
		//search("data/images/all_souls_000000.jpg");
//		loadConfiguration("test/conf.xml");
		//loadConfiguration_topdown("test/conf_new.xml");
		//System.out.println(terms);
		//System.out.println(clusters);
		//System.out.println(clusterNum);
		//Search.loadConfiguration_topdown("test/conf_new.xml");
		//Search.search_topdown("data/images/all_souls_000000.jpg");
		//test("/home/yp/Desktop/results/test14");
	}
	
	
	public static void init(String terms, int clusterNum, String clusters, int topclusterNum, int botclusterNum){
		Search.terms = terms;
		Search.clusterNum = clusterNum;
		Search.clusters = clusters;
		Search.topclusterNum = topclusterNum;
		Search.botclusterNum = botclusterNum;
		
		//init the static cache files -- important
		Search.topcluster =null;
		Search.botclusters.clear();
	}
	
	
	//query with customed number of results
	public static String[] query(String s, int num_results) throws SolrServerException{//query and output results
		
		//query a numeric vector as a string
		String urlString = Search.urlString;
		HttpSolrServer server = new HttpSolrServer(urlString);
		// search
	    SolrQuery query = new SolrQuery();
	    //query.setQuery("includes:" + s);
	    query.set("q", "includes:" + s);
	    query.setRows(num_results);
	    // get results		
	    QueryResponse qresponse = server.query(query);
	    SolrDocumentList list = qresponse.getResults();
	    String[] files = new String[list.size()];
	    for(int i = 0; i < list.size(); i++){
	    	//System.out.println(list.get(i).getFieldValue("id"));
	    	files[i] = list.get(i).getFieldValue("id").toString();
	    }
	    
	    return files;
	}
	
	//call with a default number of results
	public static String[] query(String s) throws SolrServerException{//query and output results
		
	    return query(s,Search.numOfResults);
	}
}