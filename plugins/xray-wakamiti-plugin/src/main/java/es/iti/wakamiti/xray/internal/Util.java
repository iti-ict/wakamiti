/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;


import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.xray.XRaySynchronizer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class Util {

    private static final Logger LOGGER = WakamitiLogger.forClass(XRaySynchronizer.class);
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


    public static Pair<String, String> getPropertyIdAndName(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return parseNameAndId(node.getProperties().get(property));
    }


    public static Pair<String, String> getPropertyValueIdAndName(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return new Pair<>(defaultValue, null);
        }
        return parseNameAndId(node.getProperties().get(property));
    }


    public static List<Pair<String, String>> getListPropertyIdAndName(PlanNodeSnapshot node, String property) {
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


    public static Pair<String, String> parseNameAndId(String value) {
        String stripped = value.strip();
        Matcher matcher = ID_AND_NAME.matcher(stripped);
        if (matcher.matches()) {
            return new Pair<>(matcher.group(2), matcher.group(1));
        } else {
            return new Pair<>(stripped, null);
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
