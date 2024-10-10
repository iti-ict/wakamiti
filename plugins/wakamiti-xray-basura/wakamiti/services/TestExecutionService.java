package es.iti.wakamiti.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Base64;

public class TestExecutionService {

    private static final String JIRA_URL = System.getenv("JIRA_URL");
    private static final String PROJECT_KEY = System.getenv("PROJECT_KEY");
    private static final String API_TOKEN = System.getenv("API_TOKEN");
    private static final String EMAIL = System.getenv("EMAIL");

    public static String createTestExecution(String testExecutionName, String description) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            JsonObject payload = new JsonObject();
            JsonObject project = new JsonObject();
            project.addProperty("key", PROJECT_KEY);
            JsonObject fields = new JsonObject();
            fields.add("project", project);
            fields.addProperty("summary", testExecutionName);

            JsonObject descriptionContent = new JsonObject();
            descriptionContent.addProperty("type", "text");
            descriptionContent.addProperty("text", description);
            JsonObject paragraph = new JsonObject();
            paragraph.addProperty("type", "paragraph");
            JsonArray contentArray = new JsonArray();
            contentArray.add(descriptionContent);
            paragraph.add("content", contentArray);
            JsonObject doc = new JsonObject();
            doc.addProperty("type", "doc");
            doc.addProperty("version", 1);
            JsonArray docContentArray = new JsonArray();
            docContentArray.add(paragraph);
            doc.add("content", docContentArray);

            fields.add("description", doc);
            JsonObject issueType = new JsonObject();
            issueType.addProperty("name", "Test Execution");
            fields.add("issuetype", issueType);
            payload.add("fields", fields);

            String auth = EMAIL + ":" + API_TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(JIRA_URL + "/rest/api/3/issue"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                System.out.println("Test Execution created successfully.");
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonResponse.get("key").getAsString();
            } else {
                System.out.println("Failed to create Test Execution. HTTP error code: " + response.statusCode());
                System.out.println("Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
