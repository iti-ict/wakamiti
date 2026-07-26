/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


public class TestExecution {

    private String issueId;
    private JiraIssue jira;

    public TestExecution() {
        // Empty constructor
    }

    public JiraIssue getJira() {
        return jira;
    }

    public TestExecution jira(
            JiraIssue jira
    ) {
        this.jira = jira;
        return this;
    }

    public String getIssueId() {
        return issueId;
    }

    public TestExecution issueId(
            String issueId
    ) {
        this.issueId = issueId;
        return this;
    }

}
