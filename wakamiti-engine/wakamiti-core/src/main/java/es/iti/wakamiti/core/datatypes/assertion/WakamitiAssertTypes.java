/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.datatypes.assertion;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import es.iti.wakamiti.core.datatypes.WakamitiDataTypeBase;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.ThrowableFunction;
import org.hamcrest.Matcher;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.DataTypeContributor;
import es.iti.wakamiti.core.datatypes.WakamitiNumberDataType;



@Extension(provider =  "es.iti.wakamiti", name = "assertion-types", version = "1.1")
public class WakamitiAssertTypes implements DataTypeContributor {

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
        return types;
    }


    public static <T extends Comparable<T>> WakamitiAssertDataType binaryNumberAssert(
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


    public static <T extends Comparable<T>> WakamitiAssertDataType binaryBigDecimalAssert(
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


    public static WakamitiAssertDataType binaryStringAssert(String name) {
        return new WakamitiAssertDataType(
            name,
            "matcher.string",
            new BinaryStringAssertProvider(),
            new UnaryStringAssertProvider()
        );
    }


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
                locale -> ".*",
                locale -> AbstractAssertProvider.getAllExpressions(locale,prefix),
                parseProvider(matcherProviders)
            );
        }
    }


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



    private static <T> Assertion<T> adapt(Matcher<T> matcher) {
        return new MatcherAssertion<>(matcher);
    }

}