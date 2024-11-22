/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.iti.wakamiti.api.plan.Result;


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

    public TestResult id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public TestResult startedDate(String startedDate) {
        this.startedDate = startedDate;
        return this;
    }

    public String startedDate() {
        return startedDate;
    }

    public TestResult completedDate(String completedDate) {
        this.completedDate = completedDate;
        return this;
    }

    public String completedDate() {
        return completedDate;
    }

    public TestResult outcome(Type outcome) {
        this.outcome = outcome;
        this.comment = outcome.comment();
        return this;
    }

    public Type outcome() {
        return outcome;
    }

    public TestResult comment(String comment) {
        this.comment = comment;
        return this;
    }

    public String comment() {
        return comment;
    }

    public TestResult testCase(TestCase testCase) {
        this.testCase = testCase;
        return this;
    }

    public TestCase testCase() {
        return testCase;
    }

    public TestResult state(TestRun.Status state) {
        this.state = state;
        return this;
    }

    public TestRun.Status state() {
        return state;
    }

    public TestResult errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public TestResult merge(TestResult other) {
        return this.startedDate(other.startedDate())
                .completedDate(other.completedDate())
                .outcome(other.outcome())
                .comment(other.comment())
                .errorMessage(other.errorMessage());
    }

    public enum Type {
        @JsonProperty("Unspecified")
        UNSPECIFIED(Result.UNDEFINED, "Execution undefined"),
        @JsonProperty("Passed")
        PASSED(Result.PASSED, "Execution successful"),
        @JsonProperty("Failed")
        FAILED(Result.FAILED, "Execution failed"),
        @JsonProperty("Error")
        ERROR(Result.ERROR, "Execution error"),
        @JsonProperty("NotApplicable")
        NOT_APPLICABLE(Result.NOT_IMPLEMENTED, "Execution not implemented"),
        @JsonProperty("NotExecuted")
        NOT_EXECUTED(Result.SKIPPED, "Execution skipped");

        private final Result result;
        private final String comment;

        Type(Result result, String comment) {
            this.result = result;
            this.comment = comment;
        }

        public String comment() {
            return comment;
        }

        public static Type valueOf(Result result) {
            if (result == null) return null;
            for (Type type : Type.values()) {
                if (type.result == result) {
                    return type;
                }
            }
            return null;
        }
    }
}
