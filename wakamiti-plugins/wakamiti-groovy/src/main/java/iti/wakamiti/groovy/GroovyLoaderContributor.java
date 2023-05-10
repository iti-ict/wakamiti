/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.groovy;

import groovy.lang.GroovyClassLoader;
import iti.commons.jext.Extension;
import iti.wakamiti.api.extensions.LoaderContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This {@link LoaderContributor} allows load groovy sources as Java
 * classes.
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@Extension(provider = "iti.wakamiti", name = "groovy-loader")
public class GroovyLoaderContributor implements LoaderContributor {

    public static final Logger LOGGER = LoggerFactory.getLogger("iti.wakamiti.groovy");

    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    @Override
    public Stream<? extends Class<?>> load(List<String> discoveryPaths) {
        List<Path> groovyPaths = discoveryPaths.stream()
                .map(Paths::get)
                .flatMap(this::listFiles)
                .filter(file -> file.toFile().getName().toLowerCase().endsWith(".groovy"))
                .collect(Collectors.toList());

        if (groovyPaths.isEmpty()) {
            LOGGER.debug("No groovy classes to load");
            return Stream.empty();
        } else {
            groovyPaths.forEach(path -> LOGGER.debug("Groovy file [{}] found", path.getFileName()));
        }

        groovyPaths.stream().map(Path::getParent).map(Objects::toString).distinct()
                .forEach(groovyClassLoader::addClasspath);
        Thread.currentThread().setContextClassLoader(groovyClassLoader);

        return groovyPaths.stream().map(this::loadClass)
                .filter(Objects::nonNull);
    }

    private Class<?> loadClass(Path path) {
        try {
            return groovyClassLoader.parseClass(path.toFile());
        } catch (Exception e) {
            LOGGER.error("Cannot parse file [{}]", path, e);
            return null;
        }
    }

    private Stream<Path> listFiles(Path dir) {
        try {
            return Stream.concat(
                    list(dir).filter(Files::isDirectory).flatMap(this::listFiles),
                    list(dir).filter(Files::isRegularFile)
            ).filter(Objects::nonNull);
        } catch (IOException e) {
            return null;
        }
    }

    private Stream<Path> list(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.collect(Collectors.toList()).stream();
        }
    }

}
