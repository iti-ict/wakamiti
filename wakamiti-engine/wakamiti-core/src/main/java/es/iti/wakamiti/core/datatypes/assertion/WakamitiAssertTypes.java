/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.DataTypeContributor;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.core.datatypes.WakamitiDataTypeBase;
import es.iti.wakamiti.core.datatypes.WakamitiDateDataType;
import es.iti.wakamiti.core.datatypes.WakamitiNumberDataType;
import es.iti.wakamiti.core.datatypes.duration.DurationProvider;
import es.iti.wakamiti.core.datatypes.duration.WakamitiDurationDataType;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;


/**
 * A contributor for Wakamiti data types related to assertions.
 * It provides various assertion types for functional and Java
 * data types, such as integer, decimal, and text assertions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
@Extension(provider = "es.iti.wakamiti", name = "assertion-types")
public class WakamitiAssertTypes implements DataTypeContributor {

    /**
     * Creates a binary number assertion data type.
     *
     * @param name            The name of the data type.
     * @param includeDecimals A flag indicating whether to include decimals in the assertion.
     * @param mapper          The mapper function for the assertion.
     * @param <T>             The type of the assertion.
     * @return The created data type.
     */
    @SuppressWarnings("rawtypes")
    public static <T extends Comparable<T>> WakamitiDataTypeBase<Assertion> binaryNumberAssert(
            String name,
            boolean includeDecimals,
            ThrowableFunction<Number, T> mapper
    ) {
        return new WakamitiAssertDataType(
                name,
                "matcher.number",
                BinaryNumberAssertProvider.createFromNumber(
                        locale -> WakamitiNumberDataType.numericRegexPattern(locale, includeDecimals),
                        mapper
                ),
                new UnaryNumberAssertProvider()
        );
    }

    /**
     * Creates a binary BigDecimal assertion data type.
     *
     * @param name            The name of the data type.
     * @param includeDecimals A flag indicating whether to include decimals in the assertion.
     * @param mapper          The mapper function for the assertion.
     * @param <T>             The type of the assertion.
     * @return The created data type.
     */
    @SuppressWarnings("rawtypes")
    public static <T extends Comparable<T>> WakamitiDataTypeBase<Assertion> binaryBigDecimalAssert(
            String name,
            boolean includeDecimals,
            ThrowableFunction<BigDecimal, T> mapper
    ) {
        return new WakamitiAssertDataType(
                name,
                "matcher.number",
                BinaryNumberAssertProvider.createFromBigDecimal(
                        locale -> WakamitiNumberDataType.numericRegexPattern(locale, includeDecimals),
                        mapper
                ),
                new UnaryNumberAssertProvider()
        );
    }

    /**
     * Creates a binary duration assertion data type.
     *
     * @param name            The name of the data type.
     * @param mapper          The mapper function for the assertion.
     * @param <T>             The type of the assertion.
     * @return The created data type.
     */
    @SuppressWarnings("rawtypes")
    public static <T extends Comparable<T>> WakamitiDataTypeBase<Assertion> binaryDurationAssert(
            String name,
            ThrowableFunction<Duration, T> mapper
    ) {
        return new WakamitiAssertDataType(
                name,
                "matcher.number",
                BinaryNumberAssertProvider.createFromDuration(
                        WakamitiDurationDataType::regexPattern,
                        mapper
                ),
                new UnaryNumberAssertProvider()
        );
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Comparable<T> & TemporalAccessor> WakamitiDataTypeBase<Assertion> binaryDateAssert(
            String name,
            Class<? extends T> dateType
    ) {
        return new WakamitiAssertDataType(
                name,
                "matcher.number",
                BinaryNumberAssertProvider.createFromDate(
                        locale -> WakamitiDateDataType.dateTimeRegex(locale, WakamitiDateDataType.temporalProperties(dateType)),
                        dateType
                ),
                new UnaryNumberAssertProvider()
        );
    }

    /**
     * Creates a binary string assertion data type.
     *
     * @param name The name of the data type.
     * @return The created data type.
     */
    @SuppressWarnings("rawtypes")
    public static WakamitiDataTypeBase<Assertion> binaryStringAssert(String name) {
        return new WakamitiAssertDataType(
                name,
                "matcher.string",
                new BinaryStringAssertProvider(),
                new UnaryStringAssertProvider()
        );
    }

    /**
     * Parses the provided assertion providers and returns a locale-specific type parser for assertions.
     *
     * @param assertProviders The assertion providers to parse.
     * @return The locale-specific type parser.
     */
    @SuppressWarnings("rawtypes")
    protected static WakamitiDataTypeBase.LocaleTypeParser<Assertion> parseProvider(
            AbstractAssertProvider[] assertProviders
    ) {
        return locale -> expression -> {
            for (AbstractAssertProvider assertProvider : assertProviders) {
                Optional<Matcher<?>> matcher = assertProvider
                        .matcherFromExpression(locale, expression);
                if (matcher.isPresent()) {
                    return adapt(matcher.get());
                }
            }
            return null;
        };
    }

    /**
     * Adapts a generic Matcher into an Assertion.
     *
     * @param matcher The matcher to adapt.
     * @param <T>     The type of the matcher.
     * @return The adapted Assertion.
     */
    private static <T> Assertion<T> adapt(Matcher<T> matcher) {
        return new MatcherAssertion<>(matcher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WakamitiDataType<?>> contributeTypes() {
        List<WakamitiDataType<?>> types = new ArrayList<>();
        // functional datatypes
        types.add(binaryNumberAssert("integer-assertion", false, Number::intValue));
        types.add(binaryBigDecimalAssert("decimal-assertion", true, x -> x));
        types.add(binaryStringAssert("text-assertion"));
        // java datatypes
        types.add(binaryNumberAssert("short-assertion", false, Number::shortValue));
        types.add(binaryNumberAssert("int-assertion", false, Number::intValue));
        types.add(binaryNumberAssert("long-assertion", false, Number::longValue));
        types.add(binaryBigDecimalAssert("biginteger-assertion", true, BigDecimal::toBigInteger));
        types.add(binaryBigDecimalAssert("float-assertion", true, BigDecimal::floatValue));
        types.add(binaryBigDecimalAssert("double-assertion", true, BigDecimal::doubleValue));
        types.add(binaryBigDecimalAssert("bigdecimal-assertion", true, x -> x));
        types.add(binaryDurationAssert("duration-assertion", x -> x));

        types.add(binaryDateAssert("datetime-assertion", LocalDateTime.class));
        types.add(binaryDateAssert("date-assertion", LocalDate.class));
        types.add(binaryDateAssert("time-assertion", LocalTime.class));

        return types;
    }

    /**
     * A specific data type for Wakamiti assertions. It is parametrized by the type of the assertion.
     */
    @SuppressWarnings("rawtypes")
    private static class WakamitiAssertDataType extends WakamitiDataTypeBase<Assertion> {
        public WakamitiAssertDataType(
                String name,
                String prefix,
                AbstractAssertProvider... matcherProviders
        ) {
            super(
                    name,
                    Assertion.class,
                    locale -> {
                        String[] expressions = Arrays.stream(matcherProviders)
                                .map(provider -> provider.regex(locale))
                                .flatMap(Collection::stream)
                                .toArray(String[]::new);
                        return "(" + String.join("|", expressions) + ")";
                    },
                    locale -> AbstractAssertProvider.getAllExpressions(locale, prefix),
                    parseProvider(matcherProviders)
            );
        }
    }

}