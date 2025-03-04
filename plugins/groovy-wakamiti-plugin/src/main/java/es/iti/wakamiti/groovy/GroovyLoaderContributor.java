/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.util.ThrowableFunction;
import groovy.lang.GroovyClassLoader;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.LoaderContributor;
import groovy.lang.GroovyCodeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This {@link LoaderContributor} allows load groovy sources as Java
 * classes.
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@Extension(provider =  "es.iti.wakamiti", name = "groovy-loader", version = "2.6")
public class GroovyLoaderContributor implements LoaderContributor {

    public static final Logger LOGGER = LoggerFactory.getLogger( "es.iti.wakamiti.groovy");

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


        groovyPaths.stream().map(Path::getParent).map(Path::toUri).distinct()
                .map((ThrowableFunction<URI, URL>) URI::toURL)
                .forEach(groovyClassLoader::addURL);
        Thread.currentThread().setContextClassLoader(groovyClassLoader);
        WakamitiAPI.instance().contributors().setClassLoaders(Thread.currentThread().getContextClassLoader());

        return loadClasses(groovyPaths).stream();
    }

    private List<Class<?>> loadClasses(List<Path> paths) {
        List<Class<?>> compiled = new LinkedList<>();;
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
                break;
            }
            pending = new ArrayList<>(failed.keySet());
        }
        return compiled;
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
