/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.appium.test;


import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.appium.AppiumConfigContributor;

//@RunWith(WakamitiJUnitRunner.class)
// Appium tests requires Appium server and a AVD to work, cannot be included as part of automatic build
@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/appium.feature"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".platformName", value = "Android"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".platformVersion", value = "11"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".appPackage", value = "io.appium.android.apis"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".appActivity", value = ".view.TextFields"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".app", value = "ApiDemos-debug.apk"),
        @Property(key = AppiumConfigContributor.APPIUM_URL, value = "http://127.0.0.1:4723/wd/hub")
})
public class TestAppium {

}
