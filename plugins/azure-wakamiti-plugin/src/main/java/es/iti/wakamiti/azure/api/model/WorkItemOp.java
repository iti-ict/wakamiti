/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonProperty;


public class WorkItemOp {

    @JsonProperty
    private Operation op;
    @JsonProperty
    private String path;
    @JsonProperty
    private String value;

    public WorkItemOp op(Operation op) {
        this.op = op;
        return this;
    }

    public Operation op() {
        return op;
    }

    public WorkItemOp path(String path) {
        this.path = path;
        return this;
    }

    public String path() {
        return path;
    }

    public WorkItemOp value(String value) {
        this.value = value;
        return this;
    }

    public String value() {
        return value;
    }

    public enum Operation {
        add,
        remove,
        replace
    }

}
