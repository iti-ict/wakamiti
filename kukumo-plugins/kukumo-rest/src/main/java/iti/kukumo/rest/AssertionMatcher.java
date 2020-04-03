package iti.kukumo.rest;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import iti.kukumo.api.datatypes.Assertion;

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