/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSet {

    private String issueId;
    private JiraIssue jira;
    private List<TestCase> testCases = new ArrayList<>();

    public TestSet(
            String issueId,
            JiraIssue jira,
            List<TestCase> testCases
    ) {
        this.issueId = issueId;
        this.jira = jira;
        this.testCases = testCases;
    }

    public TestSet() {
    }

    public TestSet issueId(
            String issueId
    ) {
        this.issueId = issueId;
        return this;
    }

    public TestSet issue(
            JiraIssue issue
    ) {
        this.jira = issue;
        return this;
    }

    public TestSet testCases(
            List<TestCase> testCases
    ) {
        this.testCases = testCases;
        return this;
    }

    public String getIssueId() {
        return issueId;
    }

    public JiraIssue getJira() {
        return jira;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

}
