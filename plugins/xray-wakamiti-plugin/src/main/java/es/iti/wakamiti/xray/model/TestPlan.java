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


/**
 * Provides the Test Plan functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPlan {

    private String issueId;

    private JiraIssue jira = new JiraIssue();
    private String projectId;
    private List<TestCase> testCases;
    private TestExecution testExecution;

    /**
     * Creates a complete Xray Test Plan projection.
     *
     * @param issueId Xray internal issue identifier
     * @param jira backing Jira issue
     * @param projectId Jira project identifier
     * @param testCases tests currently assigned to the plan
     */
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

    /**
     * Creates an empty plan for GraphQL deserialization.
     */
    public TestPlan() {
    }

    /**
     * Assigns the internal Xray issue identifier returned for this Test Plan.
     *
     * @param id Xray internal issue identifier
     * @return this plan
     */
    public TestPlan id(
            String id
    ) {
        this.issueId = id;
        return this;
    }

    /**
     * Associates the Jira issue that stores the Test Plan's visible metadata.
     *
     * @param jira backing Jira issue projection
     * @return this plan
     */
    public TestPlan jira(
            JiraIssue jira
    ) {
        this.jira = jira;
        return this;
    }

    /**
     * Sets the Jira project identifier reported by Xray for this plan.
     *
     * @param projectId Jira project identifier
     * @return this plan
     */
    public TestPlan projectId(
            String projectId
    ) {
        this.projectId = projectId;
        return this;
    }

    /**
     * Replaces the tests currently associated with the remote Test Plan.
     *
     * @param testCases tests assigned to the plan
     * @return this plan
     */
    public TestPlan testCases(
            List<TestCase> testCases
    ) {
        this.testCases = testCases;
        return this;
    }

    /**
     * Records the Test Execution created for the current Wakamiti run.
     *
     * @param testExecution execution created for this synchronization
     * @return this plan
     */
    public TestPlan testExecution(
            TestExecution testExecution
    ) {
        this.testExecution = testExecution;
        return this;
    }

    /**
     * @return Xray internal issue identifier
     */
    public String getIssueId() {
        return issueId;
    }

    /**
     * @return Jira issue backing the plan
     */
    public JiraIssue getJira() {
        return jira;
    }

    /**
     * @return Jira project identifier
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * @return tests assigned to the plan
     */
    public List<TestCase> getTestCases() {
        return testCases;
    }

    /**
     * @return execution created for this plan run
     */
    public TestExecution getTestExecution() {
        return testExecution;
    }

}
