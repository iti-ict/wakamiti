/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;

import es.iti.wakamiti.api.plan.Result;

import static es.iti.wakamiti.api.plan.Result.*;

public class TestResult extends BaseModel {

    private String id;
    private TestRun.Status state;
    private String createdDate;
    private String completedDate;
    private Type outcome;
    private String comment;
    private TestPlan testPlan;
    private TestSuite testSuite;
    private TestCase testCase;
    private TestRun testRun;

    public TestResult id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public TestResult state(TestRun.Status state) {
        this.state = state;
        return this;
    }

    public TestRun.Status state() {
        return state;
    }

    public TestResult createdDate(String createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public String createdDate() {
        return createdDate;
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

    public TestResult testPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
        return this;
    }

    public TestPlan testPlan() {
        return testPlan;
    }

    public TestResult testSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
        return this;
    }

    public TestSuite testSuite() {
        return testSuite;
    }

    public TestResult testCase(TestCase testCase) {
        this.testCase = testCase;
        return this;
    }

    public TestCase testCase() {
        return testCase;
    }

    public TestResult testRun(TestRun testRun) {
        this.testRun = testRun;
        return this;
    }

    public TestRun testRun() {
        return testRun;
    }

    public enum Type {
        Unspecified(UNDEFINED, "Execution undefined"),
        Passed(PASSED, "Execution successful"),
        Failed(FAILED, "Execution failed"),
        Error(ERROR, "Execution error"),
        NotApplicable(NOT_IMPLEMENTED, "Execution not implemented"),
        NotExecuted(SKIPPED, "Execution skipped"),;

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
            for (Type type : Type.values()) {
                if (type.result == result) {
                    return type;
                }
            }
            return null;
        }
    }
}
