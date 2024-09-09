package es.iti.wakamiti.core.generator.features;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.generator.features.enums.ChatMessageRole;
import es.iti.wakamiti.core.generator.features.enums.ModelEnum;
import es.iti.wakamiti.core.generator.features.model.ChatCompletionRequest;
import es.iti.wakamiti.core.generator.features.model.ChatCompletionResult;
import es.iti.wakamiti.core.generator.features.model.ChatMessage;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class FeatureGenerator {

    private static final Logger LOGGER = WakamitiLogger.forClass(Wakamiti.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_JSON_VALUE = "application/ json";

    private static final String FOLDER_SEPARATOR = "/";
    private static final String UNDERSCORE = "_";
    private static final String FEATURE_EXTENSION = ".feature";
    private static final String HTTP = "http";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    private static final String DEFAULT_PROMPT = "generator/features/prompt.txt";

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private final String apiKey;
    private final String apiDocs;
    private final String prompt;


    public FeatureGenerator(String apiKey, String apiDocs) throws URISyntaxException, IOException {
        this.apiKey = apiKey;
        this.apiDocs = parseApiDocs(apiDocs);
        this.prompt = loadPrompt();
    }

    public FeatureGenerator(String apiKey, String apiDocs, String prompt) throws URISyntaxException, IOException {
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

        Path path = Path.of(destinationPath).toAbsolutePath();
        if (!Files.exists(path)) {
            throw new WakamitiException("No such directory: " + path);
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

    }

    /**
     * @param text Text to add to the prompt
     * @return The result of the AI generated feature text
     * @throws URISyntaxException      Malformed API URI
     * @throws JsonProcessingException Response processing error
     */
    private String runPrompt(String text) throws URISyntaxException, JsonProcessingException {
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), text);
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(ModelEnum.GPT_4.value(), Collections.singletonList(message));

        HttpRequest request = HttpRequest.newBuilder()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, AUTHORIZATION_PREFIX + this.apiKey)
                .uri(new URI(API_URL))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(chatCompletionRequest)))
                .build();

        StringBuilder result = new StringBuilder();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(result::append)
                .join();

        ChatCompletionResult completionResult = mapper.readValue(result.toString(), ChatCompletionResult.class);

        return completionResult.getChoices().isEmpty() ? "" : completionResult.getChoices().get(0).getMessage().getContent();
    }

    /**
     * Parse the API docs from swagger. It can be a URL or a JSON.
     *
     * @param apiDocs API docs
     * @return API docs JSON
     * @throws URISyntaxException Malformed API URI
     * @throws IOException        Error sending the request
     */
    private String parseApiDocs(String apiDocs) throws URISyntaxException, IOException {
        if (apiDocs.startsWith(HTTP)) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiDocs))
                    .GET()
                    .build();
            try {
                apiDocs = this.client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            } catch (InterruptedException e) {
                LOGGER.trace("", e);
                Thread.currentThread().interrupt();
            }
        }
        return apiDocs;
    }

    /**
     * Loads the default prompt from a resource file
     *
     * @return The loaded prompt
     * @throws IOException        Error reading file
     * @throws URISyntaxException Malformed API URI
     */
    private String loadPrompt() throws IOException, URISyntaxException {
        return Files.readString(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(DEFAULT_PROMPT)).toURI()));
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
                String content = runPrompt(prompt.concat(info));
                Files.write(path, content.getBytes());

            } catch (IOException | URISyntaxException e) {
                LOGGER.trace("", e);
            }
        }
    }
}
