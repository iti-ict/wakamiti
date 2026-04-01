/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iti.wakamiti.api.WakamitiException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * JSON comparator used by AMQP message assertions.
 */
class AmqpJsonDiff {

    private final ObjectMapper mapper = new ObjectMapper();


    void assertValidExpected(
            String expected
    ) {
        try {
            mapper.readTree(expected);
        } catch (JsonProcessingException e) {
            throw new WakamitiException("Expected message is not a valid JSON document", e);
        }
    }

    boolean matches(
            String expected,
            String actual,
            MatchMode matchMode
    ) {
        try {
            JsonNode expectedJson = mapper.readTree(expected);
            JsonNode actualJson = mapper.readTree(actual);
            return compareJsonNode(matchMode, expectedJson, actualJson);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private boolean compareJsonNode(
            MatchMode matchMode,
            JsonNode expectedNode,
            JsonNode actualNode
    ) {
        if (expectedNode.getNodeType() != actualNode.getNodeType()) {
            return false;
        }
        if (expectedNode.isArray()) {
            return compareJsonArray(matchMode, expectedNode, actualNode);
        }
        if (expectedNode.isContainerNode()) {
            return compareJsonObject(matchMode, expectedNode, actualNode);
        }
        if (expectedNode.isNumber()) {
            return actualNode.isNumber()
                    && expectedNode.decimalValue().compareTo(actualNode.decimalValue()) == 0;
        }
        return expectedNode.equals(actualNode);
    }

    private boolean compareJsonArray(
            MatchMode matchMode,
            JsonNode expectedNode,
            JsonNode actualNode
    ) {
        switch (matchMode) {
            case STRICT:
                return compareJsonArrayStrict(expectedNode, actualNode);
            case STRICT_ANY_ORDER:
                return compareJsonArrayStrictAnyOrder(expectedNode, actualNode);
            case LOOSE:
                return compareJsonArrayLoose(expectedNode, actualNode);
            default:
                return false;
        }
    }

    private boolean compareJsonArrayStrict(
            JsonNode expectedNode,
            JsonNode actualNode
    ) {
        if (expectedNode.size() != actualNode.size()) {
            return false;
        }
        for (int i = 0; i < expectedNode.size(); i++) {
            if (!compareJsonNode(MatchMode.STRICT, expectedNode.get(i), actualNode.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean compareJsonArrayStrictAnyOrder(
            JsonNode expectedNode,
            JsonNode actualNode
    ) {
        if (expectedNode.size() != actualNode.size()) {
            return false;
        }
        return compareJsonArrayUnordered(expectedNode, actualNode, MatchMode.STRICT_ANY_ORDER);
    }

    private boolean compareJsonArrayLoose(
            JsonNode expectedNode,
            JsonNode actualNode
    ) {
        if (expectedNode.size() > actualNode.size()) {
            return false;
        }
        return compareJsonArrayUnordered(expectedNode, actualNode, MatchMode.LOOSE);
    }

    private boolean compareJsonArrayUnordered(
            JsonNode expectedNode,
            JsonNode actualNode,
            MatchMode mode
    ) {
        List<Integer> usedIndexes = new ArrayList<>();
        for (int i = 0; i < expectedNode.size(); i++) {
            boolean currentElementMatch = false;
            for (int j = 0; j < actualNode.size() && !currentElementMatch; j++) {
                if (usedIndexes.contains(j)) {
                    continue;
                }
                if (compareJsonNode(mode, expectedNode.get(i), actualNode.get(j))) {
                    usedIndexes.add(j);
                    currentElementMatch = true;
                }
            }
            if (!currentElementMatch) {
                return false;
            }
        }
        return true;
    }

    private boolean compareJsonObject(
            MatchMode matchMode,
            JsonNode expectedNode,
            JsonNode actualNode
    ) {
        var expectedFields = asList(expectedNode.fieldNames());
        var actualFields = asList(actualNode.fieldNames());

        if (!actualFields.containsAll(expectedFields)) {
            return false;
        }
        if (matchMode != MatchMode.LOOSE && !expectedFields.containsAll(actualFields)) {
            return false;
        }

        for (int i = 0; i < expectedFields.size(); i++) {
            String expectedField = expectedFields.get(i);
            if (matchMode == MatchMode.STRICT) {
                if (actualFields.size() <= i || !expectedField.equals(actualFields.get(i))) {
                    return false;
                }
            }
            if (!compareJsonNode(matchMode, expectedNode.get(expectedField), actualNode.get(expectedField))) {
                return false;
            }
        }
        return true;
    }

    private static <T> List<T> asList(
            Iterator<T> i
    ) {
        List<T> list = new ArrayList<>();
        i.forEachRemaining(list::add);
        return list;
    }
}
