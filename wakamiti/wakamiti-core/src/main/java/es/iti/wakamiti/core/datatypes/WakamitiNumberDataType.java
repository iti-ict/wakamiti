/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.datatypes;


import es.iti.wakamiti.api.util.ThrowableFunction;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;


public class WakamitiNumberDataType<T> extends WakamitiDataTypeBase<T> {

    public static <T> WakamitiNumberDataType<T> createFromNumber(
        String name,
        Class<T> javaType,
        boolean includeDecimals,
        ThrowableFunction<Number, T> converter
    ) {
        return new WakamitiNumberDataType<>(name, javaType, includeDecimals, false, converter);
    }


    public static <T> WakamitiNumberDataType<T> createFromBigDecimal(
        String name,
        Class<T> javaType,
        boolean includeDecimals,
        ThrowableFunction<BigDecimal, T> converter
    ) {
        return new WakamitiNumberDataType<>(
            name, javaType, includeDecimals, true, castConverter(BigDecimal.class::cast, converter)
        );
    }


    protected WakamitiNumberDataType(
                    String name, Class<T> javaType, boolean includeDecimals, boolean useBigDecimal,
                    ThrowableFunction<Number, T> converter
    ) {
        super(
            name, javaType,
            locale -> numericRegexPattern(locale, includeDecimals),
            locale -> Arrays.asList(decimalFormat(locale, useBigDecimal).toLocalizedPattern()),
            locale -> parser(locale, includeDecimals, converter)
        );
    }


    public static <T> ThrowableFunction<Number, T> castConverter(
        ThrowableFunction<Number, BigDecimal> caster,
        ThrowableFunction<BigDecimal, T> converter
    ) {
        return number -> converter.apply(caster.apply(number));
    }


    public static <T> TypeParser<T> parser(
        Locale locale,
        boolean includeDecimals,
        ThrowableFunction<Number, T> converter
    ) {
        final ThrowableFunction<String, Number> parser = source -> decimalFormat(
            locale,
            includeDecimals
        ).parse(source);
        return TypeParser.from(parser.andThen(converter));
    }


    public static DecimalFormat decimalFormat(Locale locale, boolean useBigDecimal) {
        DecimalFormat format;
        if (useBigDecimal) {
            format = (DecimalFormat) NumberFormat.getNumberInstance(locale);
            format.setParseBigDecimal(true);
        } else {
            format = (DecimalFormat) NumberFormat.getIntegerInstance(locale);
        }
        return format;
    }


    public static String numericRegexPattern(Locale locale, boolean includeDecimals) {
        final DecimalFormat format = decimalFormat(locale, includeDecimals);
        final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        final StringBuilder pattern = new StringBuilder("-?");
        pattern
            .append("\\d{1," + format.getGroupingSize() + "}")
            .append("(\\" + symbols.getGroupingSeparator() + "?")
            .append("\\d{1," + format.getGroupingSize() + "})*");
        if (includeDecimals) {
            pattern.append("\\" + symbols.getDecimalSeparator()).append("\\d+?");
        }
        return pattern + "|" + WakamitiCoreTypes.PROPERTY_REGEX;
    }
}