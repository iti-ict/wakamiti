/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.core.util.TokenParser;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.util.MapUtils.map;
import static es.iti.wakamiti.core.datatypes.WakamitiCoreTypes.PROPERTY_REGEX;


/**
 * Class representing a data type for dates and times in Wakamiti.
 * Allows the definition of specific formats, regular expressions, and parsers
 * for handling temporal data.
 *
 * <p>This class extends {@link WakamitiDataTypeBase} and provides
 * functionality specific to the handling of temporal data types.</p>
 *
 * @param <T> Type of temporal data implementing the {@link TemporalAccessor} interface.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiDateDataType<T extends TemporalAccessor> extends WakamitiDataTypeBase<T> {

    private static final List<FormatStyle> FORMAT_STYLES = List.of(
            FormatStyle.SHORT, FormatStyle.MEDIUM, FormatStyle.LONG, FormatStyle.FULL);

    private static final String[] ISO_8601_DATE_FORMATS = {
            "yyyy-MM-dd",
            PROPERTY_REGEX
    };

    private static final String[] ISO_8601_TIME_FORMATS = {
            "hh:mm",
            "hh:mm:ss",
            "hh:mm:ss.SSS",
            PROPERTY_REGEX
    };

    private static final String[] ISO_8601_DATETIME_FORMATS = {
            "yyyy-MM-dd'T'hh:mm",
            "yyyy-MM-dd'T'hh:mm:ss",
            "yyyy-MM-dd'T'hh:mm:ss.SSS",
            PROPERTY_REGEX + "'T'" + PROPERTY_REGEX,
            PROPERTY_REGEX
    };

    private static final String REGEX_ALPHAS = "[^\\s]+";
    private static final String REGEX_2_NUMBER = "[0-9]{2}";
    private static final String REGEX_3_NUMBER = "[0-9]{3}";
    private static final String REGEX_4_NUMBER = "[0-9]{4}";
    private static final String REGEX_1_2_NUMBER = "[0-9]{1,2}";
    private static final String REGEX_1_3_NUMBER = "[0-9]{1,3}";
    private static final String REGEX_2_3_NUMBER = "[0-9]{2,3}";
    private static final String REGEX_2_4_NUMBER = "[0-9]{2,4}";

    private static final Map<String, String> REGEX_SYMBOLS = Stream.of(new String[][]{
                    {"MMMM", REGEX_ALPHAS},
                    {"EEEE", REGEX_ALPHAS},
                    {"yyyy", REGEX_4_NUMBER},
                    {"MMM", REGEX_ALPHAS},
                    {"EEE", REGEX_ALPHAS},
                    {"SSS", REGEX_3_NUMBER},
                    {"MM", REGEX_2_NUMBER},
                    {"EE", REGEX_ALPHAS},
                    {"HH", REGEX_2_NUMBER},
                    {"hh", REGEX_2_NUMBER},
                    {"dd", REGEX_2_NUMBER},
                    {"mm", REGEX_2_NUMBER},
                    {"SS", REGEX_2_3_NUMBER},
                    {"yy", REGEX_2_NUMBER},
                    {"ss", REGEX_2_NUMBER},
                    {"M", REGEX_1_2_NUMBER},
                    {"d", REGEX_1_2_NUMBER},
                    {"E", REGEX_ALPHAS},
                    {"H", REGEX_1_2_NUMBER},
                    {"h", REGEX_1_2_NUMBER},
                    {"m", REGEX_1_2_NUMBER},
                    {"s", REGEX_1_2_NUMBER},
                    {"S", REGEX_1_3_NUMBER},
                    {"a", REGEX_ALPHAS},
                    {"z", REGEX_ALPHAS},
                    {"y", REGEX_2_4_NUMBER}
            })
            .collect(Collectors.toMap(p -> p[0], p -> p[1], (p1, p2) -> p1, LinkedHashMap::new));

    private static final List<String> REGEX_SPECIAL_SYMBOLS = Arrays.asList(
            "\\",
            "[",
            "^",
            "$",
            ".",
            ",",
            "|",
            "?",
            "*",
            "+",
            "(",
            ")",
            "/",
            "-",
            ":"
    );

    /**
     * Constructor for creating a data type for dates and times in Wakamiti.
     *
     * @param name          Name of the data type.
     * @param javaType      Associated Java data type.
     */
    public WakamitiDateDataType(
            String name, Class<T> javaType
    ) {
        super(
                name, javaType,
                locale -> dateTimeRegex(locale, temporalProperties(javaType)),
                locale -> dateTimePatterns(locale, temporalProperties(javaType)),
                locale -> dateTimeParser(locale, temporalProperties(javaType), temporalQuery(javaType))
        );
    }

    /**
     * Temporal query used for parsing the input string.
     *
     * @param javaType Associated Java data type.
     * @return Temporal query used for parsing the input string.
     * @param <T> The TemporalAccessor
     */
    @SuppressWarnings("unchecked")
    public static <T extends TemporalAccessor> TemporalQuery<T> temporalQuery(Class<T> javaType) {
        return (TemporalQuery<T>) map(
                LocalDateTime.class, (TemporalQuery<LocalDateTime>) LocalDateTime::from,
                LocalDate.class, (TemporalQuery<LocalDate>) LocalDate::from,
                LocalTime.class, (TemporalQuery<LocalTime>) LocalTime::from
        ).get(javaType);
    }

    /**
     * Indicates whether the data type should include date and/or time information.
     *
     * @param javaType Associated Java data type.
     * @return Whether the data type should include date and/or time information.
     * @param <T> The TemporalAccessor
     */
    public static <T extends TemporalAccessor> TemporalProperties temporalProperties(Class<T> javaType) {
        return map(
                LocalDateTime.class, new TemporalProperties(){},
                LocalDate.class, new TemporalProperties() {
                    public boolean withTime() {
                        return false;
                    }
                },
                LocalTime.class, new TemporalProperties() {
                    public boolean withDate() {
                        return false;
                    }
                }
        ).get(javaType);
    }

    /**
     * Constructs a regular expression pattern for date and time based on the specified locale,
     * with optional inclusion of date and/or time components.
     *
     * @param locale   The locale for which the date and time formats should be considered.
     * @param properties Indicates whether the data type should include date and/or time information.
     * @return A regular expression pattern for date and time.
     */
    public static String dateTimeRegex(Locale locale, TemporalProperties properties) {
        final Set<String> regex;
        if (properties.withDate() && properties.withTime()) {
            regex = regexWithDateAndTime(locale);
        } else {
            regex = regexWithDateOrTime(locale, properties.withDate(), properties.withTime());
        }
        regex.addAll(dateTimeRegexISO(properties));
        return "(" + String.join(")|(", regex) + ")";
    }

    /**
     * Generates a set of regular expression patterns for date and time, considering the specified locale,
     * with optional inclusion of date and/or time components.
     *
     * @param locale   The locale for which the date and time formats should be considered.
     * @param withDate Indicates whether the date component should be included in the patterns.
     * @param withTime Indicates whether the time component should be included in the patterns.
     * @return A set of regular expression patterns for date and time.
     */
    private static Set<String> regexWithDateOrTime(Locale locale, boolean withDate, boolean withTime) {
        Set<String> regex = new HashSet<>();
        for (final FormatStyle formatStyle : FORMAT_STYLES) {
            regex.add(
                    dateTimeRegex(
                            locale,
                            withDate ? formatStyle : null,
                            withTime ? formatStyle : null
                    )
            );
        }
        return regex;
    }

    /**
     * Generates a set of regular expression patterns for date and time, considering the specified locale,
     * with the inclusion of both date and time components.
     *
     * @param locale The locale for which the date and time formats should be considered.
     * @return A set of regular expression patterns for date and time, including both date and time components.
     */
    private static Set<String> regexWithDateAndTime(Locale locale) {
        Set<String> regex = new HashSet<>();
        for (final FormatStyle dateFormatStyle : FORMAT_STYLES) {
            for (final FormatStyle timeFormatStyle : FORMAT_STYLES) {
                regex.add(dateTimeRegex(locale, dateFormatStyle, timeFormatStyle));
            }
        }
        return regex;
    }

    /**
     * Generates a list of regular expression patterns for ISO 8601 formatted date and time strings,
     * considering the specified inclusion of date and time components.
     *
     * @param properties Indicates whether the data type should include date and/or time information.
     * @return A list of regular expression patterns for ISO 8601 formatted date and time strings.
     */
    private static List<String> dateTimeRegexISO(TemporalProperties properties) {
        String[] formats;
        if (properties.withDate() && properties.withTime()) {
            formats = ISO_8601_DATETIME_FORMATS;
        } else if (properties.withDate()) {
            formats = ISO_8601_DATE_FORMATS;
        } else {
            formats = ISO_8601_TIME_FORMATS;
        }
        return Stream.of(formats).map(WakamitiDateDataType::patternToRegex)
                .collect(Collectors.toList());
    }

    /**
     * Generates a regular expression pattern from the provided date/time format pattern.
     *
     * @param formatPattern The input date/time format pattern to convert into a regular expression.
     * @return The regular expression pattern derived from the input format pattern.
     * @throws WakamitiException If a date/time format symbol in the input has no equivalent regex representation.
     */
    private static String patternToRegex(String formatPattern) {
        String whitespaces = "[\\s\\h\\v]";
        List<String> tokens = new ArrayList<>(REGEX_SYMBOLS.keySet());
        tokens.addAll(REGEX_SPECIAL_SYMBOLS);
        TokenParser parser = new TokenParser(formatPattern, tokens, List.of("'[^']*'", whitespaces));
        StringBuilder regex = new StringBuilder();
        while (parser.hasMoreTokens()) {
            String nextToken = parser.nextToken();
            if (nextToken.matches(whitespaces)) {
                regex.append(" ");
            } else if (REGEX_SPECIAL_SYMBOLS.contains(nextToken)) {
                regex.append("\\").append(nextToken);
            } else if (nextToken.startsWith("'")) {
                regex.append(nextToken.replace("'", ""));
            } else {
                String regexSymbol = REGEX_SYMBOLS.get(nextToken);
                if (regexSymbol == null) {
                    throw new WakamitiException("Date/time format symbol '" + nextToken + "' has no equivalent regex");
                }
                regex.append(REGEX_SYMBOLS.get(nextToken));
            }
        }
        return "(" + regex + "|" + PROPERTY_REGEX + ")";
    }

    /**
     * Generates a composite regular expression pattern for date and time based on the specified
     * date and time format styles. It delegates the generation to the {@link WakamitiDateDataType#patternToRegex}
     * method by providing the result of {@link WakamitiDateDataType#dateTimePattern}.
     *
     * @param locale          The locale for localization.
     * @param dateFormatStyle The format style for date.
     * @param timeFormatStyle The format style for time.
     * @return The composite regular expression pattern for date and time.
     * @see WakamitiDateDataType#dateTimePattern(Locale, FormatStyle, FormatStyle)
     * @see WakamitiDateDataType#patternToRegex(String)
     */
    private static String dateTimeRegex(
            Locale locale,
            FormatStyle dateFormatStyle,
            FormatStyle timeFormatStyle
    ) {
        return patternToRegex(dateTimePattern(locale, dateFormatStyle, timeFormatStyle));
    }

    /**
     * Retrieves the localized date and time pattern using the {@link DateTimeFormatterBuilder}
     * with the specified date and time format styles. The pattern is obtained based on the provided
     * locale and ISO chronology.
     *
     * @param locale          The locale for localization.
     * @param dateFormatStyle The format style for date.
     * @param timeFormatStyle The format style for time.
     * @return The localized date and time pattern.
     */
    private static String dateTimePattern(
            Locale locale,
            FormatStyle dateFormatStyle,
            FormatStyle timeFormatStyle
    ) {
        return DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                dateFormatStyle,
                timeFormatStyle,
                IsoChronology.INSTANCE,
                locale
        );
    }

    /**
     * Retrieves a list of date and time patterns based on the specified locale, indicating
     * whether the pattern should include date and/or time information.
     *
     * @param locale   The locale for localization.
     * @param properties Indicates whether the data type should include date and/or time information.
     * @return A list of date and time patterns.
     */
    public static List<String> dateTimePatterns(Locale locale, TemporalProperties properties) {
        if (properties.withDate() && properties.withTime()) {
            return patternsWithDateAndTime(locale);
        } else {
            return patternsWithDateOrTime(locale, properties.withDate(), properties.withTime());
        }
    }

    /**
     * Retrieves a list of date and/or time patterns based on the specified locale,
     * indicating whether the pattern should include date and/or time information.
     *
     * @param locale   The locale for localization.
     * @param withDate Indicates whether the pattern should include date information.
     * @param withTime Indicates whether the pattern should include time information.
     * @return A list of date and/or time patterns.
     */
    private static List<String> patternsWithDateOrTime(Locale locale, boolean withDate, boolean withTime) {
        List<String> patterns = new ArrayList<>(Arrays.asList(withDate ? ISO_8601_DATE_FORMATS : ISO_8601_TIME_FORMATS));
        for (final FormatStyle formatStyle : FORMAT_STYLES) {
            patterns.add(
                    dateTimePattern(
                            locale,
                            withDate ? formatStyle : null,
                            withTime ? formatStyle : null
                    )
            );
        }
        return patterns;
    }

    /**
     * Retrieves a list of date and time patterns based on the specified locale.
     *
     * @param locale The locale for localization.
     * @return A list of date and time patterns.
     */
    private static List<String> patternsWithDateAndTime(Locale locale) {
        List<String> patterns = new ArrayList<>(Arrays.asList(ISO_8601_DATETIME_FORMATS));
        for (final FormatStyle dateFormatStyle : FORMAT_STYLES) {
            for (final FormatStyle timeFormatStyle : FORMAT_STYLES) {
                patterns.add(dateTimePattern(locale, dateFormatStyle, timeFormatStyle));
            }
        }
        return patterns;
    }

    /**
     * Creates a date and time parser based on the specified parameters.
     *
     * @param <T>           Type of temporal data implementing the {@link TemporalAccessor} interface.
     * @param locale        The locale for localization.
     * @param properties Indicates whether the data type should include date and/or time information.
     * @param temporalQuery Temporal query used for parsing the input string.
     * @return A {@link TypeParser} instance for parsing date and time.
     */
    public static <T extends TemporalAccessor> TypeParser<T> dateTimeParser(
            Locale locale,
            TemporalProperties properties,
            TemporalQuery<T> temporalQuery
    ) {

        List<DateTimeFormatter> formatters;
        if (properties.withDate() && properties.withTime()) {
            formatters = formattersWithDateAndTime(locale);
        } else {
            formatters = formattersWithDateOrTime(locale, properties.withDate(), properties.withTime());
        }
        return (String input) -> parse(formatters, input, temporalQuery);
    }

    /**
     * Creates a list of date and time formatters with or without a date
     * and time components based on the specified parameters.
     *
     * @param locale   The locale for localization.
     * @param withDate Indicates whether the data type should include date information.
     * @param withTime Indicates whether the data type should include time information.
     * @return A list of {@link DateTimeFormatter} instances.
     */
    private static List<DateTimeFormatter> formattersWithDateOrTime(Locale locale, boolean withDate, boolean withTime) {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        for (final FormatStyle formatStyle : FORMAT_STYLES) {
            DateTimeFormatter formatter = formatter(
                    locale,
                    withDate ? formatStyle : null,
                    withTime ? formatStyle : null
            );
            formatters.add(formatter);
        }
        formatters.add(withDate ? DateTimeFormatter.ISO_DATE : DateTimeFormatter.ISO_TIME);
        return formatters;
    }

    /**
     * Creates a list of date and time formatters with both date and time
     * components, based on the specified locale.
     *
     * @param locale The locale for localization.
     * @return A list of {@link DateTimeFormatter} instances.
     */
    private static List<DateTimeFormatter> formattersWithDateAndTime(Locale locale) {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        for (final FormatStyle dateFormatStyle : FORMAT_STYLES) {
            for (final FormatStyle timeFormatStyle : FORMAT_STYLES) {
                DateTimeFormatter formatter = formatter(
                        locale,
                        dateFormatStyle,
                        timeFormatStyle
                );
                formatters.add(formatter);
            }
        }
        formatters.add(DateTimeFormatter.ISO_DATE_TIME);
        return formatters;
    }

    /**
     * Creates a DateTimeFormatter based on the specified locale, date
     * format style, and time format style.
     *
     * @param locale          The locale for localization.
     * @param dateFormatStyle The format style for the date component.
     * @param timeFormatStyle The format style for the time component.
     * @return A DateTimeFormatter instance.
     */
    private static DateTimeFormatter formatter(
            Locale locale,
            FormatStyle dateFormatStyle,
            FormatStyle timeFormatStyle
    ) {
        return new DateTimeFormatterBuilder().parseCaseInsensitive().append(
                DateTimeFormatter.ofPattern(dateTimePattern(locale, dateFormatStyle, timeFormatStyle))
        ).toFormatter(locale);
    }

    /**
     * Parses the input string using a list of DateTimeFormatters and returns the parsed TemporalAccessor.
     *
     * @param formatters    List of DateTimeFormatters to use for parsing.
     * @param input         The input string to parse.
     * @param temporalQuery TemporalQuery used for parsing.
     * @param <T>           Type of TemporalAccessor.
     * @return The parsed TemporalAccessor.
     * @throws RuntimeException If parsing fails to use all provided formatters.
     */
    private static <T extends TemporalAccessor> T parse(
            List<DateTimeFormatter> formatters,
            String input,
            TemporalQuery<T> temporalQuery
    ) {
        RuntimeException ex = new RuntimeException();
        for (DateTimeFormatter formatter : formatters) {
            try {
                return formatter.parse(input, temporalQuery);
            } catch (DateTimeParseException e) {
                ex = e;
            }
        }
        throw ex;
    }

    /**
     * Gets a list of date and time patterns for the specified locale.
     *
     * @param locale The locale for which to retrieve date and time patterns.
     * @return List of date and time patterns.
     */
    public List<String> getDateTimeFormats(Locale locale) {
        return dateTimePatterns(locale, temporalProperties(getJavaType()));
    }

    public interface TemporalProperties {
        default boolean withDate() {
            return true;
        }
        default boolean withTime() {
            return true;
        }
    }
}