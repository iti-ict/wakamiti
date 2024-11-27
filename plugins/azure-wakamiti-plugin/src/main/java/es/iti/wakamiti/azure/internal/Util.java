/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.azure.api.model.TestSuite;
import es.iti.wakamiti.azure.api.model.TestSuiteTree;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;


/**
 * Utility class providing static helper methods for common operations related to test suites,
 * file system paths, and date-time conversions.
 * <p>
 * This class is designed to be non-instantiable and offers functionality such as formatting dates,
 * reading and processing hierarchical test suite structures, and performing file searches.
 * </p>
 */
public abstract class Util {

    private Util() {
        // prevent instantiation
    }

    /**
     * Converts a datetime string to a {@link LocalDateTime} in the specified {@link ZoneId}.
     *
     * @param datetime the datetime string to convert.
     * @param zoneId   the target time zone.
     * @return the formatted datetime string in ISO local date-time format.
     */
    public static String toZoneId(String datetime, ZoneId zoneId) {
        try {
            return ZonedDateTime.parse(datetime).withZoneSameInstant(zoneId).toLocalDateTime()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            return toZoneId(LocalDateTime.parse(datetime), zoneId)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    /**
     * Converts a {@link LocalDateTime} to another {@link ZoneId}.
     *
     * @param dateTime the local date-time to convert.
     * @param zoneId   the target time zone.
     * @return the converted {@link LocalDateTime}.
     */
    public static LocalDateTime toZoneId(LocalDateTime dateTime, ZoneId zoneId) {
        return dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId).toLocalDateTime();
    }

    /**
     * Converts a list of {@link TestSuiteTree} objects into a flat list of {@link TestSuite} objects.
     *
     * @param suites the hierarchical list of test suite trees.
     * @return a flat list of {@link TestSuite} objects.
     */
    public static List<TestSuite> readTree(List<TestSuiteTree> suites) {
        List<TestSuite> result = new LinkedList<>();
        if (!isEmpty(suites)) {
            suites.forEach(it -> {
                TestSuite parent = new TestSuite().id(it.id()).name(it.name()).parent(it.parent())
                        .suiteType(it.suiteType()).hasChildren(!isEmpty(it.children()));
                result.add(parent);
                if (parent.hasChildren()) {
                    result.addAll(readTree(it.children().stream().peek(t -> t.parent(parent))
                            .collect(Collectors.toList())));
                }
            });
        }
        return result;
    }

    /**
     * Updates the {@code hasChildren} property of parent test suites based on their children.
     *
     * @param suites the list of test suites to process.
     * @return the updated list of test suites.
     */
    public static List<TestSuite> filterHasChildren(List<TestSuite> suites) {
        return suites.stream().peek(suite -> {
            if (Objects.nonNull(suite.parent())) {
                suite.parent().hasChildren(true);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Flattens a test suite hierarchy into a stream of test suites, starting from the root.
     *
     * @param suite the test suite to flatten.
     * @return a stream of {@link TestSuite} objects from the hierarchy.
     */
    public static Stream<TestSuite> flatten(TestSuite suite) {
        if (suite.parent() == null) {
            return Stream.of(suite);
        } else {
            return Stream.concat(flatten(suite.parent()), Stream.of(suite));
        }
    }

    /**
     * Converts a {@link Path} to a string with backslashes as the separator.
     *
     * @param path the path to convert.
     * @return the path string with backslashes.
     */
    public static String path(Path path) {
        return path.toString().replace("/", "\\");
    }

    /**
     * Matches if file path matches with glob.
     *
     * @param file the base directory to search from.
     * @param glob the glob pattern to match files against.
     * @return {@code true} if the file matches with glob, {@code false} otherwise.
     */
    public static boolean match(Path file, String glob) {
        return FileSystems.getDefault().getPathMatcher("glob:" + glob).matches(file);
    }

}
