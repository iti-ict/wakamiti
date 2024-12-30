package es.iti.wakamiti.rest.helpers;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.rest.MatchMode;
import org.hamcrest.Matchers;
import org.junit.ComparisonFailure;
import org.junit.Test;

public class TextHelperTest {

    private final TextHelper helper = new TextHelper();

    private static final String NORMAL = "Normal test text";
    private static final String WRONG = "Wrong test text";
    private static final String PARTIAL = "test";

    @Test
    public void testStrictNormal() {
        helper.assertContent(NORMAL, NORMAL, MatchMode.STRICT);
    }

    @Test
    public void testStrictAnyOrderNormal() {
        helper.assertContent(NORMAL, NORMAL, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLooseNormal() {
        helper.assertContent(NORMAL, NORMAL, MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictWrong() {
        helper.assertContent(NORMAL, WRONG, MatchMode.STRICT);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderWrong() {
        helper.assertContent(NORMAL, WRONG, MatchMode.STRICT_ANY_ORDER);
    }

    @Test(expected = ComparisonFailure.class)
    public void testLooseWrong() {
        helper.assertContent(NORMAL, WRONG, MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictPartial() {
        helper.assertContent(NORMAL, PARTIAL, MatchMode.STRICT);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderPartial() {
        helper.assertContent(NORMAL, PARTIAL, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLoosePartial() {
        helper.assertContent(PARTIAL, NORMAL, MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testLoosePartialReverse() {
        helper.assertContent(NORMAL, PARTIAL, MatchMode.LOOSE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAssertFragment() {
        ValidatableResponse response = new ResponseBuilder()
                .setContentType(ContentType.JSON)
                .setStatusCode(200)
                .setBody(NORMAL)
                .build().then();
        helper.assertFragment("", response, String.class, new MatcherAssertion<>(Matchers.equalTo(NORMAL)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAssertContentSchema() {
        helper.assertContentSchema(NORMAL, NORMAL);
    }
}
