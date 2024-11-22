/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRun extends BaseModel {

    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private TestPlan plan;
    @JsonProperty
    private List<String> pointIds;
    @JsonProperty
    private List<Tag> tags;
    @JsonProperty
    private Status state;
    @JsonProperty
    private String comment;
    @JsonProperty
    private String errorMessage;
    @JsonProperty
    private final boolean automated = true;

    public TestRun id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public TestRun name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public TestRun plan(TestPlan plan) {
        this.plan = plan;
        return this;
    }

    public TestPlan plan() {
        return plan;
    }

    public TestRun pointIds(List<String> pointIds) {
        this.pointIds = pointIds;
        return this;
    }

    public List<String> pointIds() {
        return pointIds;
    }

    public TestRun tags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public List<Tag> tags() {
        return tags;
    }

    public TestRun state(Status state) {
        this.state = state;
        return this;
    }

    public Status state() {
        return state;
    }

    public TestRun comment(String comment) {
        this.comment = comment;
        return this;
    }

    public String comment() {
        return comment;
    }

    public TestRun errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public enum Status {
        @JsonProperty("Unspecified")
        UNSPECIFIED,
        @JsonProperty("NotStarted")
        NOT_STARTED,
        @JsonProperty("Pending")
        PENDING,
        @JsonProperty("InProgress")
        IN_PROGRESS,
        @JsonProperty("Completed")
        COMPLETED,
        @JsonProperty("Waiting")
        WAITING,
        @JsonProperty("Aborted")
        ABORTED,
        @JsonProperty("NeedsInvestigation")
        NEEDS_INVESTIGATION;
    }
}
