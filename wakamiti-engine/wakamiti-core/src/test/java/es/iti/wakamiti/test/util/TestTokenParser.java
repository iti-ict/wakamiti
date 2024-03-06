/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.util;


import es.iti.wakamiti.core.util.TokenParser;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


public class TestTokenParser {

    private static final List<String> TOKENS = Arrays.asList(
            "mmmm",
            "mm",
            "dd",
            "d",
            "yyyy",
            "yy",
            "-",
            "\\",
            "/",
            " "
    );


    @Test
    public void testTokenParser1() {
        TokenParser parser = new TokenParser(
                "mmmmmm/ ddd-yyyy-yy", TOKENS, List.of("'[^']*'")
        );
        assertNextToken(parser, "mmmm");
        assertNextToken(parser, "mm");
        assertNextToken(parser, "/");
        assertNextToken(parser, " ");
        assertNextToken(parser, "dd");
        assertNextToken(parser, "d");
        assertNextToken(parser, "-");
        assertNextToken(parser, "yyyy");
        assertNextToken(parser, "-");
        assertNextToken(parser, "yy");
    }


    @Test
    public void testTokenParser2() {
        TokenParser parser = new TokenParser(
                "d' de 'mmmm' de 'yyyy", TOKENS, List.of("'[^']*'")
        );
        assertNextToken(parser, "d");
        assertNextToken(parser, "' de '");
        assertNextToken(parser, "mmmm");
        assertNextToken(parser, "' de '");
        assertNextToken(parser, "yyyy");
    }


    private void assertNextToken(TokenParser parser, String string) {
        Assertions.assertThat(parser.hasMoreTokens()).isTrue();
        Assertions.assertThat(parser.nextToken()).isEqualTo(string);
    }

}