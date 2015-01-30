import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramProcessLM;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class MainController {
	static String inputLocationURL = "C:\\Users\\Rahul\\Desktop\\Msft_data\\MS_CNN\\Microsoft";
	
	public static void main(String args[]){
		//read the list of documents
		String content = "empty yet";
		try {
			
			File docFolder = new File(inputLocationURL);
			for (final File fileEntry : docFolder.listFiles()) {
	    		if(fileEntry.getName().equals("Thumbs.db")){
	    			continue;
	    		}
	    		System.out.println("reading file" + fileEntry.getAbsolutePath());
	    		Scanner scanner = new Scanner(new File(fileEntry.getAbsolutePath()));
				content = scanner.useDelimiter("\\Z").next();
	    		//System.out.println(content);
	    		/*List<Result> output =*/ 
				String[] args1 = {"C:\\Users\\Rahul\\Desktop\\Msft_data\\Reviews"};
		    	try {
		    		DynamicLMClassifier<NGramProcessLM> classifier = new PolarityBasic(args1).run();
		    		List<Integer> scores = new ArrayList<>();
		    		Properties props = new Properties();
		            props.put("annotators", "tokenize, ssplit, pos, parse, sentiment");
		            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		            Annotation document = new Annotation(content);
		            pipeline.annotate(document);
		            for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
		                Tree annotatedTree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
		                int score = RNNCoreAnnotations.getPredictedClass(annotatedTree);
		                scores.add(score);
		                System.out.println(sentence);
		                System.out.println("Stan_NLP::" + toCss(score));
		                System.out.println("LingPipe::" + classifier.classify(sentence.toString()).bestCategory() 
		                		+ "::" + classifier.classify(sentence.toString()).conditionalProbability(0));
		            }
		    		System.out.println(classifier.classify("this movie is awesome").bestCategory());
		    		System.out.println(classifier.classify("this movie is awesome").category(0));
		    		System.out.println(classifier.classify("this movie is awesome").conditionalProbability(0));
		    		System.out.println(classifier.classify("this movie is awesome").jointLog2Probability(0));
		    		System.out.println(classifier.classify("this movie is awesome").score(0));
		        
		    	
		    	} catch (Throwable t) {
		            System.out.println("Thrown: " + t);
		            t.printStackTrace(System.out);
		        }
				
	    		//printOutput(output);
	    		scanner.close();
	    		break;
			}	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//convert t0 string
		//print sentiments
	}
	
	/*public static void printOutput(List<Result> output){
		System.out.println(output.size());
		ResultCounter resultCounters = new ResultCounter(0,0,0,0,0);
		for(Result res : output){
			//System.out.println("Sentence:" + res.getSentence());
			//System.out.println("Score:" + res.getScore());
			switch (res.getScore()) {
	        case 1:
	            resultCounters.veryNegativeCount ++;
	            break;
	        case 2:
	            resultCounters.negativeCount ++;
	            break;
	        case 3:
	            resultCounters.neutralCount ++;
	            break;
	        case 4:
	            resultCounters.positiveCount ++;
	            break;
	        case 5:
	            resultCounters.veryPositiveCount ++;
	            break;
	        default:
	        }
		}
		System.out.println(resultCounters.veryNegativeCount);
		System.out.println(resultCounters.negativeCount);
		System.out.println(resultCounters.neutralCount);
		System.out.println(resultCounters.positiveCount);
		System.out.println(resultCounters.veryPositiveCount);
	}*/
	
/*	public static void printOutput(List<Result> output){
		System.out.println(output.size());
		ResultCounter resultCounters = new ResultCounter(0,0,0,0,0);
		for(Result res : output){
			switch (res.getScore()) {
	        case 1:
	            resultCounters.negativeCount ++;
	            break;
	        case 2:
	            resultCounters.positiveCount ++;
	            break;
	        default:
	        }
		}
		System.out.println(resultCounters.negativeCount);
		System.out.println(resultCounters.positiveCount);
	}
*/	
	private static String toCss(int sentiment) {
        switch (sentiment) {
        case 1:
            return "alert very-negative";
        case 2:
            return "alert negative";
        case 3:
            return "alert neutral";
        case 4:
            return "alert positive";
        case 5:
            return "alert very-positive";
        default:
            return "";
        }
    }
}	
