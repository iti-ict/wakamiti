/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


/**
 * Execution outcomes for plan nodes.
 * <p>
 * Enum declaration order is significant: values are sorted from least severe
 * ({@link #PASSED}) to most severe ({@link #ERROR}), so natural-order
 * comparisons can be used to aggregate child results.
 * </p>
 */
public enum Result implements Comparable<Result> {

    /**
     * The node and all of its children have been executed
     * successfully.
     */
    PASSED,

    /**
     * The node could not be executed because it has no runnable steps.
     */
    NOT_IMPLEMENTED,

    /**
     * The node was intentionally not executed because a previous prerequisite
     * failed or was filtered out.
     */
    SKIPPED,

    /**
     * The node definition cannot be resolved into an executable step.
     */
    UNDEFINED,

    /**
     * The node completed but assertions or validations did not pass.
     */
    FAILED,

    /**
     * The node execution aborted due to an unexpected runtime error.
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
