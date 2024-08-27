/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.plan.PlanNode;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;


/**
 * Utility class for working with paths and replacing placeholders.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class PathUtil {

    private static final DateTimeFormatter YEAR_4 = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter YEAR_2 = DateTimeFormatter.ofPattern("yy", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd", Locale.ENGLISH);
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH", Locale.ENGLISH);
    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("mm", Locale.ENGLISH);
    private static final DateTimeFormatter SEC = DateTimeFormatter.ofPattern("ss", Locale.ENGLISH);
    private static final DateTimeFormatter MILLIS = DateTimeFormatter.ofPattern("SSS", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmssSSS", Locale.ENGLISH);

    private PathUtil() {

    }

    /**
     * Replaces placeholders in the provided path based on the given PlanNode.
     *
     * @param path     The original path with placeholders.
     * @param planNode The PlanNode containing information for placeholder replacement.
     * @return The path with replaced placeholders.
     */
    public static Path replacePlaceholders(Path path, PlanNode planNode) {
        var instant = planNode.startInstant().orElseGet(Instant::now).atZone(ZoneId.systemDefault());
        var executionID = Objects.requireNonNullElse(planNode.executionID(), "");
        String pathString = replaceTemporalPlaceholders(path.toString(), instant);
        pathString = pathString.replace("%execID%", executionID);
        return Path.of(pathString);
    }

    /**
     * Replaces temporal placeholders in the provided path with the current timestamp.
     *
     * @param path The original path with temporal placeholders.
     * @return The path with replaced temporal placeholders.
     */
    public static Path replaceTemporalPlaceholders(Path path) {
        return replaceTemporalPlaceholders(path, Instant.now());
    }

    public static Path replaceTemporalPlaceholders(Path path, Instant instant) {
        return Path.of(replaceTemporalPlaceholders(path.toString(), instant.atZone(ZoneId.systemDefault())));
    }

    private static String replaceTemporalPlaceholders(String pathString, ZonedDateTime instant) {
        pathString = pathString.replace("%YYYY%", YEAR_4.format(instant));
        pathString = pathString.replace("%YY%", YEAR_2.format(instant));
        pathString = pathString.replace("%MM%", MONTH.format(instant));
        pathString = pathString.replace("%DD%", DAY.format(instant));
        pathString = pathString.replace("%hh%", HOUR.format(instant));
        pathString = pathString.replace("%mm%", MINUTE.format(instant));
        pathString = pathString.replace("%ss%", SEC.format(instant));
        pathString = pathString.replace("%sss%", MILLIS.format(instant));
        pathString = pathString.replace("%DATE%", DATE.format(instant));
        pathString = pathString.replace("%TIME%", TIME.format(instant));
        return pathString;
    }
}
