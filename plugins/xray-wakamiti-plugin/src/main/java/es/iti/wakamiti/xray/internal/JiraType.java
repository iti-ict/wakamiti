/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;


/**
 * Defines the values supported by Jira Type.
 */
public enum JiraType {

    /** Xray test issue type used to store an executable test definition. */
    TEST("Test"),
    /** Xray test-plan issue type used to group tests and their executions. */
    TEST_PLAN("Test Plan");

    private String name;

    JiraType(
            String name
    ) {
        this.name = name;
    }

    /**
     * Returns the issue-type name expected by Jira and Xray queries.
     *
     * @return external Jira issue-type name
     */
    public String getName() {
        return name;
    }

}
