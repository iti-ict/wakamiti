/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Test Suite Tree functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSuiteTree extends TestSuite {

    @JsonProperty
    private List<TestSuiteTree> children;

    /**
     * Replaces the immediate suite nodes nested below this point in the tree.
     *
     * @param children immediate child suites returned by Azure
     * @return this tree node
     */
    public TestSuiteTree children(
            List<TestSuiteTree> children
    ) {
        this.children = children;
        return this;
    }

    /**
     * @return immediate child suite nodes
     */
    public List<TestSuiteTree> children() {
        return children;
    }

}
