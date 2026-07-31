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


/**
 * Provides the Point Assignment functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointAssignment extends BaseModel {

    @JsonProperty
    private String id;
    @JsonProperty
    private String configurationId;
    @JsonProperty
    private String configurationName;

    /**
     * Creates an empty assignment for JSON deserialization or fluent population.
     */
    public PointAssignment() {
    }

    /**
     * Creates a complete Azure test-point assignment.
     *
     * @param id test-point identifier used when creating a run
     * @param configurationId assigned test-configuration identifier
     * @param configurationName human-readable configuration name
     */
    public PointAssignment(
            String id,
            String configurationId,
            String configurationName
    ) {
        this.id = id;
        this.configurationId = configurationId;
        this.configurationName = configurationName;
    }

    /**
     * @return Azure test-point identifier
     */
    public String id() {
        return id;
    }

    /**
     * Assigns the Azure test-point identifier used when scheduling a run.
     *
     * @param id Azure test-point identifier
     * @return this assignment
     */
    public PointAssignment id(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return assigned Azure test-configuration identifier
     */
    public String configurationId() {
        return configurationId;
    }

    /**
     * Assigns the identifier of the Test Configuration behind this point.
     *
     * @param configurationId Azure test-configuration identifier
     * @return this assignment
     */
    public PointAssignment configurationId(
            String configurationId
    ) {
        this.configurationId = configurationId;
        return this;
    }

    /**
     * @return human-readable assigned configuration name
     */
    public String configurationName() {
        return configurationName;
    }

    /**
     * Sets the display name of the Test Configuration behind this point.
     *
     * @param configurationName displayed configuration name
     * @return this assignment
     */
    public PointAssignment configurationName(
            String configurationName
    ) {
        this.configurationName = configurationName;
        return this;
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{id};
    }

}
