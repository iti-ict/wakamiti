package es.iti.wakamiti.xray.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XRayPlan {

    private String issueId;

    private JiraIssue jira = new JiraIssue();
    private String projectId;
    private List<XRayTestCase> testCases;


    public XRayPlan(String issueId, JiraIssue jira, String projectId, List<XRayTestCase> testCases) {
        this.issueId = issueId;
        this.jira = jira;
        this.projectId = projectId;
        this.testCases = testCases;
    }

    public XRayPlan() {

    }

    public XRayPlan id(String id) {
        this.issueId = id;
        return this;
    }

    public XRayPlan jira(JiraIssue jira) {
        this.jira = jira;
        return this;
    }

    public XRayPlan projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public XRayPlan testCases(List<XRayTestCase> testCases) {
        this.testCases = testCases;
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

    public List<XRayTestCase> getTestCases() {
        return testCases;
    }
}