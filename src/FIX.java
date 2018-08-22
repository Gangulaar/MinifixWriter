
public class FIX 
{
	public static char FIXDELIMITER = '\u0001';
	public static char PARSED_DELIMITER = '|';
	public void setDelimiter(String line)
	{
		if(line.contains(FIXDELIMITER+""))
			return;
		else
			FIXDELIMITER = '|';
	}
	public static String getParsedLog(String Line)
	{
		return Line.replace(FIX.FIXDELIMITER, FIX.PARSED_DELIMITER);
	}

}