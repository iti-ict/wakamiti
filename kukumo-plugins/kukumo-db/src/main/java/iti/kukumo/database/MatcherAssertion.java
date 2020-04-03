package iti.kukumo.database;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import iti.kukumo.api.datatypes.Assertion;

public class MatcherAssertion<T> implements Assertion<T> {


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
