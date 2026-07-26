/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRun {

    private String id;
    private TestStatus status;
    private TestCase test;

    public TestRun() {
        // Empty constructor
    }

    public String getId() {
        return id;
    }

    public TestRun setId(
            String id
    ) {
        this.id = id;
        return this;
    }

    public TestStatus getStatus() {
        return status;
    }

    public TestRun setStatus(
            TestStatus status
    ) {
        this.status = status;
        return this;
    }

    public TestCase getTest() {
        return test;
    }

    public TestRun setTest(
            TestCase test
    ) {
        this.test = test;
        return this;
    }

}
