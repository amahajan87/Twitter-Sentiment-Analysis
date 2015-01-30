import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class MainController2 {
	static String inputLocationURL = "C:\\Users\\Apurv Mahajan\\Downloads\\Msft_data\\MS_CNN\\Microsoft";
	static String outputLocationURL = "C:\\Users\\Apurv Mahajan\\Downloads\\Msft_data\\scores\\MS_CNN\\Microsoft";
	
	public static void main(String args[]) throws IOException, ClassNotFoundException{
		//read the list of documents
		String content = "empty yet";
		try {
			
			File docFolder = new File(inputLocationURL);
			File directoryOut =new File(outputLocationURL);
			if (!directoryOut.exists()) {
				if (directoryOut.mkdirs()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			
			//creating ling pipe classifier
			String[] args1 = {"C:\\Users\\Apurv Mahajan\\Downloads\\Msft_data"};
			DynamicLMClassifier<NGramProcessLM> classifier = new PolarityBasic(args1).run();
			
			//starting stanford nlp
			Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, parse, sentiment, pos");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			
            
            Map<String,ResultCounter> monthlyAverageMap = new HashMap<String,ResultCounter>();
            
			//scanning through files
            for (final File fileEntry : docFolder.listFiles()) {
	    		if(fileEntry.getName().equals("Thumbs.db")){
	    			continue;
	    		}
	    		
	    		System.out.println("Reading file " + fileEntry.getAbsolutePath());
	    		//FileWriter f0 = new FileWriter(outputLocationURL + "\\" + fileEntry.getName() + "_Score");
	    		String newLine = System.getProperty("line.separator");
	    		
	    		Scanner scanner = new Scanner(new File(fileEntry.getAbsolutePath()));
				content = scanner.useDelimiter("\\Z").next();
				
				List<Result> sentenceResultList = new ArrayList<Result>();
		    	try {
		            Annotation document = new Annotation(content);
		            pipeline.annotate(document);
		            //System.out.println(document.get(CoreAnnotations.ParagraphsAnnotation.class));
		            for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
		                //Tree annotatedTree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
		                //f0.write(sentence + newLine);
		                //f0.write("LingPipe::" + classifier.classify(sentence.toString()).bestCategory() 
		                	//	+ "::" + classifier.classify(sentence.toString()).conditionalProbability(0) + newLine);
		                //f0.write(RNNCoreAnnotations.getPredictions(annotatedTree) + newLine);
		            	if(sentence.toString().contains("microsoft") || 
		            			sentence.toString().contains("msft") || 
		            			sentence.toString().contains("company") ){
		            		sentenceResultList.add(new Result(classifier.classify(sentence.toString()).conditionalProbability(0),classifier.classify(sentence.toString()).bestCategory()));
		            	}
		            }
		    	} catch (Throwable t) {
		            System.out.println("Thrown: " + t);
		            t.printStackTrace(System.out);
		        }

		    	//f0.close();
	    		scanner.close();
	    		ResultCounter documentResultCounter = printOutput(sentenceResultList);
	    		String mothYear = getMonthAndYear(fileEntry.getName());
	    		
	    		ResultCounter monthlyCounter;
	    		if(monthlyAverageMap.get(mothYear) == null){
	    			monthlyCounter = new ResultCounter(documentResultCounter.negativeSentenceCount,documentResultCounter.positiveSentenceCount
	    					, documentResultCounter.negativeAggrigate, documentResultCounter.positiveAggrigate);
	    			
	    			if(documentResultCounter.negativeSentenceCount > documentResultCounter.positiveSentenceCount){
	    				monthlyCounter.negativeDocumentCount ++;
	    			}else{
	    				monthlyCounter.positiveDocumentCount ++;
	    			}
	    			
	    			monthlyAverageMap.put(mothYear, monthlyCounter);
	    			System.out.println(monthlyAverageMap.toString());
	    		}else{
	    			monthlyCounter = monthlyAverageMap.get(mothYear);
	    			monthlyCounter.positiveSentenceCount = monthlyCounter.positiveSentenceCount + documentResultCounter.positiveSentenceCount;
	    			monthlyCounter.negativeSentenceCount = monthlyCounter.negativeSentenceCount + documentResultCounter.negativeSentenceCount;
	    			monthlyCounter.positiveAggrigate = monthlyCounter.positiveAggrigate + documentResultCounter.positiveAggrigate;
	    			monthlyCounter.negativeAggrigate = monthlyCounter.negativeAggrigate + documentResultCounter.negativeAggrigate;		
	    			
	    			if(documentResultCounter.negativeSentenceCount > documentResultCounter.positiveSentenceCount){
	    				monthlyCounter.negativeDocumentCount ++;
	    			}else{
	    				monthlyCounter.positiveDocumentCount ++;
	    			}
	    			monthlyAverageMap.put(mothYear, monthlyCounter);
	    		}
			}	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static String getMonthAndYear(String filename){
		return filename.substring(0, 6);
	}
	
	public static ResultCounter printOutput(List<Result> output){
		System.out.println(output.size());
		ResultCounter resultCounters = new ResultCounter(0,0,0.0,0.0);
		for(Result res : output){
			switch (res.getType()) {
	        case "pos":
	            resultCounters.positiveSentenceCount ++;
	            resultCounters.positiveAggrigate = resultCounters.positiveAggrigate + res.getScore();
	            break;
	        case "neg":
	        	resultCounters.negativeSentenceCount ++;
	            resultCounters.negativeAggrigate = resultCounters.negativeAggrigate + res.getScore();
	            break;
	        default:
	        }
		}
		/*System.out.println("POS:: " + resultCounters.positiveSentenceCount);
		System.out.println("NEG:: " + resultCounters.negativeSentenceCount);
		System.out.println("POS_VALUE:: " + resultCounters.positiveAggrigate);
		System.out.println("NEG_VALUE:: " + resultCounters.negativeAggrigate);*/
		return resultCounters;
	}
	
	/*private static String toCss(int sentiment) {
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
    }*/
}	
