package archer;

import java.io.File;

public final class Utils
{
	public static String loggingFolder = "C:/Users/bjg/Desktop/neat";
	
	public static void createFolderIfNotExists(String folderPath)
	{
		File f = new File(folderPath);
		if(!f.exists())
		{
			f.mkdir();
		}
	}
}
