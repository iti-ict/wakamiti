package es.iti.wakamiti.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class TestRunService {

    private static final String JIRA_URL = System.getenv("JIRA_URL");

    public static void updateTestRun(String jwt, int testRunId, String status, String comment, String assignee, String[] defects, Evidence[] evidences, Example[] examples) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            JsonObject payload = new JsonObject();
            payload.addProperty("status", status);
            payload.addProperty("comment", comment);
            payload.addProperty("assignee", assignee);

            JsonArray defectsArray = new JsonArray();
            for (String defect : defects) {
                defectsArray.add(defect);
            }
            JsonObject defectsObj = new JsonObject();
            defectsObj.add("add", defectsArray);
            payload.add("defects", defectsObj);

            JsonArray evidencesArray = new JsonArray();
            for (Evidence evidence : evidences) {
                JsonObject evidenceObj = new JsonObject();
                evidenceObj.addProperty("filename", evidence.filename);
                evidenceObj.addProperty("contentType", evidence.contentType);
                evidenceObj.addProperty("data", evidence.data);
                evidencesArray.add(evidenceObj);
            }
            JsonObject evidencesObj = new JsonObject();
            evidencesObj.add("add", evidencesArray);
            payload.add("evidences", evidencesObj);

            JsonArray examplesArray = new JsonArray();
            for (Example example : examples) {
                JsonObject exampleObj = new JsonObject();
                exampleObj.addProperty("id", example.id);
                exampleObj.addProperty("status", example.status);
                examplesArray.add(exampleObj);
            }
            payload.add("examples", examplesArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(JIRA_URL + "/rest/raven/1.0/api/testrun/" + testRunId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .PUT(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Test Run updated successfully.");
                System.out.println("Response: " + response.body());
            } else {
                System.out.println("Failed to update Test Run. HTTP error code: " + response.statusCode());
                System.out.println("Response: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
