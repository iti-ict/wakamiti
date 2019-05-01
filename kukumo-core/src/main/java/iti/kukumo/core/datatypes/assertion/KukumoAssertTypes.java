package iti.kukumo.core.datatypes.assertion;

import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.core.datatypes.KukumoDataTypeBase;
import iti.kukumo.core.datatypes.KukumoNumberDataType;
import iti.kukumo.util.ThrowableFunction;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Extension(provider="iti.kukumo", name="assertion-types", version="1.0.0")
public class KukumoAssertTypes implements DataTypeContributor {


    @Override
    public List<KukumoDataType<?>> contributeTypes() {
        List<KukumoDataType<?>> types = new ArrayList<>();
        // functional datatypes
        types.add(binaryNumbeAssert("integer-assertion", false, Number::intValue));
        types.add(binaryBigDecimalAssert("decimal-assertion",  true, x->x));
        types.add(binaryStringAssert("text-assertion"));
        // java datatypes
        types.add(binaryNumbeAssert("short-assertion",  false, Number::shortValue));
        types.add(binaryNumbeAssert("int-assertion",  false, Number::intValue));
        types.add(binaryNumbeAssert("long-assertion", false, Number::longValue));
        types.add(binaryBigDecimalAssert("biginteger-assertion", true, BigDecimal::toBigInteger));
        types.add(binaryBigDecimalAssert("float-assertion", true, BigDecimal::floatValue));
        types.add(binaryBigDecimalAssert("double-assertion", true, BigDecimal::doubleValue));
        types.add(binaryBigDecimalAssert("bigdecimal-assertion", true, x->x));
        return types;
    }




    public static <T extends Comparable<T>> KukumoAssertDataType binaryNumbeAssert(
            String name,
            boolean includeDecimals,
            ThrowableFunction<Number,T> mapper
    ) {
        return new KukumoAssertDataType(name, 
            BinaryNumberAssertProvider.createFromNumber(
                    locale -> KukumoNumberDataType.numericRegexPattern(locale, includeDecimals),
                    mapper
            ),
            new GenericUnaryAssertProvider()
        );
    }



    public static <T extends Comparable<T>> KukumoAssertDataType binaryBigDecimalAssert(
            String name,
            boolean includeDecimals,
            ThrowableFunction<BigDecimal,T> mapper
    ) {
        return new KukumoAssertDataType(name, 
            BinaryNumberAssertProvider.createFromBigDecimal(
                    locale -> KukumoNumberDataType.numericRegexPattern(locale, includeDecimals),
                    mapper
            ),
            new GenericUnaryAssertProvider()
        );
    }



    public static KukumoAssertDataType binaryStringAssert(String name) {
        return new KukumoAssertDataType(name, 
            new BinaryStringAssertProvider(),
            new GenericUnaryAssertProvider()
        );
    }



    @SuppressWarnings("rawtypes")
    private static class KukumoAssertDataType extends KukumoDataTypeBase<Matcher> {
        public KukumoAssertDataType(
                String name,
                AbstractAssertProvider... matcherProviders
        ) {
            super(
                    name,
                    Matcher.class,
                    locale -> ".*",
                    hintProvider(matcherProviders),
                    parseProvider(matcherProviders)
            );
        }
    }



    protected static KukumoDataTypeBase.LocaleHintProvider hintProvider(AbstractAssertProvider[] assertProviders) {
        return locale -> {
            StringBuilder string = new StringBuilder();
            for (AbstractAssertProvider assertProvider : assertProviders) {
                assertProvider.printAllExpressions(locale, string);
            }
            return string.toString();
        };
    }


    @SuppressWarnings("rawtypes")
    protected static KukumoDataTypeBase.LocaleTypeParser<Matcher> parseProvider(AbstractAssertProvider[] assertProviders) {
        return locale -> expression -> {
            for (AbstractAssertProvider assertProvider : assertProviders) {
                Optional<Matcher<?>> matcher = assertProvider.matcherFromExpression(locale, expression);
                if (matcher.isPresent()) {
                    return matcher.get();
                }
            }
            return null;
        };
    }


}
