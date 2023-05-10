/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.rest;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import iti.wakamiti.api.datatypes.Assertion;

class AssertionMatcher<T> extends BaseMatcher<T> {

    static <T> Matcher<T> matcher(Assertion<T> assertion) {
        return new AssertionMatcher<>(assertion);
    }


    private final Assertion<T> assertion;

    public AssertionMatcher(Assertion<T> assertion) {
        this.assertion = assertion;
    }

    @Override
    public boolean matches(Object actual) {
        return assertion.test(actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(assertion.description());
    }

}