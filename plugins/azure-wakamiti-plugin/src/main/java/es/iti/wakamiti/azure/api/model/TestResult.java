/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.iti.wakamiti.api.plan.Result;


/**
 * Provides the Test Result functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestResult extends BaseModel {

    @JsonProperty
    private String id;
    @JsonProperty
    private String startedDate;
    @JsonProperty
    private String completedDate;
    @JsonProperty
    private Type outcome;
    @JsonProperty
    private String comment;
    @JsonProperty
    private TestCase testCase;
    @JsonProperty
    private TestRun.Status state;
    @JsonProperty
    private String errorMessage;

    /**
     * Assigns the identifier of this result within its Azure Test Run.
     *
     * @param id Azure test-result identifier
     * @return this result
     */
    public TestResult id(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return Azure test-result identifier
     */
    public String id() {
        return id;
    }

    /**
     * Sets the execution start instant serialized in Azure's expected format.
     *
     * @param startedDate Azure-compatible execution start timestamp
     * @return this result
     */
    public TestResult startedDate(
            String startedDate
    ) {
        this.startedDate = startedDate;
        return this;
    }

    /**
     * @return serialized execution start timestamp
     */
    public String startedDate() {
        return startedDate;
    }

    /**
     * Sets the execution completion instant serialized in Azure's expected format.
     *
     * @param completedDate Azure-compatible execution completion timestamp
     * @return this result
     */
    public TestResult completedDate(
            String completedDate
    ) {
        this.completedDate = completedDate;
        return this;
    }

    /**
     * @return serialized execution completion timestamp
     */
    public String completedDate() {
        return completedDate;
    }

    /**
     * Sets the Azure outcome and initializes its conventional explanatory
     * comment. A later {@link #comment(String)} call can provide more specific
     * execution detail.
     *
     * @param outcome mapped Wakamiti execution outcome
     * @return this result
     */
    public TestResult outcome(
            Type outcome
    ) {
        this.outcome = outcome;
        this.comment = outcome.comment();
        return this;
    }

    /**
     * @return Azure outcome mapped from the Wakamiti result
     */
    public Type outcome() {
        return outcome;
    }

    /**
     * Sets the human-readable execution summary shown with this result.
     *
     * @param comment execution summary shown with the result
     * @return this result
     */
    public TestResult comment(
            String comment
    ) {
        this.comment = comment;
        return this;
    }

    /**
     * @return execution summary shown by Azure DevOps
     */
    public String comment() {
        return comment;
    }

    /**
     * Associates the Azure Test Case whose execution produced this result.
     *
     * @param testCase Azure test case executed by this result
     * @return this result
     */
    public TestResult testCase(
            TestCase testCase
    ) {
        this.testCase = testCase;
        return this;
    }

    /**
     * @return Azure test case executed by this result
     */
    public TestCase testCase() {
        return testCase;
    }

    /**
     * Sets the publication state Azure should assign to this result.
     *
     * @param state containing run's publication state for this result
     * @return this result
     */
    public TestResult state(
            TestRun
                    .Status state
    ) {
        this.state = state;
        return this;
    }

    /**
     * @return result publication state represented with Azure run statuses
     */
    public TestRun.Status state() {
        return state;
    }

    /**
     * Sets the technical diagnostic text published when execution fails.
     *
     * @param errorMessage technical failure detail, stack trace or cause
     * @return this result
     */
    public TestResult errorMessage(
            String errorMessage
    ) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * @return technical failure detail published to Azure
     */
    public String errorMessage() {
        return errorMessage;
    }

    /**
     * Copies execution timing and outcome details from another result while
     * preserving this result's Azure identifier and linked test case.
     *
     * @param other newly calculated execution result
     * @return this merged result
     */
    public TestResult merge(
            TestResult other
    ) {
        return this.startedDate(other.startedDate())
                .completedDate(other.completedDate())
                .outcome(other.outcome())
                .comment(other.comment())
                .errorMessage(other.errorMessage());
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{id};
    }

    /**
     * Defines the values supported by Type.
     */
    public enum Type {

        /** Outcome was not provided by Azure DevOps. */
        @JsonProperty("Unspecified")
        UNSPECIFIED(Result.UNDEFINED, "Execution undefined"),
        /** Test execution completed successfully. */
        @JsonProperty("Passed")
        PASSED(Result.PASSED, "Execution successful"),
        /** Test execution completed with failed assertions. */
        @JsonProperty("Failed")
        FAILED(Result.FAILED, "Execution failed"),
        /** Test execution ended due to an execution error. */
        @JsonProperty("Error")
        ERROR(Result.ERROR, "Execution error"),
        /** Test is not applicable for the current context. */
        @JsonProperty("NotApplicable")
        NOT_APPLICABLE(Result.NOT_IMPLEMENTED, "Execution not implemented"),
        /** Test was skipped and therefore not executed. */
        @JsonProperty("NotExecuted")
        NOT_EXECUTED(Result.SKIPPED, "Execution skipped");

        /** Wakamiti result mapped to this Azure DevOps outcome. */
        private final Result result;
        /** Default human-readable explanation for this outcome. */
        private final String comment;

        Type(
                Result result,
                String comment
        ) {
            this.result = result;
            this.comment = comment;
        }

        /**
         * Returns the default explanation associated with this Azure outcome.
         *
         * @return concise execution summary
         */
        public String comment() {
            return comment;
        }

        /**
         * Maps a Wakamiti plan result to its Azure DevOps outcome.
         *
         * @param result Wakamiti execution result, or {@code null}
         * @return matching Azure outcome, or {@code null} when the input is
         *         null or has no mapping
         */
        public static Type valueOf(
                Result result
        ) {
            if (result == null) {
                return null;
            }
            for (Type type : Type.values()) {
                if (type.result == result) {
                    return type;
                }
            }
            return null;
        }

    }

}
