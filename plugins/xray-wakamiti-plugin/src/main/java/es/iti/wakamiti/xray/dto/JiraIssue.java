package es.iti.wakamiti.xray.dto;

import java.util.List;

public class JiraIssue {

    private final String key;
    private final String self;
    private final String summary;
    private final String description;
    private final String type;
    private final List<String> labels;

    public JiraIssue(String key, String self, String summary, String description, String type, List<String> labels) {
        this.key = key;
        this.self = self;
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.labels = labels;
    }

    public String getKey() {
        return key;
    }

    public String getSelf() {
        return self;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public List<String> getLabels() {
        return labels;
    }
}
