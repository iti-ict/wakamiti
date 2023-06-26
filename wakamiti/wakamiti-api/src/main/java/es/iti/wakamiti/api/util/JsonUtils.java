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
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.InputStream;


public class JsonUtils {

    private final static Configuration CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();
    private final static ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode json(String input) {
        try {
            return MAPPER.readTree(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode json(InputStream input) {
        try {
            return MAPPER.readTree(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode json(Object input) {
        try {
            return MAPPER.readTree(MAPPER.writeValueAsString(input));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readStringValue(JsonNode obj, String expression) throws JsonProcessingException {
        if (expression.startsWith("$")) {
            JsonNode result = MAPPER.readTree(JsonPath.using(CONFIG).parse(obj).read(expression).toString());
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
            return shell.evaluate("Eval.x(new groovy.json.JsonSlurper().parseText(obj), " + exp + ")").toString();
        }
    }

}
