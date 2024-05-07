/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.auth.oauth;


public final class Oauth2Provider {

    public static final String ACCESS_TOKEN = "access_token";

    private final Oauth2ProviderConfig oauth2ProviderConfig = new Oauth2ProviderConfig();
    private AccessTokenRetriever retriever;


    public Oauth2ProviderConfig configuration() {
        return oauth2ProviderConfig;
    }

    public Oauth2Provider setRetriever(AccessTokenRetriever retriever) {
        this.retriever = retriever;
        return this;
    }

    public String getAccessToken() {
        if (retriever == null) {
            throw new IllegalStateException("Access token retriever is needed");
        }
        return oauth2ProviderConfig.findCachedToken()
                .orElseGet(() -> {
                    oauth2ProviderConfig.checkParameters();
                    String token = retriever.get(oauth2ProviderConfig);
                    return oauth2ProviderConfig.storeTokenAndGet(token);
                });
    }

    public interface AccessTokenRetriever {

        String get(Oauth2ProviderConfig config);
    }

}
