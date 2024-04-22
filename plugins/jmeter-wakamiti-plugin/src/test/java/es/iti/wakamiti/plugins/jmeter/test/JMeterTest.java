package es.iti.wakamiti.plugins.jmeter.test;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
        @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti.json")
})

public class JMeterTest {

    private static final ClientAndServer client = startClientAndServer(8888);


    @BeforeClass
    public static void setupServer() {

        client.when(HttpRequest.request().withPath("/")).respond(HttpResponse.response().withStatusCode(200));
        // Configuración para el endpoint GET '/inicio'
        client.when(HttpRequest.request().withMethod("GET").withPath("/inicio"))
                .respond(HttpResponse.response().withStatusCode(200).withBody("{\"mensaje\": \"123\"}"));

        // Configuración para el endpoint POST '/login'
        client.when(HttpRequest.request().withMethod("POST").withPath("/login/123"))
                .respond(HttpResponse.response().withStatusCode(200).withBody("{\"mensaje\": \"Usuario autenticado\"}"));

        // Configuración para el endpoint PUT '/actualizar'
        client.when(HttpRequest.request().withMethod("PUT").withPath("/actualizar"))
                .respond(HttpResponse.response().withStatusCode(200).withBody("{\"mensaje\": \"Información actualizada\"}"));

    }


    @AfterClass
    public static void teardownServer() {
        client.close();
    }

}