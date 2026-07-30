/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven.internal;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import es.iti.wakamiti.core.maven.MavenFetcher;
import es.iti.wakamiti.core.maven.Repository;


/**
 * Stores the configuration used by the Maven Fetcher Config component.
 */
public class MavenFetcherConfig {

    /** Property containing semicolon-separated remote repository URLs. */
    public static final String REMOTE_REPOSITORIES = "remoteRepositories";
    /** Property selecting the Maven local-repository directory. */
    public static final String LOCAL_REPOSITORY = "localRepository";
    /** Property containing the outbound proxy URL. */
    public static final String PROXY_URL = "proxy.url";
    /** Property containing the proxy authentication user name. */
    public static final String PROXY_USERNAME = "proxy.username";
    /** Property containing the proxy authentication password. */
    public static final String PROXY_PASSWORD = "proxy.password";
    /** Property containing semicolon-separated proxy bypass patterns. */
    public static final String PROXY_EXCEPTIONS = "proxy.exceptions";

    private final Properties properties;

    /**
     * Loads UTF-8 Maven-fetcher properties from a file.
     *
     * @param configFile path to the Java properties file
     * @throws IOException if the file cannot be opened or read
     */
    public MavenFetcherConfig(
            String configFile
    ) throws IOException {
        try (Reader reader = new InputStreamReader(
                new FileInputStream(configFile), StandardCharsets.UTF_8
        )) {
            this.properties = new Properties();
            this.properties.load(reader);
        }
    }

    /**
     * Creates a configuration backed by existing properties.
     *
     * @param properties properties to apply; retained without copying
     */
    public MavenFetcherConfig(
            Properties properties
    ) {
        this.properties = properties;
    }

    /**
     * Applies configured repositories, local cache, and proxy settings to a
     * Maven fetcher.
     *
     * @param fetcher target fetcher
     * @throws MalformedURLException if a configured repository or proxy URL is
     *                               invalid
     */
    public void config(
            MavenFetcher fetcher
    ) throws MalformedURLException {
        String remoteRepositories = properties.getProperty(REMOTE_REPOSITORIES);
        if (remoteRepositories != null) {
            for (String remoteRepository : remoteRepositories.split(";")) {
                fetcher.addRemoteRepository(new Repository(remoteRepository, remoteRepository));
            }
        }
        String localRepository = properties.getProperty(LOCAL_REPOSITORY);
        if (localRepository != null) {
            fetcher.localRepositoryPath(localRepository);
        }
        String proxyUrl = properties.getProperty(PROXY_URL);
        if (proxyUrl != null) {
            fetcher.proxyURL(proxyUrl);
            String proxyUsername = properties.getProperty(PROXY_USERNAME);
            String proxyPassword = properties.getProperty(PROXY_PASSWORD);
            if (proxyUsername != null && proxyPassword != null) {
                fetcher.proxyCredentials(proxyUsername, proxyPassword);
            }
            String proxyExceptions = properties.getProperty(PROXY_EXCEPTIONS);
            if (proxyExceptions != null) {
                fetcher.proxyExceptions(Arrays.asList(proxyExceptions.split(";")));
            }
        }
    }

}
