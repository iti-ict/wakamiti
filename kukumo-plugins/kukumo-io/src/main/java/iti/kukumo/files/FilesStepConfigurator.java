package iti.kukumo.files;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationConsumer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.Configurator;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static iti.kukumo.files.FilesStepConfiguration.*;

@Extension(provider = "iti.kukumo", name = "kukumo-files-step-config", version = "1.0")
public class FilesStepConfigurator implements Configurator<FilesStepContributor> {

    private static final String ENTRY_SEPARATOR = "=";

    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof FilesStepContributor;
    }

    @Override
    public void configure(FilesStepContributor contributor, Configuration configuration) {

        ConfigurationConsumer.of(configuration, contributor)
                .orDefault(
                        FILES_ACCESS_TIMEOUT,
                        Long.class,
                        Defaults.DEFAULT_FILES_ACCESS_TIMEOUT,
                        FilesStepContributor::setTimeout
                )
                .orDefault(
                        FILES_ENABLE_CLEANUP_UPON_COMPLETION,
                        Boolean.class,
                        Defaults.DEFAULT_FILES_ENABLE_CLEANUP_UPON_COMPLETION,
                        FilesStepContributor::setEnableCleanupUponCompletion
                )
        ;

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
