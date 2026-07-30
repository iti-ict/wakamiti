/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Work Item Op functionality used by Wakamiti.
 */
public class WorkItemOp {

    @JsonProperty
    private Operation op;
    @JsonProperty
    private String path;
    @JsonProperty
    private String value;

    /**
     * Sets the JSON Patch operation, such as {@code add} or {@code replace}.
     *
     * @param op JSON Patch operation applied to an Azure work item
     * @return this operation
     */
    public WorkItemOp op(
            Operation op
    ) {
        this.op = op;
        return this;
    }

    /** @return JSON Patch operation kind */
    public Operation op() {
        return op;
    }

    /**
     * Sets the JSON Pointer that identifies the work-item property to change.
     *
     * @param path JSON Pointer path, typically under {@code /fields}
     * @return this operation
     */
    public WorkItemOp path(
            String path
    ) {
        this.path = path;
        return this;
    }

    /** @return Azure JSON Patch target path */
    public String path() {
        return path;
    }

    /**
     * Sets the value written to the selected work-item property.
     *
     * @param value serialized field or relation value
     * @return this operation
     */
    public WorkItemOp value(
            String value
    ) {
        this.value = value;
        return this;
    }

    /**
     * @return value supplied to the patch operation
     */
    public String value() {
        return value;
    }

    /**
     * Defines the values supported by Operation.
     */
    public enum Operation {

        /** Adds a new value to the specified work-item field. */
        @JsonProperty("add")
        ADD,
        /** Removes the value from the specified work-item field. */
        @JsonProperty("remove")
        REMOVE,
        /** Replaces the existing value of the specified work-item field. */
        @JsonProperty("replace")
        REPLACE

    }

}
