/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;


/**
 * Wakamiti config contributor for the Groovy loader.
 * <p>
 * Declares supported configuration keys and maps them to
 * {@link GroovyLoaderContributor} at runtime.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "groovy-config",
        version = "2.13",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class GroovyConfigContributor implements ConfigContributor<GroovyLoaderContributor> {

    /**
     * When {@code true} (default), any Groovy compilation error aborts test
     * execution. Set to {@code false} to log errors and continue loading the
     * remaining sources.
     */
    public static final String GROOVY_COMPILATION_FAIL_ON_ERROR = "groovy.compilation.failOnError";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                GROOVY_COMPILATION_FAIL_ON_ERROR, Boolean.TRUE.toString()
        );
    }

    @Override
    public Configurer<GroovyLoaderContributor> configurer() {
        return this::configure;
    }

    private void configure(
            GroovyLoaderContributor contributor,
            Configuration configuration
    ) {
        configuration.get(GROOVY_COMPILATION_FAIL_ON_ERROR, Boolean.class)
                .ifPresent(contributor::setFailOnError);
    }

}
