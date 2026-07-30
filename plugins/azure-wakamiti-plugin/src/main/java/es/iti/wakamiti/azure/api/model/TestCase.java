/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;


/**
 * Provides the Test Case functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase extends BaseModel {

    /** Azure DevOps work-item category used to recognize Test Case types. */
    public static final String CATEGORY = "Microsoft.TestCaseCategory";

    @JsonProperty
    private String id;
    private String name;
    private String description;
    private String tag;
    private int order;
    private TestSuite suite;
    private List<PointAssignment> pointAssignments;
    private transient PlanNodeSnapshot metadata;

    /**
     * Assigns the work-item identifier of the Azure Test Case.
     *
     * @param id Azure Test Case work-item identifier
     * @return this test case
     */
    public TestCase id(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return Azure Test Case work-item identifier
     */
    public String id() {
        return id;
    }

    /**
     * Sets the title synchronized from the Wakamiti feature or scenario.
     *
     * @param name synchronized Test Case title
     * @return this test case
     */
    public TestCase name(
            String name
    ) {
        this.name = name;
        return this;
    }

    /**
     * @return synchronized Test Case title
     */
    public String name() {
        return name;
    }

    /**
     * Sets the formatted description published in the Azure work item.
     *
     * @param description Azure Test Case description, generally HTML
     * @return this test case
     */
    public TestCase description(
            String description
    ) {
        this.description = description;
        return this;
    }

    /**
     * @return Azure Test Case description
     */
    public String description() {
        return description;
    }

    /**
     * Sets the label used to correlate this Azure Test Case with a Wakamiti node.
     *
     * @param tag synchronization tag associated with this case
     * @return this test case
     */
    public TestCase tag(
            String tag
    ) {
        this.tag = tag;
        return this;
    }

    /**
     * @return synchronization tag associated with this case
     */
    public String tag() {
        return tag;
    }

    /**
     * Sets the Test Case's position within its containing suite.
     *
     * @param order zero-based position within its suite
     * @return this test case
     */
    public TestCase order(
            int order
    ) {
        this.order = order;
        return this;
    }

    /**
     * @return position used to order the case within its suite
     */
    public int order() {
        return order;
    }

    /**
     * Associates the Azure suite in which this Test Case is organized.
     *
     * @param suite Azure suite containing this test case
     * @return this test case
     */
    public TestCase suite(
            TestSuite suite
    ) {
        this.suite = suite;
        return this;
    }

    /**
     * @return Azure suite containing this test case
     */
    public TestSuite suite() {
        return suite;
    }

    /**
     * Replaces the executable points combining this Test Case and configurations.
     *
     * @param pointAssignments executable Azure test points for this case
     * @return this test case
     */
    public TestCase pointAssignments(
            List<PointAssignment> pointAssignments
    ) {
        this.pointAssignments = pointAssignments;
        return this;
    }

    /**
     * @return test-point and configuration assignments returned by Azure
     */
    public List<PointAssignment> pointAssignments() {
        return pointAssignments;
    }

    /**
     * Retains the immutable Wakamiti node required to map execution results later.
     *
     * @param metadata immutable Wakamiti plan node used to publish results
     * @return this test case
     */
    public TestCase metadata(
            PlanNodeSnapshot metadata
    ) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return source Wakamiti plan metadata; not serialized to Azure
     */
    public PlanNodeSnapshot metadata() {
        return metadata;
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{identifier()};
    }

    /**
     * Extracts the stable synchronization identifier from a title prefix.
     * For example, {@code [login-1] Valid login} yields {@code login-1}.
     *
     * @return bracketed identifier, or the unchanged title when no prefix exists
     */
    public String identifier() {
        return this.name.replaceAll("^\\[([^]]+)].+$", "$1");
    }

    /**
     * Compares the Azure-updatable title and description with another case.
     *
     * @param testCase candidate synchronized state
     * @return {@code true} when the candidate exists and either value differs
     */
    public boolean isDifferent(
            TestCase testCase
    ) {
        return Objects.nonNull(testCase)
                && (!this.name.equals(testCase.name) || !Objects.equals(this.description, testCase.description)
                );
    }

    /**
     * Copies suite, title and description from a newly derived case while
     * preserving this instance's Azure identity and execution assignments.
     *
     * @param testCase source state
     * @return this merged test case
     */
    public TestCase merge(
            TestCase testCase
    ) {
        this.suite = testCase.suite;
        this.name = testCase.name;
        this.description = testCase.description;
        return this;
    }

}
