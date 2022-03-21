/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import iti.kukumo.api.datatypes.Assertion;

public class MatcherAssertion<T> implements Assertion<T> {

    public static <T> Matcher<T> asMatcher(Assertion<T> assertion) {
        if (assertion instanceof MatcherAssertion) {
            return ((MatcherAssertion<T>) assertion).matcher;
        } else {
            return new BaseMatcher<T>() {
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



    private final Matcher<T> matcher;

    public MatcherAssertion(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean test(Object actualValue) {
        return matcher.matches(actualValue);
    }

    @Override
    public String description() {
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        return description.toString();
    }

    @Override
    public String describeFailure(Object actuaValue) {
        StringDescription description = new StringDescription();
        matcher.describeMismatch(actuaValue, description);
        return description.toString();
    }



}