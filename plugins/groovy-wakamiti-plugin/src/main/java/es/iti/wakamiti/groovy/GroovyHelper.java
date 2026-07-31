/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;


/**
 * Provides the Groovy Helper functionality used by Wakamiti.
 */
public final class GroovyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("es.iti.wakamiti.groovy");

    private GroovyHelper() {
    }

    /**
     * Evaluates a Groovy script with Wakamiti execution bindings.
     * <p>
     * The script receives {@code log}, an SLF4J logger, and {@code ctx}, the
     * current backend's extra-property map. For example,
     * {@code ctx.result = 2 + 2} stores a value for later steps.
     *
     * @param script Groovy source to evaluate
     * @return value of the script's final expression
     * @throws WakamitiException if compilation or execution fails
     */
    public static Object executeScript(
            String script
    ) {
        try {
            Binding binding = new Binding();
            binding.setVariable("log", LOGGER);
            binding.setVariable("ctx", WakamitiStepRunContext.current().backend().getExtraProperties());
            GroovyShell shell = new GroovyShell(binding);
            return shell.evaluate(script);
        } catch (Throwable e) { //NOSONAR
            throw new WakamitiException("Error executing groovy script", e);
        }
    }

}
