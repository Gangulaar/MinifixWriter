


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JSONToObj {

	@SuppressWarnings("unchecked")
	public static void main(String ar[]) {
		String jsonString = null;
		try {
			jsonString = readFile("C:\\Users\\agangula\\eclipse-workspace\\FCT\\cache.json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Start");
		Object obj = JSONValue.parse(jsonString);
		JSONObject jsonObject = (JSONObject) obj;
		for (int i = 1; i <= jsonObject.size(); i++) {
			JSONObject single = (JSONObject) jsonObject.get("trans" + i);
			//JSONArray ja = new JSONArray();
			List<String> inner = (List<String>) single.get("Order");
			System.out.println();
			System.out.println("Order");
			for(String trans : inner)
				System.out.println(trans);
			System.out.println();
			System.out.println("Request");
			inner = (List<String>) single.get("Request");
			for(String trans : inner)
				System.out.println(trans);
			System.out.println("---------------------------------------------------");
		}

		JSONArray ja = new JSONArray();
		// ja.add(object1.get("Response"));
		for (Object response : ja)
			System.out.println(response.toString());

		// qr.add(new QuoteResponse().set((String)ja.get(0)));
	}

	static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}
}
