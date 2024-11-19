package es.iti.wakamiti.xray.model;

import java.util.List;

public class XRayTestSet {

    private String issueId;
    private JiraIssue jira;
    private List<XRayTestCase> testCases;

    public XRayTestSet(String issueId, JiraIssue jira, List<XRayTestCase> testCases) {
        this.issueId = issueId;
        this.jira = jira;
        this.testCases = testCases;
    }

    public XRayTestSet() {

    }

    public XRayTestSet issueId(String issueId) {
        this.issueId = issueId;
        return this;
    }

    public XRayTestSet issue(JiraIssue issue) {
        this.jira = issue;
        return this;
    }

    public XRayTestSet testCases(List<XRayTestCase> testCases) {
        this.testCases = testCases;
        return this;
    }

    public String getIssueId() {
        return issueId;
    }

    public JiraIssue getJira() {
        return jira;
    }

    public List<XRayTestCase> getTestCases() {
        return testCases;
    }
}
