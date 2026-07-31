/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven;


import java.util.Objects;


/**
 * Provides the Repository functionality used by Wakamiti.
 */
public class Repository {

    private final String id;
    private final String url;
    private String username;
    private String password;
    private int priority = -1;

    /**
     * Creates an unauthenticated remote-repository descriptor.
     *
     * @param id  non-null repository identifier used in resolver diagnostics
     * @param url non-null base repository URL
     * @throws NullPointerException if either value is {@code null}
     */
    public Repository(
            String id,
            String url
    ) {
        this.id = Objects.requireNonNull(id);
        this.url = Objects.requireNonNull(url);
    }

    /**
     * Configures basic credentials for this repository.
     *
     * @param username non-null user name
     * @param password non-null password
     * @return this repository descriptor
     * @throws NullPointerException if either credential is {@code null}
     */
    public Repository credentials(
            String username,
            String password
    ) {
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        return this;
    }

    /**
     * Sets the ordering priority used when repositories are assembled.
     *
     * @param priority repository priority; the default is {@code -1}
     * @return this repository descriptor
     */
    public Repository priority(
            int priority
    ) {
        this.priority = priority;
        return this;
    }

    /**
     * Returns the configured repository ordering priority.
     *
     * @return the priority, or {@code -1} when none was assigned
     */
    public int priority() {
        return priority;
    }

    @Override
    public String toString() {
        if (username != null) {
            return String.format("%s=%s [%s=%s]", id, url, username, password);
        } else {
            return String.format("%s=%s", id, url);
        }
    }

}
