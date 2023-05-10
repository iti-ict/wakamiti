/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import iti.wakamiti.api.WakamitiException;
import iti.wakamiti.api.WakamitiStepRunContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("iti.wakamiti.groovy");

    public static Object executeScript(String script) {
        try {
            Binding binding = new Binding();
            binding.setVariable("log", LOGGER);
            binding.setVariable("ctx", WakamitiStepRunContext.current());
            GroovyShell shell = new GroovyShell(binding);
            return shell.evaluate(script);
        } catch (Throwable e) { //NOSONAR
            throw new WakamitiException("Error executing groovy script", e);
        }
    }
}
