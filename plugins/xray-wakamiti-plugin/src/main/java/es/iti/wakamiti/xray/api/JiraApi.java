package es.iti.wakamiti.xray.api;

import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.xray.internal.JiraType;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.XRayPlan;
import es.iti.wakamiti.xray.model.XRayTestCase;
import org.slf4j.Logger;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.JsonUtils.read;

public class JiraApi extends BaseApi {

    private static final String API_ISSUE = "/rest/api/3/issue";
    private final String project;
    private final Logger logger;

    public JiraApi(URL urlBase, String credentials, String project, Logger logger) {
        super(urlBase, "Basic " + credentials, logger);
        this.project = project;
        this.logger = logger;
    }

    public JiraIssue createIssue(String summary, String description, String type) {
        String payload = toJSON(Map.of(
                "fields", Map.of(
                        "project", Map.of(
                                "key", project)),
                "issueType", Map.of(
                        "name", type),
                "summary", summary,
                "description", Map.of(
                        "type", "doc",
                        "version", 1,
                        "content", List.of(Map.of(
                                "type", "paragraph",
                                "content", List.of(Map.of(
                                        "text", description,
                                        "type", "text"
                                ))
                        ))
                )
        ));

        JsonNode response = post(API_ISSUE, payload);

        return extractJiraIssue(response);
    }

    public JiraIssue getIssue(String id) {
        JsonNode response = get(API_ISSUE + "/" + id);

        return extractJiraIssue(response);
    }

    public JiraIssue updateIssue(String id, String newSummary, String newDescription, List<String> newLabels) {

        String payload = toJSON(Map.of(
                "update", Map.of(
                        "summary", Collections.singletonList(Map.of(
                                "set", newSummary
                        )),
                        "description", Collections.singletonList(Map.of(
                                "set", newDescription
                        )),
                        "labels", newLabels.stream().map(label -> Map.of("add", label)).collect(Collectors.toList())
                )));

        JsonNode response = put(API_ISSUE + "/" + id, payload);

        return extractJiraIssue(response);
    }

    private JiraIssue extractJiraIssue(JsonNode response) {
        JiraIssue jiraIssue = read(response, "$", JiraIssue.class);

//        String key = extract(response, "$.id", "Cannot find the attribute 'id' of the issue");
//        String self = extract(response, "$.self", "Cannot find the attribute 'self' of the issue");
//        String summary = extract(response, "$.fields.summary", "Cannot find the attribute 'summary' of the issue");
//        String description = extract(response, "$.fields.description.content[0].content[0].text", "Cannot find the attribute 'description' of the issue");
//        String type = extract(response, "$.fields.issuetype.name", "Cannot find the attribute 'name' of the issue");
//        String labels = extract(response, "$.fields.labels", "Cannot find the attribute 'labels' of the issue");

        return jiraIssue;
    }

    public void updateTestCases(List<Pair<XRayTestCase, XRayTestCase>> testCases) {
        testCases.forEach(p -> {
            XRayTestCase oldTest = p.key();
            XRayTestCase newTest = p.value();

            updateIssue(oldTest.getIssueId(), newTest.getJira().getSummary(), newTest.getJira().getDescription(), newTest.getJira().getLabels());
        });
    }

    public List<String> createTestCases(XRayPlan remotePlan, List<XRayTestCase> newTests) {
        return newTests.stream().map(test -> {
            JiraIssue created = createIssue(test.getJira().getSummary(), test.getJira().getDescription(), JiraType.TEST.getName());
            return created.getKey();
        }).collect(Collectors.toList());
    }
}
