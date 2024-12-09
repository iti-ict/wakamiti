package es.iti.wakamiti.xray.api;

import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestCase;
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


    public JiraIssue updateIssue(String id, String newSummary, String newDescription, List<String> newLabels) {

        String payload = toJSON(Map.of(
                "update", Map.of(
                        "summary", Collections.singletonList(Map.of(
                                "set", newSummary
                        ))
//                        ,
//                        "description", Collections.singletonList(Map.of(
//                                "set", newDescription
//                        ))
//                        ,
//                        "labels", newLabels.stream().map(label -> Map.of("add", label)).collect(Collectors.toList())
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

    public void updateTestCases(List<Pair<TestCase, TestCase>> testCases) {
        testCases.forEach(p -> {
            TestCase oldTest = p.key();
            TestCase newTest = p.value();

            updateIssue(oldTest.getIssueId(), newTest.getJira().getSummary(), newTest.getJira().getDescription(), newTest.getJira().getLabels());
        });
    }

}
