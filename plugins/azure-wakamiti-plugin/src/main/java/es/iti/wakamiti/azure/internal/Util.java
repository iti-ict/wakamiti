package es.iti.wakamiti.azure.internal;

import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.AzureReporter;
import org.slf4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Util {

    private static final Logger LOGGER = WakamitiLogger.forClass(AzureReporter.class);
    private static final Pattern ID_AND_NAME = Pattern.compile("\\[([^\\]]+)]\\s+(.+)");
    private static final String SEPARATOR = Pattern.quote("\\\\");
    private static final String PROPERTY_NOT_PRESENT_IN_TEST_CASE = "Property {} not present in test case {}";

    private Util() {
        //
    }

    public static String getPropertyValue(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return node.getProperties().get(property);
    }



    public static String getPropertyValue(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return defaultValue;
        }
        return node.getProperties().get(property);
    }



    public static Pair<String,String> getPropertyIdAndName(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return parseNameAndId(node.getProperties().get(property));
    }



    public static Pair<String,String> getPropertyValueIdAndName(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return new Pair<>(defaultValue,null);
        }
        return parseNameAndId(node.getProperties().get(property));
    }


    public static List<Pair<String,String>> getListPropertyIdAndName(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return Stream.of(node.getProperties().get(property).split(SEPARATOR))
            .map(Util::parseNameAndId)
            .collect(Collectors.toList());
    }


    public static String property(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn(PROPERTY_NOT_PRESENT_IN_TEST_CASE, property, node.getDisplayName());
            return null;
        }
        return node.getProperties().get(property);
    }



    public static String property(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return defaultValue;
        }
        return node.getProperties().get(property);
    }



    public static Pair<String,String> parseNameAndId(String value) {
        String stripped = value.strip();
        Matcher matcher = ID_AND_NAME.matcher(stripped);
        if (matcher.matches()) {
            return new Pair<>(matcher.group(2), matcher.group(1));
        } else {
            return new Pair<>(stripped,null);
        }
    }

}
