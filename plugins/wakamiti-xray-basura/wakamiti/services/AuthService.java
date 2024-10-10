package es.iti.wakamiti.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;

public class AuthService {

    private static final String AUTH_URL = System.getenv("AUTH_URL");
    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");

    public static String getJWT() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            JsonObject authPayload = new JsonObject();
            authPayload.addProperty("client_id", CLIENT_ID);
            authPayload.addProperty("client_secret", CLIENT_SECRET);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(authPayload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Authentication successful.");
                return response.body().replace("\"", "");  // El token JWT
            } else {
                System.out.println("Failed to authenticate. HTTP error code: " + response.statusCode());
                System.out.println("Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
