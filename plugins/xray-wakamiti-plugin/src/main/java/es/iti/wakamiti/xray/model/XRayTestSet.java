package es.iti.wakamiti.xray.model;

import java.util.List;

public class XRayTestSet {

    private String issueId;
    private JiraIssue issue;
    private List<XRayTestCase> testCases;

    public XRayTestSet(String issueId, JiraIssue issue, List<XRayTestCase> testCases) {
        this.issueId = issueId;
        this.issue = issue;
        this.testCases = testCases;
    }

    public XRayTestSet() {

    }

    public XRayTestSet issueId(String issueId) {
        this.issueId = issueId;
        return this;
    }

    public XRayTestSet issue(JiraIssue issue) {
        this.issue = issue;
        return this;
    }

    public XRayTestSet testCases(List<XRayTestCase> testCases) {
        this.testCases = testCases;
        return this;
    }

    public String getIssueId() {
        return issueId;
    }

    public JiraIssue getIssue() {
        return issue;
    }

    public List<XRayTestCase> getTestCases() {
        return testCases;
    }
}
