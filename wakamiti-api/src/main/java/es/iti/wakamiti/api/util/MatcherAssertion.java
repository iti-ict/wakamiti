/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.datatypes.Assertion;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;


/**
 * A utility class that adapts an {@link Assertion} to a Hamcrest
 * {@link Matcher}.
 *
 * @param <T> The type of the value being asserted.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class MatcherAssertion<T> implements Assertion<T> {

    private final Matcher<T> matcher;

    public MatcherAssertion(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    /**
     * Converts an {@link Assertion} to a Hamcrest {@link Matcher}.
     *
     * @param <T>       The type of the value being asserted.
     * @param assertion The assertion to be converted.
     * @return A Hamcrest Matcher representing the given assertion.
     */
    public static <T> Matcher<T> asMatcher(Assertion<T> assertion) {
        if (assertion instanceof MatcherAssertion) {
            return ((MatcherAssertion<T>) assertion).matcher;
        } else {
            return new BaseMatcher<>() {
                @Override
                public boolean matches(Object actual) {
                    return assertion.test(actual);
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText(assertion.description());
                }
            };
        }
    }

    /**
     * Tests the given actual value against the underlying Hamcrest
     * Matcher.
     *
     * @param actualValue The value to be tested.
     * @return {@code true} if the actual value matches the expected
     * condition, {@code false} otherwise.
     */
    @Override
    public boolean test(Object actualValue) {
        return matcher.matches(actualValue);
    }

    /**
     * Provides a description of the assertion.
     *
     * @return A string describing the expected condition.
     */
    @Override
    public String description() {
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        return description.toString();
    }

    /**
     * Describes the failure of the assertion for the given actual value.
     *
     * @param actualValue The actual value that failed the assertion.
     * @return A string describing the failure.
     */
    @Override
    public String describeFailure(Object actualValue) {
        StringDescription description = new StringDescription();
        matcher.describeMismatch(actualValue, description);
        return description.toString();
    }

}