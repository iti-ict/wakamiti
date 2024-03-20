/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.ThrowableFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A base class for implementing Wakamiti data types. It provides
 * functionality for parsing, matching, and retrieving information
 * about the data type.
 *
 * @param <T> The type of the Wakamiti data.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiDataTypeBase<T> implements WakamitiDataType<T> {

    private final String name;
    private final Class<T> javaType;
    private final LocaleRegexProvider regexProvider;
    private final LocaleHintProvider hintProvider;
    private final LocaleTypeParser<T> parserProvider;
    private final Map<Locale, String> regexByLocale = new HashMap<>();
    private final Map<Locale, List<String>> hintsByLocale = new HashMap<>();
    private final Map<Locale, TypeParser<T>> parserByLocale = new HashMap<>();

    /**
     * Creates a new instance of WakamitiDataTypeBase.
     *
     * @param name           The name of the data type.
     * @param javaType       The Java type of the data.
     * @param regexProvider  The provider for regular expressions.
     * @param hintProvider   The provider for hints.
     * @param parserProvider The provider for type parsers.
     */
    public WakamitiDataTypeBase(
            String name,
            Class<T> javaType,
            LocaleRegexProvider regexProvider,
            LocaleHintProvider hintProvider,
            LocaleTypeParser<T> parserProvider
    ) {
        this.name = name;
        this.javaType = javaType;
        this.regexProvider = regexProvider;
        this.hintProvider = hintProvider;
        this.parserProvider = parserProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T parse(Locale locale, String value) {
        try {
            return parserForLocale(locale).parse(value);
        } catch (final Exception e) {
            throw new WakamitiException(
                    "Error parsing type {} using language {}: '{}'\n\tExpected {}",
                    name, locale, value, getHints(locale), e
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public Matcher matcher(Locale locale, CharSequence value) {
        try {
            return Pattern.compile(regexForLocale(locale)).matcher(value);
        } catch (final Exception e) {
            throw new WakamitiException(
                    "Cannot create regex pattern for type {} using language {}", name, locale, e
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRegex(Locale locale) {
        return regexForLocale(locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getHints(Locale locale) {
        return hintsForLocale(locale);
    }

    /**
     * Retrieves or computes the type parser for the given locale.
     *
     * @param locale The locale for which the type parser is retrieved.
     * @return The type parser.
     */
    protected TypeParser<T> parserForLocale(Locale locale) {
        parserByLocale.computeIfAbsent(locale, parserProvider::parser);
        return parserByLocale.get(locale);
    }

    /**
     * Retrieves or computes the regular expression for the given locale.
     *
     * @param locale The locale for which the regular expression is retrieved.
     * @return The regular expression.
     */
    protected String regexForLocale(Locale locale) {
        regexByLocale.computeIfAbsent(locale, regexProvider::regex);
        return regexByLocale.get(locale);
    }

    /**
     * Retrieves or computes the hints for the given locale.
     *
     * @param locale The locale for which hints are retrieved.
     * @return The list of hints.
     */
    protected List<String> hintsForLocale(Locale locale) {
        hintsByLocale.computeIfAbsent(locale, hintProvider::hints);
        return hintsByLocale.get(locale);
    }

    /**
     * A functional interface for parsing values of the Wakamiti data type.
     *
     * @param <T> The type of the parsed value.
     */
    public interface TypeParser<T> {

        /**
         * Creates a {@code TypeParser} from a throwable function.
         *
         * @param function The throwable function for parsing.
         * @param <T>      The type of the parsed value.
         * @return The created {@code TypeParser}.
         */
        static <T> TypeParser<T> from(ThrowableFunction<String, T> function) {
            return function::apply;
        }

        /**
         * Parses the given value and returns the result.
         *
         * @param value The value to parse.
         * @return The parsed result.
         * @throws Exception If an error occurs during parsing.
         */
        T parse(String value) throws Exception;
    }

    /**
     * A functional interface for providing locale-specific parsers for a specific data type.
     *
     * @param <T> The type of data that the parser produces.
     */
    public interface LocaleTypeParser<T> {

        TypeParser<T> parser(Locale locale);
    }

    /**
     * A functional interface for providing regular expressions based on the locale.
     */
    public interface LocaleRegexProvider {

        /**
         * Provides the regular expression for the data type based on the given locale.
         *
         * @param locale The locale for which the regular expression is provided.
         * @return The regular expression.
         */
        String regex(Locale locale);
    }

    /**
     * A functional interface for providing hints based on the locale.
     */
    public interface LocaleHintProvider {

        /**
         * Provides hints for the data type based on the given locale.
         *
         * @param locale The locale for which hints are provided.
         * @return The list of hints.
         */
        List<String> hints(Locale locale);
    }

}