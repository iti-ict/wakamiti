/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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


    @Test
    public void testParserExtended() throws IOException {
        try (var reader = new InputStreamReader(
                classLoader.getResourceAsStream("gherkinDocument2.feature")
        )) {
            GherkinDocument document = new GherkinParser().parse(reader);
            assertNotNull(document);
            assertTrue(document.getFeature().getChildren().get(0).getSteps().get(0).getKeyword().equals("Dado que "));
        }
    }
}