/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


/**
 * Represents different results of the execution of a node
 * in a plan.
 * Results are ordered in inverted order of severity.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public enum Result implements Comparable<Result> {

    /**
     * The node and all of its children have been executed
     * successfully.
     */
    PASSED,

    /**
     * The node was not executed due to having no children.
     */
    NOT_IMPLEMENTED,

    /**
     * The node was not executed due to the previous step
     * not passing the test.
     */
    SKIPPED,

    /**
     * The node or any of its children was not executed due
     * to malformed definition.
     */
    UNDEFINED,

    /**
     * The node or any of its children has not passed the
     * validation.
     */
    FAILED,

    /**
     * The node or any of its children has experienced a fatal
     * error.
     */
    ERROR;

    /**
     * Checks if the result represents a successful execution.
     *
     * @return {@code true} if the result is {@link #PASSED},
     * otherwise {@code false}.
     */
    public boolean isPassed() {
        return this == PASSED;
    }

}