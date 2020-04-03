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
