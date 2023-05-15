/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.database;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import es.iti.wakamiti.api.datatypes.Assertion;

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