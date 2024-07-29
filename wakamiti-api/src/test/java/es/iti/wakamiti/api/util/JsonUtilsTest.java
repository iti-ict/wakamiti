/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static es.iti.wakamiti.api.util.JsonUtils.*;
import static es.iti.wakamiti.api.util.MapUtils.map;
import static org.assertj.core.api.Assertions.assertThat;


public class JsonUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final String json = "{\"name\":\"Arnold\",\"age\":47}";
    private final String jsonList = "[{\"name\":\"Arnold\",\"age\":47},{\"name\":\"Susan\",\"age\":32}]";
    private final String jsonError = "{\"name:\"Arnold\",\"age\":47}";

    @Test
    public void testJsonWhenStringWithSuccess() {
        JsonNode obj = json(json);
        assertThat(obj).hasToString(json);
    }

    @Test(expected = RuntimeException.class)
    public void testJsonWhenStringWithError() {
        json(jsonError);
    }

    @Test
    public void testJsonWhenStreamWithSuccess() {
        JsonNode obj = json(new ByteArrayInputStream(json.getBytes()));
        assertThat(obj).hasToString(json);
    }

    @Test(expected = RuntimeException.class)
    public void testJsonWhenStreamWithError() {
        json(new ByteArrayInputStream(jsonError.getBytes()));
    }

    @Test
    public void testJsonWhenMapWithSuccess() throws JsonProcessingException {
        JsonNode obj = json(
                Map.of(
                        "statusCode", "200",
                        "body", mapper.readTree(json),
                        "headers", Map.of("keep-alive", "true"),
                        "tags", List.of("something", "other")
                ));

        assertThat(obj).isNotNull();
        assertThat(obj.toString()).contains("\"body\":" + json);
        assertThat(obj.toString()).contains("\"statusCode\":\"200\"");
        assertThat(obj.toString()).contains("\"headers\":{\"keep-alive\":\"true\"}");
        assertThat(obj.toString()).contains("\"tags\":[\"something\",\"other\"]");
    }

    @Test(expected = RuntimeException.class)
    public void testJsonStringWhenSingleValueWithError() {
        json("12345");
    }

    @Test(expected = RuntimeException.class)
    public void testJsonInputStreamWhenSingleValueWithError() {
        json(new ByteArrayInputStream("12345".getBytes()));
    }

    @Test
    public void testReadStringValueWithSuccess() throws JsonProcessingException, JSONException {
        JsonNode obj = json(
                Map.of(
                        "statusCode", "200",
                        "body", mapper.readTree(json),
                        "headers", Map.of("keep-alive", "true", "content-type", "application/json"),
                        "tags", List.of("something", "other")
                ));

        String result = readStringValue(obj, "$.headers.content-type");
        assertThat(result).isEqualTo("application/json");

        JsonNode result2 = read(obj, "$..*", JsonNode.class);
        JSONAssert.assertEquals(
                new JSONArray(List.of(
                        "application/json", "true", "200", "Arnold", 47, "something", "other",
                        new JSONObject(json),
                        new JSONObject(Map.of("keep-alive", "true", "content-type", "application/json")),
                        new JSONArray(List.of("something", "other"))
                )),
                new JSONArray(result2.toString()),
                JSONCompareMode.NON_EXTENSIBLE
        );

        result = readStringValue(obj, "$.statusCode");
        assertThat(result).isEqualTo("200");

        JsonNode result3 = read(obj, "$", JsonNode.class);
        JSONAssert.assertEquals(
                new JSONObject(obj.toString()),
                new JSONObject(result3.toString()),
                JSONCompareMode.NON_EXTENSIBLE
        );

        result = readStringValue(obj, "$..*.length()");
        assertThat(result).isEqualTo("6");

        String jsonNull = "{\"name\":\"Arnold\",\"age\":null}";
        result = readStringValue(json(jsonNull), "$.age");
        assertThat(result).isNull();

        result = readStringValue(json(jsonNull), "age");
        assertThat(result).isNull();

        result = readStringValue(json(jsonList), "find{ it.name == 'Susan' }.age");
        assertThat(result).isEqualTo("32");

        result = readStringValue(json(jsonList), "[0].age");
        assertThat(result).isEqualTo("47");
    }

    @Test
    public void testReadWhenClassWithSuccess() throws MalformedURLException {

        assertThat(read(json(jsonList), "[1].age", Long.class))
                .isEqualTo(32L);

        assertThat(read(json("{\"date\":\"2010-12-03\"}"), "date", LocalDate.class))
                .isEqualTo(LocalDate.parse("2010-12-03"));
        assertThat(read(json("{\"date\":\"http://example.org\"}"), "date", URL.class))
                .isEqualTo(new URL("http://example.org"));

        assertThat(read(json("{\"child\":{\"date\":\"2010-12-03\"}}"), "child", JsonNode.class))
                .isEqualTo(json("{\"date\":\"2010-12-03\"}"));
    }

    @Test
    public void testReadWhenTypeRefWithSuccess() {
        assertThat(read(json("{\"date\":\"2010-12-03\"}"), new TypeRef<Map<String, LocalDate>>(){}))
                .isEqualTo(map("date", LocalDate.parse("2010-12-03")));
    }

    @Test(expected = PathNotFoundException.class)
    public void testReadStringValueWithError() throws JsonProcessingException {
        JsonNode obj = json(
                Map.of(
                        "statusCode", "200",
                        "body", mapper.readTree(json),
                        "headers", Map.of("keep-alive", "true", "content-type", "application/json"),
                        "tags", List.of("something", "other")
                ));

        readStringValue(obj, "$.body.id");
    }
}
