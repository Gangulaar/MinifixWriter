import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MinifixWriter {
	static Transaction t[];
	private final String NewOrder = "NewOrderSingle";
	private final String Exec = "ExecutionReport";
	private final String Send = "Send";
	private final String Colon = ":";
	private final String Wait = "wait";
	private final String LastIn = "$LASTIN";
	private final String Lastout = "$LASTOUT";
	private final String Unique = "$UNIQUE";
	private final String Date = "$DATE";
	private final String Timestamp = "$TIMESTAMP";
	public static String inputFile;
	public ArrayList<String> ExecTransaction;
	public ArrayList<String> InboundTransaction;
	public ArrayList<String> QuoteResponse;
	public ArrayList<String> QuoteRequest;
	public ArrayList<String> OrderTransaction;
	public static int ID;
	private static String workingDir = "C:\\MiniFIX";
	private String InboundFile = "\\Inbound.txt";
	private String OutboundFile = "\\Outbound.txt";
	private String TradeDate;
	int diff;
	FIXHandler response, execution, inbound;
	static FIXHandler trans;
	String SP;
	final DateTimeFormatter formatter;
	HolidayCalenderReader hcr;
	Boolean dateChanged;

	static String outputFile;
	static String minifixFile;

	MinifixWriter() {
		diff = 0;
		SP = "SP";
		TradeDate = new String();
		response = new FIXHandler();
		execution = new FIXHandler();
		formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		hcr = new HolidayCalenderReader();
		hcr.mapHolidays();
		dateChanged = false;
		createFile(outputFile);
	}

	public void truncateFile(File file) {
		FileWriter fw;
		try {
			fw = new FileWriter(file, false);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createFile(String f) {
		try {
			File file = new File(f);
			if (file.exists()) {
				truncateFile(file);
			} else {
				Files.createFile(Paths.get(f));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String arg[]) {
		if (arg.length == 4) {
			trans = new FIXHandler();
			inputFile = arg[0];
			ID = Integer.parseInt(arg[1]);
			outputFile = arg[2];
			workingDir = arg[3];
			MinifixWriter mw = new MinifixWriter();
			mw.readJSON(inputFile, ID);
			System.out.println("Done");
		} else {
			System.out.println("Invalid input");
			System.exit(0);
		}
	}

	@SuppressWarnings("unchecked")
	private void readJSON(String inputFile, int id) {
		createHeader();
		HashMap<String, Integer> map = new HashMap<>();
		String jsonString = null;
		try {
			jsonString = readFile(inputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Started reading input file...");

		ArrayList<String> i_ccy = new ArrayList<>();
		ArrayList<String> i_price = new ArrayList<>();
		ArrayList<String> i_side = new ArrayList<>();
		ArrayList<String> i_qty = new ArrayList<>();

		ArrayList<String> i_quote_id = new ArrayList<>();
		ArrayList<String> i_round_qty = new ArrayList<>();
		ArrayList<String> i_trade_type = new ArrayList<>();
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		JSONObject req_trans = (JSONObject) jsonObject.get("trans" + id);
		ArrayList<String> orders = (ArrayList<String>) req_trans.get("Order");
		ArrayList<String> requests = (ArrayList<String>) req_trans.get("Request");
		ArrayList<String> responses = (ArrayList<String>) req_trans.get("Response");
		ArrayList<String> execs = (ArrayList<String>) req_trans.get("Execution");
		ArrayList<String> i_symbol = new ArrayList<>();
		int size_orders = orders.size();
		if (size_orders == 0)
			i_symbol.add(getValue(requests.get(0), "55"));
		for (int i = 0; i < size_orders; i++) {
			i_ccy.add(getValue(orders.get(i), "15"));
			i_symbol.add(getValue(orders.get(i), "55"));
			i_qty.add(getValue(orders.get(i), "38"));
			i_side.add(getValue(orders.get(i), "54"));
			i_price.add(getValue(orders.get(i), "44"));
			i_quote_id.add(getValue(orders.get(i), "117"));
			i_round_qty.add(round(getValue(orders.get(i), "38")));
			i_trade_type.add(decide(getValue(orders.get(i), "55"), getValue(orders.get(i), "15")));
		}
		int i = 0;
		for (String exec : execs) {
			execManipulator(exec);
			map.put(execution.getValueForKey("37"), i++);
		}
		writeFiles(workingDir + OutboundFile, Wait + Colon + "35=R");
		for (String resp : responses) {

			String res = responseManipultor(resp);
			writeOutbound(res, NewOrder);
			if (map.containsKey(response.getValueForKey("117"))) {
				System.out.println("found");
				writeFiles(workingDir + OutboundFile, Wait + Colon + "35=D");
				writeOutbound(execManipulator(execs.get(map.get(response.getValueForKey("117")))), Exec);
			}
		}
		for (i = 0; i < greater(requests.size(), orders.size()); i++) {
			String entry = (requests.size() + "," + get(requests, i) + "," + size_orders + "," + get(orders, i) + ","
					+ get(i_price, i) + "," + get(i_side, i) + "," + get(i_qty, i) + "," + get(i_symbol, i) + ","
					+ get(i_ccy, i) + "," + get(i_quote_id, i) + "," + get(i_round_qty, i) + "," + get(i_trade_type, i)
					+ "\n");
			try {
				Files.write(Paths.get(MinifixWriter.outputFile), entry.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeFiles(String inputFile, String tags) {
		try {
			Files.write(Paths.get(inputFile), (tags + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}

	private String decide(String symbol, String ccy) {
		return symbol.split("/")[0].equals(ccy) ? "Base" : "Trem";
	}

	private String round(String value) {		
		float float_value = (float) Integer.parseInt(value) / 1000000;
		int scale = (int) Math.pow(10, 1);
		return Double.toString((double) Math.round(float_value * scale) / scale);
	}

	private void createHeader() {
		ArrayList<String> header = new ArrayList<>();
		header.add("Number_Request");
		header.add("i_Request");
		header.add("Number_Orders");
		header.add("i_orders");
		header.add("i_price");
		header.add("i_side");
		header.add("i_qty");
		header.add("i_symbol");
		header.add("i_ccy");
		header.add("i_Quote_id");
		header.add("i_round_qty");
		header.add("i_trade_type");
		for (String cell : header)
			try {
				Files.write(Paths.get(MinifixWriter.outputFile), (cell + ",").getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		try {
			Files.write(Paths.get(MinifixWriter.outputFile), ("\n").getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String get(ArrayList<String> array, int i) {
		try {
			return array.get(i);
		} catch (Exception e) {
			return "";
		}
	}

	private int greater(int size_request, int size_orders) {

		return size_request < size_orders ? size_orders : size_request;
	}

	private String getValue(String string, String key) {
		int fromIndex = string.indexOf(FIX.PARSED_DELIMITER + key + "=") + (2 + key.length());
		return string.substring(fromIndex, string.indexOf(FIX.PARSED_DELIMITER, fromIndex));
	}

	public void inboundManipulator() {
		// InboundTransaction += "64=" + TradeDate + FIX.PARSED_DELIMITER;
	}

	public String responseManipultor(String res) {
		res = response.cleanMessage(res);
		response.splitMesage(res);
		res = removeTags(res, response, "8", "9", "10", "56", "49", "52", "34", "10");
		if (response.getValueForKey("35").equals("S")) {
			TradeDate = getLatestDate(response.getValueForKey("52"), response.getValueForKey("64"),
					response.getValueForKey("55"));
			res = changeTags(res, response, "131=" + getLastIn("R", "131"), "64=" + TradeDate);
		}
		return res;
	}

	public String execManipulator(String exec) {
		exec = execution.cleanMessage(exec);
		execution.splitMesage(exec);
		exec = removeTags(exec, execution, "8", "9", "56", "49", "52", "34", "10");
		exec = changeTags(exec, execution, "11=" + getLastIn("D", "11"), "17=" + Unique, "64=" + TradeDate,
				"75=" + Date, "60=" + Timestamp);
		return exec;
	}

	public String removeTags(String inputTrans, FIXHandler obj, String... tags) {
		for (String tag : tags) {
			inputTrans = inputTrans.replace(tag + "=" + obj.getValueForKey(tag) + FIX.PARSED_DELIMITER, "");
		}
		return inputTrans;
	}

	public String changeTags(String inputTrans, FIXHandler obj, String... tags) {
		for (String tag : tags) {
			String key = tag.substring(0, tag.indexOf("="));
			inputTrans = inputTrans.replace(FIX.PARSED_DELIMITER + key + "=" + obj.getValueForKey(key),
					FIX.PARSED_DELIMITER + tag);
		}
		return inputTrans;

	}

	public void writeInbound() {
		inboundManipulator();
		writeFiles(workingDir + InboundFile, Send + Colon + NewOrder + Colon + InboundTransaction);
	}

	public void writeOutbound(String respose, String profile) {
		writeFiles(workingDir + OutboundFile, "Sleep" + Colon + "100");
		writeFiles(workingDir + OutboundFile, Send + Colon + profile + Colon + respose);
	}

	public String getLastIn(String TranType, String key) {
		return LastIn + "[" + TranType + "," + key + "]";
	}

	public String getLastOut(String TranType, String key) {
		return Lastout + "[" + TranType + "," + key + "]";
	}

	private String getPresentDate() {
		return LocalDate.now().format(formatter);
	}

	private int getDateDifference(String SendingTime, String SettlementTime) {
		return (int) ChronoUnit.DAYS.between(LocalDate.parse(SendingTime, formatter),
				LocalDate.parse(SettlementTime, formatter));
	}

	private String getFutureDates(String PresentDate, int additionalDates) {
		return LocalDate.parse(PresentDate, formatter).plusDays(additionalDates).format(formatter);
	}

	private String getLatestDate(String SendingTime, String SettlementTime, String Currency) {
		diff = getDateDifference(
				LocalDate.parse(SendingTime, DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")).format(formatter),
				SettlementTime);
		return reCaluculateDate(getFutureDates(getPresentDate(), diff), Currency);
	}

	private String reCaluculateDate(String Date, String Currency) {
		Date = checkForHolidays(Date, Currency);
		Date = checkForWeekends(Date);
		if (dateChanged) {
			dateChanged = false;
			Date = reCaluculateDate(Date, Currency);
		}
		return Date;
	}

	private String checkForHolidays(String Date, String CurrencyPairs) {
		String setOfCurrencies = hcr.getCurrencyHoliday(Date);
		String FinalDate = Date;
		if (setOfCurrencies.equals(null)) {
			String allCurrency[] = setOfCurrencies.split(",");
			String CurrPairs[] = CurrencyPairs.split("/");
			for (String BaseOrTerm : CurrPairs) {
				if (Arrays.asList(allCurrency).contains(BaseOrTerm)) {
					FinalDate = getFutureDates(Date, 1);
					System.out.println("Holiday");
					dateChanged = true;
				}
			}
		}
		return FinalDate;
	}

	private String checkForWeekends(String GivenDate) {
		String day = null;
		String FinalDate = GivenDate;
		try {
			Date givenDateFormat = new SimpleDateFormat("yyyyMMdd").parse(GivenDate);
			SimpleDateFormat DayFormat = new SimpleDateFormat("E");
			day = DayFormat.format(givenDateFormat);
			if (day.equals("Sun")) {
				dateChanged = true;
				System.out.println("Sunday");
				FinalDate = getFutureDates(GivenDate, 1);
			} else if (day.equals("Sat")) {
				System.out.println("Saturday");
				dateChanged = true;
				FinalDate = getFutureDates(GivenDate, 2);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return FinalDate;
	}

}
