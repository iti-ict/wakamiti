package es.iti.wakamiti.core.generator.features.enums;

/**
 * see {@link es.iti.wakamiti.core.generator.features.model.ChatMessage} documentation.
 */
public enum ChatMessageRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    FUNCTION("function");

    private final String value;

    ChatMessageRole(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}