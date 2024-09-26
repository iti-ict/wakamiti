package es.iti.wakamiti.test.generator.feature;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.iti.wakamiti.api.util.WakamitiLogger;
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
import org.slf4j.Logger;

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

    private static final Logger LOGGER = WakamitiLogger.forClass(FeatureGeneratorTest.class);

    private static final String API_DOCS_JSON = "api_docs.json";
    private static final String TEMP_PATH = Files.temporaryFolderPath();
    private static final Integer PORT = 4321;
    private static final String BASE_URL = MessageFormat.format("http://localhost:{0}", PORT);
    private static final ClientAndServer server = startClientAndServer(PORT);
    private static final List<File> FEATURES = Stream.of(
                    "get_health", "post_notifications", "get_notifications_{id}", "put_notifications_{id}",
                    "get_notifications_kinds", "get_notifications_personal", "get_notifications_reasons")
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
    public void testGenerateTestWhenContentWithSuccess() throws URISyntaxException, IOException {
        generateTest(content(API_DOCS_JSON));
    }

    @Test(expected = FeatureGeneratorException.class)
    public void testGenerateTestWhenWrongContentWithError() {
        try {
            generateTest("ghewpiog");
        } catch (URISyntaxException | IOException e) {
            throw new FeatureGeneratorException("Controlled exception", e);
        } catch (Exception e) {
            LOGGER.error("Uncontrolled exception");
            throw e;
        }
    }

    @Test
    public void testGenerateTestWhenLocalURLWithSuccess() throws URISyntaxException, IOException {
        generateTest(url(API_DOCS_JSON).getFile());
    }

    @Test(expected = FeatureGeneratorException.class)
    public void testGenerateTestWhenWrongLocalURLWithError() {
        try {
            generateTest(url("simplelogger.properties").getFile());
        } catch (URISyntaxException | IOException e) {
            throw new FeatureGeneratorException("Controlled exception", e);
        } catch (Exception e) {
            LOGGER.error("Uncontrolled exception");
            throw e;
        }
    }

    @Test(expected = FeatureGeneratorException.class)
    public void testGenerateTestWhenNonExistentLocalURLWithError() {
        try {
            generateTest("somefile.json");
        } catch (URISyntaxException | IOException e) {
            throw new FeatureGeneratorException("Controlled exception", e);
        } catch (Exception e) {
            LOGGER.error("Uncontrolled exception");
            throw e;
        }
    }

    @Test
    public void testGenerateTestWhenHttpURLWithSuccess() throws URISyntaxException, IOException {
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/api_docs")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(content(API_DOCS_JSON))
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

        try {
            generateTest(BASE_URL + "/api_docs");
        } catch (URISyntaxException | IOException e) {
            throw new FeatureGeneratorException("Controlled exception", e);
        } catch (Exception e) {
            LOGGER.error("Uncontrolled exception");
            throw e;
        }
    }

    private void generateTest(String file) throws URISyntaxException, IOException {
        FeatureGenerator featureGenerator = new FeatureGenerator(openAIService, "", file);
        featureGenerator.generate(TEMP_PATH);

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
