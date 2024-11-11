package es.iti.wakamiti.xray.dto;

import java.util.List;

public class XRayPlan {

    private final String id;

    private final String summary;
    private final String projectId;
    private final List<XRayTestCase> testCases;


    public XRayPlan(String id, String summary, String projectId, List<XRayTestCase> testCases) {
        this.id = id;
        this.summary = summary;
        this.projectId = projectId;
        this.testCases = testCases;
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