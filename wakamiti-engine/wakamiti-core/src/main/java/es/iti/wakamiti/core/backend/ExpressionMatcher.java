/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.Either;
import es.iti.wakamiti.core.Wakamiti;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Utility class for matching expressions in Wakamiti test plans.
 * It provides methods for generating regular expressions based on
 * translated step definitions and performing pattern matching on
 * model step names.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class ExpressionMatcher {

    private static final Logger LOGGER = Wakamiti.LOGGER;

    private static final String NAMED_ARGUMENT_REGEX = "\\{(\\w++):(\\w+?-?+\\w++)\\}";
    private static final String UNNAMED_ARGUMENT_REGEX = "\\{(\\w+?-?+\\w++)\\}";

    private static final Map<ExpressionMatcher, String> cache = new HashMap<>();

    private final String translatedDefinition;
    private final WakamitiDataTypeRegistry typeRegistry;
    private final Locale locale;

    private ExpressionMatcher(
            String translatedDefinition,
            WakamitiDataTypeRegistry typeRegistry,
            Locale locale
    ) {
        this.translatedDefinition = translatedDefinition;
        this.typeRegistry = typeRegistry;
        this.locale = locale;
    }

    /**
     * Retrieves a Matcher for a given translated expression, WakamitiDataTypeRegistry, locale, and model step.
     *
     * @param translatedDefinition The translated expression to create a Matcher for.
     * @param typeRegistry         The WakamitiDataTypeRegistry for type information.
     * @param locale               The locale for localization.
     * @param modelStep            The model step for which to create the Matcher.
     * @return The Matcher for the specified parameters.
     */
    public static Matcher matcherFor(
            String translatedDefinition,
            WakamitiDataTypeRegistry typeRegistry,
            Locale locale,
            Either<PlanNode, String> modelStep
    ) {
        ExpressionMatcher matcher = new ExpressionMatcher(
                translatedDefinition, typeRegistry, locale
        );
        String regex = cache.computeIfAbsent(matcher, ExpressionMatcher::computeRegularExpression);
        return Pattern.compile(regex).matcher(modelStep.mapValueOrFallback(PlanNode::name));
    }

    /**
     * Computes the regular expression for the specified translated expression.
     *
     * @param translatedExpression The translated expression for which to compute the regular expression.
     * @return The computed regular expression.
     */
    public static String computeRegularExpression(String translatedExpression) {
        String regex = regexPriorAdjustments(translatedExpression);
        regex = regexFinalAdjustments(regex);
        LOGGER.trace("Expression Matcher: {} ==> {}", translatedExpression, regex);
        return regex;
    }

    /**
     * Adjusts the regular expression prior to final adjustments.
     *
     * @param sourceExpression The source expression to adjust.
     * @return The adjusted regular expression.
     */
    protected static String regexPriorAdjustments(String sourceExpression) {
        String regex = sourceExpression;
        // a|b|c -> (a|b|c)
        regex = regex.replaceAll("[^ |(]*(\\|[^ |)]+)+", "($0)");
        // (( -> ( and )) -> (
        regex = regex.replaceAll("\\(\\(([^()]*)\\)\\)", "($1)");
        // * -> any value
        regex = regex.replaceAll("(?<!\\\\)\\*", "(.*)");
        // () -> ...
        regex = regexBracketedAdjustments(regex);

        return regex;
    }

    private static String regexBracketedAdjustments(String regex) {
        Pattern bracketed = Pattern.compile("(?<x>\\((?:(?!(?<!\\\\)[()]).)*+(?<!\\\\)\\))");
        Pattern nested = Pattern.compile("(?<!\\\\)\\((?:(?!(?<!\\\\)[()]).)*+(?<!\\\\)"
                + bracketed.pattern() + "(?:(?!(?<!\\\\)[()]).)*+(?<!\\\\)\\)");

        BiFunction<String, Pattern, String> doReplace = (string, pattern) -> {
            while (string.matches(".*" + pattern.pattern() + ".*")) {
                List<String> parts = new LinkedList<>();
                Matcher matcher = pattern.matcher(string);
                while (matcher.find()) {
                    parts.add(matcher.group("x"));
                }
                string = regexBracketedAdjustments(string, parts);
            }
            return string;
        };

        regex = doReplace.apply(regex, nested);
        regex = doReplace.apply(regex, bracketed);

        return regex.replace("[", "(").replace("]", ")");
    }

    private static String regexBracketedAdjustments(String regex, List<String> texts) {
        texts = texts.stream().distinct().collect(Collectors.toList());
        Collections.reverse(texts);
        for (String text : texts) {
            String aux = text;

            // ( ) -> optional
            aux = aux.replaceAll("(?<!\\\\)\\(([^!][^)]*)\\)", "(?:$1)?");
            // (!a) -> ((?!a).)*
            aux = aux.replaceAll("(?<!\\\\)\\(!([^)]*)\\)", "(?:(?:(?!$1).)*)?");
            // _(?:.*)? -> (?:.*)?
            aux = aux.replaceAll(" (\\(\\?:.+\\*\\)\\?)", "$1");

            aux = aux.replace("(", "[").replace(")", "]");
            regex = regex.replace(text, aux);

            if (regex.contains(aux + " ")) {
                regex = regex.replace(aux + " ", "[?:" + aux + " ]?");
            } else if (regex.contains(" " + aux)) {
                regex = regex.replace(" " + aux, "[?: " + aux + "]?");
            }
        }
        return regex;
    }

    /**
     * Performs final adjustments on the regular expression.
     *
     * @param computingRegex The intermediate regular expression.
     * @return The final adjusted regular expression.
     */
    protected static String regexFinalAdjustments(String computingRegex) {
        String regex = computingRegex;
        regex = regex.replace(" $", "$");
        regex = regex.replace("((?!\\).)$", "\1\\s*$");
        return regex;
    }

    /**
     * Computes the regular expression for the translated expression.
     *
     * @return The computed regular expression.
     */
    protected String computeRegularExpression() {
        String regex = regexPriorAdjustments(translatedDefinition);
        regex = regexArgumentSubstitution(regex);
        regex = regexFinalAdjustments(regex);
        LOGGER.trace("Expression Matcher: {} ==> {}", translatedDefinition, regex);
        return regex;
    }

    /**
     * Substitutes arguments in the regular expression based on the translated definition.
     *
     * @param computingRegex The intermediate regular expression.
     * @return The final regular expression with arguments substituted.
     */
    protected String regexArgumentSubstitution(String computingRegex) {
        String regex = computingRegex;
        // unnamed arguments
        Matcher unnamedArgs = Pattern.compile(UNNAMED_ARGUMENT_REGEX).matcher(regex);
        while (unnamedArgs.find()) {
            String typeName = unnamedArgs.group(1);
            WakamitiDataType<?> type = typeRegistry.getType(typeName);
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
            WakamitiDataType<?> type = typeRegistry.getType(argType);
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

    /**
     * Throws a WakamitiException for a type that is not registered in the WakamitiDataTypeRegistry.
     *
     * @param type The type that is not registered.
     */
    protected void throwTypeNotRegistered(String type) {
        throw new WakamitiException(
                "Wrong step definition '{}' : unknown argument type '{}'\nAvailable types are: {}",
                translatedDefinition, type,
                typeRegistry.allTypeNames().sorted().collect(Collectors.joining(", "))
        );
    }

    /**
     * Computes the hash code for this {@code ExpressionMatcher}.
     *
     * @return The computed hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(translatedDefinition, typeRegistry, locale);
    }

    /**
     * Checks if this {@code ExpressionMatcher} is equal to another object.
     *
     * @param obj The object to compare.
     * @return {@code true} if equal, {@code false} otherwise.
     */
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