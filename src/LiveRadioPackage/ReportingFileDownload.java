package LiveRadioPackage;


import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public abstract class ReportingFileDownload {
    // The Perfecto Continuous Quality Lab you work with
    //public static String CQL_NAME = "demo";

    // The reporting Server address depends on the location of the lab. Please refer to the documentation at
    // http://developers.perfectomobile.com/display/PD/Reporting#Reporting-ReportingserverAccessingthereports to find your relevant address
    // For example the following is used for US:
    public static String REPORTING_SERVER_URL;

    // See http://developers.perfectomobile.com/display/PD/Using+the+Reporting+Public+API on how to obtain an Offline Token
    public static String OFFLINE_TOKEN;

    public static String CQL_SERVER_URL;


    public static void downloadAttachments() throws Exception {
    	OFFLINE_TOKEN = System.getenv().get("PERFECTO_CLOUD_SECURITY_TOKEN");
    	CQL_SERVER_URL = "https://"+System.getenv().get("PERFECTO_CLOUD");
    	REPORTING_SERVER_URL = "https://"+System.getenv().get("PERFECTO_CLOUD").toLowerCase().replace(".perfectomobile.com", ".reporting.perfectomobile.com");
    	
    	// Retrieve a list of the test executions in your lab (as a json)
        JsonObject executions = retrieveTestExecutions();

        JsonArray resources = executions.getAsJsonArray("resources");
        if (resources.size() == 0) {
            System.out.println("there are no test executions for that period of time");
        } else {
            JsonObject testExecution = resources.get(0).getAsJsonObject();
            String testId = testExecution.get("id").getAsString();
            String driverExecutionId = testExecution.get("externalId").getAsString();

            // Retrieves a list of commands of a single test (as a json)
            //retrieveTestCommands(testId);

            // Download an execution summary PDF report of an execution (may contain several tests)
            //downloadExecutionSummaryReport(driverExecutionId);

            // Download a PDF report of a single test
            //downloadTestReport(testId);

            // Download video
            //downloadVideo(testExecution);

            // Download attachments such as device logs, vitals or network files (relevant for Mobile tests only)
            downloadAttachments(testExecution);
        }
    }

    private static JsonObject retrieveTestExecutions() throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(REPORTING_SERVER_URL + "/export/api/v1/test-executions");

        // Optional: Filter by range. In this example: retrieve test executions of the past month (result may contain tests of multiple driver executions)
        uriBuilder.addParameter("startExecutionTime[0]", Long.toString(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)));
        uriBuilder.addParameter("endExecutionTime[0]", Long.toString(System.currentTimeMillis()));

        // Optional: Filter by a specific driver execution ID that you can obtain at script execution
        // uriBuilder.addParameter("externalId[0]", "SOME_DRIVER_EXECUTION_ID");

        HttpGet getExecutions = new HttpGet(uriBuilder.build());
        addDefaultRequestHeaders(getExecutions);
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpResponse getExecutionsResponse = httpClient.execute(getExecutions);
        JsonObject executions;
        try (InputStreamReader inputStreamReader = new InputStreamReader(getExecutionsResponse.getEntity().getContent())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String response = IOUtils.toString(inputStreamReader);
            try {
                executions = gson.fromJson(response, JsonObject.class);
            } catch (JsonSyntaxException e) {
                throw new RuntimeException("Unable to parse response: " + response);
            }
            System.out.println("\nList of test executions response:\n" + gson.toJson(executions));
        }
        return executions;
    }

    private static void retrieveTestCommands(String testId)
            throws URISyntaxException, IOException {
        HttpGet getCommands = new HttpGet(new URI(REPORTING_SERVER_URL + "/export/api/v1/test-executions/" + testId + "/commands"));
        addDefaultRequestHeaders(getCommands);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse getCommandsResponse = httpClient.execute(getCommands);
        try (InputStreamReader inputStreamReader = new InputStreamReader(getCommandsResponse.getEntity().getContent())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject commands = gson.fromJson(IOUtils.toString(inputStreamReader), JsonObject.class);
            System.out.println("\nList of commands response:\n" + gson.toJson(commands));
        }
    }

    private static void downloadExecutionSummaryReport(String driverExecutionId)
            throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(REPORTING_SERVER_URL + "/export/api/v1/test-executions/pdf");
        uriBuilder.addParameter("externalId[0]", driverExecutionId);
        downloadFileAuthenticated(driverExecutionId, uriBuilder.build(), ".pdf", "execution summary PDF report");
    }

    private static void downloadTestReport(String testId)
            throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(REPORTING_SERVER_URL + "/export/api/v1/test-executions/pdf/" + testId);
        downloadFileAuthenticated(testId, uriBuilder.build(), ".pdf", "test PDF report");
    }

    private static void downloadVideo(JsonObject testExecution) throws IOException, URISyntaxException {
        JsonArray videos = testExecution.getAsJsonArray("videos");
        if (videos.size() > 0) {
            JsonObject video = videos.get(0).getAsJsonObject();
            String downloadVideoUrl = video.get("downloadUrl").getAsString();
            String format = "." + video.get("format").getAsString();
            String testId = testExecution.get("id").getAsString();
            downloadFile(testId, URI.create(downloadVideoUrl), format, "video");
        } else {
            System.out.println("\nNo videos found for test execution");
        }
    }

    private static void downloadAttachments(JsonObject testExecution)
            throws IOException, URISyntaxException {
        // Example for downloading device logs
        JsonArray artifacts = testExecution.getAsJsonArray("artifacts");
        for (JsonElement artifactElement : artifacts) {
            JsonObject artifact = artifactElement.getAsJsonObject();
            String artifactType = artifact.get("type").getAsString();
            if (artifactType.equals("DEVICE_LOGS")) {
                String testId = testExecution.get("id").getAsString();
                String path = artifact.get("path").getAsString();
                URIBuilder uriBuilder = new URIBuilder(path);
                downloadFile(testId, uriBuilder.build(), ".zip", "device logs");
            }
        }
    }


    // Utils

    private static void downloadFile(String fileName, URI uri, String suffix, String description)
            throws IOException {
        downloadFileToFS(new HttpGet(uri), fileName, suffix, description);
    }

    private static void downloadFileAuthenticated(String fileName, URI uri, String suffix, String description)
            throws IOException {
        HttpGet httpGet = new HttpGet(uri);
        addDefaultRequestHeaders(httpGet);
        downloadFileToFS(httpGet, fileName, suffix, description);
    }

    private static void downloadFileToFS(HttpGet httpGet, String fileName, String suffix, String description)
            throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(httpGet);
        FileOutputStream fileOutputStream = null;
        try {
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                Path file = Files.createTempFile(fileName, suffix);
                fileOutputStream = new FileOutputStream(file.toFile());
                IOUtils.copy(response.getEntity().getContent(), fileOutputStream);
                System.out.println("\nSaved " + description + " to: " + file.toFile().getAbsolutePath());
            } else {
                String errorMsg = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                System.err.println("Error downloading file. Status: " + response.getStatusLine() + ".\nInfo: " + errorMsg);
            }
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    private static void addDefaultRequestHeaders(HttpRequestBase request) {
        request.addHeader("PERFECTO_AUTHORIZATION", OFFLINE_TOKEN);
    }
}