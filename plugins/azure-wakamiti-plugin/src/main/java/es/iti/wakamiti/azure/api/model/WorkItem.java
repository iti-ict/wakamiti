/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import static es.iti.wakamiti.api.util.MapUtils.toMap;


@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkItem {

    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    private Map<String, String> workItemFields;

    public WorkItem id(String id) {
        this.id = id;
        return this;
    }

    public WorkItem name(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty
    public WorkItem workItemFields(List<Map<String, String>> workItemFields) {
        this.workItemFields = workItemFields.stream().flatMap(m -> m.entrySet().stream()).collect(toMap());
        return this;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Map<String, String> workItemFields() {
        return workItemFields;
    }

}
