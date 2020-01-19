package iti.commons.gherkin;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class TestParser {

    private static ClassLoader classLoader = TestParser.class.getClassLoader();

    @Test
    public void testParser() throws IOException {
        try (var reader = new InputStreamReader(
            classLoader.getResourceAsStream("gherkinDocument.feature")
        )) {
            GherkinDocument document = new GherkinParser().parse(reader);
            assertNotNull(document);
        }
    }
}
