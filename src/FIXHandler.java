
import java.util.Hashtable;

public class FIXHandler {
	Hashtable<String, String> FIXmsg;

	private Boolean findFixTagPresence(String fixTag) {
		for (String temp : FIXmsg.keySet()) {
			if (temp.equals(fixTag)) {
				return true;
			}

		}
		return false;
	}

	public void splitMesage(String msg) {
		/*
		 * message should be in the format of tag=value|tag=value with not extra symbols
		 * Will save the data to FIXmsg - Hashtable
		 */
		FIXmsg = new Hashtable<String, String>();
		String transaction[] = msg.split(("\\" + FIX.PARSED_DELIMITER));
		for (String temp : transaction) {
			String TagValue[] = temp.split("=");
			// System.out.println(TagValue[0]+" : "+TagValue[1]);
			FIXmsg.put(TagValue[0], TagValue[1]);
		}

	}

	public void displayAllValues() {
		for (String temp : FIXmsg.keySet()) {
			String value = FIXmsg.get(temp);
			System.out.println(temp + " = " + value);
		}
	}

	public String getValueForKey(String key) {
		if (!findFixTagPresence(key)) {
			System.out.println(key + " isn't found in the message");
			return "";
		}
		return FIXmsg.get(key);
	}

	public String cleanMessage(String msg) {
		String intialTag = "[8=FIX";
		int index = msg.indexOf(intialTag);
		msg = msg.substring(index + 1, msg.length() - 2);
		return msg;
	}

}
