package haxball;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Logger 
{
	public static String path = Utils.loggingFolder + "/log.txt";
	public static String tabs = "";
	public static DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss.SSS]	");

	public static void log(String logText)
	{
		Date date = new Date();
		
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true))))
		{
		    out.println(dateFormat.format(date) + tabs + logText);
		    System.out.println(tabs + logText);
		}
		catch (IOException e)
		{
			System.out.println("ERROR LOGGING IN " + logText);
		}
	}
	
	public static void log(String logText, boolean newLine)
	{
		Date date = new Date();
		
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true))))
		{
		    out.println(dateFormat.format(date) + tabs + logText);
		    System.out.println(tabs + logText);
		    
		    if(newLine)
		    {
		    	out.println();
		    	System.out.println();
		    }
		}
		catch (IOException e)
		{
			System.err.println("ERROR LOGGING IN " + logText);
		}
	}
	
	public static void addTab()
	{
		tabs += "\t";
	}
	
	public static void removeTab()
	{
		if(tabs.length() >= 1)
		{
			tabs = tabs.substring(1);			
		}
	}
	
	public static void logToFile(String filepath, String text)
	{
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filepath))))
		{
			out.println(text);
		}
		catch (IOException e)
		{
			System.err.println("ERROR LOGGING TO " + filepath);
		}
	}
}
