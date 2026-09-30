/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.jwt;


import static java.util.Objects.isNull;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.http.TokenProviderConfig;


/** Stores the configuration used to retrieve JWT authentication tokens. */
public class JwtProviderConfig extends TokenProviderConfig<JwtProviderConfig> {

    private String usernameField = "username";
    private String passwordField = "password";
    private String tokenPath = "token";

    /**
     * Returns the JSON property used for the login username.
     *
     * @return the username property name
     */
    public String usernameField() {
        return usernameField;
    }

    /**
     * Sets the JSON property used for the login username.
     *
     * @param usernameField the username property name
     * @return this configuration
     */
    public JwtProviderConfig usernameField(
            String usernameField
    ) {
        this.usernameField = usernameField;
        return this;
    }

    /**
     * Returns the JSON property used for the login password.
     *
     * @return the password property name
     */
    public String passwordField() {
        return passwordField;
    }

    /**
     * Sets the JSON property used for the login password.
     *
     * @param passwordField the password property name
     * @return this configuration
     */
    public JwtProviderConfig passwordField(
            String passwordField
    ) {
        this.passwordField = passwordField;
        return this;
    }

    /**
     * Returns the path of the token in the JSON login response.
     *
     * @return the JSON token path
     */
    public String tokenPath() {
        return tokenPath;
    }

    /**
     * Sets the path of the token in the JSON login response.
     *
     * @param tokenPath the JSON token path
     * @return this configuration
     */
    public JwtProviderConfig tokenPath(
            String tokenPath
    ) {
        this.tokenPath = tokenPath;
        return this;
    }

    @Override
    public void checkParameters() {
        if (isNull(url())) {
            throw new WakamitiException("Missing JWT configuration parameter: url");
        }
    }

    @Override
    protected JwtProviderConfig self() {
        return this;
    }

}
