package LiveRadioPackage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.acrcloud.utils.ACRCloudRecognizer;


public abstract class AudioFunctions {

	public static String audioToText(String url){
		// Reads the audio file into memory
		String resultString = null;
		try {
			Path filePath = download(url);  
			
			ACRCloudRecognizer acr = new ACRCloudRecognizer();
//			resultString = acr.recognizeByFile(System.getenv().get("Project_Path")+"92235d3d405030d4d95e1791379a624b6c0a874e989ca91b2ed5323a1dbed0d8.wav", 0);
			resultString = acr.recognizeByFile(filePath.toString(), 0);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultString;
	}
	private static Path download(String sourceURL) throws IOException
	{
	    URL url = new URL(sourceURL);
	    String fileName = sourceURL.substring(sourceURL.lastIndexOf('/') + 1, sourceURL.length());
	    Path targetPath = new File(System.getenv().get("Project_Path") + fileName).toPath();
	    Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

	    return targetPath;
	}

}





