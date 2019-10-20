/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest.helpers;


import java.io.File;

import org.junit.ComparisonFailure;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.Document;
import iti.kukumo.rest.ContentTypeHelper;
import iti.kukumo.util.ResourceLoader;



@Extension(provider = "iti.kukumo", name = "rest-text-helper", extensionPoint = "iti.kukumo.rest.ContentTypeHelper")
public class TextHelper implements ContentTypeHelper {

    private static final ResourceLoader resourceLoader = Kukumo.instance().resourceLoader();


    @Override
    public ContentType contentType() {
        return ContentType.TEXT;
    }


    @Override
    public void assertContent(
        Document expected,
        ExtractableResponse<Response> response,
        boolean exactMatch
    ) {
        assertContent(expected.getContent(), response.asString(), exactMatch);
    }


    @Override
    public void assertContent(
        File expected,
        ExtractableResponse<Response> response,
        boolean exactMatch
    ) {
        assertContent(resourceLoader.readFileAsString(expected), response.asString(), exactMatch);
    }


    protected void assertContent(String expected, String actual, boolean exactMatch) {
        if (exactMatch && !actual.trim().equals(expected.trim())) {
            throw new ComparisonFailure("Text differences", expected, actual);
        } else if (!exactMatch && !actual.trim().contains(expected.trim())) {
            throw new ComparisonFailure("Text differences", expected, actual);
        }
    }

}
