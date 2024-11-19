package es.iti.wakamiti.xray.model;

import java.util.List;

public class XRayPlan {

    private String id;

    private String summary;
    private String projectId;
    private List<XRayTestCase> testCases;


    public XRayPlan(String id, String summary, String projectId, List<XRayTestCase> testCases) {
        this.id = id;
        this.summary = summary;
        this.projectId = projectId;
        this.testCases = testCases;
    }

    public XRayPlan() {

    }

    public XRayPlan id(String id) {
        this.id = id;
        return this;
    }

    public XRayPlan summary(String summary) {
        this.summary = summary;
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

    public String getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<XRayTestCase> getTestCases() {
        return testCases;
    }
}