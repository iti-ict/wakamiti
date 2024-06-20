/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.matcher;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class CharSequenceLengthMatcherTest {

    @Test
    public void testMatcherSafelyWhenTextNotNullWithSuccess() {
        assertThat("AB", CharSequenceLengthMatcher.length(equalTo(2)));
    }

    @Test
    public void testMatcherSafelyWhenIntWithSuccess() {
        assertThat("AB", CharSequenceLengthMatcher.length(2));
    }

    @Test(expected = AssertionError.class)
    public void testMatcherSafelyWhenTextNullWithError() {
        try {
            assertThat(null, CharSequenceLengthMatcher.length(0));
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("Expected: has string length <2>\n     but: was null"));
            throw e;
        }
    }

    @Test(expected = AssertionError.class)
    public void testMatcherSafelyWhenTextNotNullWithError() {
        try {
            assertThat("A", CharSequenceLengthMatcher.length(equalTo(2)));
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("Expected: has string length <2>\n     but: was length \"1\""));
            throw e;
        }
    }

    @Test(expected = AssertionError.class)
    public void testMatcherSafelyWhenIntWithError() {
        try {
            assertThat("A", CharSequenceLengthMatcher.length(2));
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("Expected: has string length <2>\n     but: was length \"1\""));
            throw e;
        }
    }
}
