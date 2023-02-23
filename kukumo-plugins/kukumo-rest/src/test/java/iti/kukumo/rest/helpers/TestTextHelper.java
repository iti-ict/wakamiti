package iti.kukumo.rest.helpers;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import iti.kukumo.api.util.MatcherAssertion;
import iti.kukumo.rest.MatchMode;
import org.hamcrest.Matchers;
import org.junit.ComparisonFailure;
import org.junit.Test;

public class TestTextHelper {

    private final TextHelper helper = new TextHelper();

    private static final String normal = "Normal test text";
    private static final String wrong = "Wrong test text";
    private static final String partial = "test";

    @Test
    public void testStrictNormal() {
        helper.assertContent(normal, normal, MatchMode.STRICT);
    }

    @Test
    public void testStrictAnyOrderNormal() {
        helper.assertContent(normal, normal, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLooseNormal() {
        helper.assertContent(normal, normal, MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictWrong() {
        helper.assertContent(normal, wrong, MatchMode.STRICT);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderWrong() {
        helper.assertContent(normal, wrong, MatchMode.STRICT_ANY_ORDER);
    }

    @Test(expected = ComparisonFailure.class)
    public void testLooseWrong() {
        helper.assertContent(normal, wrong, MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictPartial() {
        helper.assertContent(normal, partial, MatchMode.STRICT);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderPartial() {
        helper.assertContent(normal, partial, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLoosePartial() {
        helper.assertContent(partial, normal, MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testLoosePartialReverse() {
        helper.assertContent(normal, partial, MatchMode.LOOSE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAssertFragment() {
        ValidatableResponse response = new ResponseBuilder()
                .setContentType(ContentType.JSON)
                .setStatusCode(200)
                .setBody(normal)
                .build().then();
        helper.assertFragment("", response, String.class, new MatcherAssertion<>(Matchers.equalTo(normal)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAssertContentSchema() {
        helper.assertContentSchema(normal, normal);
    }
}
