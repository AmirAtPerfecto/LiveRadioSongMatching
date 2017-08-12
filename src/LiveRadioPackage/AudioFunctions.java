package LiveRadioPackage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.acrcloud.utils.ACRCloudRecognizer;


public abstract class AudioFunctions {

	public static String detectSong(String url){
		// Reads the audio file into memory
		String resultString = null;
		try {
			Path filePath = download(url);  
			
			ACRCloudRecognizer acr = new ACRCloudRecognizer();
			resultString = acr.recognizeByFile(filePath.toString(), 0);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultString;
	}
	public static void uploadSongToRepository(){
		try {
			PerfectoLabUtils.uploadMedia(System.getenv().get("PERFECTO_CLOUD"), System.getenv().get("PERFECTO_CLOUD_USERNAME"), System.getenv().get("PERFECTO_CLOUD_SECURITY_TOKEN"), System.getenv().get("Project_Path")+"/media/" + "song.wav", 
					System.getenv().get("PERFECTO_CLOUD_REPOSITORY_KEY")+ "song.wav");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
	private static Path download(String sourceURL) throws IOException
	{
	    URL url = new URL(sourceURL);
	    String fileName = sourceURL.substring(sourceURL.lastIndexOf('/') + 1, sourceURL.length());
	    Path targetPath = new File(System.getenv().get("Project_Path")+"/media/" + "song.wav").toPath();
	    Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

	    return targetPath;
	}
	

}





