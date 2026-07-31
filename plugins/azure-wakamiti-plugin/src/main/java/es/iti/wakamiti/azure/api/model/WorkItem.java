/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import static es.iti.wakamiti.api.util.MapUtils.toMap;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Work Item functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkItem {

    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    private Map<String, String> workItemFields;

    /**
     * Assigns the numeric identifier returned by Azure DevOps for this work item.
     *
     * @param id Azure work-item identifier
     * @return this work item
     */
    public WorkItem id(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return Azure work-item identifier
     */
    public String id() {
        return id;
    }

    /**
     * Sets the display name used when this lightweight work-item reference is shown.
     *
     * @param name display name associated with the work item
     * @return this work item
     */
    public WorkItem name(
            String name
    ) {
        this.name = name;
        return this;
    }

    /**
     * @return work-item display name
     */
    public String name() {
        return name;
    }

    /**
     * Flattens Azure's list of field maps into a single reference-name map.
     *
     * @param workItemFields field fragments returned by the API
     * @return this work item
     */
    @JsonProperty
    public WorkItem workItemFields(
            List<Map<String, String>> workItemFields
    ) {
        this.workItemFields = workItemFields.stream().flatMap(m -> m.entrySet().stream()).collect(toMap());
        return this;
    }

    /**
     * @return fields keyed by Azure reference name
     */
    public Map<String, String> workItemFields() {
        return workItemFields;
    }

}
