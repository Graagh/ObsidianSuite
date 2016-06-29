package MCEntityAnimator.distribution;

import java.io.IOException;
import java.util.List;

import MCEntityAnimator.MCEA_Main;

public class SaveLoadHandler 
{
	
	public static List<String> upload()
	{
		try 
		{		
			MCEA_Main.dataHandler.saveNBTData();
			return ServerAccess.uploadAll();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static void download()
	{
		try 
		{		
			ServerAccess.downloadAll();
			MCEA_Main.dataHandler.loadNBTData();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}
