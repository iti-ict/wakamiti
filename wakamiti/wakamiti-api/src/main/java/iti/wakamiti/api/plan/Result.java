/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.plan;


public enum Result implements Comparable<Result> {

    // in inverted order of severity

    /**
     * The node and all of its children has been executed successfully
     */
    PASSED,

    /**
     * The node was not executed due to has no children
     */
    NOT_IMPLEMENTED,

    /**
     * The node was not executed due to previous step did not pass the test
     */
    SKIPPED,

    /**
     * The node or any of its children was not executed due to malformed
     * definition
     */
    UNDEFINED,

    /**
     * The node or any of its children has not passed the validation
     */
    FAILED,

    /**
     * The node or any of its children has experienced a fatal error
     */
    ERROR;

    public boolean isPassed() {
        return this == PASSED;
    }

}