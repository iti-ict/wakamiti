/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("es.iti.wakamiti.groovy");

    public static Object executeScript(String script) {
        //Map<String, Object> ctx = WakamitiStepRunContext.current().backend().getExtraProperties();
        try {
            Binding binding = new Binding();
            binding.setVariable("log", LOGGER);
            binding.setVariable("ctx", WakamitiStepRunContext.current().backend().getExtraProperties());
            GroovyShell shell = new GroovyShell(binding);
            Object result = shell.evaluate(script);
            //WakamitiStepRunContext.current().backend().getExtraProperties().putAll(ctx);
            return result;
        } catch (Throwable e) { //NOSONAR
            throw new WakamitiException("Error executing groovy script", e);
        }
    }

}
