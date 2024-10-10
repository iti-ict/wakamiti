package es.iti.wakamiti.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;

public class TestRepositoryService {

    public static void createTestFolder(String jwt, String projectKey, String folderName, String parentFolderId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            JsonObject payload = new JsonObject();
            payload.addProperty("name", folderName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://xxxxxxxxxxxxxxx.atlassian.net/rest/raven/1.0/api/testrepository/" + projectKey + "/folders/" + parentFolderId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("Test Repository folder created successfully.");
                System.out.println("Response: " + response.body());
            } else {
                System.out.println("Failed to create Test Repository folder. HTTP error code: " + response.statusCode());
                System.out.println("Response: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
