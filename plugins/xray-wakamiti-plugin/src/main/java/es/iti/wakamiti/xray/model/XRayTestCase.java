package es.iti.wakamiti.xray.model;

public class XRayTestCase {

    private final String issueId;
    private final JiraIssue issue;
    private final String gherkin;


    public XRayTestCase(String issueId, JiraIssue issue, String gherkin) {
        this.issueId = issueId;
        this.issue = issue;
        this.gherkin = gherkin;
    }

    public String getIssueId() {
        return issueId;
    }

    public JiraIssue getIssue() {
        return issue;
    }

    public String getGherkin() {
        return gherkin;
    }
}
