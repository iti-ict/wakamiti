package es.iti.wakamiti.xray.model;

public class TestExecution {

    private String issueId;
    private JiraIssue jira;

    public TestExecution() {
    }

    public JiraIssue getJira() {
        return jira;
    }

    public TestExecution jira(JiraIssue jira) {
        this.jira = jira;
        return this;
    }

    public String getIssueId() {
        return issueId;
    }

    public TestExecution issueId(String issueId) {
        this.issueId = issueId;
        return this;
    }
}
