import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HolidayCalenderReader 
{
	String HolidayFile = "Holiday.xml";
	HashMap<String,String> holidays = new HashMap<>();
	public void mapHolidays() 
	{	
		try
		{
			File fXmlFile = new File(HolidayFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();	
			NodeList nList = doc.getElementsByTagName("com.indigo.ccyconfig.beans.HolidayCalendarDataBean");
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element eElement = (Element) nNode;
					holidays.put(eElement.getElementsByTagName("date").item(0).getTextContent(), eElement.getElementsByTagName("currenciesList").item(0).getTextContent());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public String getCurrencyHoliday(String Date)
	{
		String Currencies= holidays.get(Date);
		return Currencies==null ? "":Currencies;
	}
}
