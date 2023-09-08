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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final String json = "{\"name\":\"Arnold\",\"age\":47}";
    private final String jsonError = "{\"name:\"Arnold\",\"age\":47}";
    private final String jsonNull = "{\"name\":\"Arnold\",\"age\":null}";

    private final String jsonList = "[{\"name\":\"Arnold\",\"age\":47},{\"name\":\"Susan\",\"age\":32}]";

    @Test
    public void testJsonWhenStringWithSuccess() {
        JsonNode obj = JsonUtils.json(json);

        assertThat(obj).isNotNull();
        assertThat(obj.toString()).isEqualTo(json);
    }

    @Test(expected = RuntimeException.class)
    public void testJsonWhenStringWithError() {
        JsonUtils.json(jsonError);
    }

    @Test
    public void testJsonWhenStreamWithSuccess() {
        JsonNode obj = JsonUtils.json(new ByteArrayInputStream(json.getBytes()));

        assertThat(obj).isNotNull();
        assertThat(obj.toString()).isEqualTo(json);
    }

    @Test(expected = RuntimeException.class)
    public void testJsonWhenStreamWithError() {
        JsonUtils.json(new ByteArrayInputStream(jsonError.getBytes()));
    }

    @Test
    public void testJsonWhenMapWithSuccess() throws JsonProcessingException {
        JsonNode obj = JsonUtils.json(
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
    public void testJsonWhenMapWithError() throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {

        ObjectMapper mapperMock = mock(ObjectMapper.class);
        when(mapperMock.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        setField(JsonUtils.class, "MAPPER", mapperMock);
        try {
            JsonUtils.json(Map.of());
        } catch (Exception e) {
            throw e;
        } finally {
            setField(JsonUtils.class, "MAPPER", mapper);
        }
    }

    @Test(expected = RuntimeException.class)
    public void testJsonStringWhenSingleValueWithError() {
        JsonUtils.json("12345");
    }

    @Test(expected = RuntimeException.class)
    public void testJsonInputStreamWhenSingleValueWithError() {
        JsonUtils.json(new ByteArrayInputStream("12345".getBytes()));
    }

    @Test
    public void testReadStringValueWithSuccess() throws JsonProcessingException, JSONException {
        JsonNode obj = JsonUtils.json(
                Map.of(
                        "statusCode", "200",
                        "body", mapper.readTree(json),
                        "headers", Map.of("keep-alive", "true", "content-type", "application/json"),
                        "tags", List.of("something", "other")
                ));

        String result = JsonUtils.readStringValue(obj, "$.headers.content-type");
        assertThat(result).isEqualTo("application/json");

        result = JsonUtils.readStringValue(obj, "$..*");
        JSONAssert.assertEquals(
                new JSONArray(List.of(
                        "application/json", "true", "200", "Arnold", 47, "something", "other",
                        new JSONObject(json),
                        new JSONObject(Map.of("keep-alive", "true", "content-type", "application/json")),
                        new JSONArray(List.of("something", "other"))
                )),
                new JSONArray(result),
                JSONCompareMode.NON_EXTENSIBLE
        );

        result = JsonUtils.readStringValue(obj, "$.statusCode");
        assertThat(result).isEqualTo("200");

        result = JsonUtils.readStringValue(obj, "$");
        JSONAssert.assertEquals(
                new JSONObject(obj.toString()),
                new JSONObject(result),
                JSONCompareMode.NON_EXTENSIBLE
        );

        result = JsonUtils.readStringValue(obj, "$..*.length()");
        assertThat(result).isEqualTo("6");

        result = JsonUtils.readStringValue(JsonUtils.json(jsonNull), "$.age");
        assertThat(result).isNull();

        result = JsonUtils.readStringValue(JsonUtils.json(jsonNull), "age");
        assertThat(result).isNull();

        result = JsonUtils.readStringValue(JsonUtils.json(jsonList), "find{ it.name == 'Susan' }.age");
        assertThat(result).isEqualTo("32");

        result = JsonUtils.readStringValue(JsonUtils.json(jsonList), "[0].age");
        assertThat(result).isEqualTo("47");
    }

    @Test(expected = PathNotFoundException.class)
    public void testReadStringValueWithError() throws JsonProcessingException {
        JsonNode obj = JsonUtils.json(
                Map.of(
                        "statusCode", "200",
                        "body", mapper.readTree(json),
                        "headers", Map.of("keep-alive", "true", "content-type", "application/json"),
                        "tags", List.of("something", "other")
                ));

        JsonUtils.readStringValue(obj, "$.body.id");
    }

    private void setField(Class<?> cls, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = cls.getDeclaredField(field);
        f.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

        f.set(null, value);
    }
}
