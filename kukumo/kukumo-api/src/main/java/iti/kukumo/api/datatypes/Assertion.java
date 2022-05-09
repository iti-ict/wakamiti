/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.api.datatypes;

public interface Assertion<T> {

    boolean test(Object actualValue);

    String description();

    String describeFailure(Object actualValue);

    static <T> void assertThat(T actualValue, Assertion<T> assertion) {
        if (!assertion.test(actualValue)) {
            throw new AssertionError(assertion.describeFailure(actualValue));
        }
    }
}