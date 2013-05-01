** It's an Eclipse Dynamic web-project.


** All the required jars are placed in the following folder: SentimenAnalysis\WebContent\WEB-INF\lib\


** Yelp Dataset
Location: SentimenAnalysis\WebContent\
File name: tmpNFvucr

** Dictionaries
Location: SentimenAnalysis\WebContent\
File name: ourdictionary.txt, SentiWordNet.txt, and subjclueslen1-HLTEMNLP05.txt

** File: review.txt
Location: SentimenAnalysis\WebContent\
Description: contains only reviews that we have extracted from the dataset. These reviews are processed before storing in it.



** File: noun.txt, and nouns.txt
Location: SentimenAnalysis\WebContent\
Description: Both contains the list of nouns extracted from the reviews.

** File: FrequencyCount.txt
Location: SentimenAnalysis\WebContent\
Description: COntains frequncy count of noun words obtained in file noun.txt
=============================================================
Code
Location: SentimenAnalysis\src\binit\

Package featureExtractor: It is used for finding nouns in review. It parses all the revies in the dataset. It's an implementation of part-2 of our project.

Package Stemmer: Porter code, for finding stem (root) of the words.

Package textcleam: For cleaning review text. We are removing ellipsis, extra full-stops, etc.

package tree: Conatains the code of actual sentiment analysis.
