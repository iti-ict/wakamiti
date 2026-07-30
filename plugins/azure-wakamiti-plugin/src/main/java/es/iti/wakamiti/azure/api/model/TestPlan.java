/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Test Plan functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPlan extends BaseModel {

    /** Azure DevOps work-item category used to recognize Test Plan types. */
    public static final String CATEGORY = "Microsoft.TestPlanCategory";

    @JsonProperty
    private String id;
    @JsonProperty(required = true)
    private String name;
    @JsonProperty(value = "areaPath", required = true)
    private String area;
    @JsonProperty(required = true)
    private String iteration;
    @JsonProperty
    private String state;
    @JsonProperty
    private TestSuite rootSuite;

    /**
     * Creates an empty plan for JSON deserialization or fluent population.
     */
    public TestPlan() {
    }

    /**
     * Creates a plan identity from its name and classification paths.
     *
     * @param name Azure test-plan name
     * @param area owning Azure area path
     * @param iteration owning Azure iteration path
     */
    public TestPlan(
            String name,
            Path area,
            Path iteration
    ) {
        this.name = name;
        area(area);
        iteration(iteration);
    }

    /**
     * Assigns the numeric identifier returned by Azure for this Test Plan.
     *
     * @param id Azure test-plan identifier
     * @return this plan
     */
    public TestPlan id(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return Azure test-plan identifier
     */
    public String id() {
        return id;
    }

    /**
     * Sets the display name used to locate or create the Test Plan.
     *
     * @param name Azure test-plan display name
     * @return this plan
     */
    public TestPlan name(
            String name
    ) {
        this.name = name;
        return this;
    }

    /**
     * @return Azure test-plan display name
     */
    public String name() {
        return name;
    }

    /**
     * Sets the Azure classification area path assigned to the plan.
     *
     * @param area classification path; separators are normalized for Azure
     * @return this plan
     */
    public TestPlan area(
            Path area
    ) {
        this.area = area.toString().replace("/", "\\");
        return this;
    }

    /**
     * @return owning area classification as a {@link Path}
     */
    public Path area() {
        return Path.of(area);
    }

    /**
     * Sets the Azure iteration path assigned to the plan.
     *
     * @param iteration iteration path; separators are normalized for Azure
     * @return this plan
     */
    public TestPlan iteration(
            Path iteration
    ) {
        this.iteration = iteration.toString().replace("/", "\\");
        return this;
    }

    /**
     * @return owning iteration classification as a {@link Path}
     */
    public Path iteration() {
        return Path.of(iteration);
    }

    /**
     * Sets the Azure lifecycle state, controlling whether the plan is active.
     *
     * @param state Azure plan lifecycle state, such as active or inactive
     * @return this plan
     */
    public TestPlan state(
            String state
    ) {
        this.state = state;
        return this;
    }

    /**
     * @return Azure plan lifecycle state
     */
    public String state() {
        return state;
    }

    /**
     * Associates the root suite that Azure creates automatically with the plan.
     *
     * @param rootSuite root suite created by Azure for this plan
     * @return this plan
     */
    public TestPlan rootSuite(
            TestSuite rootSuite
    ) {
        this.rootSuite = rootSuite;
        return this;
    }

    /**
     * @return plan's root Azure test suite
     */
    public TestSuite rootSuite() {
        return rootSuite;
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{name, area, iteration};
    }

}
