/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Provides the Test Status functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestStatus {

    String name;

    /**
     * Creates an empty status for GraphQL deserialization.
     */
    public TestStatus() {
        // Empty constructor
    }

    /**
     * @return Xray status name, for example {@code PASSED}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the status name used when comparing or updating Xray Test Runs.
     *
     * @param name Xray execution status name
     * @return this status
     */
    public TestStatus setName(
            String name
    ) {
        this.name = name;
        return this;
    }

}
