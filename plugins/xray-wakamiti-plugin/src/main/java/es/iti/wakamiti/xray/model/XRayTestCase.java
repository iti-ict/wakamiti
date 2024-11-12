package es.iti.wakamiti.xray.model;

import java.util.HashSet;

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

    public boolean isDifferent(XRayTestCase testCase) {
        return !this.getGherkin().equals(testCase.getGherkin()) || !this.getIssue().getSummary().equals(testCase.getIssue().getSummary())
                || !this.getIssue().getDescription().equals(testCase.getIssue().getDescription());
    }


    public boolean hasSameLabels(XRayTestCase testCase) {
        return new HashSet<>(this.getIssue().getLabels()).containsAll(testCase.getIssue().getLabels());
    }

}
