/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.plan.PlanNode;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Builds JUnit Platform {@link TestSource} instances from the
 * {@link PlanNode#source()} value so that IDEs can navigate
 * ("jump to source") to the actual feature file and line.
 *
 * <p>The plan stores the feature location as a path relative to the configured
 * resource roots, so the relative path is resolved against those roots (and the
 * working directory) in order to produce a {@link FileSource} pointing to an
 * existing file.</p>
 */
final class WakamitiTestSources {

    private static final Pattern POSITION = Pattern.compile("\\[(\\d+),(\\d+)]$");
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";

    private WakamitiTestSources() {
        // static utility
    }

    static Optional<TestSource> from(PlanNode node, List<String> resourceRoots) {
        String rawSource = node.source();
        if (rawSource == null || rawSource.isBlank()) {
            return Optional.empty();
        }

        FilePosition position = parsePosition(rawSource);
        String location = POSITION.matcher(rawSource).replaceFirst("");

        if (location.startsWith(CLASSPATH_PREFIX)) {
            String resource = "/" + location.substring(CLASSPATH_PREFIX.length()).replaceFirst("^/+", "");
            return Optional.of(position == null
                    ? ClasspathResourceSource.from(resource)
                    : ClasspathResourceSource.from(resource, position));
        }

        if (location.startsWith(FILE_PREFIX)) {
            location = location.substring(FILE_PREFIX.length()).replaceFirst("^/+(?=[A-Za-z]:)", "");
        } else if (location.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
            // Non-file URI (e.g. http): no navigable file source available.
            return Optional.empty();
        }

        File file = resolveFile(location, resourceRoots);
        return Optional.of(position == null
                ? FileSource.from(file)
                : FileSource.from(file, position));
    }

    private static File resolveFile(String location, List<String> resourceRoots) {
        Path candidate = Path.of(location);
        if (candidate.isAbsolute() && Files.exists(candidate)) {
            return candidate.toFile();
        }
        if (resourceRoots != null) {
            for (String root : resourceRoots) {
                if (root == null || root.isBlank()) {
                    continue;
                }
                Path resolved = Path.of(root).resolve(location).normalize();
                if (Files.exists(resolved)) {
                    return resolved.toAbsolutePath().toFile();
                }
            }
        }
        if (Files.exists(candidate)) {
            return candidate.toAbsolutePath().toFile();
        }
        return candidate.toFile();
    }

    private static FilePosition parsePosition(String source) {
        Matcher matcher = POSITION.matcher(source);
        if (matcher.find()) {
            return FilePosition.from(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
            );
        }
        return null;
    }

}
