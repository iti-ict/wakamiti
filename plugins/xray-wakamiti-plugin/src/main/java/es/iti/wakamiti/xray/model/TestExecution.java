/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


/**
 * Provides the Test Execution functionality used by Wakamiti.
 */
public class TestExecution {

    private String issueId;
    private JiraIssue jira;

    /**
     * Creates an empty Xray Test Execution for GraphQL deserialization.
     */
    public TestExecution() {
        // Empty constructor
    }

    /**
     * @return Jira issue backing the Xray Test Execution
     */
    public JiraIssue getJira() {
        return jira;
    }

    /**
     * Associates the Jira issue backing this Xray Test Execution.
     *
     * @param jira Jira issue backing the execution
     * @return this execution
     */
    public TestExecution jira(
            JiraIssue jira
    ) {
        this.jira = jira;
        return this;
    }

    /**
     * @return Xray internal issue identifier
     */
    public String getIssueId() {
        return issueId;
    }

    /**
     * Assigns the internal Xray issue identifier returned for this execution.
     *
     * @param issueId Xray internal issue identifier
     * @return this execution
     */
    public TestExecution issueId(
            String issueId
    ) {
        this.issueId = issueId;
        return this;
    }

}
