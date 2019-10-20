/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import java.io.File;

import org.hamcrest.Matcher;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.plan.Document;



@ExtensionPoint
public interface ContentTypeHelper {

    ContentType contentType();


    void assertContent(
        Document expected,
        ExtractableResponse<Response> response,
        boolean exactMatch
    );


    void assertContent(File expected, ExtractableResponse<Response> response, boolean exactMatch);


    default <T> void assertFragment(
        String fragment,
        ValidatableResponse response,
        Class<T> dataType,
        Matcher<T> matcher
    ) {
        throw new UnsupportedOperationException(
            "Not implemented for content type " + contentType()
        );
    }

}
