

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ParseJSONData {
	
	
	public void decode(String input) {
		System.out.println("\nDecoding.....");
		//JSONParser parser = new JSONParser();S
		JSONObject tempObj	= new JSONObject();
		try {
			JSONObject obj = (JSONObject) new JSONTokener(input).nextValue();
			JSONArray textList = (JSONArray)obj.get("data");
			//System.out.println(textList);
			int iter = textList.length();
			for (int i = 0; i<iter; i++){
				tempObj = (JSONObject)textList.get(i);
				System.out.println(i + "   " + tempObj.get("polarity") + "   " + tempObj.get("text"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	

}
