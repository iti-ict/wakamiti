/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.server.infra.auth;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;

import iti.kukumo.server.spi.ApplicationContext;

@RequestScoped
public class QuarkusApplicationContext implements ApplicationContext {

    private String user;

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public Optional<String> user() {
        return Optional.ofNullable(user);
    }

}