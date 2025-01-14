package main;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import support.wordnet.WordNet;
import support.wordnet.disambiguate.IWSD_Disambiguator;
import support.wordnet.disambiguate.SenseRelate_AllWords;

/**
 * Program for trying the different features of wordnet
 */
public class wordnetExample {

	public static void main(String[] args)
	{
		runExample();
	}
	 public static void runExample(){
		    
		     // construct the URL to the Wordnet dictionary directory
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
		    // look up first sense of the word "dog"
		    IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
		    IWordID wordID = idxWord.getWordIDs().get(0);
		    IWord word = dict.getWord(wordID);
		    System.out.println("Id = " + wordID);
		    System.out.println("Lemma = " + word.getLemma());
		    System.out.println("Gloss = " + word.getSynset().getGloss());
		  
	      //  System.out.println(lemma + " " + dict.getSemanticClass(lemma));
		    
		    System.out.println("=============================================");
		    System.out.println("HashMap-synonyms> [dog]"+WordNet.getInstance().getSynonymsMap("dog", POS.NOUN));
		    System.out.println("HashMapsynonyms> [cat]"+WordNet.getInstance().getSynonymsMap("cat", POS.NOUN));
		    System.out.println("HashMapsynonyms> [author]"+WordNet.getInstance().getSynonymsMap("author", POS.NOUN));
		    System.out.println("HashMapsynonyms> [can]"+WordNet.getInstance().getSynonymsMap("can", POS.NOUN));
		    System.out.println("HashMapsynonyms> [service]"+WordNet.getInstance().getSynonymsMap("service", POS.NOUN));
		    
		    check_disambiguator();
	 }
	private static void check_disambiguator() {
		IWSD_Disambiguator disamb = new SenseRelate_AllWords();
		disamb.disambiguatePhrase("Can I set up an appointment to service my bike tomorrow at 2pm?");
		
	}
	
}
