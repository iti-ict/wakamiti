import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

    public static class HttpResponse {
        public int statusCode;
        public String body;

        public HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    public HttpResponse authenticate(String clientId, String clientSecret) throws Exception {
        URL url = new URL("https://xray.cloud.getxray.app/api/v2/authenticate");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = "{ \"client_id\": \"" + clientId + "\", \"client_secret\": \"" + clientSecret + "\" }";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return new HttpResponse(code, response.toString());
    }

    public static void main(String[] args) {
        Main main = new Main();
        try {
            HttpResponse response = main.authenticate("", "");
            System.out.println("Código de respuesta: " + response.statusCode);
            System.out.println("Respuesta del servidor: " + response.body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
