package es.iti.wakamiti.core.generator.features.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionChoice {

    /**
     * This index of this completion in the returned list.
     */
    Integer index;

    /**
     * The {@link es.iti.wakamiti.core.generator.features.enums.ChatMessageRole#ASSISTANT} message or delta (when streaming) which was generated
     */
    @JsonAlias("delta")
    ChatMessage message;

    /**
     * The reason why GPT stopped generating, for example "length".
     */
    @JsonProperty("finish_reason")
    String finishReason;


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}