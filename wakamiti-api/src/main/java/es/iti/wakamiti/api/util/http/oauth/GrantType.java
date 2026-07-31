/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.oauth;


import java.util.LinkedList;
import java.util.List;


/**
 * Defines the values supported by Grant Type.
 */
public enum GrantType {

    /**
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.3">RFC 6749</a>
     */
    PASSWORD("grant_type", "username", "password"),

    /**
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.4">RFC 6749</a>
     */
    CLIENT_CREDENTIALS("grant_type");

    final List<String> requiredFields = new LinkedList<>();

    GrantType(
            String... requiredFields
    ) {
        this.requiredFields.addAll(List.of(requiredFields));
    }

    /**
     * Returns the form parameters required by this OAuth 2.0 grant flow.
     *
     * @return the required parameter names used when validating token requests
     */
    public List<String> requiredFields() {
        return requiredFields;
    }

}
