package es.iti.wakamiti.core.generator.features.enums;

/**
 * see {@link es.iti.wakamiti.core.generator.features.model.ChatMessage} documentation.
 */
public enum ModelEnum {
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    GPT_3_5_TURBO_0301("gpt-3.5-turbo-0301"),
    GPT_4_0314("gpt-4-0314"),
    GPT_4_32K("gpt-4-32k"),
    GPT_4_32K_0314("gpt-4-32k-0314"),
    GPT_4_1106_PREVIEW("gpt-4-1106-preview"),
    GPT_4("gpt-4");

    private final String value;

    ModelEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}