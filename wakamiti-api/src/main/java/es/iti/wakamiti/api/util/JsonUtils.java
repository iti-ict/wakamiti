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


public class JsonUtils {

    private final static Configuration CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();
    private final static ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode json(String input) {
        try {
            JsonNode result = MAPPER.readTree(input);
            if (result instanceof ValueNode) {
                throw new RuntimeException("Single value not allowed");
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode json(InputStream input) {
        try {
            JsonNode result = MAPPER.readTree(input);
            if (result instanceof ValueNode) {
                throw new RuntimeException("Single value not allowed");
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode json(Object input) {
        try {
            return json(MAPPER.writeValueAsString(input));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

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

}
