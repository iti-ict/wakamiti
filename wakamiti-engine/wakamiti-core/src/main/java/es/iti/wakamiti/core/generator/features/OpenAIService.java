/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.generator.features;


import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.util.http.HttpClient;
import es.iti.wakamiti.core.generator.features.enums.ChatMessageRole;
import es.iti.wakamiti.core.generator.features.enums.ModelEnum;
import es.iti.wakamiti.core.generator.features.model.ChatCompletionRequest;
import es.iti.wakamiti.core.generator.features.model.ChatCompletionResult;
import es.iti.wakamiti.core.generator.features.model.ChatMessage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static es.iti.wakamiti.api.util.JsonUtils.read;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;


/**
 * Class for running OpenAI prompt
 */
public class OpenAIService extends HttpClient<OpenAIService> {

    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    private static final URL BASE_URL;

    static {
        try {
            BASE_URL = new URL("https://api.openai.com/v1");
        } catch (MalformedURLException e) {
            throw new FeatureGeneratorException(e.getMessage(), e);
        }
    }

    public OpenAIService() {
        super(BASE_URL);
        postCall(response -> {
            if (response.statusCode() >= 400) {
                throw new FeatureGeneratorException("Invalid HTTP response code: {}" + System.lineSeparator() + "{}",
                        response.statusCode(), response.body().map(JsonNode::toPrettyString).orElse(""));
            }
        });
    }

    /**
     * @param text Text to add to the prompt
     * @return The result of the AI generated feature text
     */
    public CompletableFuture<String> runPrompt(String text, String apiKey) {
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), text);
        ChatCompletionRequest chatCompletionRequest =
                new ChatCompletionRequest(ModelEnum.GPT_4_MINI.value(), Collections.singletonList(message));

        return newRequest()
                .header(AUTHORIZATION, AUTHORIZATION_PREFIX + apiKey)
                .body(json(chatCompletionRequest).toString())
                .postAsync("/chat/completions")
                .thenApply(response -> response.body()
                        .map(json -> read(json, ChatCompletionResult.class))
                        .filter(r -> !isEmpty(r.getChoices()))
                        .map(r -> r.getChoices().get(0).getMessage().getContent())
                        .orElseThrow());
    }
}
