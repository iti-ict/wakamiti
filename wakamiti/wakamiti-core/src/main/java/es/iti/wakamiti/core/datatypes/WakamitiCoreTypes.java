/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.datatypes;


import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.extensions.DataTypeContributor;


@Extension(provider =  "es.iti.wakamiti", name = "core-types", version = "1.1")
public class WakamitiCoreTypes implements DataTypeContributor {

    public static final String PROPERTY_REGEX = "(\\$\\{.+\\})";
    public static final String STRING_REGEX = "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(\\\\.[^'\\\\]*)*)'";
    public static final String WORD_REGEX = "[\\w-]+|[\\w-]*" + PROPERTY_REGEX + "[\\w-]*";
    public static final String IDENTIFIER_REGEX = "[\\w|\\d_]+|[\\w|\\d_]*" + PROPERTY_REGEX + "[\\w|\\d_]*";
    public static final String FILE_REGEX = "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(\\\\.[^'\\\\]*)*)'";
    public static final String URL_REGEX = "(http|ftp|https):\\/\\/(([\\w+?\\.\\w+])+|" + PROPERTY_REGEX + ")" +
            "(([\\w\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)_\\-\\=\\+\\\\\\/\\?\\.\\:\\;\\'\\,]|" + PROPERTY_REGEX + ")*)?|"
            + PROPERTY_REGEX;

    private static final WakamitiDataTypeBase.LocaleHintProvider PATH_HINT = locale -> Arrays
        .asList("<path/file>");
    private static final WakamitiDataTypeBase.LocaleHintProvider TEXT_HINT = locale -> Arrays
        .asList("'<text>'");
    private static final WakamitiDataTypeBase.LocaleHintProvider WORD_HINT = locale -> Arrays
        .asList("<word>");
    private static final WakamitiDataTypeBase.LocaleHintProvider URL_HINT = locale -> Arrays
        .asList("http://...", "https://...", "ftp://...");


    @Override
    public List<WakamitiDataType<?>> contributeTypes() {
        final ArrayList<WakamitiDataType<?>> types = new ArrayList<>();
        // functional datatypes
        types.add(
            WakamitiNumberDataType.createFromNumber("integer", Long.class, false, Number::longValue)
        );
        types.add(
            WakamitiNumberDataType.createFromBigDecimal("decimal", BigDecimal.class, true, x -> x)
        );
        types.add(
            new WakamitiDataTypeBase<>(
                "text", String.class, locale -> STRING_REGEX, TEXT_HINT,
                locale -> WakamitiCoreTypes::prepareString
            )
        );
        types.add(
            new WakamitiDataTypeBase<>(
                "word", String.class, locale -> WORD_REGEX, WORD_HINT, locale -> (input -> input)
            )
        );
        types.add(
            new WakamitiDataTypeBase<>(
                "id", String.class, locale -> IDENTIFIER_REGEX, WORD_HINT,
                locale -> (input -> input)
            )
        );
        types.add(new WakamitiDateDataType<>("date", LocalDate.class, true, false, LocalDate::from));
        types.add(new WakamitiDateDataType<>("time", LocalTime.class, false, true, LocalTime::from));
        types.add(
            new WakamitiDateDataType<>(
                "datetime", LocalDateTime.class, true, true, LocalDateTime::from
            )
        );
        // java datatypes
        types.add(
            new WakamitiDataTypeBase<>(
                "string", String.class, locale -> STRING_REGEX, TEXT_HINT,
                locale -> WakamitiCoreTypes::prepareString
            )
        );
        types.add(
            WakamitiNumberDataType.createFromNumber("byte", Byte.class, false, Number::byteValue)
        );
        types.add(
            WakamitiNumberDataType.createFromNumber("short", Short.class, false, Number::shortValue)
        );
        types.add(
            WakamitiNumberDataType.createFromNumber("int", Integer.class, false, Number::intValue)
        );
        types.add(
            WakamitiNumberDataType.createFromNumber("long", Long.class, false, Number::longValue)
        );
        types.add(
            WakamitiNumberDataType.createFromNumber(
                "biginteger",
                BigInteger.class,
                false,
                x -> BigInteger.valueOf(x.longValue())
            )
        );
        types.add(
            WakamitiNumberDataType
                .createFromBigDecimal("float", Float.class, true, BigDecimal::floatValue)
        );
        types.add(
            WakamitiNumberDataType
                .createFromBigDecimal("double", Double.class, true, BigDecimal::doubleValue)
        );
        types.add(
            WakamitiNumberDataType.createFromBigDecimal("bigdecimal", BigDecimal.class, true, x -> x)
        );
        // file datatype
        types.add(
            new WakamitiDataTypeBase<>(
                "file", File.class, locale -> FILE_REGEX, PATH_HINT,
                locale -> file -> new File(prepareString(file))
            )
        );
        // net datatype
        types.add(
            new WakamitiDataTypeBase<>(
                "url", URL.class, locale -> URL_REGEX, URL_HINT, locale -> URL::new
            )
        );
        return types;
    }


    /* remove leading and tailing " or ' , and replace escaped characters */
    private static String prepareString(String input) {
        return input.substring(1, input.length() - 1).replaceAll("\\\\\"", "\"")
            .replaceAll("\\\\'", "'");
    }

}