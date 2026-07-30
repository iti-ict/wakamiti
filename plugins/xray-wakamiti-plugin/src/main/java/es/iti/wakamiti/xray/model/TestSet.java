/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Provides the Test Set functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSet {

    private String issueId;
    private JiraIssue jira;
    private List<TestCase> testCases = new ArrayList<>();

    /**
     * Creates a complete Xray Test Set projection.
     *
     * @param issueId Xray internal issue identifier
     * @param jira backing Jira issue
     * @param testCases tests assigned to the set
     */
    public TestSet(
            String issueId,
            JiraIssue jira,
            List<TestCase> testCases
    ) {
        this.issueId = issueId;
        this.jira = jira;
        this.testCases = testCases;
    }

    /**
     * Creates an empty set for GraphQL deserialization.
     */
    public TestSet() {
    }

    /**
     * Assigns the internal Xray issue identifier returned for this Test Set.
     *
     * @param issueId Xray internal issue identifier
     * @return this set
     */
    public TestSet issueId(
            String issueId
    ) {
        this.issueId = issueId;
        return this;
    }

    /**
     * Associates the Jira issue that stores the Test Set's visible metadata.
     *
     * @param issue backing Jira issue
     * @return this set
     */
    public TestSet issue(
            JiraIssue issue
    ) {
        this.jira = issue;
        return this;
    }

    /**
     * Replaces the tests currently associated with this remote Test Set.
     *
     * @param testCases tests assigned to this set
     * @return this set
     */
    public TestSet testCases(
            List<TestCase> testCases
    ) {
        this.testCases = testCases;
        return this;
    }

    /**
     * @return Xray internal issue identifier
     */
    public String getIssueId() {
        return issueId;
    }

    /**
     * @return Jira issue backing the set
     */
    public JiraIssue getJira() {
        return jira;
    }

    /**
     * @return tests assigned to the set
     */
    public List<TestCase> getTestCases() {
        return testCases;
    }

}
