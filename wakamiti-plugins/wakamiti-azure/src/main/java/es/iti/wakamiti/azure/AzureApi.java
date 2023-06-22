package es.iti.wakamiti.azure;

import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AzureApi {


    private final String urlBase;
    private String credentials;
    private String planApiVersion;
    private String runApiVersion;


    private final HttpClient httpClient;



    private static class PlanDetails {
        final String planID;
        final String suiteID;
        public PlanDetails(String planID, String suiteID) {
            this.planID = planID;
            this.suiteID = suiteID;
        }
    }



    AzureApi(String urlBase, String credentials) {
        this.urlBase = urlBase;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    }




    public PlanDetails getTestPlanDetails(String planName) {
        String url = "/_apis/test/plans?api-version="+planApiVersion;
        JsonNode response = get(url);
        String planID = response.findPath("$.value.[?(@.name == '" + planName + "')].id").get(0).asText();
        String suiteID = response.findPath("$.value.[?(@.name == '" + planName + "')].rootSuite.id").get(0).asText();
        return new PlanDetails(planID,suiteID);
    }



    public String getTestSuiteDetails(String planName, String suiteName) {
        PlanDetails planDetails = getTestPlanDetails(planName);
        String url = "/_apis/test/plans/"+planDetails.planID+"/suites??api-version="+planApiVersion;
        JsonNode response = get(url);
        String suiteID = response.findPath("$.value.[?(@.name == '" + suiteName + "')].id").get(0).asText();
        return suiteID;
    }



    public void getTestCaseID() {

    }



    public void getTestPointID() {

    }


    public void createRun() {



    }



    public void getTestResultID() {


    }



    public void updateResult() {


    }




    private JsonNode get(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create(uri))
                .header("Authorization",credentials)
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return JsonUtils.json(response.body());
        } catch (IOException e) {
            throw new WakamitiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WakamitiException(e);
        }
    }



}
