package es.iti.wakamiti.xray.model;

import java.util.HashSet;
import java.util.List;

public class XRayTestCase {

    private String issueId;
    private JiraIssue issue;
    private String gherkin;
    private List<XRayTestSet> testSetList;

    public XRayTestCase(String issueId, JiraIssue issue, String gherkin) {
        this.issueId = issueId;
        this.issue = issue;
        this.gherkin = gherkin;
    }

    public XRayTestCase() {
    }

    public XRayTestCase issueId(String issueId) {
        this.issueId = issueId;
        return this;
    }

    public XRayTestCase issue(JiraIssue issue) {
        this.issue = issue;
        return this;
    }

    public XRayTestCase gherkin(String gherkin) {
        this.gherkin = gherkin;
        return this;
    }

    public XRayTestCase testSetList(List<XRayTestSet> testSetList) {
        this.testSetList = testSetList;
        return this;
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

    public List<XRayTestSet> getTestSetList() {
        return testSetList;
    }

    public boolean isDifferent(XRayTestCase testCase) {
        return !this.getGherkin().equals(testCase.getGherkin()) || !this.getIssue().getSummary().equals(testCase.getIssue().getSummary())
                || !this.getIssue().getDescription().equals(testCase.getIssue().getDescription());
    }


    public boolean hasSameLabels(XRayTestCase testCase) {
        return new HashSet<>(this.getIssue().getLabels()).containsAll(testCase.getIssue().getLabels());
    }

}
