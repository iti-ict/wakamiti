package iti.kukumo.rest.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author ITI
 * Created by ITI on 18/04/19
 */
public class TestMockServer {


    private static MockServer server;


    @BeforeClass
    public static void setupServer() throws Exception {
        server = new MockServer(MockServer.Format.XML, StandardCharsets.UTF_8,8888, "src/test/resources/data.json", MockServer.Format.JSON);
    }

    @Test
    public void testSerializedXML() throws Exception {
        Object resolved = server.resolvePath("/users/user1").pop();
        byte[] serializedBytes = server.serialize(resolved);
        String serializedString = new String(serializedBytes);
        System.out.println(serializedString);
        assertEquals(serializedString,
        "<data><id>user1</id><name>User One</name><age>11</age><vegetables><id>1</id><description>Cucumber</description>" +
        "</vegetables><vegetables><id>2</id><description>Gherkin</description></vegetables><contact><email>user1@mail</email></contact></data>");
    }


    @AfterClass
    public static void teardownServer() {
        server.stop();
    }
}
