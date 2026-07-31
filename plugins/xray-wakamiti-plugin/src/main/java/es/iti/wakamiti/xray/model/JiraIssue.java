/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Provides the Jira Issue functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {

    private String key;
    private String self;
    private String summary = "";
    private String description = "";
    private String type;
    private List<String> labels = new ArrayList<>();

    /**
     * Creates a complete Jira issue projection.
     *
     * @param key human-readable issue key
     * @param self Jira REST resource URL
     * @param summary issue summary
     * @param description issue description
     * @param type Jira issue-type name
     * @param labels Jira labels
     */
    public JiraIssue(
            String key,
            String self,
            String summary,
            String description,
            String type,
            List<String> labels
    ) {
        this.key = key;
        this.self = self;
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.labels = labels;
    }

    /**
     * Creates an empty issue for JSON deserialization or fluent population.
     */
    public JiraIssue() {
    }

    /**
     * Sets the human-readable key assigned to the backing Jira issue.
     *
     * @param key Jira issue key
     * @return this issue
     */
    public JiraIssue key(
            String key
    ) {
        this.key = key;
        return this;
    }

    /**
     * Sets the REST resource URL identifying the backing Jira issue.
     *
     * @param self Jira REST resource URL
     * @return this issue
     */
    public JiraIssue self(
            String self
    ) {
        this.self = self;
        return this;
    }

    /**
     * Sets the Jira summary displayed as the Xray entity's title.
     *
     * @param summary Jira issue summary
     * @return this issue
     */
    public JiraIssue summary(
            String summary
    ) {
        this.summary = summary;
        return this;
    }

    /**
     * Sets the Jira description synchronized from the Wakamiti plan.
     *
     * @param description Jira issue description
     * @return this issue
     */
    public JiraIssue description(
            String description
    ) {
        this.description = description;
        return this;
    }

    /**
     * Sets the Jira issue-type name represented by this projection.
     *
     * @param type Jira issue-type name
     * @return this issue
     */
    public JiraIssue type(
            String type
    ) {
        this.type = type;
        return this;
    }

    /**
     * Replaces the labels used to correlate and filter synchronized tests.
     *
     * @param labels labels used for filtering and synchronization
     * @return this issue
     */
    public JiraIssue labels(
            List<String> labels
    ) {
        this.labels = labels;
        return this;
    }

    /**
     * @return human-readable Jira issue key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return Jira REST resource URL
     */
    public String getSelf() {
        return self;
    }

    /**
     * @return issue summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return issue description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Jira issue-type name
     */
    public String getType() {
        return type;
    }

    /**
     * @return labels attached to the issue
     */
    public List<String> getLabels() {
        return labels;
    }

}
