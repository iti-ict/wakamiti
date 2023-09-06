/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test;

import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;

import java.time.ZoneId;
import java.time.ZoneOffset;


@I18nResource("customs")
public class WakamitiSteps implements StepContributor {

    private static final Logger LOGGER = WakamitiLogger.forClass(WakamitiSteps.class);

    @Override
    public String info() {
        return "test";
    }

    @Step(value = "number.addition", args = {"x:int", "y:int"})
    public void whatever(Integer x, Integer y) {
        int result = x + y;
        LOGGER.info("{} + {} = {}", x, y, result);
    }

    @Step(value = "something", args = {"name:word"})
    public void something(String name) {
        LOGGER.info("Hello {}!", name);
        LOGGER.info("TZ: {}", ZoneId.systemDefault());

        assert ZoneOffset.systemDefault() == ZoneId.of("Europe/Madrid");
    }

}