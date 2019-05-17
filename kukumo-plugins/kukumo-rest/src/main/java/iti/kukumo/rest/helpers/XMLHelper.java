package iti.kukumo.rest.helpers;

import java.io.File;

import org.junit.ComparisonFailure;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.ElementSelectors;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.Document;
import iti.kukumo.rest.ContentTypeHelper;
import iti.kukumo.util.ResourceLoader;

/**
 * @author ITI
 * Created by ITI on 17/04/19
 */
@Extension(provider="iti.kukumo",name="rest-xml-helper", version="1.0-SNAPSHOT", extensionPoint = "iti.kukumo.rest.ContentTypeHelper")
public class XMLHelper implements ContentTypeHelper {

    private static final ResourceLoader resourceLoader = Kukumo.getResourceLoader();

    private static final DifferenceEvaluator looseDifferenceEvaluator = (Comparison comparison, ComparisonResult outcome) -> {
        // ignore when the test has more elements than the control
        if (outcome == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH) {
                Integer controlNodes = (Integer) comparison.getControlDetails().getValue();
                Integer testNodes = (Integer) comparison.getTestDetails().getValue();
                return testNodes.compareTo(controlNodes) > 0 ? ComparisonResult.SIMILAR : ComparisonResult.DIFFERENT;
            }
            else if (comparison.getType() == ComparisonType.CHILD_LOOKUP && comparison.getControlDetails().getValue() == null) {
                return ComparisonResult.SIMILAR;
            } else if (comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE) {
                return ComparisonResult.SIMILAR;
            }
        }
        return outcome;
    };




    @Override
    public ContentType contentType() {
        return ContentType.XML;
    }

    @Override
    public void assertContent(Document expected, ExtractableResponse<Response> response, boolean exactMatch) {
        assertContent(expected.getContent(), response.asString(),exactMatch);
    }


    @Override
    public void assertContent(File expected, ExtractableResponse<Response> response, boolean exactMatch) {
        assertContent(resourceLoader.readFileAsString(expected), response.asString(),exactMatch);
    }






    public void assertContent(String expected, String actual, boolean exactMatch) {
        Diff diff = exactMatch ?
                diffBuilder(expected,actual)
                        .build() :
                diffBuilder(expected,actual)
                        .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                        .withDifferenceEvaluator(looseDifferenceEvaluator)
                        .build();

        if (diff.hasDifferences()) {
            throw new ComparisonFailure(diff.toString(),expected,actual);
        }
    }



    private DiffBuilder diffBuilder(String expected, String actual) {
        return DiffBuilder.compare(expected).withTest(actual)
                .ignoreComments()
                .ignoreElementContentWhitespace()
                .ignoreWhitespace()
                .normalizeWhitespace()
                .checkForSimilar();
    }








}
