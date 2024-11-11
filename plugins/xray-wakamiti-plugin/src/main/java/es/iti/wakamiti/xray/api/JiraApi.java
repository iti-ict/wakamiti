package es.iti.wakamiti.xray.api;

import es.iti.wakamiti.xray.dto.JiraIssue;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JiraApi extends BaseApi {

    private static final String API_ISSUE = "/rest/api/3/issue";
    private final String project;
    private final Logger logger;

    public JiraApi(String urlBase, String credentials, String project, Logger logger) {
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

        String response = post(API_ISSUE, payload);
        String key = extract(response, "$.id", "Cannot find the attribute 'id' of the issue");
        String self = extract(response, "$.self", "Cannot find the attribute 'self' of the issue");

        return new JiraIssue(key, self, summary, description, type);
    }

    public JiraIssue getIssue(String id) {
        String response = get(API_ISSUE + "/" + id);

        String key = extract(response, "$.id", "Cannot find the attribute 'id' of the issue");
        String self = extract(response, "$.self", "Cannot find the attribute 'self' of the issue");
        String summary = extract(response, "$.fields.summary", "Cannot find the attribute 'summary' of the issue");
        String description = extract(response, "$.fields.description.content[0].content[0].text", "Cannot find the attribute 'description' of the issue");
        String type = extract(response, "$.fields.issuetype.name", "Cannot find the attribute 'self' of the issue");
        String labels = extract(response, "$.fields.labels", "Cannot find the attribute 'labels' of the issue");

        return new JiraIssue(key, self, summary, description, type, Arrays.asList(labels.split(",")));
    }
}
