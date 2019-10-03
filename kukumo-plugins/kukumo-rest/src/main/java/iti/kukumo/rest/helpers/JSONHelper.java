package iti.kukumo.rest.helpers;

import java.io.File;

import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.core.plan.Document;
import iti.kukumo.rest.ContentTypeHelper;
import iti.kukumo.util.ResourceLoader;

/**
 * @author ITI
 * Created by ITI on 17/04/19
 */
@Extension(provider="iti.kukumo",name="rest-json-helper", extensionPoint = "iti.kukumo.rest.ContentTypeHelper")
public class JSONHelper implements ContentTypeHelper {

    private static final ResourceLoader resourceLoader = Kukumo.instance().getResourceLoader();


    @Override
    public ContentType contentType() {
        return ContentType.JSON;
    }

    @Override
    public void assertContent(Document expected, ExtractableResponse<Response> response, boolean exactMatch) {
        assertJSONIs(expected.getContent(),response, exactMatch ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT);
    }


    @Override
    public void assertContent(File expected, ExtractableResponse<Response> response, boolean exactMatch) {
        assertJSONIs(resourceLoader.readFileAsString(expected),response, exactMatch ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT);
    }


    @Override
    public <T> void assertFragment(String fragment, ValidatableResponse response, Class<T> dataType, Matcher<T> matcher) {
        response.body(fragment,matcher);
    }


    protected void assertJSONIs(String expected, ExtractableResponse<Response> response, JSONCompareMode mode )  {
        try {
            if (expected.trim().startsWith("[")) { // body is an array instead of a JSON object
                JSONAssert.assertEquals(new JSONArray(expected), new JSONArray(response.asString()), mode);
            } else {
                JSONAssert.assertEquals(expected, response.asString(), mode);
            }
        } catch (Exception e) {
            throw new KukumoException(e);
        }
    }





}
