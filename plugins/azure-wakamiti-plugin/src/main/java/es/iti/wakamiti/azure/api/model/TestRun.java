/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Test Run functionality used by Wakamiti.
 */
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
    private Status state;
    @JsonProperty
    private String comment;
    @JsonProperty
    private String errorMessage;
    @JsonProperty
    private final boolean automated = true;

    /**
     * Assigns the numeric identifier returned for this Azure Test Run.
     *
     * @param id Azure test-run identifier
     * @return this run
     */
    public TestRun id(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return Azure test-run identifier
     */
    public String id() {
        return id;
    }

    /**
     * Sets the human-readable name displayed for the run in Azure DevOps.
     *
     * @param name run name displayed by Azure DevOps
     * @return this run
     */
    public TestRun name(
            String name
    ) {
        this.name = name;
        return this;
    }

    /**
     * @return run name displayed by Azure DevOps
     */
    public String name() {
        return name;
    }

    /**
     * Associates the Azure Test Plan under which this run is published.
     *
     * @param plan Azure test plan owning this run
     * @return this run
     */
    public TestRun plan(
            TestPlan plan
    ) {
        this.plan = plan;
        return this;
    }

    /**
     * @return Azure test plan owning this run
     */
    public TestPlan plan() {
        return plan;
    }

    /**
     * Replaces the executable test-point identifiers scheduled by this run.
     *
     * @param pointIds Azure test points scheduled in this automated run
     * @return this run
     */
    public TestRun pointIds(
            List<String> pointIds
    ) {
        this.pointIds = pointIds;
        return this;
    }

    /**
     * @return identifiers of scheduled Azure test points
     */
    public List<String> pointIds() {
        return pointIds;
    }

    /**
     * Sets the Azure lifecycle state used to open or complete this run.
     *
     * @param state lifecycle state published to Azure
     * @return this run
     */
    public TestRun state(
            Status state
    ) {
        this.state = state;
        return this;
    }

    /**
     * @return Azure test-run lifecycle state
     */
    public Status state() {
        return state;
    }

    /**
     * Sets the run-level summary displayed to Azure Test Plans users.
     *
     * @param comment human-readable run summary
     * @return this run
     */
    public TestRun comment(
            String comment
    ) {
        this.comment = comment;
        return this;
    }

    /**
     * @return human-readable run summary
     */
    public String comment() {
        return comment;
    }

    /**
     * Sets diagnostic failure information that applies to the complete run.
     *
     * @param errorMessage run-level failure detail
     * @return this run
     */
    public TestRun errorMessage(
            String errorMessage
    ) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * @return run-level failure detail
     */
    public String errorMessage() {
        return errorMessage;
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{id};
    }

    /**
     * Defines the values supported by Status.
     */
    public enum Status {

        /** The run state is not specified. */
        @JsonProperty("Unspecified")
        UNSPECIFIED,
        /** The run has not been started yet. */
        @JsonProperty("NotStarted")
        NOT_STARTED,
        /** The run is pending and waiting to be started. */
        @JsonProperty("Pending")
        PENDING,
        /** The run is currently in progress. */
        @JsonProperty("InProgress")
        IN_PROGRESS,
        /** The run has been completed successfully. */
        @JsonProperty("Completed")
        COMPLETED,
        /** The run is waiting for an external condition or resource. */
        @JsonProperty("Waiting")
        WAITING,
        /** The run was aborted before completion. */
        @JsonProperty("Aborted")
        ABORTED,
        /** The run requires investigation due to an unexpected result or failure. */
        @JsonProperty("NeedsInvestigation")
        NEEDS_INVESTIGATION;

    }

}
