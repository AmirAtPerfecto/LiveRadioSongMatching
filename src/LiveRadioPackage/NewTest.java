package LiveRadioPackage;

import java.io.IOException;
import java.net.MalformedURLException;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.Job;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;

public class NewTest {
	RemoteWebDriver driver, webDriver;
	PerfectoExecutionContext perfectoExecutionContext;
	ReportiumClient reportiumClient;
	
	@Parameters({ "platformName", "platformVersion", "manufacturer", "model", "deviceName", "appID" })
	@BeforeTest
	public void beforeTest(String platformName, String platformVersion, String manufacturer, String model, String deviceName, String appID) throws IOException {
		driver = Utils.getRemoteWebDriver(platformName, platformVersion, manufacturer, model, deviceName, appID );        
        PerfectoExecutionContext perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
                .withProject(new Project("My Project", "1.0"))
                .withJob(new Job("My Job", 45))
                .withContextTags("tag1")
                .withWebDriver(driver)
                .build();
         reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
// Only for Mac!
         System.load(System.getenv().get("Project_Path")+ "libacrcloud_extr_tool.dylib");
	} 
  @Test
  public void test() {
      try {
    	  reportiumClient.testStart("Live Radio", new TestContext("tag2", "tag3"));
    	  
    	  // Start recording from the device
    	  String audioFileRecording = PerfectoUtils.startAudioRecording(driver);
    	  // grab meta data from device
    	  Thread.sleep(15000);
    	     	  
    	  PerfectoUtils.stopAudioRecording(driver);
    	  String songNameOnDevice = driver.findElementByXPath("//AppiumAUT/UIAApplication[1]/UIAWindow[1]/UIAElement[3]/UIAScrollView[1]/UIAElement[1]/UIAStaticText[1]").getText();  
    	  String artistNameOnDevice = driver.findElementByXPath("//AppiumAUT/UIAApplication[1]/UIAWindow[1]/UIAElement[3]/UIAScrollView[1]/UIAElement[1]/UIAStaticText[2]").getText();
    	  System.out.println("Song Name on Device:" +songNameOnDevice);  
    	  System.out.println("Artist Name on Device:" +artistNameOnDevice);  
    	  Thread.sleep(5000);

    	  // Detect the song
    	  String songData = AudioFunctions.detectSong(audioFileRecording);
    	  System.out.println("Song Data: " +songData);
    	  System.out.println("Audio File: " +audioFileRecording);

    	    			
// Let's have fun with Shazam
    	  AudioFunctions.uploadSongToRepository();
    	  try {
			PerfectoUtils.closeApp(driver, "Shazam");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  Thread.sleep(500);
    	  PerfectoUtils.launchApp(driver, "Shazam");
    	  
    	  PerfectoUtils.ocrTextCheck(driver, "Discover", 95, 20);
    	  PerfectoUtils.ocrImageSelect(driver, "PUBLIC:Amir/Shazam_icon.png");
    	  
    	  Thread.sleep(500);
    	  PerfectoUtils.injectAudio(driver, System.getenv().get("PERFECTO_CLOUD_REPOSITORY_KEY")+ "song.wav");
    	  
    	  // Let's wait until the song is detected
    	  PerfectoUtils.ocrTextCheck(driver, "Buy", 99, 20);
    	  Thread.sleep(1000);
    	  
    	  // We need to go to "My Shazam" to find the song that was detected..
    	  driver.findElementByXPath("//*[@label=\"navigation back\"]").click();
    	  
    	  
    	  PerfectoUtils.ocrTextCheck(driver, "Discover", 99, 20);
    	  
    	  PerfectoUtils.swipe(driver, "10%", "10%", "90%", "10%");
    	  
    	  PerfectoUtils.ocrTextCheck(driver, "save", 99, 20);
    	  String attribute1 = driver.findElementByXPath("//AppiumAUT/UIAApplication[1]/UIAWindow[1]/UIAScrollView[1]/UIATableView[1]/UIATableCell[1]/UIAStaticText[1]").getAttribute("text");
    	  String attribute2 = driver.findElementByXPath("//AppiumAUT/UIAApplication[1]/UIAWindow[1]/UIAScrollView[1]/UIATableView[1]/UIATableCell[1]/UIAStaticText[2]").getAttribute("text");

    	  System.out.println("Song Name on Device:" +songNameOnDevice);  
    	  System.out.println("Artist Name on Device:" +artistNameOnDevice);  
    	  System.out.println("Shazam Song name " + attribute1);
    	  System.out.println("Shazam Artist name " + attribute2);
    	  
    	  

          reportiumClient.testStop(TestResultFactory.createSuccess());
      } catch (Exception e) {
          //reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e));
          e.printStackTrace();
      }
  }

  @AfterTest
  public void afterTest() {
      try {
          // Retrieve the URL of the Single Test Report, can be saved to your execution summary and used to download the report at a later point
          String reportURL = reportiumClient.getReportUrl();

          // For documentation on how to export reporting PDF, see https://github.com/perfectocode/samples/wiki/reporting
          // String reportPdfUrl = (String)(driver.getCapabilities().getCapability("reportPdfUrl"));

          driver.close();
          System.out.println("Report: "+ reportURL);


          // In case you want to download the report or the report attachments, do it here.
          // PerfectoLabUtils.downloadAttachment(driver, "video", "C:\\test\\report\\video", "flv");
          // PerfectoLabUtils.downloadAttachment(driver, "image", "C:\\test\\report\\images", "jpg");

      } catch (Exception e) {
          e.printStackTrace();
      }

      driver.quit();
      webDriver.quit();
  }

}
