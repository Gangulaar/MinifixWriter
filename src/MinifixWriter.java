import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MinifixWriter 
{
	static Transaction t[];
	private final String NewOrder = "NewOrderSingle"; 
	private final String Exec = "ExecutionReport";
	private final String Send = "Send";
	@SuppressWarnings("unused")
	private final String Expect = "Expect";
	private final String Colon = ":";
	private final String Wait = "wait";
	private final String LastIn = "$LASTIN";
	private final String Lastout = "$LASTOUT";
	private final String Unique = "$UNIQUE";
	private final String Date = "$DATE";
	private final String Timestamp = "$TIMESTAMP";
	public static String inputFile;
	public String ExecTransaction;
	public String InboundTransaction;
	public String QuoteResponse;
	public String QuoteRequest;
	public String OrderTransaction;
	public static int ID;
	private final String workingDir = "C:\\MiniFIX\\";
	private final String InboundFile = "Inbound.txt";
	private final String OutboundFile = "Outbound.txt";
	private String TradeDate;
	int diff;
	FIXHandler response,execution,inbound;
	static FIXHandler trans;
	String SP;
	final DateTimeFormatter formatter;
	MinifixWriter()
	{
		diff =0;
		SP ="SP";
		TradeDate = new String();
		response = new FIXHandler();
		execution = new FIXHandler();
		formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	}
	public static void main(String arg[])
	{
		if(arg.length == 2)
		{
			trans = new FIXHandler();
			inputFile = arg[0];
			ID= Integer.parseInt(arg[1])-1;
			MinifixWriter mw =new MinifixWriter();
			mw.readXml(inputFile,ID);
			
			mw.responseManipultor();
			mw.execManipulator();
			mw.WriteInbound();
			mw.WriteOutbound();
		}
		else
		{
			System.out.println("Invalid input");
			System.exit(0);
		}
		//System.out.println(new MinifixWriter().getFutureDates("20180830",2));
	}
	public void writeFiles (String inputFile,String tags)
	{
		try 
		{
			Files.write(Paths.get(inputFile), (tags+"\n").getBytes(),StandardOpenOption.APPEND);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public void readXml(String inputFile, int ID)
	{
		try
		{
			File fXmlFile = new File(MinifixWriter.inputFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();	
			NodeList nList = doc.getElementsByTagName("Transaction");
			Node nNode = nList.item(ID);
			Element e = (Element)nNode;
			ExecTransaction = e.getElementsByTagName("Execution").item(0).getTextContent();
			QuoteResponse = e.getElementsByTagName("QuoteResponse").item(0).getTextContent();
			OrderTransaction = e.getElementsByTagName("Order").item(0).getTextContent();
			QuoteRequest = e.getElementsByTagName("QuoteRequest").item(0).getTextContent();
			InboundTransaction = e.getElementsByTagName("Inboundorder").item(0).getTextContent();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void inboundManipulator()
	{
		InboundTransaction += "64="+ TradeDate + FIX.PARSED_DELIMITER;
	}
	public void responseManipultor()
	{
		QuoteResponse = response.cleanMessage(QuoteResponse);
		response.splitMesage(QuoteResponse);
		TradeDate = getLatestDate(response.getValueForKey("52"),response.getValueForKey("64"));
		QuoteResponse= removeTags(QuoteResponse,response,"8","9","10","56","49","52","34","10");
		QuoteResponse =changeTags(QuoteResponse,response,"131="+getLastIn("R","131"),"64="+TradeDate);
	}
	public void execManipulator()
	{
		ExecTransaction = execution.cleanMessage(ExecTransaction);
		execution.splitMesage(ExecTransaction);
		ExecTransaction = removeTags(ExecTransaction, execution, "8","9","56","49","52","34","10");
		ExecTransaction = changeTags(ExecTransaction, execution, "11="+getLastIn("D", "11"),"17="+Unique,"64="+TradeDate,"75="+Date,"60="+Timestamp);
	}
	public String removeTags(String inputTrans,FIXHandler obj, String... tags)
	{
		for(String tag: tags)
		{
			inputTrans = inputTrans.replace(tag+"="+obj.getValueForKey(tag)+FIX.PARSED_DELIMITER, "");
		}
		return inputTrans;
	}
	public String changeTags(String inputTrans,FIXHandler obj,String... tags)
	{
		for(String tag: tags)
		{
			String key = tag.substring(0,tag.indexOf("="));
			inputTrans = inputTrans.replace(FIX.PARSED_DELIMITER+key+"="+obj.getValueForKey(key),FIX.PARSED_DELIMITER+tag);
		}
		return inputTrans;
		
	}
	public void WriteInbound()
	{
		inboundManipulator();
		writeFiles(workingDir+InboundFile,Send+Colon+NewOrder+Colon+InboundTransaction);
	}
	public void WriteOutbound()
	{
		writeFiles(workingDir+OutboundFile,Wait+Colon+"35=R");
		writeFiles(workingDir+OutboundFile,Send+Colon+NewOrder+Colon+QuoteResponse);
		writeFiles(workingDir+OutboundFile,Wait+Colon+"35=D");
		writeFiles(workingDir+OutboundFile,Send+Colon+Exec+Colon+ExecTransaction);
	}
	public String getLastIn(String TranType,String key)
	{
		return LastIn+"["+TranType+","+key+"]";
	}
	public String getLastOut(String TranType,String key)
	{
		return Lastout+"["+TranType+","+key+"]";
	}
	@SuppressWarnings("unused")
	private String convertTimeToDate(String Time)
	{
		try
		{
			
			Date date = new SimpleDateFormat("yyyyMMdd-HH:mm:ss").parse(Time);
			String newDate = new SimpleDateFormat("yyyyMMdd").format(date);
			//System.out.println(newDate);
			return newDate;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	private String getPresentDate()
	{
		return LocalDate.now().format(formatter);		
	}
	private int getDateDifference(String SendingTime,String SettlementTime)
	{
		return (int) ChronoUnit.DAYS.between(LocalDate.parse(SendingTime, formatter), LocalDate.parse(SettlementTime, formatter));
	}
	private String getFutureDates(String PresentDate,int additionalDates)
	{
		return LocalDate.parse(PresentDate, formatter).plusDays(additionalDates).format(formatter);
	}
	private String getLatestDate(String SendingTime,String SettlementTime)
	{
		diff = getDateDifference(LocalDate.parse(SendingTime, DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")).format(formatter),SettlementTime);
		return getFutureDates(getPresentDate(),diff);
	}
	
}

