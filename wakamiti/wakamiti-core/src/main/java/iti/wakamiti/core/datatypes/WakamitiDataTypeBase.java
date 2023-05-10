/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.core.datatypes;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import iti.wakamiti.api.WakamitiDataType;
import iti.wakamiti.api.WakamitiException;
import iti.wakamiti.api.util.ThrowableFunction;


public class WakamitiDataTypeBase<T> implements WakamitiDataType<T> {

    public interface TypeParser<T> {

        T parse(String value) throws Exception;


        static <T> TypeParser<T> from(ThrowableFunction<String, T> function) {
            return function::apply;
        }
    }


    public interface LocaleTypeParser<T> {

        TypeParser<T> parser(Locale locale);
    }


    public interface LocaleRegexProvider {

        String regex(Locale locale);
    }


    public interface LocaleHintProvider {

        List<String> hints(Locale locale);
    }


    private final String name;
    private final Class<T> javaType;
    private final LocaleRegexProvider regexProvider;
    private final LocaleHintProvider hintProvider;
    private final LocaleTypeParser<T> parserProvider;
    private final Map<Locale, String> regexByLocale = new HashMap<>();
    private final Map<Locale, List<String>> hintsByLocale = new HashMap<>();
    private final Map<Locale, TypeParser<T>> parserByLocale = new HashMap<>();


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


    @Override
    public String getName() {
        return name;
    }


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


    @Override
    public String getRegex(Locale locale) {
        return regexForLocale(locale);
    }


    @Override
    public List<String> getHints(Locale locale) {
        return hintsForLocale(locale);
    }


    protected TypeParser<T> parserForLocale(Locale locale) {
        parserByLocale.computeIfAbsent(locale, parserProvider::parser);
        return parserByLocale.get(locale);
    }


    protected String regexForLocale(Locale locale) {
        regexByLocale.computeIfAbsent(locale, regexProvider::regex);
        return regexByLocale.get(locale);
    }


    protected List<String> hintsForLocale(Locale locale) {
        hintsByLocale.computeIfAbsent(locale, hintProvider::hints);
        return hintsByLocale.get(locale);
    }

}