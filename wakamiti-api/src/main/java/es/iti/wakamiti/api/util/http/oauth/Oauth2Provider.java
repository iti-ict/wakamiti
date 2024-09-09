/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.oauth;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.JsonUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static org.apache.commons.lang3.StringUtils.isBlank;


public final class Oauth2Provider {

    public static final String ACCESS_TOKEN = "access_token";

    private final Oauth2ProviderConfig oauth2ProviderConfig = new Oauth2ProviderConfig();
    private AccessTokenRetriever retriever;

    public Oauth2Provider() {
        this.retriever = new DefaultAccessTokenRetriever();
    }

    public Oauth2ProviderConfig configuration() {
        return oauth2ProviderConfig;
    }

    public Oauth2Provider setRetriever(AccessTokenRetriever retriever) {
        if (retriever == null) {
            throw new WakamitiException("Access token retriever is needed");
        }
        this.retriever = retriever;
        return this;
    }

    public String getAccessToken() {
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

    private static class DefaultAccessTokenRetriever implements AccessTokenRetriever {

        public String get(Oauth2ProviderConfig config) {
            String auth = Base64.getEncoder()
                    .encodeToString((config.clientId() + ":" + config.clientSecret()).getBytes());
            List<NameValuePair> formData = config.parameters().entrySet().stream()
                    .map(e -> new BasicNameValuePair(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            HttpUriRequest request = RequestBuilder.create(HttpPost.METHOD_NAME)
                    .setUri(config.url().toString())
                    .setHeader("Authorization", "Basic " + auth)
                    .setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8))
                    .build();

            CloseableHttpClient httpClient = HttpClients.createDefault();

            try {
                CloseableHttpResponse response = httpClient.execute(request);
                int status = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                if (status >= 400) {
                    throw new IllegalStateException(status + (isBlank(body) ? "" : ". " + body));
                }
                String token = JsonUtils.readStringValue(json(body), ACCESS_TOKEN);
                httpClient.close();
                response.close();
                return token;
            } catch (Exception e) {
                throw new WakamitiException("Error retrieving oauth2 authentication", e);
            }
        }
    }

}
