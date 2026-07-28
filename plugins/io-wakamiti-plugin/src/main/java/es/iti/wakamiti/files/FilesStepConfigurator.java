/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.files;


import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;


/**
 * Provides the Files Step Configurator functionality used by Wakamiti.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "files-step-config",
        version = "2.6"
)
public class FilesStepConfigurator implements ConfigContributor<FilesStepContributor> {

    private static final String ENTRY_SEPARATOR = "=";

    public static String FILES_ACCESS_TIMEOUT = "files.timeout";
    public static String FILES_LINKS = "files.links";
    public static String FILES_ENABLE_CLEANUP_UPON_COMPLETION = "files.enableCleanupUponCompletion";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                FILES_ACCESS_TIMEOUT, "60",
                FILES_ENABLE_CLEANUP_UPON_COMPLETION, "false"
        );
    }

    @Override
    public Configurer<FilesStepContributor> configurer() {
        return this::configure;
    }

    public void configure(
            FilesStepContributor contributor,
            Configuration configuration
    ) {
        configuration.get(FILES_ACCESS_TIMEOUT, Long.class)
                .ifPresent(contributor::setTimeout);
        configuration.get(FILES_ENABLE_CLEANUP_UPON_COMPLETION, Boolean.class)
                .ifPresent(contributor::setEnableCleanupUponCompletion);

        configuration.get(FILES_LINKS, String.class)
                .map(str -> str.split("[,;]"))
                .map(list -> Stream.of(list)
                        .filter(it -> it.contains(ENTRY_SEPARATOR))
                        .map(it -> it.split(ENTRY_SEPARATOR))
                        .collect(Collectors.toMap(it -> Path.of(it[0].trim()), it -> Path.of(it[1].trim())))
                )
                .ifPresent(contributor::setLinks);
    }

}
