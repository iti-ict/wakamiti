package es.iti.wakamiti.xray.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {

    private String key;
    private String self;
    private String summary = "";
    private String description = "";
    private String type;
    private List<String> labels = new ArrayList<>();

    public JiraIssue(String key, String self, String summary, String description, String type, List<String> labels) {
        this.key = key;
        this.self = self;
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.labels = labels;
    }

    public JiraIssue() {

    }

    public JiraIssue key(String key) {
        this.key = key;
        return this;
    }

    public JiraIssue self(String self) {
        this.self = self;
        return this;
    }

    public JiraIssue summary(String summary) {
        this.summary = summary;
        return this;
    }

    public JiraIssue description(String description) {
        this.description = description;
        return this;
    }

    public JiraIssue type(String type) {
        this.type = type;
        return this;
    }

    public JiraIssue labels(List<String> labels) {
        this.labels = labels;
        return this;
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
