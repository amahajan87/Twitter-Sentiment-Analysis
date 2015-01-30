import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Rahul
 *
 */
public class FindTopOccuringWordsinTechDocs {
	static String inputLocationURL = "C:\\Users\\Rahul\\Desktop\\Msft_data\\MS_CNN\\Microsoft";
	static String outputLocationURL = "C:\\Users\\Rahul\\Desktop\\Msft_data\\words\\Microsoft\\MS_CNN";

	static Map<String, Integer> wordMap = new HashMap<String, Integer>();
	static ArrayList<String> sortList = new ArrayList<String>();
	static String newLine = System.getProperty("line.separator");
	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String args[]) throws IOException, ClassNotFoundException{
		String content = "empty yet";
		try {
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

			//create and init all the files for saving output
			FileWriter f0 = new FileWriter(outputLocationURL + "\\" + "Top_Words" + "_Score");

			//starting stanford nlp
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, parse, sentiment, pos");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			int count = 0;
			for (final File fileEntry : docFolder.listFiles()) {
				if(fileEntry.getName().equals("Thumbs.db")){
					continue;
				}
				/*count ++;
				if(count == 20){
					break;
				}*/
				System.out.println("Reading file " + fileEntry.getAbsolutePath());
				Scanner scanner = new Scanner(new File(fileEntry.getAbsolutePath()));
				content = scanner.useDelimiter("\\Z").next();

				try {
					Annotation document = new Annotation(content);
					pipeline.annotate(document);
					for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
						String[] abc = sentence.toString().split(" ");
						addToMap(abc);
					}
				}catch (Throwable t) {
					System.out.println("Thrown: " + t);
					t.printStackTrace(System.out);
				}
				scanner.close();
			}
			
			sortMap();
			printMap(f0);
			f0.flush();
			f0.close();
		}catch(IOException e){

		}
	}
	
	public static void sortMap(){
		System.out.println("sortMAp");
		for(String key1: wordMap.keySet()){
			String index = key1;
			for(String key2: wordMap.keySet()){
				if ((wordMap.get(key2) > wordMap.get(index)) & !sortList.contains(key2)) 
                    index = key2;
			}
			sortList.add(index);
			System.out.println(index +" :: "+ wordMap.get(index));
			if(wordMap.get(index)<5){
				break;
			}
		}
	}
	
	public static void printMap(FileWriter file) throws IOException{
		System.out.println("printSortedMAp");
		for(String key: sortList){
			file.write(key + " :: " + wordMap.get(key) + newLine);
		}
	}
	
	public static void addToMap(String[] temp){
		System.out.println("addToMap");
		for(int i=0;i<temp.length;i++){
			if(wordMap.containsKey(temp[i])){
				int count = wordMap.get(temp[i]);
				count++;
				wordMap.put(temp[i], count);
			}else{
				wordMap.put(temp[i], 1);
			}
		}
	}
}