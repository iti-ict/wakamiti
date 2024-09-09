/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.InputStream;


/**
 * Utility class for working with JSON data.
 *
 * <p>This class provides methods for converting JSON strings, InputStreams, and objects into {@link JsonNode}.
 * It also includes a method for reading string values from a JsonNode based on a JSONPath expression.</p>
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final Configuration CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider(MAPPER))
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();

    private JsonUtils() {

    }

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
     */
    public static String readStringValue(JsonNode obj, String expression) {
        return read(obj, expression, String.class);
    }

    public static <T> T read(JsonNode obj, String expression, Class<T> type) {
        if (expression.startsWith("$")) {
            return JsonPath.using(CONFIG).parse(obj).read(expression, type);
        } else {
            return read(obj, expression, TypeFactory.defaultInstance().constructType(type));

        }
    }

    public static <T> T read(JsonNode obj, String expression, TypeRef<T> type) {
        if (expression.startsWith("$")) {
            return JsonPath.using(CONFIG).parse(obj).read(expression, type);
        } else {
            return read(obj, expression, MAPPER.getTypeFactory().constructType(type.getType()));
        }
    }

    public static <T> T read(JsonNode obj, Class<T> type) {
        return MAPPER.convertValue(obj, type);
    }

    public static <T> T read(JsonNode obj, TypeRef<T> type) {
        return MAPPER.convertValue(obj, MAPPER.getTypeFactory().constructType(type.getType()));
    }

    private static <T> T read(JsonNode obj, String expression, JavaType type) {
        Binding binding = new Binding();
        binding.setVariable("obj", obj.toString());
        binding.setVariable("exp", expression);
        GroovyShell shell = new GroovyShell(binding);
        String exp = (obj.isArray() && expression.startsWith("[") ? "'x'" : "'x.'") + " + exp";
        Object result = shell.evaluate("Eval.x(new groovy.json.JsonSlurper().parseText(obj), " + exp + ")");
        if (result == null) return null;
        return MAPPER.convertValue(result, type);
    }

    /**
     * Exception class used to wrap exceptions related to JSON processing.
     */
    public static class JsonRuntimeException extends RuntimeException {

        JsonRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        JsonRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
