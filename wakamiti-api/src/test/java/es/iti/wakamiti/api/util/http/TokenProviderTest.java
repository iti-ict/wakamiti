/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import es.iti.wakamiti.api.WakamitiException;


public class TokenProviderTest {

    @Test
    public void testRetrieveTokenWithConfigurationAndRequest() throws MalformedURLException {
        // prepare
        URL url = new URL("https://example.org/token");
        TestProvider provider = new TestProvider();
        provider.configuration()
                .url(url)
                .addParameter("tenant", "wakamiti")
                .cacheAuth(true);
        provider.setRetriever((configuration, request) -> {
            assertThat(configuration.url()).isEqualTo(url);
            assertThat(configuration.parameters()).containsEntry("tenant", "wakamiti");
            assertThat(configuration.cacheAuth()).isTrue();
            return request + "-token";
        });

        // act
        String token = provider.getToken("request");

        // check
        assertThat(token).isEqualTo("request-token");
        assertThat(provider.configuration().checked).isTrue();
    }

    @Test
    public void testRetrieveTokenWithoutRetrieverFails() {
        // prepare
        TestProvider provider = new TestProvider();

        // act and check
        assertThatThrownBy(() -> provider.getToken("request"))
                .isInstanceOf(WakamitiException.class)
                .hasMessage("A retriever is required");
    }

    private static final class TestProvider extends AbstractTokenProvider<TestConfiguration, String> {

        private TestProvider() {
            super(new TestConfiguration(), null, "A retriever is required");
        }

        private void setRetriever(
                TokenRetriever<TestConfiguration, String> retriever
        ) {
            setTokenRetriever(retriever);
        }

        @Override
        public String getToken(
                String request
        ) {
            return retrieveToken(request);
        }

    }

    private static final class TestConfiguration extends TokenProviderConfig<TestConfiguration> {

        private boolean checked;

        @Override
        public void checkParameters() {
            checked = true;
        }

        @Override
        protected TestConfiguration self() {
            return this;
        }

    }

}
