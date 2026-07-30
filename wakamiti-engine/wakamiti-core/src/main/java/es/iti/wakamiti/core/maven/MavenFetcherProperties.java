/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven;


/**
 * Provides the Maven Fetcher Properties functionality used by Wakamiti.
 */
public final class MavenFetcherProperties {

    private MavenFetcherProperties() {
    }

    /**
     * Set whether the default Maven central repository should be used (`true` by default)
     */
    public static final String USE_DEFAULT_REMOTE_REPOSITORY = "useDefaultRemoteRepository";

    /**
     * A list of remote repositories in form of <pre>&lt;id&gt;=&lt;url&gt;</pre> and separated with `;`
     */
    public static final String REMOTE_REPOSITORIES = "remoteRepositories";

    /**
     * The path of the Maven local repository folder
     */
    public static final String LOCAL_REPOSITORY = "localRepository";

    /**
     * A proxy URL, if required
     */
    public static final String PROXY_URL = "proxy.url";

    /**
     * The username for proxy credentials
     */
    public static final String PROXY_USERNAME = "proxy.username";

    /**
     * The password for proxy credentials
     */
    public static final String PROXY_PASSWORD = "proxy.password";

    /**
     * A list of proxy exceptions separated with `;`
     */
    public static final String PROXY_EXCEPTIONS = "proxy.exceptions";

}
