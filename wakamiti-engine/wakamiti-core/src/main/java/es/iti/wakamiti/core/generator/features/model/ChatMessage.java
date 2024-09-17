package es.iti.wakamiti.core.generator.features.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    /**
     * Must be either 'system', 'user', 'assistant' or 'function'.<br>
     * You may use {@link es.iti.wakamiti.core.generator.features.enums.ChatMessageRole} enum.
     */
    @JsonProperty("role")
    String role;
    @JsonProperty("content")
    String content;
    @JsonProperty("name")
    String name;

    public ChatMessage() {
        // Empty constructor
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String role, String content, String name) {
        this.role = role;
        this.content = content;
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}