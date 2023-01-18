/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.KukumoStepRunContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.groovy");

    public static Object executeScript(String script) {
        try {
            Binding binding = new Binding();
            binding.setVariable("log", LOGGER);
            binding.setVariable("ctx", KukumoStepRunContext.current());
            GroovyShell shell = new GroovyShell(binding);
            return shell.evaluate(script);
        } catch (Throwable e) { //NOSONAR
            throw new KukumoException("Error executing groovy script", e);
        }
    }
}
