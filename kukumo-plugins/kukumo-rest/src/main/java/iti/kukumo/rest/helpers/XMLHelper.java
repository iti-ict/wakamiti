/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest.helpers;


import org.hamcrest.Matcher;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import iti.commons.jext.Extension;
import iti.kukumo.rest.ContentTypeHelper;
import iti.kukumo.rest.MatchMode;



@Extension(provider = "iti.kukumo", name = "rest-xml-helper", extensionPoint = "iti.kukumo.rest.ContentTypeHelper")
public class XMLHelper extends JSONHelper implements ContentTypeHelper {

    private final JsonXmlDiff diff = new JsonXmlDiff(ContentType.XML);

    @Override
    public ContentType contentType() {
        return ContentType.XML;
    }


    @Override
    public void assertContent(String expected, String actual, MatchMode matchMode) {
       diff.assertContent(expected, actual, matchMode);
    }


    @Override
    public <T> void assertFragment(
        String fragment,
        ValidatableResponse response,
        Class<T> dataType,
        Matcher<T> matcher
    ) {
        response.body(fragment, matcher);
    }

/*
    private static final DifferenceEvaluator looseDifferenceEvaluator =
    (Comparison comparison, ComparisonResult outcome) -> {
        // ignore when the test has more elements than the control
        if (outcome == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH) {
                Integer controlNodes = (Integer) comparison.getControlDetails().getValue();
                Integer testNodes = (Integer) comparison.getTestDetails().getValue();
                return testNodes.compareTo(controlNodes) > 0 ? ComparisonResult.SIMILAR
                                : ComparisonResult.DIFFERENT;
            } else if (comparison.getType() == ComparisonType.CHILD_LOOKUP
                            && comparison.getControlDetails().getValue() == null) {
                return ComparisonResult.SIMILAR;
            } else if (comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE) {
                return ComparisonResult.SIMILAR;
            }
        }
        return outcome;
    };


    private static final DifferenceEvaluator anyOrderDifferenceEvaluator =
    (Comparison comparison, ComparisonResult outcome) -> {
        // ignore when the test has more elements than the control
        if (outcome == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE) {
                return ComparisonResult.SIMILAR;
            }
        }
        return outcome;
    };


    private static final DifferenceEvaluator anyOrderDifferenceEvaluator =
    (Comparison comparison, ComparisonResult outcome) -> {
        // ignore when the test has more elements than the control
        if (outcome == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE) {
                return ComparisonResult.SIMILAR;
            }
        }
        return outcome;
    };


    private static final DifferenceEvaluator anyOrderDifferenceEvaluator =
    (Comparison comparison, ComparisonResult outcome) -> {
        // ignore when the test has more elements than the control
        if (outcome == ComparisonResult.DIFFERENT) {
            if (comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE) {
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
    public void assertContent(String expected, String actual, MatchMode matchMode) {
        DiffBuilder diffBuilder = diffBuilder(expected, actual);
        if (matchMode == MatchMode.LOOSE) {
            diffBuilder = diffBuilder
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                .withDifferenceEvaluator(looseDifferenceEvaluator);
        } else if (matchMode == MatchMode.STRICT_ANY_ORDER) {
            diffBuilder = diffBuilder
                    .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                    .withDifferenceEvaluator(anyOrderDifferenceEvaluator);
        }
        Diff diff = diffBuilder.build();
        if (diff.hasDifferences()) {
            throw new ComparisonFailure(diff.toString(), expected, actual);
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


    @Override
    public <T> void assertFragment(
        String fragment,
        ValidatableResponse response,
        Class<T> dataType,
        Matcher<T> matcher
    ) {
        response.body(fragment,matcher);
    }
    */
}
