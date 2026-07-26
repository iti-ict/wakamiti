/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven;

import java.util.Objects;

public class Repository {

    private final String id;
    private final String url;
    private String username;
    private String password;
    private int priority = -1;


    public Repository(String id, String url) {
        this.id = Objects.requireNonNull(id);
        this.url = Objects.requireNonNull(url);
    }


    public Repository credentials(String username, String password) {
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        return this;
    }


    public Repository priority(int priority) {
        this.priority = priority;
        return this;
    }


    public int priority() {
        return priority;
    }


    @Override
    public String toString() {
        if (username != null) {
            return String.format("%s=%s [%s=%s]", id, url, username, password );
        } else {
            return String.format("%s=%s", id, url);
        }
    }


}
