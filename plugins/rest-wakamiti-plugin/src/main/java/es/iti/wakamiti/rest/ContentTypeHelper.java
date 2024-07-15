/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest;


import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.plan.Document;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
@ExtensionPoint
public interface ContentTypeHelper {

    ContentType contentType();

    void assertContent(String expected, String actual, MatchMode matchMode);

    default void assertContent(
            String fragment,
            String expected,
            ExtractableResponse<Response> response,
            MatchMode mode
    ) {
        throw new UnsupportedOperationException("Not implemented for content type " + contentType());
    }

    default void assertContent(
            Document expected,
            ExtractableResponse<Response> response,
            MatchMode matchMode
    ) {
        assertContent(expected.getContent(), response.asString(), matchMode);
    }

    default void assertContent(
            String expected,
            ExtractableResponse<Response> response,
            MatchMode matchMode
    ) {
        assertContent(expected, response.asString(), matchMode);
    }

    default <T> void assertFragment(
            String fragment,
            ValidatableResponse response,
            Class<T> dataType,
            Assertion<T> matcher
    ) {
        throw new UnsupportedOperationException("Not implemented for content type " + contentType());
    }

    default void assertContentSchema(String expectedSchema, String content) {
        throw new UnsupportedOperationException("Not implemented for content type " + contentType());
    }
}