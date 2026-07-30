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
 * Provides the Test Run functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRun {

    private String id;
    private TestStatus status;
    private TestCase test;

    /** Creates an empty Xray Test Run for GraphQL deserialization. */
    public TestRun() {
        // Empty constructor
    }

    /**
     * @return Xray Test Run identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Assigns the identifier used by Xray Test Run mutations.
     *
     * @param id Xray Test Run identifier
     * @return this run
     */
    public TestRun setId(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return current Xray execution status
     */
    public TestStatus getStatus() {
        return status;
    }

    /**
     * Sets the execution status currently reported for this Test Run.
     *
     * @param status current Xray execution status
     * @return this run
     */
    public TestRun setStatus(
            TestStatus status
    ) {
        this.status = status;
        return this;
    }

    /**
     * @return Xray Test executed by this run
     */
    public TestCase getTest() {
        return test;
    }

    /**
     * Associates the Xray Test whose execution this run represents.
     *
     * @param test Xray Test executed by this run
     * @return this run
     */
    public TestRun setTest(
            TestCase test
    ) {
        this.test = test;
        return this;
    }

}
