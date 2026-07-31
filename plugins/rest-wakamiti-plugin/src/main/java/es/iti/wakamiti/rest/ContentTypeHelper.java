/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.plan.Document;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;


/**
 * Adapts response assertions to a structured HTTP media type.
 * <p>
 * Implementations define how complete documents, selected fragments and
 * schemas are interpreted for formats such as JSON and XML. Operations that a
 * format cannot support retain the default implementation and fail explicitly.
 */
@ExtensionPoint
public interface ContentTypeHelper {

    /**
     * Returns the media type handled by this adapter.
     *
     * @return REST Assured content-type identifier
     */
    ContentType contentType();

    /**
     * Compares complete expected and actual payloads according to a match mode.
     *
     * @param expected expected structured payload
     * @param actual actual structured payload
     * @param matchMode strictness and array-order policy
     * @throws AssertionError when the payloads do not satisfy the selected mode
     */
    void assertContent(
            String expected,
            String actual,
            MatchMode matchMode
    );

    /**
     * Compares an expected payload with a structured fragment extracted from a
     * response.
     *
     * @param fragment format-specific path identifying the actual fragment
     * @param expected expected fragment payload
     * @param response extractable HTTP response
     * @param mode strictness and array-order policy
     * @throws UnsupportedOperationException when fragment comparison is not
     *                                       supported for this media type
     */
    default void assertContent(
            String fragment,
            String expected,
            ExtractableResponse<Response> response,
            MatchMode mode
    ) {
        throw new UnsupportedOperationException("Not implemented for content type " + contentType());
    }

    /**
     * Compares a response body with an expected Wakamiti document.
     *
     * @param expected document whose content supplies the expected payload
     * @param response extractable HTTP response
     * @param matchMode strictness and array-order policy
     */
    default void assertContent(
            Document expected,
            ExtractableResponse<Response> response,
            MatchMode matchMode
    ) {
        assertContent(expected.getContent(), response.asString(), matchMode);
    }

    /**
     * Compares a response body with an expected payload.
     *
     * @param expected expected structured payload
     * @param response extractable HTTP response
     * @param matchMode strictness and array-order policy
     */
    default void assertContent(
            String expected,
            ExtractableResponse<Response> response,
            MatchMode matchMode
    ) {
        assertContent(expected, response.asString(), matchMode);
    }

    /**
     * Converts a response fragment to a requested type and applies an
     * assertion.
     *
     * @param fragment format-specific fragment path
     * @param response response prepared for REST Assured validation
     * @param dataType Java type expected from the fragment
     * @param matcher assertion applied to the converted value
     * @param <T> fragment value type
     * @throws UnsupportedOperationException when typed fragments are not
     *                                       supported for this media type
     */
    default <T> void assertFragment(
            String fragment,
            ValidatableResponse response,
            Class<T> dataType,
            Assertion<T> matcher
    ) {
        throw new UnsupportedOperationException("Not implemented for content type " + contentType());
    }

    /**
     * Validates structured content against a schema.
     *
     * @param expectedSchema schema text in the format supported by the adapter
     * @param content payload to validate
     * @throws UnsupportedOperationException when schema validation is not
     *                                       supported for this media type
     */
    default void assertContentSchema(
            String expectedSchema,
            String content
    ) {
        throw new UnsupportedOperationException("Not implemented for content type " + contentType());
    }

}
