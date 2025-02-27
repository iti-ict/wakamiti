package es.iti.wakamiti.xray.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPlan {

    private String issueId;

    private JiraIssue jira = new JiraIssue();
    private String projectId;
    private List<TestCase> testCases;
    private TestExecution testExecution;


    public TestPlan(String issueId, JiraIssue jira, String projectId, List<TestCase> testCases) {
        this.issueId = issueId;
        this.jira = jira;
        this.projectId = projectId;
        this.testCases = testCases;
    }

    public TestPlan() {

    }

    public TestPlan id(String id) {
        this.issueId = id;
        return this;
    }

    public TestPlan jira(JiraIssue jira) {
        this.jira = jira;
        return this;
    }

    public TestPlan projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public TestPlan testCases(List<TestCase> testCases) {
        this.testCases = testCases;
        return this;
    }

    public TestPlan testExecution(TestExecution testExecution) {
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