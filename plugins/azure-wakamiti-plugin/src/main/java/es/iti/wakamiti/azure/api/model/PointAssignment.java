/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PointAssignment extends BaseModel {

    @JsonProperty
    private String id;
    @JsonProperty
    private String configurationId;
    @JsonProperty
    private String configurationName;

    public PointAssignment() { }

    public PointAssignment(String id, String configurationId, String configurationName) {
        this.id = id;
        this.configurationId = configurationId;
        this.configurationName = configurationName;
    }

    public String id() {
        return id;
    }

    public PointAssignment id(String id) {
        this.id = id;
        return this;
    }

    public String configurationId() {
        return configurationId;
    }

    public PointAssignment configurationId(String configurationId) {
        this.configurationId = configurationId;
        return this;
    }

    public String configurationName() {
        return configurationName;
    }

    public PointAssignment configurationName(String configurationName) {
        this.configurationName = configurationName;
        return this;
    }

}
