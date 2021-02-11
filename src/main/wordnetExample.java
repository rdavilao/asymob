package main;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Synset;

public class wordnetExample {

	public static void main(String[] args)
	{
		runExample();
	}
	 public static void runExample(){
		    
		     // construct the URL to the Wordnet dictionary directory
		     String wnhome = System.getenv("$WNHOME");
		     String path = "/usr/local/WordNet-3.0" + File.separator + "dict";
		     URL url = null;
		     try{ url = new URL("file", null, path); } 
		     catch(MalformedURLException e){ e.printStackTrace(); }
		     if(url == null) return;
		    
		    // construct the dictionary object and open it
		    IDictionary dict = new Dictionary(url);
		    try {
				dict.open();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    WordNet_old wordNet;
		    // look up first sense of the word "dog"
		    IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
		    IWordID wordID = idxWord.getWordIDs().get(0);
		    IWord word = dict.getWord(wordID);
		    System.out.println("Id = " + wordID);
		    System.out.println("Lemma = " + word.getLemma());
		    System.out.println("Gloss = " + word.getSynset().getGloss());
		  
		    String lemma = "dog";
	      //  System.out.println(lemma + " " + dict.getSemanticClass(lemma));
	 }
	
}
