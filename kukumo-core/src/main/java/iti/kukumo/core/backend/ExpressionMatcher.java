package iti.kukumo.core.backend;

import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExpressionMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionMatcher.class);

    private static final String NAMED_ARGUMENT_REGEX = "\\{(\\w+)\\:(\\w+\\-?\\w+)\\}";
    private static final String UNNAMED_ARGUMENT_REGEX = "\\{(\\w+\\-?\\w+)\\}";


    private final String translatedDefinition;
    private final KukumoDataTypeRegistry typeRegistry;
    private final Locale locale;


    public ExpressionMatcher(
            String translatedDefinition,
            KukumoDataTypeRegistry typeRegistry,
            Locale locale)
    {
        this.translatedDefinition = translatedDefinition;
        this.typeRegistry = typeRegistry;
        this.locale = locale;
    }




    public Matcher matcher(PlanStep modelStep) {
         return Pattern.compile(computeRegularExpression()).matcher(modelStep.name());
    }


    public static String computeRegularExpression(String translatedExpression) {
        String regex = regexPriorAdjustments(translatedExpression);
        regex = regexFinalAdjustments(regex);
        LOGGER.trace("{}==>{}", translatedExpression, regex);
        return regex;
    }


    protected String computeRegularExpression() {
        String regex = regexPriorAdjustments(translatedDefinition);
        regex = regexArgumentSubstitution(regex);
        regex = regexFinalAdjustments(regex);
        LOGGER.trace("{}==>{}", translatedDefinition, regex);
        return regex;
    }



    protected static String regexPriorAdjustments(String sourceExpression) {
        String regex = sourceExpression;
        // _(...)_ -> _(..._)
        regex = regex.replaceAll(" \\(([^\\)]*)\\) ", " ($1 )");
        regex = regex.replaceAll("^\\(([^\\)]*)\\) ", "($1 )");
        // * -> any value
        regex = regex.replaceAll("(?<!\\/)\\*", "(?:.*)");
        // | -> alternative
        regex = regex.replaceAll("[^ ]+\\|[^ ]+(\\|[^ ]+)*","(?:$0)");
        // ( ) -> optional
        regex = regex.replaceAll("(?<!\\/)(\\([^\\)]*\\))","(?:$1)?");
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
                regex = regex.replace("{"+typeName+"}","(?<"+DefaultBackend.UNNAMED_ARG+">"+type.getRegex(locale)+")");
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
                regex = regex.replace("{"+argName+":"+argType+"}","(?<"+argName+">"+type.getRegex(locale)+")");
            }
        }
        return regex;
    }



    protected static String regexFinalAdjustments(String computingRegex) {
        String regex = computingRegex;
        regex = regex.replace(" $","$");
        regex = regex.replace("$","\\s*$");
        return regex;
    }


    protected void throwTypeNotRegistered(String type) {
        throw new KukumoException("Wrong step definition '{}' : unknown argument type '{}'\nAvailable types are: {}",
                translatedDefinition, type, typeRegistry.allTypeNames().sorted().collect(Collectors.joining(", ")));
    }




/*








    private static ArgumentInfo argumentInfoFromExpression(
        String sourceExpression,
        List<String> expectedArgNames,
        boolean withDataTable,
        boolean withDocString,
        ParameterTypeRegistry typeRegistry
        ) {

        List<String> foundArgNames = new ArrayList<>();
        List<ParameterType<?>> argTypes = new ArrayList<>();

        Matcher namedArgs = Pattern.compile(TYPED_ARGUMENT_REGEX).assertion(sourceExpression);
        while (namedArgs.find()) {
            String name = namedArgs.group(1);
            String type = namedArgs.group(2);
            try {
                ParameterType<?> registeredType = typeRegistry.lookupByTypeName(type);
                if (registeredType == null) {
                    throw new NullPointerException();
                }
                foundArgNames.add(name+":"+type);
                argTypes.add(registeredType);
            } catch (RuntimeException e) {
                throwTypeNotRegistered(type);
            }
        }

        if (expectedArgNames.isEmpty() && !foundArgNames.isEmpty()) {
            throw new IllegalArgumentException("Expected no arguments in _expression");
        }
        if (!foundArgNames.containsAll(expectedArgNames) || !expectedArgNames.containsAll(foundArgNames)) {
            throw new IllegalArgumentException(
                "Wrong expected arguments in _expression, should be: "+expectedArgNames);
        }

        return new ArgumentInfo(expectedArgNames,foundArgNames,argTypes,withDataTable,withDocString);
    }

*/








}
