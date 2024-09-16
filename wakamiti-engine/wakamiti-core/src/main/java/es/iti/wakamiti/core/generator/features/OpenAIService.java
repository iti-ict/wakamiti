package es.iti.wakamiti.core.generator.features;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iti.wakamiti.core.generator.features.enums.ChatMessageRole;
import es.iti.wakamiti.core.generator.features.enums.ModelEnum;
import es.iti.wakamiti.core.generator.features.model.ChatCompletionRequest;
import es.iti.wakamiti.core.generator.features.model.ChatCompletionResult;
import es.iti.wakamiti.core.generator.features.model.ChatMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

/**
 * Class for running OpenAI prompt
 */
public class OpenAIService {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_JSON_VALUE = "application/ json";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public OpenAIService() {
        // Empty constructor
    }

    /**
     * @param text Text to add to the prompt
     * @return The result of the AI generated feature text
     * @throws URISyntaxException      Malformed API URI
     * @throws JsonProcessingException Response processing error
     */
    public String runPrompt(String text, String apiKey) throws URISyntaxException, JsonProcessingException {
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), text);
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(ModelEnum.GPT_4.value(), Collections.singletonList(message));

        HttpRequest request = HttpRequest.newBuilder()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, AUTHORIZATION_PREFIX + apiKey)
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
}
