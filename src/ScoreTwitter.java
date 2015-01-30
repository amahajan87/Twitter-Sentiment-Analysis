import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.json.JSONArray;

import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramProcessLM;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Rahul
 *
 */
public class ScoreTwitter {
	
	
	
	static String inputLocation = "C:\\Google\\TweetDataSet\\Stock\\201412";
	static String outputLocation = "C:\\Google\\TweetDataSet\\Stock\\Score";
    
    /**
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String args[]) throws IOException, ClassNotFoundException{
    	//inputLocation = args[0];
    	//outputLocation = args[1];
        String content = "empty yet";
        String inputLocationURL = inputLocation;
        String outputLocationURL = outputLocation;
        try {
            //creating ling pipe classifier
            String[] args1 = {"C:\\Google\\TweetDataSet\\Stock\\201412"};
            //String[] args1 = {"C:\\Users\\Apurv Mahajan\\Downloads\\PDS"};
            DynamicLMClassifier<NGramProcessLM> classifier = new PolarityBasic(args1).run();

        	HashMap<String, String> symbolList = new HashMap<>();
			String csvFile = "C:\\Google\\TweetDataSet\\Stock\\201412\\list.csv";
			//String csvFile = "C:\\Users\\Apurv Mahajan\\Downloads\\PDS\\list.csv";
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ",";
			try {
		 
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					String[] stockName = line.split(cvsSplitBy);
					symbolList.put(stockName[0],stockName[1]);
					//System.out.println(stockName[0] + " " + stockName[1]);
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
        	
			
		   File docFolder = new File(inputLocationURL);
            //create output directory
           File directoryOut =new File(outputLocationURL);
            if (!directoryOut.exists()) {
                if (directoryOut.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
           
            directoryOut =new File(outputLocationURL);
            if (!directoryOut.exists()) {
                if (directoryOut.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
            
            //create and init all the files for saving output
            String newLine = System.getProperty("line.separator");
            FileWriter f2 = new FileWriter(outputLocationURL + "/" + "Monthly_EachSentence" + "_Score");
            FileWriter f3 = new FileWriter(outputLocationURL + "/" + "Monthly_RelevantSentence" + "_Score");
            
            f2.write("filename :: positive_DocCount :: positive_Score :: negative_DocCount :: negative_score" + newLine);
            f3.write("filename :: positive_DocCount :: positive_Score :: negative_DocCount :: negative_score" + newLine);
            
            f2.flush();
            f3.flush();
            
            //starting stanford nlp
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, parse, sentiment, pos");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            
            Map<String,ResultCounter> monthlyAverageMap_RAW = new HashMap<String,ResultCounter>();
            Map<String,ResultCounter> monthlyAverageMap_Filtered = new HashMap<String,ResultCounter>();
            
            //scanning through files
            for (final File fileEntry : docFolder.listFiles()) {
                if(fileEntry.getName().equals("Thumbs.db") || fileEntry.getName().equals("list.csv")  ){
                    continue;
                } else if(fileEntry.isDirectory()){
                	continue;
                }
                FileWriter f0 = new FileWriter(outputLocationURL + "/" + fileEntry.getName() + "EachDocument_EachSentence" + "_Score");
                FileWriter f1 = new FileWriter(outputLocationURL + "/" + fileEntry.getName() + "EachDocument_RelevantSentence" + "_Score");
                f0.write("filename :: positive_Score :: negative_score" + newLine);
                f1.write("filename :: positive_Score :: negative_score" + newLine );
                f0.flush();
                f1.flush();

                System.out.println("Reading file " + fileEntry.getAbsolutePath());
                Scanner scanner = new Scanner(new File(fileEntry.getAbsolutePath()));
                try{
                	content = scanner.useDelimiter("\\z").next();
                	
                }
                catch(Exception e){
                	System.out.println("I have no idea why ::: \\Z Exception" + e + " " + fileEntry.getAbsolutePath());
                }
                
                List<Result> sentenceResultList_Raw = new ArrayList<Result>();
                List<Result> sentenceResultList_Filtered = new ArrayList<Result>();
                String stock = fileEntry.getName().subSequence(0,(fileEntry.getName().length()-4)).toString();
                try {
                    Annotation document = new Annotation(content);
                    
                    pipeline.annotate(document);
                    for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                        sentenceResultList_Raw.add(new Result(classifier.classify(sentence.toString()).conditionalProbability(0),classifier.classify(sentence.toString()).bestCategory()));
                        if(sentence.toString().contains(stock) || sentence.toString().contains(symbolList.get(stock)) || 
                                sentence.toString().contains("company") || sentence.toString().contains("price") || 
                                sentence.toString().contains("sale") || sentence.toString().contains("anounce") ||
                                sentence.toString().contains("launch") || sentence.toString().contains("profit") ||
                                sentence.toString().contains("new") || sentence.toString().contains("product")){
                            sentenceResultList_Filtered.add(new Result(classifier.classify(sentence.toString()).conditionalProbability(0),classifier.classify(sentence.toString()).bestCategory()));
                        }
                    }
                } catch (Throwable t) {
                    System.out.println("Thrown: " + t);
                    t.printStackTrace(System.out);
                }

                scanner.close();
                //counters for entire document
                ResultCounter documentResultCounter_RAW = printOutput(sentenceResultList_Raw);
                f0.write(fileEntry.getName() + " :: " + documentResultCounter_RAW.positiveAggrigate +" :: " + documentResultCounter_RAW.negativeAggrigate);
                f0.write(newLine);
                
                ResultCounter documentResultCounter_Filtered = printOutput(sentenceResultList_Filtered);
                f1.write(fileEntry.getName() + " :: " + documentResultCounter_Filtered.positiveAggrigate +" :: " + documentResultCounter_Filtered.negativeAggrigate);
                f1.write(newLine);
                f0.flush();
                f1.flush();
                f0.close();
                f1.close();
                calculateMonthlyAggrigate(monthlyAverageMap_RAW,
                        fileEntry, documentResultCounter_RAW);
                calculateMonthlyAggrigate(monthlyAverageMap_Filtered,
                        fileEntry, documentResultCounter_Filtered);
            }
            printMonthlyMapToFile(monthlyAverageMap_RAW,f2);
            printMonthlyMapToFile(monthlyAverageMap_Filtered,f3);
            f2.close();
            f3.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param monthlyAverageMap
     * @param file
     * @throws IOException
     */
    public static void printMonthlyMapToFile(Map<String,ResultCounter> monthlyAverageMap,FileWriter file) throws IOException{
        for(String key: monthlyAverageMap.keySet()){
            file.write(key + " :: " + 
                monthlyAverageMap.get(key).positiveDocumentCount + " :: " +
                monthlyAverageMap.get(key).positiveAggrigate + " :: " +
                monthlyAverageMap.get(key).negativeDocumentCount + " :: " + 
                monthlyAverageMap.get(key).negativeAggrigate);
            file.write(System.getProperty("line.separator"));
        }
    }
    
    /**
     * @param monthlyAverageMap
     * @param fileEntry
     * @param documentResultCounter_RAW
     * @return
     */
    private static String calculateMonthlyAggrigate(
            Map<String, ResultCounter> monthlyAverageMap, final File fileEntry,
            ResultCounter documentResultCounter_RAW) {
        
        String mothYear = getMonthAndYear(fileEntry.getName());
        ResultCounter monthlyCounter;
        if(monthlyAverageMap.get(mothYear) == null){
            monthlyCounter = new ResultCounter(documentResultCounter_RAW.negativeSentenceCount,documentResultCounter_RAW.positiveSentenceCount
                    , documentResultCounter_RAW.negativeAggrigate, documentResultCounter_RAW.positiveAggrigate);
            
            if(documentResultCounter_RAW.negativeSentenceCount > documentResultCounter_RAW.positiveSentenceCount){
                monthlyCounter.negativeDocumentCount ++;
            }else{
                monthlyCounter.positiveDocumentCount ++;
            }
            
            monthlyAverageMap.put(mothYear, monthlyCounter);
            System.out.println(monthlyAverageMap.toString());
        }else{
            monthlyCounter = monthlyAverageMap.get(mothYear);
            monthlyCounter.positiveSentenceCount = monthlyCounter.positiveSentenceCount + documentResultCounter_RAW.positiveSentenceCount;
            monthlyCounter.negativeSentenceCount = monthlyCounter.negativeSentenceCount + documentResultCounter_RAW.negativeSentenceCount;
            monthlyCounter.positiveAggrigate = monthlyCounter.positiveAggrigate + documentResultCounter_RAW.positiveAggrigate;
            monthlyCounter.negativeAggrigate = monthlyCounter.negativeAggrigate + documentResultCounter_RAW.negativeAggrigate;        
            
            if(documentResultCounter_RAW.negativeSentenceCount > documentResultCounter_RAW.positiveSentenceCount){
                monthlyCounter.negativeDocumentCount ++;
            }else{
                monthlyCounter.positiveDocumentCount ++;
            }
            monthlyAverageMap.put(mothYear, monthlyCounter);
        }
        return mothYear;
    }
    
    /**
     * @param filename
     * @return
     */
    private static String getMonthAndYear(String filename){
        return filename.substring(0, 6);
    }
    
    /**
     * @param output
     * @return
     */
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
            	System.out.println("Something went wrong");
            }
        }
        return resultCounters;
    }
}    
