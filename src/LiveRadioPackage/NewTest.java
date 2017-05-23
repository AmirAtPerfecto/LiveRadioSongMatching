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
		webDriver = Utils.getRemoteWebDriver("Windows", "10", "Chrome", "58", "1280x1024" );        
         reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
// Only for Mac!
         System.load(System.getenv().get("Project_Path")+ "libacrcloud_extr_tool.dylib");
	} 
  @Test
  public void test() {
      try {
    	  reportiumClient.testStart("Live Radio", new TestContext("tag2", "tag3"));
    	  System.out.println("Yay");

    	  // Load the website, get the details for the live stream there
    	  webDriver.get("http://kiss108.iheart.com/");
    	  webDriver.findElementByClassName("listen-live-svg").click();
    	  
    	  // Start recording from the device
    	  String audioFileRecording = PerfectoUtils.startAudioRecording(driver);
    	  Thread.sleep(20000);
    	  
    	  // Grab the meta data from the website
    	  String songNameOnWeb = webDriver.findElementByClassName("player-song").getAttribute("title");
    	  String artistNameOnWeb = webDriver.findElementByClassName("player-artist").getAttribute("title");
    	  PerfectoUtils.stopAudioRecording(driver);
    	  ReportingFileDownload.downloadAttachments();
    	  String songData = AudioFunctions.audioToText(audioFileRecording);
    	  System.out.println("Song Data: " +songData);
    	  System.out.println("Audio File: " +audioFileRecording);

    	  
    	  if (songData.toLowerCase().contains(songNameOnWeb.toLowerCase()))
    		  System.out.println("Found Song! " +songNameOnWeb);
    	  else
    		  System.out.println("Not Found Song! web:" +songNameOnWeb);

    	  if (songData.toLowerCase().contains(artistNameOnWeb.toLowerCase()))
    		  System.out.println("Found artist! " +artistNameOnWeb);
    	  else
    		  System.out.println("Not Found artist! web:" +artistNameOnWeb);
  			
  			
    	  // grab meta data from device
    	String songNameOnDevice = driver.findElementByXPath("//AppiumAUT/UIAApplication[1]/UIAWindow[1]/UIAElement[1]/UIAScrollView[1]/UIAElement[1]/UIAStaticText[1]").getText();  
    	String artistNameOnDevice = driver.findElementByXPath("//AppiumAUT/UIAApplication[1]/UIAWindow[1]/UIAElement[1]/UIAScrollView[1]/UIAElement[1]/UIAStaticText[2]").getText();
    	System.out.println("Song Name on Device:" +songNameOnDevice);  
    	System.out.println("Artist Name on Device:" +artistNameOnDevice);  
    	
    	if (songData.toLowerCase().contains(songNameOnDevice.toLowerCase())){
        	System.out.println("Song Name on Device matches actual song played!:" +songNameOnDevice);
        	PerfectoUtils.comment(driver, "Song Name on Device matches actual song played!:" +songNameOnDevice);
    	} else {
        	System.out.println("Song Name on Device does not matche actual song played!:" +songNameOnDevice);
        	PerfectoUtils.comment(driver, "Song Name on Device does not matche actual song played!:" +songNameOnDevice);
    	}
    	if (songData.toLowerCase().contains(artistNameOnDevice.toLowerCase())){
        	System.out.println("Artists Name on Device matches actual song played!:" +songNameOnDevice);
        	PerfectoUtils.comment(driver, "Artists Name on Device matches actual song played!:" +songNameOnDevice);
    	} else {
        	System.out.println("Artists Name on Device does not matche actual song played!:" +songNameOnDevice);
        	PerfectoUtils.comment(driver, "Artists Name on Device does not matche actual song played!:" +songNameOnDevice);
    	}
    		
    	  
    	  

          // write your code here

          // reportiumClient.testStep("step1"); // this is a logical step for reporting
          // reportiumClient.testStep("step2");

          reportiumClient.testStop(TestResultFactory.createSuccess());
      } catch (Exception e) {
          reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e));
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
