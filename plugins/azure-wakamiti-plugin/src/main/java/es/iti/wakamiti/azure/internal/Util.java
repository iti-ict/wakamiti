/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.AzureReporter;
import es.iti.wakamiti.azure.api.model.TestCase;
import es.iti.wakamiti.azure.api.model.TestSuite;
import es.iti.wakamiti.azure.api.model.TestSuiteTree;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;


public abstract class Util {

    private static final Logger LOGGER = WakamitiLogger.forClass(AzureReporter.class);
    private static final Pattern ID_AND_NAME = Pattern.compile("\\[([^]]++)]\\s++(.++)");
    private static final String SEPARATOR = Pattern.quote("\\\\");
    private static final String PROPERTY_NOT_PRESENT_IN_TEST_CASE = "Property {} not present in test case {}";

    private Util() {
        // prevent instantiation
    }

    public static String toZoneId(String datetime, ZoneId zoneId) {
        return toZoneId(LocalDateTime.parse(datetime), zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static LocalDateTime toZoneId(LocalDateTime dateTime, ZoneId zoneId) {
        return dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId).toLocalDateTime();
    }

    public static List<TestSuite> readTree(List<TestSuiteTree> suites) {
        return suites.stream().flatMap(it -> {
            if (isEmpty(it.children())) {
                return Stream.of(it.hasChildren(false));
            } else {
                List<TestSuiteTree> children = it.children().stream()
                        .map(child -> (TestSuiteTree) child.parent(it.hasChildren(true)))
                        .collect(Collectors.toList());
                return readTree(children).stream();
            }
        }).collect(Collectors.toList());
    }

    public static List<TestSuite> filterHasChildren(List<TestSuite> suites) {
        return suites.stream().peek(suite -> {
                    if (Objects.nonNull(suite.parent())) {
                        suite.parent().hasChildren(true);
                    }
                }).collect(Collectors.toList());
    }

    public static Stream<TestSuite> flatten(TestSuite suite) {
        if (suite.parent() == null) {
            return Stream.of(suite);
        } else {
            return Stream.concat(flatten(suite.parent()), Stream.of(suite));
        }
    }

    //TODO: revisar
    public static Set<Path> findFiles(String path) throws IOException {
        var pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + path);
        try (Stream<Path> walker = Files.walk(Path.of("")).filter(pathMatcher::matches)) {
            return walker.collect(Collectors.toSet());
        }
    }

    public static String getPropertyValue(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return node.getProperties().get(property);
    }



    public static String getPropertyValue(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return defaultValue;
        }
        return node.getProperties().get(property);
    }



    public static Pair<String,String> getPropertyIdAndName(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return parseNameAndId(node.getProperties().get(property));
    }



    public static Pair<String,String> getPropertyValueIdAndName(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return new Pair<>(defaultValue,null);
        }
        return parseNameAndId(node.getProperties().get(property));
    }


    public static List<Pair<String,String>> getListPropertyIdAndName(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return Stream.of(node.getProperties().get(property).split(SEPARATOR))
            .map(Util::parseNameAndId)
            .collect(Collectors.toList());
    }


    public static String property(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return node.getProperties().get(property);
    }



    public static String property(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return defaultValue;
        }
        return node.getProperties().get(property);
    }



    public static Pair<String,String> parseNameAndId(String value) {
        String stripped = value.strip();
        Matcher matcher = ID_AND_NAME.matcher(stripped);
        if (matcher.matches()) {
            return new Pair<>(matcher.group(2), matcher.group(1));
        } else {
            return new Pair<>(stripped,null);
        }
    }

}
