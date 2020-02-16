/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.datatypes.assertion;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matcher;

import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.core.datatypes.KukumoDataTypeBase;
import iti.kukumo.core.datatypes.KukumoNumberDataType;
import iti.kukumo.util.ThrowableFunction;


@Extension(provider = "iti.kukumo", name = "assertion-types", version = "1.0")
public class KukumoAssertTypes implements DataTypeContributor {

    @Override
    public List<KukumoDataType<?>> contributeTypes() {
        List<KukumoDataType<?>> types = new ArrayList<>();
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


    public static <T extends Comparable<T>> KukumoAssertDataType binaryNumberAssert(
        String name,
        boolean includeDecimals,
        ThrowableFunction<Number, T> mapper
    ) {
        return new KukumoAssertDataType(
            name,
            "matcher.number",
            BinaryNumberAssertProvider.createFromNumber(
                locale -> KukumoNumberDataType.numericRegexPattern(locale, includeDecimals),
                mapper
            ),
            new UnaryNumberAssertProvider()
        );
    }


    public static <T extends Comparable<T>> KukumoAssertDataType binaryBigDecimalAssert(
        String name,
        boolean includeDecimals,
        ThrowableFunction<BigDecimal, T> mapper
    ) {
        return new KukumoAssertDataType(
            name,
            "matcher.number",
            BinaryNumberAssertProvider.createFromBigDecimal(
                locale -> KukumoNumberDataType.numericRegexPattern(locale, includeDecimals),
                mapper
            ),
            new UnaryNumberAssertProvider()
        );
    }


    public static KukumoAssertDataType binaryStringAssert(String name) {
        return new KukumoAssertDataType(
            name,
            "matcher.string",
            new BinaryStringAssertProvider(),
            new UnaryStringAssertProvider()
        );
    }


    @SuppressWarnings("rawtypes")
    private static class KukumoAssertDataType extends KukumoDataTypeBase<Assertion> {
        public KukumoAssertDataType(
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
    protected static KukumoDataTypeBase.LocaleTypeParser<Assertion> parseProvider(
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
