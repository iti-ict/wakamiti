package es.iti.wakamiti.core.generator.features;

import com.jayway.jsonpath.JsonPath;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.Wakamiti;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FeatureGenerator {

    private static final Logger LOGGER = WakamitiLogger.forClass(Wakamiti.class);

    private static final String FOLDER_SEPARATOR = "/";
    private static final String UNDERSCORE = "_";
    private static final String FEATURE_EXTENSION = ".feature";
    private static final String HTTP = "http";
    private static final String DEFAULT_PROMPT = "/generator/features/prompt.txt";

    private final HttpClient client = HttpClient.newHttpClient();

    private final OpenAIService openAIService;
    private final String apiKey;
    private final String apiDocs;
    private final String prompt;

    public FeatureGenerator(OpenAIService openAIService, String apiKey, String apiDocs) throws URISyntaxException, IOException {
        this.openAIService = openAIService;
        this.apiKey = apiKey;
        this.apiDocs = parseApiDocs(apiDocs);
        this.prompt = loadPrompt();
    }

    public FeatureGenerator(OpenAIService openAIService, String apiKey, String apiDocs, String prompt) throws URISyntaxException, IOException {
        this.openAIService = openAIService;
        this.apiKey = apiKey;
        this.apiDocs = parseApiDocs(apiDocs);
        this.prompt = prompt;
    }

    /**
     * Generates features by AI on the destination path
     *
     * @param destinationPath Destination path
     */
    public void generate(String destinationPath) {
        LOGGER.info("Feature generation started...");
        try {
            Path path = Path.of(destinationPath).toAbsolutePath();
            if (!Files.exists(path)) {
                throw new FeatureGeneratorException("No such directory: " + path);
            }

            Map<String, Object> endpoints = JsonPath.read(apiDocs, "$.paths");
            String finalApiDocs = apiDocs;
            endpoints.keySet().forEach(endpoint -> {
                Map<String, Object> methods = JsonPath.read(finalApiDocs, "$.paths." + endpoint);
                methods.keySet().forEach(method -> {
                    String fileName = method.concat(endpoint.replace(FOLDER_SEPARATOR, UNDERSCORE));
                    Path featurePath = Path.of(destinationPath, fileName + FEATURE_EXTENSION).toAbsolutePath();
                    String info = JsonPath.read(finalApiDocs, "$.paths." + endpoint + "." + method).toString();

                    createFeature(featurePath, info);
                });
            });
        } catch (Exception e) {
            throw new FeatureGeneratorException(e.getMessage(), e);
        }

    }


    /**
     * Parse the API docs from swagger. It can be a URL or a JSON.
     *
     * @param apiDocs API docs (file or url)
     * @return API docs JSON
     * @throws URISyntaxException Malformed API URI
     * @throws IOException        Error sending the request
     */
    private String parseApiDocs(String apiDocs) throws URISyntaxException, IOException {
        if (apiDocs.startsWith(HTTP)) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(apiDocs))
                        .GET()
                        .build();
                apiDocs = this.client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            } catch (IllegalArgumentException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FeatureGeneratorException(e.getMessage(), e);
            }
        } else {
            var file = new File(apiDocs);

            if (file.exists()) {
                apiDocs = String.join(" ", Files.readAllLines(file.toPath()));
            }

        }
        return apiDocs;
    }

    /**
     * Loads the default prompt from a resource file
     *
     * @return The loaded prompt
     * @throws IOException Error reading file
     */
    private String loadPrompt() throws IOException {
        InputStream in = getClass().getResourceAsStream(DEFAULT_PROMPT);
        byte[] data = IOUtils.toByteArray(in);
        return new String(data);
    }

    /**
     * Creates the feature file with the AI response text
     *
     * @param featurePath Path where the features have to be created
     * @param info        Swagger's endpoint info
     */
    private void createFeature(Path featurePath, String info) {
        if (!Files.exists(featurePath)) {
            try {
                Path path = Files.createFile(featurePath);
                String content = openAIService.runPrompt(prompt.concat(info), apiKey);
                Files.write(path, content.getBytes());
                LOGGER.info("File '{}' created", featurePath);

            } catch (IOException | URISyntaxException e) {
                LOGGER.trace("", e);
            }
        }
    }
}
