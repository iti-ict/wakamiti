package es.iti.wakamiti.core.generator.features.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ChatFunctionCall {

    /**
     * The name of the function being called
     */
    String name;

    /**
     * The arguments of the call produced by the model, represented as a JsonNode for easy manipulation.
     */
    JsonNode arguments;

}