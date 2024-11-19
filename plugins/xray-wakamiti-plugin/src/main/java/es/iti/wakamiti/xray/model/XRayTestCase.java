package es.iti.wakamiti.xray.model;

import java.util.HashSet;
import java.util.List;

public class XRayTestCase {

    private String issueId;
    private JiraIssue jira;
    private String gherkin;
    private List<XRayTestSet> testSetList;

    public XRayTestCase(String issueId, JiraIssue jira, String gherkin) {
        this.issueId = issueId;
        this.jira = jira;
        this.gherkin = gherkin;
    }

    public XRayTestCase() {
    }

    public XRayTestCase issueId(String issueId) {
        this.issueId = issueId;
        return this;
    }

    public XRayTestCase issue(JiraIssue issue) {
        this.jira = issue;
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

    public JiraIssue getJira() {
        return jira;
    }

    public String getGherkin() {
        return gherkin;
    }

    public List<XRayTestSet> getTestSetList() {
        return testSetList;
    }

    public boolean isDifferent(XRayTestCase testCase) {
        return !this.getGherkin().equals(testCase.getGherkin()) || !this.getJira().getSummary().equals(testCase.getJira().getSummary())
                || !this.getJira().getDescription().equals(testCase.getJira().getDescription());
    }


    public boolean hasSameLabels(XRayTestCase testCase) {
        return new HashSet<>(this.getJira().getLabels()).containsAll(testCase.getJira().getLabels());
    }

}
