/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSuiteTree extends TestSuite {

    @JsonProperty
    private List<TestSuiteTree> children;

    public TestSuiteTree children(List<TestSuiteTree> children) {
        this.children = children;
        return this;
    }

    public List<TestSuiteTree> children() {
        return children;
    }

}
