/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.duration;


import java.time.Duration;
import java.util.Locale;

import es.iti.wakamiti.core.datatypes.WakamitiDataTypeBase;


/**
 * Provides the Wakamiti Duration Data Type functionality used by Wakamiti.
 */
public class WakamitiDurationDataType extends WakamitiDataTypeBase<Duration> {

    private static final DurationProvider PROVIDER = new DurationProvider();

    /**
     * Creates a new instance of WakamitiDataTypeBase.
     */
    public WakamitiDurationDataType() {
        super("duration",
                Duration.class,
                WakamitiDurationDataType::regexPattern,
                DurationProvider::getAllExpressions,
                WakamitiDurationDataType::parser);
    }

    /**
     * Creates a parser for localized, human-readable duration expressions.
     *
     * @param locale locale controlling recognized units and number formatting
     * @return a parser that yields {@code null} for unsupported expressions
     */
    public static TypeParser<Duration> parser(
            Locale locale
    ) {
        return expression -> PROVIDER.durationFromExpression(locale, expression).orElse(null);
    }

    /**
     * Builds the complete regular expression accepted by the duration parser
     * for a locale.
     *
     * @param locale locale controlling translated duration expressions
     * @return a grouped alternation of every supported expression
     */
    public static String regexPattern(
            Locale locale
    ) {
        String[] expressions = PROVIDER.regex(locale).toArray(new String[0]);
        return "(" + String.join("|", expressions) + ")";
    }

}
