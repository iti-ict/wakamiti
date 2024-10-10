package es.iti.wakamiti.xray;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class XrayApi {

    private final String jiraUrl;
    private final String email;
    private final String apiToken;
    private final HttpClient httpClient;
    private final Logger logger;

    public XrayApi(String jiraUrl, String email, String apiToken, Logger logger) {
        this.jiraUrl = jiraUrl;
        this.email = email;
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder().build();
        this.logger = logger;
    }

    private String getAuthHeader() {
        String auth = email + ":" + apiToken;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    public String createTestExecution(String testExecutionName, String description) {
        try {
            JsonObject payload = new JsonObject();
            JsonObject project = new JsonObject();
            project.addProperty("key", System.getenv("PROJECT_KEY"));
            JsonObject fields = new JsonObject();
            fields.add("project", project);
            fields.addProperty("summary", testExecutionName);

            JsonObject descriptionContent = new JsonObject();
            descriptionContent.addProperty("type", "text");
            descriptionContent.addProperty("text", description);
            JsonObject paragraph = new JsonObject();
            paragraph.addProperty("type", "paragraph");
            paragraph.add("content", descriptionContent);
            JsonObject doc = new JsonObject();
            doc.addProperty("type", "doc");
            doc.addProperty("version", 1);
            doc.add("content", paragraph);
            fields.add("description", doc);

            JsonObject issueType = new JsonObject();
            issueType.addProperty("name", "Test Execution");
            fields.add("issuetype", issueType);
            payload.add("fields", fields);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jiraUrl + "/rest/api/3/issue"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", getAuthHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                logger.info("Test Execution created successfully.");
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonResponse.get("key").getAsString();
            } else {
                logger.error("Failed to create Test Execution. HTTP error code: " + response.statusCode());
                logger.error("Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error creating Test Execution", e);
            return null;
        }
    }

    public String createTestPlan(String testPlanName, String description) {
        // TODO TestPlanService
    }

    public void createTestFolder(String jwt, String projectKey, String folderName, String parentFolderId) {
        // TODO TestRepositoryService
    }

    public void updateTestRun(String jwt, int testRunId, String status, String comment, String assignee, String[] defects, Evidence[] evidences, Example[] examples) {
        // TODO TestRunService
    }

    public String createTestSet(String testSetName, String description) {
        // TODO TestSetService
    }

    private String sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                logger.error("Request failed. HTTP error code: " + response.statusCode());
                logger.error("Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error sending request", e);
            return null;
        }
    }
}
