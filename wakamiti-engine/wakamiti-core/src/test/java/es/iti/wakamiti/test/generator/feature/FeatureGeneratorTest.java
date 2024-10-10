/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.generator.feature;


import com.fasterxml.jackson.core.JsonProcessingException;
import es.iti.wakamiti.core.generator.features.FeatureGenerator;
import es.iti.wakamiti.core.generator.features.FeatureGeneratorException;
import es.iti.wakamiti.core.generator.features.OpenAIService;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@RunWith(MockitoJUnitRunner.Silent.class)
public class FeatureGeneratorTest {

    private static final String[] API_DOCS = {
            "examples/v2.0/json/petstore.json", "examples/v2.0/json/petstore-separate/spec/swagger.json",
            "examples/v2.0/yaml/petstore.yaml", "examples/v2.0/yaml/petstore-separate/spec/swagger.yaml",
            "examples/v3.0/petstore.json", "examples/v3.0/petstore.yaml"
    };
    private static final String TEMP_PATH = Files.temporaryFolderPath();
    private static final Integer PORT = 4321;
    private static final String BASE_URL = MessageFormat.format("http://localhost:{0}", PORT.toString());
    private static final ClientAndServer server = startClientAndServer(PORT);
    private static final List<File> FEATURES = Stream.of(
                    "pets1/getPets", "pets1/postPets", "pets2/getPetsById", "deletePetsById")
            .map(f -> Path.of(TEMP_PATH, f + ".feature").toFile())
            .collect(Collectors.toList());

    @Mock
    private OpenAIService openAIService;

    @AfterClass
    public static void shutdown() {
        server.close();
    }

    @Before
    public void beforeEach() throws URISyntaxException, JsonProcessingException {
        when(openAIService.runPrompt(anyString(), anyString())).thenReturn("Something");
        server.reset();
    }

    @After
    public void afterEach() {
        FEATURES.forEach(File::delete);
    }

    @Test
    public void testGenerateTestWhenContentWithSuccess() throws IOException {
        for (String doc : API_DOCS) {
            generateTest(content(doc));
        }
    }

    @Test(expected = FeatureGeneratorException.class)
    public void testGenerateTestWhenWrongContentWithError() {
        generateTest("ghewpiog");
    }

    @Test
    public void testGenerateTestWhenLocalURLWithSuccess() {
        for (String doc : API_DOCS) {
            generateTest(url(doc).toExternalForm());
        }
    }

    @Test(expected = FeatureGeneratorException.class)
    public void testGenerateTestWhenWrongLocalURLWithError() {
        generateTest(url("simplelogger.properties").toExternalForm());
    }

    @Test(expected = FeatureGeneratorException.class)
    public void testGenerateTestWhenNonExistentLocalURLWithError() {
        generateTest("somefile.json");
    }

    @Test
    public void testGenerateTestWhenHttpURLWithSuccess() throws IOException {
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/api_docs")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(content("examples/v3.0/petstore.json"))
        );

        generateTest(BASE_URL + "/api_docs");
    }

    @Test(expected = FeatureGeneratorException.class)
    public void testGenerateTestWhenWrongHttpURLWithError() {
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/api_docs")
                ,
                response()
                        .withStatusCode(404)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        generateTest(BASE_URL + "/api_docs");
    }

    private void generateTest(String file) {
        FeatureGenerator featureGenerator = new FeatureGenerator(openAIService, "", file);
        featureGenerator.generate(TEMP_PATH, "en");

        assertThat(FEATURES).allMatch(File::exists)
                .allMatch(f -> Files.contentOf(f, Charset.defaultCharset()).equals("Something"));
    }

    private URL url(String resource) {
        return Objects.requireNonNull(this.getClass().getClassLoader().getResource(resource));
    }

    private String content(String resource) throws IOException {
        return IOUtils.toString(url(resource), Charset.defaultCharset());
    }

    private void mockServer(HttpRequest expected, HttpResponse response) {
        mockServer(expected, response, Times.once());
    }

    private void mockServer(HttpRequest expected, HttpResponse response, Times times) {
        server.when(expected, times).respond(response);
    }

}
