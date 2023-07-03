/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.gherkin;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestParser {

    private static final ClassLoader classLoader = TestParser.class.getClassLoader();

    @Test
    public void testParser() throws IOException {
        try (var reader = new InputStreamReader(
                Objects.requireNonNull(classLoader.getResourceAsStream("gherkinDocument.feature"))
        )) {
            GherkinDocument document = new GherkinParser().parse(reader);
            assertNotNull(document);
        }
    }

    @Test
    public void testParserExtended() throws IOException {
        try (var reader = new InputStreamReader(
                Objects.requireNonNull(classLoader.getResourceAsStream("gherkinDocument2.feature"))
        )) {
            GherkinDocument document = new GherkinParser().parse(reader);
            assertNotNull(document);
            assertEquals("Dado que ", document.getFeature().getChildren().get(0).getSteps().get(0).getKeyword());
        }
    }

    @Test(expected = ParserException.CompositeParserException.class)
    public void testParserError() throws IOException {
        for (String feature : List.of(
                "gherkinDocument_tag_err.feature"
        )) {
            try (var reader = new InputStreamReader(
                    Objects.requireNonNull(classLoader.getResourceAsStream(feature))
            )) {
                GherkinDocument document = new GherkinParser().parse(reader);
                assertNotNull(document);
            }
        }
    }
}