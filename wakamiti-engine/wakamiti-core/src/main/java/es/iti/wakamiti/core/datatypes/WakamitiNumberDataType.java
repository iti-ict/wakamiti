/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes;


import es.iti.wakamiti.api.util.ThrowableFunction;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;


/**
 * Class representing a data type for numeric values in Wakamiti.
 *
 * @param <T> Type of numeric data.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiNumberDataType<T> extends WakamitiDataTypeBase<T> {

    /**
     * Protected constructor for creating a WakamitiNumberDataType.
     *
     * @param name            Name of the data type.
     * @param javaType        Associated Java data type.
     * @param includeDecimals Indicates whether the data type should include decimal values.
     * @param useBigDecimal   Indicates whether to use BigDecimal for conversion.
     * @param converter       Converter function for converting from Number to the specified type.
     */
    protected WakamitiNumberDataType(
            String name, Class<T> javaType, boolean includeDecimals, boolean useBigDecimal,
            ThrowableFunction<Number, T> converter
    ) {
        super(
                name, javaType,
                locale -> numericRegexPattern(locale, includeDecimals),
                locale -> Collections.singletonList(decimalFormat(locale, useBigDecimal).toLocalizedPattern()),
                locale -> parser(locale, includeDecimals, converter)
        );
    }

    /**
     * Creates a WakamitiNumberDataType from a Number.
     *
     * @param name            Name of the data type.
     * @param javaType        Associated Java data type.
     * @param includeDecimals Indicates whether the data type should include decimal values.
     * @param converter       Converter function for converting from Number to the specified type.
     * @param <T>             Type of numeric data.
     * @return WakamitiNumberDataType instance.
     */
    public static <T> WakamitiNumberDataType<T> createFromNumber(
            String name,
            Class<T> javaType,
            boolean includeDecimals,
            ThrowableFunction<Number, T> converter
    ) {
        return new WakamitiNumberDataType<>(name, javaType, includeDecimals, false, converter);
    }

    /**
     * Creates a WakamitiNumberDataType from a BigDecimal.
     *
     * @param name            Name of the data type.
     * @param javaType        Associated Java data type.
     * @param includeDecimals Indicates whether the data type should include decimal values.
     * @param converter       Converter function for converting from BigDecimal to the specified type.
     * @param <T>             Type of numeric data.
     * @return WakamitiNumberDataType instance.
     */
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

    /**
     * Creates a converter for casting Number to the specified type.
     *
     * @param caster    Converter function for casting to BigDecimal.
     * @param converter Converter function for converting from BigDecimal to the specified type.
     * @param <T>       Type of numeric data.
     * @return Converter function for casting Number to the specified type.
     */
    public static <T> ThrowableFunction<Number, T> castConverter(
            ThrowableFunction<Number, BigDecimal> caster,
            ThrowableFunction<BigDecimal, T> converter
    ) {
        return number -> converter.apply(caster.apply(number));
    }

    /**
     * Creates a parser for parsing numeric values.
     *
     * @param locale          The locale for parsing.
     * @param includeDecimals Indicates whether the data type should include decimal values.
     * @param converter       Converter function for converting from Number to the specified type.
     * @return TypeParser instance for parsing numeric values.
     */
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

    /**
     * Creates a DecimalFormat instance for formatting numeric values.
     *
     * @param locale        The locale for formatting.
     * @param useBigDecimal Indicates whether to use BigDecimal for conversion.
     * @return DecimalFormat instance for formatting numeric values.
     */
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

    /**
     * Generates a numeric regex pattern for validation.
     *
     * @param locale          The locale for which to generate the pattern.
     * @param includeDecimals Indicates whether the data type should include decimal values.
     * @return Numeric regex pattern.
     */
    public static String numericRegexPattern(Locale locale, boolean includeDecimals) {
        final DecimalFormat format = decimalFormat(locale, includeDecimals);
        final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        final StringBuilder pattern = new StringBuilder("-?");
        pattern.append("\\d{1,").append(format.getGroupingSize()).append("}")
                .append("(\\").append(symbols.getGroupingSeparator()).append("?")
                .append("\\d{1,").append(format.getGroupingSize()).append("})*");
        if (includeDecimals) {
            pattern.append("\\").append(symbols.getDecimalSeparator()).append("\\d+?");
        }
        return "("+pattern + "|" + WakamitiCoreTypes.PROPERTY_REGEX+")";
    }

}