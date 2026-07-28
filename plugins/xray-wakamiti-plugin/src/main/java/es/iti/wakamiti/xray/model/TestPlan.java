/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPlan {

    private String issueId;

    private JiraIssue jira = new JiraIssue();
    private String projectId;
    private List<TestCase> testCases;
    private TestExecution testExecution;
    
    public TestPlan(
            String issueId,
            JiraIssue jira,
            String projectId,
            List<TestCase> testCases
    ) {
        this.issueId = issueId;
        this.jira = jira;
        this.projectId = projectId;
        this.testCases = testCases;
    }

    public TestPlan() {
    }

    public TestPlan id(
            String id
    ) {
        this.issueId = id;
        return this;
    }

    public TestPlan jira(
            JiraIssue jira
    ) {
        this.jira = jira;
        return this;
    }

    public TestPlan projectId(
            String projectId
    ) {
        this.projectId = projectId;
        return this;
    }

    public TestPlan testCases(
            List<TestCase> testCases
    ) {
        this.testCases = testCases;
        return this;
    }

    public TestPlan testExecution(
            TestExecution testExecution
    ) {
        this.testExecution = testExecution;
        return this;
    }

    public String getIssueId() {
        return issueId;
    }

    public JiraIssue getJira() {
        return jira;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public TestExecution getTestExecution() {
        return testExecution;
    }

}
