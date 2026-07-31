/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.io.Serializable;
import java.time.ZoneId;


/**
 * Provides the Settings functionality used by Wakamiti.
 */
public class Settings implements Serializable {

    private ZoneId zoneId;
    private String configuration;
    private String testCaseType;

    /**
     * Sets the time zone used to render run and result timestamps for Azure.
     *
     * @param zoneId zone used to serialize execution timestamps
     * @return these settings
     */
    public Settings zoneId(
            ZoneId zoneId
    ) {
        this.zoneId = zoneId;
        return this;
    }

    /**
     * @return zone used when publishing Azure execution timestamps
     */
    public ZoneId zoneId() {
        return zoneId;
    }

    /**
     * Selects the Azure Test Configuration assigned to synchronized test points.
     *
     * @param configuration Azure test-configuration name selected for synchronization
     * @return these settings
     */
    public Settings configuration(
            String configuration
    ) {
        this.configuration = configuration;
        return this;
    }

    /**
     * @return selected Azure test-configuration name
     */
    public String configuration() {
        return configuration;
    }

    /**
     * Selects the Azure work-item type used when creating test cases.
     *
     * @param testCaseType Azure work-item type used for synchronized test cases
     * @return these settings
     */
    public Settings testCaseType(
            String testCaseType
    ) {
        this.testCaseType = testCaseType;
        return this;
    }

    /**
     * @return configured Azure test-case work-item type
     */
    public String testCaseType() {
        return testCaseType;
    }

}
