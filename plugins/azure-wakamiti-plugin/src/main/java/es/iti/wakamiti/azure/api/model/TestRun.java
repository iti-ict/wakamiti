/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.util.List;


public class TestRun extends BaseModel {

    private String id;
    private String name;
    private TestPlan plan;
    private List<String> pointIds;
    private List<Tag> tags;
    private Status state;
    private String startDate;
    private String completeDate;
    private String comment;
    private String errorMessage;
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

    public TestRun startDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String startDate() {
        return startDate;
    }

    public TestRun completeDate(String completeDate) {
        this.completeDate = completeDate;
        return this;
    }

    public String completeDate() {
        return completeDate;
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
        Unspecified,
        NotStarted,
        InProgress,
        Completed,
        Waiting,
        Aborted,
        NeedsInvestigation;
    }
}
