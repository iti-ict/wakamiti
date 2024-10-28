package es.iti.wakamiti.xray.dto;

import java.util.List;

public class JiraIssue {

    private String key;
    private String self;
    private String summary;
    private String description;
    private String type;
    private List<String> labels;

    public JiraIssue(String key, String self, String summary, String description, String type) {
        this.key = key;
        this.self = self;
        this.summary = summary;
        this.description = description;
        this.type = type;
    }

    public JiraIssue(String key, String self, String summary, String description, String type, List<String> labels) {
        this.key = key;
        this.self = self;
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.labels = labels;
    }
}
