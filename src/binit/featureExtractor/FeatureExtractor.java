package binit.featureExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import binit.textclean.ProcessString;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class FeatureExtractor {

	/**
	 * @param args
	 * @throws IOException
	 */

	private Properties props;
	private StanfordCoreNLP pipeline;
	private ProcessString procesString;
	private Annotation document;
	
	public FeatureExtractor() {

		procesString = new ProcessString();
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, parse");// , lemma, ner,
															// dcoref");//lemma");//,
															// ner,
															// parse"); //, dcoref");
		pipeline = new StanfordCoreNLP(props);
	}

	public void extractingFeatures() throws IOException {
		
		File nouns = new File("nouns.txt");
		BufferedWriter buffwrite = new BufferedWriter(new FileWriter(nouns,	true));

		File file = new File("reviews.txt");
		BufferedReader buffread = new BufferedReader(new FileReader(file));

		String line = "";

		while ((line = buffread.readLine()) != null) {

		
			List<String> tags = posTagger(line);

			Iterator<String> iter = tags.iterator();
			while (iter.hasNext()) {

				String[] data = iter.next().split("-");

				if (data[1].startsWith("N") && !data[1].trim().endsWith("P")) {
					buffwrite.write(data[1].trim() + "\t" + data[0].trim() + "\n");
				}
			}
		}
		buffread.close();

		buffwrite.close();
		System.out.println("done.");
	}
	
	public void readFile() throws IOException {

		File nouns = new File("nouns.txt");
		File file = new File("review.txt");;

		BufferedReader buffread = new BufferedReader(new FileReader(file));
		BufferedWriter buffwrite = new BufferedWriter(new FileWriter(nouns)); //, true));
		
		String line = "";

		int count = 1;
		
		while ((line = buffread.readLine()) != null) {

			System.out.println("Count:"+ count +"\n\t" + procesString.processString(line));
			count++;
			
			List<String> tags = posTagger(line);

			Iterator<String> iter = tags.iterator();
			while (iter.hasNext()) {

				String[] data = iter.next().split("-");
				if (data[1].startsWith("N") && !data[1].trim().endsWith("P")) {
					
					buffwrite.write(data[1].trim() + "\t" + data[0].trim()
							+ "\n");
				}
			}
		}
		buffwrite.close();
		buffread.close();
		System.out.println("done.");
	}

	public void extractReview() throws IOException {
		
		File file1 = new File("tmpNFvucr");
		File file2 = new File("review.txt");
		
		BufferedReader buffread = new BufferedReader(new FileReader(file1));
		BufferedWriter buffwrite = new BufferedWriter(new FileWriter(file2));
		
		String line = "";

		while ((line = buffread.readLine()) != null) {

			int index = line.indexOf("\"text\":");
			if (index != -1) {

				int endindex = line.indexOf("\"type\":", index);
				line = line.substring(index + "\"\text\":".length() + 2, endindex - 3);

				buffwrite.write(line+"\n\n");
			}
		}
		buffread.close();
		buffwrite.close();

		System.out.println("done.");
	} 

	public void count() throws IOException {
	
		List<String> features = new ArrayList<String>();
		int frequency = 3; //Threshold
		
		File outputFile = new File("FrequencyCount.txt");
		BufferedWriter buffwrite = new BufferedWriter(new FileWriter(outputFile));
		
		
		File file1 = new File("nouns.txt");
		BufferedReader buffread1 = new BufferedReader(new FileReader(file1));
		
		String line1 = "";
		String line2 = "";
		
		while((line1 = buffread1.readLine()) != null) {
		
			File file2 = new File("nouns - Copy.txt");
			BufferedReader buffread2 = new BufferedReader(new FileReader(file2));
			
			int count = 0;
			
			while((line2 = buffread2.readLine()) != null) {		
				
				if(line1.equals(line2)) {
					count++;
				}
			}
			buffread2.close();
			
			String ss = line1 +":"+ count;
			if(!features.contains(ss)){
				buffwrite.write(line1 +":"+ count + "\n");
			}
		}
		buffread1.close();
		buffwrite.close();
		
		System.out.println("Complete.");
	}

	public List<String> posTagger(String text) {

		List<String> tags = new ArrayList<String>();

		text += ".";
		
		String [] sen = text.split("\\.");
		List<CoreMap> sentences;
		
		for (String s : sen) {
			
			document = new Annotation(s);
			pipeline.annotate(document);

			sentences = document.get(SentencesAnnotation.class);

			for (CoreMap sentence : sentences) {
				
				SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
				String[] annot = dependencies.toString().split("->");

				for (String str : annot) {
					if (!str.equals("")) {
						
						int ind = str.indexOf("(");
						if(ind != -1) {
							tags.add(str.substring(0, ind));
						}
					}
					str = null;
				}
			}
			document = null;
			s = null;
		}
		return tags;
	}
}
