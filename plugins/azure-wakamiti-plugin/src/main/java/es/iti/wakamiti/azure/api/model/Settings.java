/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.time.ZoneId;


public class Settings extends BaseModel {

    private ZoneId zoneId;
    private String configuration;
    private String testCaseType;

    public Settings zoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public ZoneId zoneId() {
        return zoneId;
    }

    public Settings configuration(String configuration) {
        this.configuration = configuration;
        return this;
    }

    public String configuration() {
        return configuration;
    }

    public Settings testCaseType(String testCaseType) {
        this.testCaseType = testCaseType;
        return this;
    }

    public String testCaseType() {
        return testCaseType;
    }
}
