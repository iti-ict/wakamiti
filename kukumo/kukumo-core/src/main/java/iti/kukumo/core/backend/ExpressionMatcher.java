/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.util.Either;


public class ExpressionMatcher {

    private static final Logger LOGGER = Kukumo.LOGGER;

    private static final String NAMED_ARGUMENT_REGEX = "\\{(\\w+)\\:(\\w+\\-?\\w+)\\}";
    private static final String UNNAMED_ARGUMENT_REGEX = "\\{(\\w+\\-?\\w+)\\}";

    private static final Map<ExpressionMatcher, String> cache = new HashMap<>();

    private final String translatedDefinition;
    private final KukumoDataTypeRegistry typeRegistry;
    private final Locale locale;


    public static Matcher matcherFor(
        String translatedDefinition,
        KukumoDataTypeRegistry typeRegistry,
        Locale locale,
        Either<PlanNode,String> modelStep
    ) {
        ExpressionMatcher matcher = new ExpressionMatcher(
            translatedDefinition, typeRegistry, locale
        );
        String regex = cache.computeIfAbsent(matcher, ExpressionMatcher::computeRegularExpression);
        return Pattern.compile(regex).matcher(modelStep.mapValueOrFallback(PlanNode::name));
    }


    private ExpressionMatcher(
                    String translatedDefinition,
                    KukumoDataTypeRegistry typeRegistry,
                    Locale locale
    ) {
        this.translatedDefinition = translatedDefinition;
        this.typeRegistry = typeRegistry;
        this.locale = locale;
    }


    public static String computeRegularExpression(String translatedExpression) {
        String regex = regexPriorAdjustments(translatedExpression);
        regex = regexFinalAdjustments(regex);
        LOGGER.trace("Expression Matcher: {} ==> {}", translatedExpression, regex);
        return regex;
    }


    protected String computeRegularExpression() {
        String regex = regexPriorAdjustments(translatedDefinition);
        regex = regexArgumentSubstitution(regex);
        regex = regexFinalAdjustments(regex);
        LOGGER.trace("Expression Matcher: {} ==> {}", translatedDefinition, regex);
        return regex;
    }


    protected static String regexPriorAdjustments(String sourceExpression) {
        String regex = sourceExpression;
        // a|b|c -> (a|b|c)
        regex = regex.replaceAll("[^ |(]*(\\|[^ |)]+)+", "\\($0\\)");
        // (( -> ( and )) -> (
        regex = regex.replace("((", "(").replace("))", ")");
        // * -> any value
        regex = regex.replaceAll("(?<!\\\\)\\*", "(.*)");
        // ( ) -> optional
        regex = regex.replaceAll("(?<!\\\\)\\(([^!][^)]*)\\)", "(?:$1)?");
        // (...)?_ -> (?:(...)?_)?
        regex = regex.replaceAll("\\(\\?:[^)]+\\)\\? ", "(?:$0)?");
        // _(?:.*)? -> (?:.*)?
        regex = regex.replace(" (?:.*)?", "(?:.*)?");
        // (!a) -> ((?!a).)*
        regex = regex.replaceAll("(?<!\\\\)\\(!([^)]*)\\)", "((?!$1).)*");
        return regex;
    }


    protected String regexArgumentSubstitution(String computingRegex) {
        String regex = computingRegex;
        // unnamed arguments
        Matcher unnamedArgs = Pattern.compile(UNNAMED_ARGUMENT_REGEX).matcher(regex);
        while (unnamedArgs.find()) {
            String typeName = unnamedArgs.group(1);
            KukumoDataType<?> type = typeRegistry.getType(typeName);
            if (type == null) {
                throwTypeNotRegistered(typeName);
            } else {
                regex = regex.replace(
                    "{" + typeName + "}",
                    "(?<" + RunnableBackend.UNNAMED_ARG + ">" + type.getRegex(locale) + ")"
                );
            }
        }
        // named arguments
        Matcher namedArgs = Pattern.compile(NAMED_ARGUMENT_REGEX).matcher(regex);
        while (namedArgs.find()) {
            String argName = namedArgs.group(1);
            String argType = namedArgs.group(2);
            KukumoDataType<?> type = typeRegistry.getType(argType);
            if (type == null) {
                throwTypeNotRegistered(argType);
            } else {
                regex = regex.replace(
                    "{" + argName + ":" + argType + "}",
                    "(?<" + argName + ">" + type.getRegex(locale) + ")"
                );
            }
        }
        return regex;
    }


    protected static String regexFinalAdjustments(String computingRegex) {
        String regex = computingRegex;
        regex = regex.replace(" $", "$");
        regex = regex.replace("$", "\\s*$");
        return regex;
    }


    protected void throwTypeNotRegistered(String type) {
        throw new KukumoException(
            "Wrong step definition '{}' : unknown argument type '{}'\nAvailable types are: {}",
            translatedDefinition, type,
            typeRegistry.allTypeNames().sorted().collect(Collectors.joining(", "))
        );
    }


    @Override
    public int hashCode() {
        return Objects.hash(translatedDefinition, typeRegistry, locale);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpressionMatcher) {
            ExpressionMatcher other = (ExpressionMatcher) obj;
            return other.typeRegistry == this.typeRegistry &&
                            other.locale.equals(this.locale) &&
                            other.translatedDefinition.equals(this.translatedDefinition);
        }
        return false;
    }
}
