import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CSVReader 
{
	 String inputFile = "C:\\Users\\agangula\\Desktop\\FXSpotCalendar_NoSettlementUS_20180525.csv";
	 String Currency1="USD";
	 String Currency2 ="BWP";
	 String TradeDate = "20180830";
	 String Tenor = "SP";
	 List<String> matched;
	 HashMap<String,Integer> tenors = new HashMap<String, Integer>();
	CSVReader()
	{
		tenors.put( "SP" ,7);
		tenors.put( "1M" ,8);
		tenors.put( "2M",9);
		tenors.put("3M",10);
		tenors.put("4M",11);
		tenors.put( "5M",12);
		tenors.put( "6M",13);
		tenors.put( "7M",14);
		tenors.put( "8M",15);
		tenors.put("9M",16);
		tenors.put("10M",17);
		tenors.put( "11M",18);
		tenors.put("1Y",19);
		tenors.put( "2Y",20);
		tenors.put( "5Y",21);
		
	}
	public static void main(String arg[])
	{
		new CSVReader().getTradeDate();
	}
	public void getTradeDate()
	{
		try 
		{
			matched =Files.lines(Paths.get(inputFile))
				.filter(entry -> entry.contains(Currency1))
				.filter(entry -> entry.contains(Currency2))
				.filter(entry -> entry.contains(TradeDate))
				.collect(Collectors.toList());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(getDate(Tenor));
	}
	public String getDate(String Tenor)
	{
		for(String entry:matched)
		{
			String Cell[] = entry.split(",");
			if(Cell[0].equals(Currency1) && Cell[2].equals(Currency2)&&Cell[6].equals(TradeDate))
			{
				return Cell[tenors.get(Tenor)];
			}
		}
		return Tenor;
	}
}
