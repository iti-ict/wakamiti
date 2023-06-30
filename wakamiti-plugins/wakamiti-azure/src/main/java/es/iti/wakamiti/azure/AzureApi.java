package es.iti.wakamiti.azure;

import com.jayway.jsonpath.JsonPath;
import es.iti.wakamiti.api.WakamitiException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

public class AzureApi {


    private final String urlBase;
    private final String credentials;
    private final String apiVersion;
    private final HttpClient httpClient;
    private final Logger logger;




    private static class ResultData {
        final String resultID;
        final String runID;
        public ResultData(String resultID, String runID) {
            this.resultID = resultID;
            this.runID = runID;
        }
    }



    public AzureApi(String urlBase, String user, String password, String apiVersion, Logger logger) {
        this.urlBase = urlBase;
        this.credentials = Base64.getEncoder().encodeToString((user+":"+password).getBytes(StandardCharsets.UTF_8));
        this.apiVersion = apiVersion;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
        this.logger = logger;
    }




    public String getPlanID(String planName) {
        String url = "/_apis/test/plans";
        String response = get(url);
        return extract(
            response,
            "$.value.[?(@.name == '" + planName + "')].id",
            "Cannot find a test plan with name '"+planName+"'"
        );
    }



    public String getTestSuiteID(String planName, String suiteName) {
        String planID = getPlanID(planName);
        String url = "/_apis/test/plans/"+planID+"/suites";
        String response = get(url);
        return extract(
            response,
            "$.value.[?(@.name == '" + suiteName + "')].id",
            "Cannot find a test suite with name '"+suiteName+"'"
        );
    }



    public String getTestCaseID(String planName, String suiteName, String testCaseName) {
        String planID = getPlanID(planName);
        String suiteID = getTestSuiteID(planName,suiteName);
        String url = "/_apis/test/plans/" + planID + "/suites/" + suiteID+ "/points";
        String response = get(url);
        return extract(
            response,
            "$..[?(@.name == '" + testCaseName + "')].id",
            "Cannot find a test case with name '"+testCaseName+"'"
        );
    }



    public String getTestPointID(String planName, String suiteName, String testCaseName) {
        String planID = getPlanID(planName);
        String suiteID = getTestSuiteID(planName,suiteName);
        String testCaseID = getTestCaseID(planName,suiteName,testCaseName);
        String url = "/_apis/test/plans/"+planID+"/suites/"+suiteID+"/points?testCaseId="+testCaseID;
        String response = get(url);
        return extract(
            response,
            "$.value.[0].id",
            "Cannot find a test point for the test case '"+testCaseName+"'"
        );
    }


    public String createRun(String planName, String suiteName, String testCaseName, String instant) {
        String runName = testCaseName + "-" + instant;
        String planID = getPlanID(planName);
        String pointID = getTestPointID(planName, suiteName, testCaseName);
        String url = "/_apis/test/runs";
        String payload = "{\"name\":\""+runName+"\",\"plan\":{\"id\":"+planID+"},\"pointIds\":["+pointID+"]}";
        String response = post(url,payload);
        return extract(
            response,
            "$.id",
            "Error creating a new run"
        );
    }




    public ResultData getTestResultID(String runID) {
        String url = "/_apis/test/runs/"+runID+"/results";
        String response = get(url);
        String resultID = extract(
            response,"$.value.[0].id","Cannot get the test result for the run"
        );
        return new ResultData(resultID,runID);
    }




    public void attachReport(String runID, Path report) {
        try {
            String reportEnconded = Base64.getEncoder().encodeToString(Files.readAllBytes(report));
            String urlFile = "/_apis/test/runs/" + runID + "/attachments";
            String payloadFile = "{ \"attachmentType\": \"GeneralAttachment\", \"comment\": \"Resultados ejecución\", \"fileName\": \"wakamiti.html\" , \"stream\": \"" + reportEnconded + "\" }";
            post(urlFile, payloadFile);
        } catch (IOException e) {
            throw new WakamitiException("Cannot post report attachment",e);
        }

    }



    public void updateTestResult(String runID, String status) {

        ResultData resultData = getTestResultID(runID);
        String resultID = resultData.resultID;
        String url = "/_apis/test/runs/"+runID+"/results";

        String payload;
        if (status.equalsIgnoreCase("PASSED")) {
            payload = "[{ \"id\": " + resultID + ",  \"outcome\": \"Passed\" ,    \"state\": \"Completed\",    \"comment\": \"Execution Successful\"  }]";
        } else if (status.equalsIgnoreCase("FAILED")) {
            payload = "[{ \"id\": " + resultID + ",  \"outcome\": \"Failed\" ,    \"state\": \"Completed\",    \"comment\": \"Execution Failed\"  }]";
        } else {
            payload = "[{ \"id\": " + resultID + ",  \"outcome\": \"Unspecified\" ,   \"state\": \"Completed\",    \"comment\": \"Execution Error\"  }]";
        }
        patch(url,payload);

    }



    private String extract(String json, String path, String errorMessage) {
        Object object = JsonPath.read(json, path);
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            if (list.isEmpty()) {
                throw new WakamitiException(errorMessage);
            }
            return list.get(0).toString();
        } else {
            return object.toString();
        }
    }




    private String get(String uri) {
        return send(HttpRequest.newBuilder().GET()
            .uri(url(uri))
            .header("Authorization","Basic "+credentials)
            .build(),
            ""
        );
    }



    private String post(String uri, String payload) {
        return send(HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(payload))
            .uri(url(uri))
            .header("Authorization", "Basic "+credentials)
            .header("Content-Type", "application/json")
            .build(),
            payload
        );
    }



    private String patch(String uri, String payload) {
        return send(HttpRequest.newBuilder().method("PATCH",HttpRequest.BodyPublishers.ofString(payload))
            .uri(url(uri))
            .header("Authorization", "Basic "+credentials)
            .header("Content-Type", "application/json")
            .build(),
            payload
        );
    }




    private URI url(String uri) {
        return URI.create(urlBase+uri+(uri.contains("?") ? "&" : "?")+"api-version="+apiVersion);
    }



    private String send(HttpRequest request, String payload) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Azure call => {} {} {} ", request.method(), request.uri(), payload);
            }
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (logger.isDebugEnabled()) {
                logger.debug("Azure response => {} {}", response.statusCode(), response.body());
            }
            if (response.statusCode() >= 400) {
                throw new WakamitiException("The Azure API returned a non-OK response");
            }
            return response.body();
        } catch (IOException e) {
            throw new WakamitiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WakamitiException(e);
        }
    }





}
