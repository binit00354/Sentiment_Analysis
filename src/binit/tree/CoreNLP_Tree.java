package binit.tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import binit.Stemmer.Stemmer;
import binit.textclean.ProcessString;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class CoreNLP_Tree {

	/**
	 * @param args
	 */

	private ProcessString procesString;
	private Properties props;
	private StanfordCoreNLP pipeline;
	private List<List<String>> sentiwordsinSentence;
	private int positiveCount = 0;  
	private int negativeCount = 0;
	
	public CoreNLP_Tree() {

		procesString = new ProcessString();
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, parse");
		pipeline = new StanfordCoreNLP(props);

		sentiwordsinSentence = new ArrayList<List<String>>();
	}

	public String[] containsNeg(String[] deps) {

		List<String> list = new ArrayList<String>();
		List<String> newDeps = new ArrayList<String>();

		for (String s : deps) {
					
			if(s.isEmpty()) {
				continue;
			}
			
			int index = s.indexOf("(");
			int index1 = s.indexOf(",");

			String pattern = s.substring(0, index);

			if (pattern.equals("neg")) {
				String governor = s.substring(index + 1, index1);
				list.add(governor.substring(0, governor.indexOf("-")));

			}
		}

		if (list.size() != 0) {

			for (String s : deps) {

				int index = s.indexOf("(");
				int index1 = s.indexOf(",");

				String pattern = s.substring(0, index);

				String governor = s.substring(index + 1, index1);
				governor = governor.substring(0, governor.indexOf("-"));

				if (list.contains(governor)) {

					if (pattern.equals("neg")) {
						newDeps.add(s);
					} else if(pattern.equals("aux")) {
						newDeps.add(s);
					}

				} else {
					newDeps.add(s);
				}

			}
		} else {
			
			return deps;
		}

		String [] d = new String[newDeps.size()];
		newDeps.toArray(d);
		
		return d;
	}

	public String stemWord(String governor) {
		
		Stemmer stemmer = new Stemmer();
		
		File file = new File("stem");
		BufferedWriter buffwrite;
		try {
			buffwrite = new BufferedWriter(new FileWriter(file));
			buffwrite.write(governor);
			buffwrite.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stemmer.runStemmer();
	}

	public int positiveOrNegativeOrNeutral(String pos_neg_neut) {
		
		if(pos_neg_neut == null) {
			return 0;
		}
		if(pos_neg_neut.equals("neutral")) {
			return 0;
		} else if(pos_neg_neut.equals("positive")) {
			return 1;
		} else if(pos_neg_neut.equals("negative")) {
			return 2;
		} else {
			return 0;
		}
	}

	public List<String> getReviewsForBusiness(String bid) {
		
		List<String> reviews = new ArrayList<String>();
		
		File file = new File("tmpNFvucr");
		try {
			BufferedReader buffread = new BufferedReader(new FileReader(file));
			
			String line = "";
			while((line = buffread.readLine()) != null) {
				
				int index = line.indexOf("\"business_id\":");
				if(index != -1) {
				
					int endindex = line.indexOf("\"", index +  ("\"business_id\": \"").length() + 1);
					String id = line.substring(index + ("\"business_id\":\"").length() + 1, endindex);
					
					if(id.equals(bid)) {
						int tindex = line.indexOf("\"text\":");
						
						if (tindex != -1) {
							int tendindex = line.indexOf("\"", tindex +  ("\"text:\" \"").length());
							reviews.add(procesString.processString(line.substring(tindex + ("\"text:\" \"").length(), tendindex)));
						}
						
						int nameindex = line.indexOf("\"name\":");
						if (nameindex != -1) {
							int tendindex = line.indexOf("\"", nameindex +  ("\"name\": \"").length());
							reviews.add("name#" + procesString.processString(line.substring(nameindex + ("\"name\": \"").length(), tendindex)));
						}
						
						int starindex = line.indexOf("\"stars\":");
						if (starindex != -1) {
							int tendindex = line.indexOf("\"", starindex +  ("\"stars\": \"").length());
							reviews.add("stars#" + procesString.processString(line.substring(starindex + ("\"stars\":\"").length() - 1, tendindex)));
						}
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		return reviews;
	}
	
	public void findSentiWords(String sentence, List<String> postaggedSentence, String [] dependenciesList) {
	
		Hashtable<String, String> senti_words = new  Hashtable<String, String>();
		Hashtable<String, String> word_pos = new Hashtable<String, String>();

		for (String s : postaggedSentence) {

			String [] str = s.split("-");
			word_pos.put(str[0], str[1]);
		}
		
		for(String word : word_pos.keySet()) {
			
			String partOfSpeech = word_pos.get(word);
			
			if(partOfSpeech.substring(0, 1).equals("J")) {
				partOfSpeech = "adj";
			} else if(partOfSpeech.substring(0, 1).equals("V")) {
				partOfSpeech = "verb";
			} else if(partOfSpeech.substring(0, 1).equals("R")) {
				partOfSpeech = "adverb";
			} else if(partOfSpeech.substring(0, 1).equals("N")) {
				partOfSpeech = "noun";
			}
			
			String stemmedWord = stemWord(word);
			String polarity = wordPolarity3(partOfSpeech, word, stemmedWord);
			
			if(polarity != null) { 
				senti_words.put(word, polarity);
			}
		}
		applyRule(sentence, postaggedSentence,senti_words, dependenciesList);
	}
	
	public void applyRule(String sentence, List<String> postaggedSentence, Hashtable<String, String> word_pos, String [] dependencies) {
		
		Hashtable<String, String> removeWords = new Hashtable<String, String>();
		
		for (String word : word_pos.keySet()) {
			
			boolean contains_negation = false;
			boolean contains_mod = false; 
			
			for (String dep : dependencies) { // for each word checking if there is any dependency rule, i.e bigram
				
				String [] t = dep.split("\\("); // 0:Pattern
				
				if(t.length < 2) {
					continue;
				}
				String pattern = t[0];
				String st = t[1].substring(0, t[1].length() - 1);
				
				String [] govern_dependent = st.split(","); //0: govern, 1: dependent
				String govern = govern_dependent[0].split("-")[0];
				String dependent = govern_dependent[1].split("-")[0];
				
				if(!govern.equals(word)) { // Finding dependencies clause that has an impact on the governer.
					continue;
				}
			
				if(pattern.equals("neg") && !contains_mod) { // IF there is a negation, polarity of govern will be complemented.
					
					contains_negation = true;
					if(word_pos.get(govern).equals("positive")) {
						
						word_pos.put(govern, "negative");
					} else if (word_pos.get(govern).equals("negative")) {
						
						word_pos.put(govern, "positive");
					}
					removeWords.put(dependent, "");
					
				} else if(pattern.equals("advmod") && !contains_negation) {
					
					String res = adverbModifier(govern, dependent, word_pos.get(govern), word_pos.get(dependent), postaggedSentence);
					if (!contains_mod) {

						if (res.equals("Positive.")) {

							word_pos.put(govern, "positive");
						} else if (res.equals("Negative.")) {

							word_pos.put(govern, "negative");
						}
					}
					removeWords.put(dependent, "");
				} else if(pattern.equals(govern + "amod") && !contains_negation) {
					
					String res = adjectiveModifier(govern, dependent, word_pos.get(govern), word_pos.get(dependent), postaggedSentence);
					if (!contains_mod) {

						if (res.equals("Positive.")) {

							word_pos.put(govern, "positive");
						} else if (res.equals("Negative.")) {

							word_pos.put(govern, "negative");
						}
					}
					removeWords.put(dependent, "");
				} else if(pattern.equals(govern + "dobj") && !contains_negation) {
					
					String res = dObjModifier(govern, dependent, word_pos.get(govern), word_pos.get(dependent), postaggedSentence);
					if (!contains_mod) {

						if (res.equals("Positive.")) {

							word_pos.put(govern, "positive");
						} else if (res.equals("Negative.")) {

							word_pos.put(govern, "negative");
						}
					}
					
					removeWords.put(dependent, "");
				} else if(pattern.equals("aux") && (dependent.equals("could") || dependent.equals("should") || dependent.equals("would"))) {
					
					Hashtable<String, String> rw = modClause(sentence, govern, dependent, word_pos.get(govern), word_pos.get(dependent), postaggedSentence, contains_negation, word_pos);
					String [] sd = rw.get("res").split("#");
					rw.remove("res");
					word_pos.put(govern, sd[0]);
										
					if(sd.length > 1) {
						contains_mod = Boolean.parseBoolean(sd[1]);
					}
				}
			}
		}
		
		for(String s : removeWords.keySet()) {
			
			if(word_pos.containsKey(s)) {
				word_pos.remove(s);
			}
		}
		
		//removing neutral words
		List<String> wlist = new ArrayList<String>();
		for(String key: word_pos.keySet()) {
			if(word_pos.get(key).equals("neutral")) {
				wlist.add(key);
			}
		}
			
		for(String s: wlist) {
			word_pos.remove(s);
		}
		
		for(String s : word_pos.keySet()) {
			
			if(word_pos.get(s).equals("positive")) {
				positiveCount++;
			} else if(word_pos.get(s).equals("negative")) {
				negativeCount++;
			}
		}
		
		if (sentence.contains("but")) { // If there is a but clause, both the halves are evaluated.
			
			String res = butClause(sentence, word_pos);
		} else { // If no but clause what is the overall sentiment of the sentence
			
			if(word_pos.contains("positive")) {
//				System.out.println("Positive Review.");
			} else if(word_pos.contains("negative")) {
//				System.out.println("Negative Review.");
			}
		}
	}
	
	private Hashtable<String, String> modClause(String sentence, String govern, String dependent, String govern_polarity, String dependent_polarity, List<String> postaggedSentence, boolean contains_negation, Hashtable<String, String> word_pos) {
	
		String res = "";

		Hashtable<String, String> rs = new Hashtable<String, String>();
		
		int indexOfCould = sentence.indexOf("could");
		int indexOfModVerb = sentence.indexOf(dependent);
		int containsHaveBeen = sentence.indexOf("have been", indexOfModVerb);
		
		int indexOFCouldInSentence = -1;
		String [] wordsInSentence = sentence.split(" ");
		
		for(String s:wordsInSentence) {
			if(s.contains("could")) {
				break;
			}
			indexOFCouldInSentence++;
		}
		
		int isThereNo = -1; 
		
		for(int i=indexOFCouldInSentence + 1; i<wordsInSentence.length; i++) {
			
			String polarity = word_pos.get(wordsInSentence[i]);
			if(polarity!=null && polarity.equals("negative")) {
				rs.put(wordsInSentence[i],	"");
				isThereNo = 1;
			}
		}
		
		boolean check = containsHaveBeen != -1 ? true : false;
		
		if (check) {

			if (contains_negation) { // If contains 'not' && 'have been'change 
										// the polarity of governor back to
										// original
				if (govern_polarity.equals("positive")) {
					res = "negative#false";
				} else if (govern_polarity.equals("negative")) {
					res = "positive#false";
				}
			} else if (isThereNo != -1) { // Negation after could, means no
										  // impact of could have been.
										  // No impact of 'negation anymore, so sending true'	

				if (govern_polarity.equals("positive")) {
					res = "positive#true";
				} else if (govern_polarity.equals("negative")) {
					res = "negative#true";
				}
			}
		}
		
		rs.put("res", res);
		return rs;
	}
	
	private String dObjModifier(String govern, String dependent, String govern_polarity, String dependent_polarity, List<String> postaggedSentence) {
		
		String res = "";
		
		Hashtable<String, String> wp = new Hashtable<String, String>();

		for (String s : postaggedSentence) {

			String [] str = s.split("-");
			wp.put(str[0], str[1]);
		}
		
		int arg1 = 0, arg2 = 0;
		String pos1 = wp.get(govern);
		String pos2 = wp.get(dependent);

		arg1 = positiveOrNegativeOrNeutral(govern_polarity);
		arg2 = positiveOrNegativeOrNeutral(dependent_polarity);
		
		// Neutral Case
		if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("P") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("D") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("W") && arg1 == 0 && arg2 == 0) {

			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("C") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} // Negative Case
		else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 0 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 2 && arg2 == 1) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 2 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("P") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("J") && arg1 == 0 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("D") && arg1 == 2 && arg2 == 1) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("N") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("D") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("V") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} // positive Case
		 else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 0 && arg2 == 1) {
				
			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 1 && arg2 == 0) {
			
			res = "Positive.";
	    } else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 1 && arg2 == 1) {
			
	    	res = "Positive.";
	    } else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 1) {
			
	    	res = "Positive.";
	    } else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 1) {
			
	    	res = "Positive.";
	    } else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("J") && arg1 == 0 && arg2 == 1) {
			
	    	res = "Positive.";
	    } else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("V") && arg1 == 0 && arg2 == 1) {
			
	    	res = "Positive.";
	    } else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("N") && arg1 == 0 && arg2 == 1) {
			
	    	res = "Positive.";
	    }
		
		return res;
	}
	
	private String butClause(String sentence, Hashtable<String, String> word_pos) { //, List<String> leftPart, List<String> rightPart) {
		
		String res = "";
		
		List<String> leftPart = new ArrayList<String>();
		List<String> rightPart = new ArrayList<String>();
		boolean left_half = true; 
		
		String[] sen = sentence.split(" ");

		for (String s : sen) {

			s = s.replaceAll("[,.]", "");
			
			if(s.equals("but") && left_half) {
				left_half = false;
			}
			
			if(left_half) {
				
				if (word_pos.containsKey(s) && (word_pos.get(s).equals("positive") || word_pos.get(s).equals("negative"))) {
					leftPart.add(word_pos.get(s));
				}
			} else {
				if (word_pos.containsKey(s) && (word_pos.get(s).equals("positive") || word_pos.get(s).equals("negative"))) {
					rightPart.add(word_pos.get(s));
				}
			}
		}

		if(leftPart.contains("negative") && rightPart.contains("positive")) {
			res = "Mixed.";
		} else if(leftPart.contains("positive") && rightPart.contains("negative")) {
			res = "Mixed.";
		} else if((!leftPart.contains("postive") || !leftPart.contains("negative")) && rightPart.contains("positive")) { // No neg or pos and positive ==> pos
			res = "Positive.";
		} else if((!leftPart.contains("postive") || !leftPart.contains("negative")) && rightPart.contains("negative")) { // No neg or pos and positive ==> pos
			res = "Negative.";
		} else if(!leftPart.contains("postive") && rightPart.contains("positive")) {
			res = "Positive.";
		} else if(leftPart.contains("negative") && rightPart.contains("negative")) {
			res = "Negative.";
		} else if((!leftPart.contains("postive") || !leftPart.contains("negative")) && rightPart.contains("negative")) { // No neg or pos and negative ==> neg
			res = "Positive.";
		} else if(leftPart.contains("positive") && (!rightPart.contains("postive") || !rightPart.contains("negative"))) { // No neg or pos and negative ==> neg
			res = "Positive.";
		} else if(leftPart.contains("negative") && (!rightPart.contains("postive") || !rightPart.contains("negative"))) { // No neg or pos and negative ==> neg
			res = "Negative.";
		} 
		return res;
	} 
	
	private String adjectiveModifier (String govern, String dependent, String govern_polarity, String dependent_polarity, List<String> postaggedSentence) {
		
		Hashtable<String, String> wp = new Hashtable<String, String>();

		for (String s : postaggedSentence) {

			String [] str = s.split("-");
			wp.put(str[0], str[1]);
		}
		
		String res = "";
		
		int arg1 = 0, arg2 = 0;
		String pos1 = wp.get(govern);
		String pos2 = wp.get(dependent);

		arg1 = positiveOrNegativeOrNeutral(govern_polarity);
		arg2 = positiveOrNegativeOrNeutral(dependent_polarity);
		
		
		// Neutral Case
		if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("V") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("D") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		}// Negative Case
		else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 2 && arg2 == 1) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 1 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 0 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 2 && arg2 == 2) {
			
			res = "Negative.";
		} //Positive Case
		else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 1 && arg2 == 1) {
				
			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 0 && arg2 == 1) {
			
			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("J") && arg1 == 1 && arg2 == 0) {
			
			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("V") && arg1 == 0 && arg2 == 1) {
			
			res = "Positive.";
		}		
		return res;
	}
	
	private String adverbModifier(String govern, String dependent, String govern_polarity, String dependent_polarity , List<String> postaggedSentence) {
	
		Hashtable<String, String> wp = new Hashtable<String, String>();

		for (String s : postaggedSentence) {

			String [] str = s.split("-");
			wp.put(str[0], str[1]);
		}
		
		String res = "";
		
		int arg1 = 0, arg2 = 0;
		String pos1 = wp.get(govern);
		String pos2 = wp.get(dependent);

		arg1 = positiveOrNegativeOrNeutral(govern_polarity);
		arg2 = positiveOrNegativeOrNeutral(dependent_polarity);

		
		// Neutral Case
		if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 1) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 1) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("I") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("R") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("R") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 0) {
			
			res = "Neutral.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 0) {
			
			res = "Neutral.";
		} // Negative Case
		else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 1) {
				
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 1) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("D") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 1) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 0) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 2) {
			
			res = "Negative.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 2 && arg2 == 1) {
			
			res = "Negative.";
		} // Positive Case
		else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 1) {
				
			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 0 && arg2 == 1) {
			
			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("V") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 0) {
			
			res = "Positive.";
		}else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 1) {
			
			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("J") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 0) {

			res = "Positive.";
		} else if(pos1.substring(0, 1).equals("N") && pos2.substring(0, 1).equals("R") && arg1 == 1 && arg2 == 1) {
			
			res = "Positive.";
		}
		return res;
	}
	
	private String checkForExceptClause(String text) {
		
		if(text.contains("except for")) {
			text = text.replaceAll("except for", "but");
		} else if(text.contains("except that")) {
			text = text.replaceAll("except that", "but");
		} else if(text.contains("except for")) {
			text = text.replaceAll("except for", "but");
		} else if(text.contains("not only") && text.contains("but also")) {
			text = text.replaceAll("not only", "");
			text = text.replaceAll("but also", "but");
		}
		
		return text;
	}
	
	public String wordPolarity3(String partOfSpeech, String word, String stemmedWord) {

		String polarity = null;
		
		int positiveCount = 0;
		int negativeCount = 0;
		int neutralCount = 0;
		
		FileReader filereader;
		BufferedReader buffread;
		try {
			
			filereader = new FileReader("ourdictionary.txt");
			buffread = new BufferedReader(filereader);
			String line = "";
			
			while((line = buffread.readLine()) != null) {
				
				String [] words = line.split("\t"); //0:pos, 1:polarity, 2:word
				
				if((words[0].equals(partOfSpeech) && words[2].equals(word)) || (words[0].equals(partOfSpeech) && words[2].equals(stemmedWord))) {
				
					if(words[1].equals("positive")) {
						positiveCount++;
					} else if(words[1].equals("negative")) {
						negativeCount++;
					} else {
						neutralCount++;
					}
				}
			}
			
			if(positiveCount > negativeCount && positiveCount >= neutralCount) {
				polarity = "positive";
			} else if(negativeCount > positiveCount && negativeCount >= neutralCount) {
				polarity = "negative";
			} else if(neutralCount > positiveCount && neutralCount > negativeCount) {
				polarity = "neutral";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return polarity;
	}

	public List<String> analyze(List<String> list) throws IOException {
		
		List<String> output = new ArrayList<String>();
		
		int numberOfPositiveReview = 0;
		int numberOfNegativeReview = 0;
		
		Iterator<String> iter = list.iterator();

		String name = "";
		String stars = "";
		
		while (iter.hasNext()) {

			String text = iter.next();
			
			if(text.startsWith("name#")) {
				name = text.split("#")[1];
				continue;
			} else if(text.startsWith("stars#")) {
				stars = text.split("#")[1];
				continue;
			}
				
			text = checkForExceptClause(text);
			
			if(text.length() > 450) {
				text = text.substring(0, 450);
			}
			
			Annotation document = new Annotation(text);

			pipeline.annotate(document);

			List<CoreMap> sentences = document.get(SentencesAnnotation.class);

			for (CoreMap sentence : sentences) {

				sentiwordsinSentence.add(new ArrayList<String>());

				Tree tree = sentence.get(TreeAnnotation.class);

				SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
							
				String[] annot = dependencies.toString().split("->");

				List<String> sentis = new ArrayList<String>();
				for (String s : annot) {
					if (!s.equals("")) {

						sentis.add(s.substring(0, s.indexOf("(")).trim());
					}
				}
				
				findSentiWords(sentence.toString(), sentis, dependencies.toList().split("\n"));	
			}
			
			if(positiveCount > negativeCount) {
				numberOfPositiveReview++;
				System.out.println("Sentiment of the review: Positive.");
			} else if(negativeCount > positiveCount) {
				numberOfNegativeReview++;
				System.out.println("Sentiment of the review: Negative.");
			}
			
			positiveCount = 0;
			negativeCount = 0;
			
		}
			
		float ourrating = ((float)(5 * numberOfPositiveReview))/(numberOfPositiveReview +  numberOfNegativeReview);
		int posPercent = (numberOfPositiveReview * 100)/(numberOfPositiveReview + numberOfNegativeReview);
		int negPercent = 100 - posPercent;		
		
		output.add(name);
		output.add(stars);
		output.add(ourrating + "");		
		output.add(""+posPercent);
		output.add(""+negPercent);
		
		return output;
	}
}
