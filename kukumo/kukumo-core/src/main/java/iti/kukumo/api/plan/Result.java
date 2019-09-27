package iti.kukumo.api.plan;

public enum Result implements Comparable<Result> {

    // in inverted order of severity

    /** The node or all of its children has been executed succesfully */
    PASSED,

    /** The node was not executed due to previous step did not passed the test */
    SKIPPED,

    /** The node or any of its children was not executed due to malformed definition */
    UNDEFINED,

    /** The node or any of its children has not passed the validation */
    FAILED,

    /** The node or any of its children has experienced a fatal error */
    ERROR;


    public boolean isPassed() {
        return this == PASSED;
    }

}