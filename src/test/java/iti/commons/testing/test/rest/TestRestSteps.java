/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.test.rest;

import java.io.IOException;
import java.util.Locale;

import iti.commons.testing.TestingException;
import iti.commons.testing.rest.RestAssuredHelper;
import iti.commons.testing.rest.RestSteps;
import iti.commons.testing.test.MockServer;

public class TestRestSteps extends RestSteps {

    private static final String TEST_HOST = "http://localhost";
    private static final int TEST_PORT = 8099;


    public static MockServer server = createServer();


    public static MockServer createServer() {
        try {
            return new MockServer(MockServer.Format.JSON,"UTF-8",TEST_PORT, "data.json");
        } catch (IOException e) {
            throw new TestingException(e);
        }
    }


    public TestRestSteps() throws IOException {
        super(new RestAssuredHelper(),
              Thread.currentThread().getContextClassLoader(),
              Locale.forLanguageTag("es"),
              "restSteps");
        helper.setHostPort(TEST_HOST, TEST_PORT);
    }



}
