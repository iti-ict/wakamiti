/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.datatypes;


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

import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.extensions.DataTypeContributor;


@Extension(provider = "iti.kukumo", name = "core-types", version = "1.1")
public class KukumoCoreTypes implements DataTypeContributor {

    public static final String PROPERTY_REGEX = "(\\$\\{.+\\})";
    public static final String STRING_REGEX = "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(\\\\.[^'\\\\]*)*)'";
    public static final String WORD_REGEX = "[\\w-]+|[\\w-]*" + PROPERTY_REGEX + "[\\w-]*";
    public static final String IDENTIFIER_REGEX = "[\\w|\\d_]+|[\\w|\\d_]*" + PROPERTY_REGEX + "[\\w|\\d_]*";
    public static final String FILE_REGEX = "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(\\\\.[^'\\\\]*)*)'";
    public static final String URL_REGEX = "(http|ftp|https):\\/\\/(([\\w+?\\.\\w+])+|" + PROPERTY_REGEX + ")" +
            "(([\\w\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)_\\-\\=\\+\\\\\\/\\?\\.\\:\\;\\'\\,]|" + PROPERTY_REGEX + ")*)?|"
            + PROPERTY_REGEX;

    private static final KukumoDataTypeBase.LocaleHintProvider PATH_HINT = locale -> Arrays
        .asList("<path/file>");
    private static final KukumoDataTypeBase.LocaleHintProvider TEXT_HINT = locale -> Arrays
        .asList("'<text>'");
    private static final KukumoDataTypeBase.LocaleHintProvider WORD_HINT = locale -> Arrays
        .asList("<word>");
    private static final KukumoDataTypeBase.LocaleHintProvider URL_HINT = locale -> Arrays
        .asList("http://...", "https://...", "ftp://...");


    @Override
    public List<KukumoDataType<?>> contributeTypes() {
        final ArrayList<KukumoDataType<?>> types = new ArrayList<>();
        // functional datatypes
        types.add(
            KukumoNumberDataType.createFromNumber("integer", Long.class, false, Number::longValue)
        );
        types.add(
            KukumoNumberDataType.createFromBigDecimal("decimal", BigDecimal.class, true, x -> x)
        );
        types.add(
            new KukumoDataTypeBase<>(
                "text", String.class, locale -> STRING_REGEX, TEXT_HINT,
                locale -> KukumoCoreTypes::prepareString
            )
        );
        types.add(
            new KukumoDataTypeBase<>(
                "word", String.class, locale -> WORD_REGEX, WORD_HINT, locale -> (input -> input)
            )
        );
        types.add(
            new KukumoDataTypeBase<>(
                "id", String.class, locale -> IDENTIFIER_REGEX, WORD_HINT,
                locale -> (input -> input)
            )
        );
        types.add(new KukumoDateDataType<>("date", LocalDate.class, true, false, LocalDate::from));
        types.add(new KukumoDateDataType<>("time", LocalTime.class, false, true, LocalTime::from));
        types.add(
            new KukumoDateDataType<>(
                "datetime", LocalDateTime.class, true, true, LocalDateTime::from
            )
        );
        // java datatypes
        types.add(
            new KukumoDataTypeBase<>(
                "string", String.class, locale -> STRING_REGEX, TEXT_HINT,
                locale -> KukumoCoreTypes::prepareString
            )
        );
        types.add(
            KukumoNumberDataType.createFromNumber("byte", Byte.class, false, Number::byteValue)
        );
        types.add(
            KukumoNumberDataType.createFromNumber("short", Short.class, false, Number::shortValue)
        );
        types.add(
            KukumoNumberDataType.createFromNumber("int", Integer.class, false, Number::intValue)
        );
        types.add(
            KukumoNumberDataType.createFromNumber("long", Long.class, false, Number::longValue)
        );
        types.add(
            KukumoNumberDataType.createFromNumber(
                "biginteger",
                BigInteger.class,
                false,
                x -> BigInteger.valueOf(x.longValue())
            )
        );
        types.add(
            KukumoNumberDataType
                .createFromBigDecimal("float", Float.class, true, BigDecimal::floatValue)
        );
        types.add(
            KukumoNumberDataType
                .createFromBigDecimal("double", Double.class, true, BigDecimal::doubleValue)
        );
        types.add(
            KukumoNumberDataType.createFromBigDecimal("bigdecimal", BigDecimal.class, true, x -> x)
        );
        // file datatype
        types.add(
            new KukumoDataTypeBase<>(
                "file", File.class, locale -> FILE_REGEX, PATH_HINT,
                locale -> file -> new File(prepareString(file))
            )
        );
        // net datatype
        types.add(
            new KukumoDataTypeBase<>(
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