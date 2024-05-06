/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.duration;


import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.core.datatypes.WakamitiCoreTypes;
import es.iti.wakamiti.core.datatypes.WakamitiDataTypeBase;

import java.text.*;
import java.time.Duration;
import java.util.Locale;


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


    public static TypeParser<Duration> parser(
            Locale locale
    ) {
        return expression -> PROVIDER.durationFromExpression(locale, expression).orElse(null);
    }

    public static String regexPattern(Locale locale) {
        String[] expressions = PROVIDER.regex(locale).toArray(new String[0]);
        return "(" + String.join("|", expressions) + ")";
    }}
