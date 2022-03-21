/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.rest;

import java.net.URL;

public class Oauth2ProviderConfiguration {

    private URL url;
    private String clientId;
    private String clientSecret;

    public URL url() {
        return url;
    }

    public Oauth2ProviderConfiguration url(URL url) {
        this.url = url;
        return this;
    }

    public String clientId() {
        return clientId;
    }

    public Oauth2ProviderConfiguration clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public Oauth2ProviderConfiguration clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

}