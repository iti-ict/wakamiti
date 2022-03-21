/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.rest.helpers;
/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */






import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.ComparisonFailure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.restassured.http.ContentType;
import iti.kukumo.api.KukumoException;
import iti.kukumo.rest.MatchMode;

import static iti.kukumo.rest.MatchMode.*;

public class JsonXmlDiff  {

    private final ObjectMapper mapper;


    public JsonXmlDiff(ContentType contentType) {
        if (contentType == ContentType.JSON) {
            this.mapper = new ObjectMapper();
        } else if (contentType == ContentType.XML){
            this.mapper = new XmlMapper();
        } else {
            throw new IllegalArgumentException("Only either JSON or XML types are allowed");
        }
        this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }



    public void assertContent(String expected, String actual, MatchMode matchMode) {
        try {
            List<String> errors = new ArrayList<>();
            JsonNode expectedJson = mapper.readTree(expected);
            JsonNode actualJson = mapper.readTree(actual);
            compareJsonNode(matchMode,expectedJson,actualJson,"",errors);
            throwExceptionIfHasErrors(errors, expected, actual);
        } catch (JsonProcessingException e) {
            throw new KukumoException(e);
        }
    }



    private void throwExceptionIfHasErrors(List<String> errors, String expected, String actual)
    throws ComparisonFailure, JsonProcessingException {
        if (!errors.isEmpty()) {
            var message = errors.stream().collect(Collectors.joining(
                "\n\t-", "The expected and actual responses have differences:\n\t-", "\n"
            ));
            throw new ComparisonFailure(message, format(expected), format(actual));
        }
    }





    private String format(String content) throws JsonProcessingException {
        return mapper.writeValueAsString(mapper.readTree(content));
    }



    private void compareJsonNode(
        MatchMode matchMode,
        JsonNode expectedNode,
        JsonNode actualNode,
        String prefix,
        List<String> errors
    ) {
        var expectedType = expectedNode.getNodeType();
        var actualType = actualNode.getNodeType();
        String segmentExpected = literalSegmentExpected(prefix);

        if (actualNode.isMissingNode() && !expectedNode.isMissingNode() && matchMode == STRICT) {
            errors.add(segmentExpected+", but is not present");
            return;
        }

        if (expectedType != actualType) {
            errors.add(segmentExpected+" to be a "+expectedType+", but it is "+actualType);
            return;
        }

        if (expectedNode.isArray()) {
            compareJsonArray(matchMode, expectedNode, actualNode, prefix, errors);
        } else if (expectedNode.isContainerNode()) {
            compareJsonObject(matchMode, expectedNode, actualNode, prefix, errors);
        } else if (expectedNode.isValueNode() && !expectedNode.equals(actualNode)) {
            errors.add(
                segmentExpected+": '"+expectedNode.asText()+
                "', actual: '"+actualNode.asText()+"'"
            );
        }
    }






    private void compareJsonArray(
        MatchMode matchMode,
        JsonNode expectedNode,
        JsonNode actualNode,
        String prefix,
        List<String> errors
    ) {
        String segmentExpected = literalSegmentExpected(prefix);
        if (List.of(STRICT, STRICT_ANY_ORDER).contains(matchMode) && expectedNode.size() != actualNode.size()) {
            errors.add(
                segmentExpected+" size: "+expectedNode.size()+
                ", actual size: "+actualNode.size()
            );
            return;
        }
        if (matchMode == LOOSE && expectedNode.size() > actualNode.size()) {
            errors.add(
                segmentExpected+" minimum size: "+expectedNode.size()+
                ", actual size: "+actualNode.size()
            );
            return;
        }
        for (int i=0;i<expectedNode.size();i++) {
            compareJsonNode(
                matchMode,
                expectedNode.get(i),
                actualNode.get(i),
                prefix+"["+i+"]",
                errors
            );
        }

    }





    private void compareJsonObject(
        MatchMode matchMode,
        JsonNode expectedNode,
        JsonNode actualNode,
        String prefix,
        List<String> errors
    ) {

        String segmentExpected = literalSegmentExpected(prefix);
        var expectedFields = asList(expectedNode.fieldNames());
        var actualFields = asList(actualNode.fieldNames());

        var missingExpectedFields = expectedFields.stream()
            .filter(Predicate.not(actualFields::contains))
            .collect(Collectors.toList());
        var nonExpectedActualFields = actualFields.stream()
            .filter(Predicate.not(expectedFields::contains))
            .collect(Collectors.toList());

        if (!missingExpectedFields.isEmpty()) {
            errors.add(
                segmentExpected+" to have fields "+missingExpectedFields+
                ", but they are not present"
            );
            return;
        }

        if (!nonExpectedActualFields.isEmpty() && matchMode == STRICT) {
            errors.add(
                segmentExpected+" not to have fields "+nonExpectedActualFields+
                ", but they are present"
            );
            return;
        }

        for (int i=0;i<expectedFields.size();i++) {
            String expectedField = expectedFields.get(i);
            String actualFieldInSamePosition = actualFields.get(i);
            if (matchMode == STRICT && !expectedField.equals(actualFieldInSamePosition)) {
                errors.add(
                    segmentExpected+" to have field '"+expectedField+"' at position "+
                    i + " but it was '"+actualFieldInSamePosition+"'"
                );
                continue;
            }
            if (!missingExpectedFields.contains(expectedField)) {
                compareJsonNode(
                    matchMode,
                    expectedNode.get(expectedField),
                    actualNode.get(expectedField),
                    prefix + (prefix.isEmpty() ? "" : "." )+expectedField,errors)
                ;
            }

        }
    }


    private static String literalSegmentExpected(String prefix) {
        return prefix.isEmpty() ? "root segment expected" : "segment '"+prefix+"' expected";
    }


    private static <T> List<T> asList(Iterator<T> i) {
        List<T> list = new ArrayList<>();
        i.forEachRemaining(list::add);
        return list;
    }

}