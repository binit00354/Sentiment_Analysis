package binit.tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ProcessFile {

	/**
	 * @param args
	 */
	
	private String partOfSpeech(String word) {
	
		String pos = "";
		if(word.equals("a")) {
			pos = "adj";	
		} else if(word.equals("n")) {
			pos = "noun";	
		} else if(word.equals("v")) {
			pos = "verb";	
		} else if(word.equals("r")) {
			pos = "adverb";	
		}
		return pos;
	}
	private String polarity(float pos, float neg) {
		
		String polarity = "";
		float res = pos - neg;
		
		if(res == 0) {
			polarity = "neutral";
		} else if(res > 0) {
			polarity = "positive";
		} else if(res < 0) {
			polarity = "negative";
		}
		
		return polarity;
	}
	
	public void compileBothDictionary() {

		FileReader filereader;
		FileWriter filewriter;
		BufferedReader buffread;
		BufferedWriter buffwrite;
				
		String line = "";
		
		try {

			filereader = new FileReader("SentiWordNet.txt");
			filewriter = new FileWriter("ourdictionary.txt");
			
			buffread = new BufferedReader(filereader);
			buffwrite = new BufferedWriter(filewriter);
			
			while((line = buffread.readLine()) != null) {
				
				String [] data = line.split("\t");
				
				String pos = partOfSpeech(data[0]);
				
				float positivePolarity = Float.parseFloat(data[2]);
				float negativePolarity = Float.parseFloat(data[3]);
				
				String polaityOfWords = polarity(positivePolarity, negativePolarity);
				
				String [] words = data[4].split(" ");
				
				for(int i=0; i<words.length; i++) {
					words[i] = words[i].split("#")[0];
					
					buffwrite.write(pos +"\t"+ polaityOfWords +"\t"+ words[i]);
					buffwrite.write("\n");
				}
			}
			System.out.println("Processing of sentiwordnet file completed.");
			
			filereader = new FileReader("subjclueslen1-HLTEMNLP05.txt");
			filewriter = new FileWriter("ourdictionary.txt", true);
			
			buffread = new BufferedReader(filereader);
			buffwrite = new BufferedWriter(filewriter);
			
			while((line = buffread.readLine()) != null) {
				
				String [] data = line.split(" ");
			
				String word = data[2].split("=")[1];
				String pos = data[3].split("=")[1];
				
				String polaityOfWords = data[5].split("=")[1];
				
				if(pos.equals("anypos")) {
					buffwrite.write("adj" +"\t"+ polaityOfWords +"\t"+  word);
					buffwrite.write("\n");
					buffwrite.write("noun" +"\t"+ polaityOfWords +"\t"+  word);
					buffwrite.write("\n");
					buffwrite.write("verb" +"\t"+ polaityOfWords +"\t"+  word);
					buffwrite.write("\n");
					buffwrite.write("adverb" +"\t"+  polaityOfWords +"\t"+  word);
					buffwrite.write("\n");
				} else {
					buffwrite.write(pos +"\t"+ polaityOfWords +"\t"+ word);
					buffwrite.write("\n");
				}
			}
			System.out.println("Processing of subclueslen file completed.");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readFile() {
		
		FileReader filereader;
		try {
			
			filereader = new FileReader("ourdictionary.txt");
			BufferedReader buffread = new BufferedReader(filereader);
			String line = "";
			
			int i = 1;
			while((line = buffread.readLine()) != null) {
				
				String [] words = line.split("\t");
				
				System.out.println(i +"-"+ words[0] +","+ words[1] +","+ words[2]);
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {

		ProcessFile pf = new ProcessFile();
		pf.compileBothDictionary();
		pf.readFile();
	}
}
