/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.LoaderContributor;
import es.iti.wakamiti.api.util.ThrowableFunction;
import groovy.lang.GroovyClassLoader;


/**
 * This {@link LoaderContributor} allows load groovy sources as Java
 * classes.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "groovy-loader",
        version = "2.13"
)
public class GroovyLoaderContributor implements LoaderContributor {

    /** Logger category used while discovering and compiling Groovy contributors. */
    public static final Logger LOGGER = LoggerFactory.getLogger("es.iti.wakamiti.groovy");

    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private boolean failOnError = true;

    /**
     * Sets whether a Groovy compilation error should abort test execution.
     * <p>
     * When {@code true} (the default) any unresolvable compilation failure
     * throws a {@link WakamitiException}. When {@code false} errors are
     * logged and the remaining sources continue loading.
     *
     * @param failOnError {@code true} to abort on error, {@code false} to continue
     */
    public void setFailOnError(
            boolean failOnError
    ) {
        this.failOnError = failOnError;
    }

    @Override
    public Stream<? extends Class<?>> load(
            List<String> discoveryPaths
    ) {
        List<Path> groovyPaths = discoveryPaths.stream()
                .map(Paths::get)
                .flatMap(this::listFiles)
                .filter(file -> file.toFile().getName().toLowerCase().endsWith(".groovy"))
                .toList();

        if (groovyPaths.isEmpty()) {
            LOGGER.debug("No groovy classes to load");
            return Stream.empty();
        } else {
            groovyPaths.forEach(path -> LOGGER.debug("Groovy file [{}] found", path.getFileName()));
        }

        groovyPaths.stream().map(Path::getParent).map(Path::toUri).distinct()
                .map((ThrowableFunction<URI, URL>) URI::toURL)
                .forEach(groovyClassLoader::addURL);
        Thread.currentThread().setContextClassLoader(groovyClassLoader);
        WakamitiAPI.instance().contributors().setClassLoaders(Thread.currentThread().getContextClassLoader());

        return loadClasses(groovyPaths).stream();
    }

    private List<Class<?>> loadClasses(
            List<Path> paths
    ) {
        List<Class<?>> compiled = new LinkedList<>();
        List<Path> pending = new ArrayList<>(paths);
        Map<Path, Exception> failed = new HashMap<>();

        while (!pending.isEmpty()) {
            failed.clear();
            for (Path scriptFile : pending) {
                try {
                    compiled.add(groovyClassLoader.parseClass(scriptFile.toFile()));
                } catch (Exception e) {
                    failed.put(scriptFile, e);
                }
            }
            if (failed.size() == pending.size()) {
                failed.forEach((k, e) -> LOGGER.error("Cannot parse file [{}]", k, e));
                if (failOnError) {
                    throw new WakamitiException(
                            "Groovy compilation failed for {} file(s); aborting test execution. "
                            + "Set '{}=false' to ignore compilation errors.",
                            failed.size(),
                            GroovyConfigContributor.GROOVY_COMPILATION_FAIL_ON_ERROR
                    );
                }
                break;
            }
            pending = new ArrayList<>(failed.keySet());
        }
        return compiled;
    }

    private Stream<Path> listFiles(
            Path dir
    ) {
        try {
            return Stream.concat(
                    list(dir).filter(Files::isDirectory).flatMap(this::listFiles),
                    list(dir).filter(Files::isRegularFile)
            ).filter(Objects::nonNull);
        } catch (IOException e) {
            return null;
        }
    }

    private Stream<Path> list(
            Path dir
    ) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.toList().stream();
        }
    }

}
