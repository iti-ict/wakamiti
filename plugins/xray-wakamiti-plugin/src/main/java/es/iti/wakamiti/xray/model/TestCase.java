/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Provides the Test Case functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase {

    private String testRunId;
    private String issueId;
    private JiraIssue jira;
    private String gherkin = "";
    private String status;
    private List<TestSet> testSetList;

    /**
     * Creates an Xray Test projection.
     *
     * @param issueId Xray internal issue identifier
     * @param jira associated Jira issue fields
     * @param gherkin serialized scenario or feature
     */
    public TestCase(
            String issueId,
            JiraIssue jira,
            String gherkin
    ) {
        this.issueId = issueId;
        this.jira = jira;
        this.gherkin = gherkin;
    }

    /**
     * Creates an empty test for GraphQL deserialization.
     */
    public TestCase() {
    }

    /**
     * Associates the remote Test Run used when publishing this test's result.
     *
     * @param testRunId Xray run identifier for result updates
     * @return this test
     */
    public TestCase testRunId(
            String testRunId
    ) {
        this.testRunId = testRunId;
        return this;
    }

    /**
     * Assigns the internal Xray issue identifier returned by GraphQL.
     *
     * @param issueId Xray internal issue identifier
     * @return this test
     */
    public TestCase issueId(
            String issueId
    ) {
        this.issueId = issueId;
        return this;
    }

    /**
     * Associates the Jira projection that stores this test's user-visible fields.
     *
     * @param issue associated Jira issue projection
     * @return this test
     */
    public TestCase issue(
            JiraIssue issue
    ) {
        this.jira = issue;
        return this;
    }

    /**
     * Sets the serialized Gherkin definition sent to Xray for this test.
     *
     * @param gherkin serialized feature or scenario source
     * @return this test
     */
    public TestCase gherkin(
            String gherkin
    ) {
        this.gherkin = gherkin;
        return this;
    }

    /**
     * Sets the desired Xray execution status for result synchronization.
     *
     * @param status Xray execution status name
     * @return this test
     */
    public TestCase status(
            String status
    ) {
        this.status = status;
        return this;
    }

    /**
     * Replaces the Xray Test Sets to which this test should belong.
     *
     * @param testSetList Xray Test Sets containing this test
     * @return this test
     */
    public TestCase testSetList(
            List<TestSet> testSetList
    ) {
        this.testSetList = testSetList;
        return this;
    }

    /**
     * @return Xray run identifier used to update this test's status
     */
    public String getTestRunId() {
        return testRunId;
    }

    /**
     * @return Xray internal issue identifier
     */
    public String getIssueId() {
        return issueId;
    }

    /**
     * @return associated Jira issue fields
     */
    public JiraIssue getJira() {
        return jira;
    }

    /**
     * @return synchronized Gherkin source
     */
    public String getGherkin() {
        return gherkin;
    }

    /**
     * @return Xray execution status name
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return Xray Test Sets containing this test
     */
    public List<TestSet> getTestSetList() {
        return testSetList;
    }

    /**
     * Compares synchronized summary, description and Gherkin source.
     *
     * @param testCase candidate local state
     * @return {@code true} when any synchronized content differs
     */
    public boolean isDifferent(
            TestCase testCase
    ) {
        return !hasSameSummary(testCase) || !hasSameGherkin(testCase) || !hasSameDescription(testCase);
    }

    private boolean hasSameSummary(
            TestCase testCase
    ) {
        return this.getJira().getSummary().equals(testCase.getJira().getSummary());
    }

    private boolean hasSameDescription(
            TestCase testCase
    ) {
        return this.getJira().getDescription().equals(testCase.getJira().getDescription());
    }

    private boolean hasSameGherkin(
            TestCase testCase
    ) {
        return this.getGherkin().equals(testCase.getGherkin());
    }

    /**
     * Tests whether this remote test already contains every candidate label.
     *
     * @param testCase candidate local state
     * @return {@code true} when no candidate label needs adding
     */
    public boolean hasSameLabels(
            TestCase testCase
    ) {
        return new HashSet<>(this.getJira().getLabels()).containsAll(testCase.getJira().getLabels());
    }

}
