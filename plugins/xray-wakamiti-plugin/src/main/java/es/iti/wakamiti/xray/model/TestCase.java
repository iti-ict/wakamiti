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

    public TestCase(
            String issueId,
            JiraIssue jira,
            String gherkin
    ) {
        this.issueId = issueId;
        this.jira = jira;
        this.gherkin = gherkin;
    }

    public TestCase() {
    }

    public TestCase testRunId(
            String testRunId
    ) {
        this.testRunId = testRunId;
        return this;
    }

    public TestCase issueId(
            String issueId
    ) {
        this.issueId = issueId;
        return this;
    }

    public TestCase issue(
            JiraIssue issue
    ) {
        this.jira = issue;
        return this;
    }

    public TestCase gherkin(
            String gherkin
    ) {
        this.gherkin = gherkin;
        return this;
    }

    public TestCase status(
            String status
    ) {
        this.status = status;
        return this;
    }

    public TestCase testSetList(
            List<TestSet> testSetList
    ) {
        this.testSetList = testSetList;
        return this;
    }

    public String getTestRunId() {
        return testRunId;
    }

    public String getIssueId() {
        return issueId;
    }

    public JiraIssue getJira() {
        return jira;
    }

    public String getGherkin() {
        return gherkin;
    }

    public String getStatus() {
        return status;
    }

    public List<TestSet> getTestSetList() {
        return testSetList;
    }

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

    public boolean hasSameLabels(
            TestCase testCase
    ) {
        return new HashSet<>(this.getJira().getLabels()).containsAll(testCase.getJira().getLabels());
    }

}
