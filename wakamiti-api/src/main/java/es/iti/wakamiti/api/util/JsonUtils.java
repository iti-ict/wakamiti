/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Utility class for working with JSON data.
 *
 * <p>This class provides methods for converting JSON strings, InputStreams, and objects into {@link JsonNode}.
 * It also includes a method for reading string values from a JsonNode based on a JSONPath expression.</p>
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class JsonUtils {

    private final static Configuration CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();
    private final static ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parses the given JSON string into a JsonNode.
     *
     * @param input The JSON string to parse.
     * @return The parsed JsonNode.
     * @throws JsonRuntimeException     If there is an issue parsing the JSON string.
     * @throws IllegalArgumentException If the JSON string represents a single value.
     */
    public static JsonNode json(String input) {
        try {
            JsonNode result = MAPPER.readTree(input);
            if (result instanceof ValueNode) {
                throw new IllegalArgumentException("Single value not allowed");
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new JsonRuntimeException(e);
        }
    }

    /**
     * Reads the JSON content from the given InputStream and parses it into a JsonNode.
     *
     * @param input The InputStream containing the JSON content.
     * @return The parsed JsonNode.
     * @throws JsonRuntimeException     If there is an issue reading or parsing the JSON content.
     * @throws IllegalArgumentException If the JSON content represents a single value.
     */
    public static JsonNode json(InputStream input) {
        try {
            JsonNode result = MAPPER.readTree(input);
            if (result instanceof ValueNode) {
                throw new IllegalArgumentException("Single value not allowed");
            }
            return result;
        } catch (IOException e) {
            throw new JsonRuntimeException(e);
        }
    }

    /**
     * Converts the given object into a JSON string and then parses it into a JsonNode.
     *
     * @param input The object to convert to JSON.
     * @return The parsed JsonNode.
     * @throws JsonRuntimeException     If there is an issue converting or parsing the JSON string.
     * @throws IllegalArgumentException If the JSON string represents a single value.
     */
    public static JsonNode json(Object input) {
        try {
            return json(MAPPER.writeValueAsString(input));
        } catch (JsonProcessingException e) {
            throw new JsonRuntimeException(e);
        }
    }

    /**
     * Reads a string value from the given JsonNode based on the specified JSONPath expression.
     *
     * @param obj        The JsonNode from which to read the value.
     * @param expression The JSONPath expression specifying the value to read.
     * @return The string value read from the JsonNode.
     * @throws JsonProcessingException If there is an issue processing the JSON content.
     */
    public static String readStringValue(JsonNode obj, String expression) throws JsonProcessingException {
        if (expression.startsWith("$")) {
            JsonNode result = MAPPER.readTree(JsonPath.using(CONFIG).parse(obj).read(expression).toString());
            if (List.of(JsonNodeType.NULL, JsonNodeType.MISSING).contains(result.getNodeType())) {
                return null;
            }
            if (result.getNodeType() == JsonNodeType.STRING) {
                return result.textValue();
            }
            return result.toString();
        } else {
            Binding binding = new Binding();
            binding.setVariable("obj", obj.toString());
            binding.setVariable("exp", expression);
            GroovyShell shell = new GroovyShell(binding);
            String exp = (obj.isArray() && expression.matches("\\[\\d+].*") ? "'x'" : "'x.'") + " + exp";
            Object result = shell.evaluate("Eval.x(new groovy.json.JsonSlurper().parseText(obj), " + exp + ")");
            return result != null ? result.toString() : null;
        }
    }

    /**
     * Exception class used to wrap exceptions related to JSON processing.
     */
    private static class JsonRuntimeException extends RuntimeException {

        JsonRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
