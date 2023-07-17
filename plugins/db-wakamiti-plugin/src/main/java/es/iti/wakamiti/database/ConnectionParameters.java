/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.database;


public class ConnectionParameters {

    private String url;
    private String username;
    private String password = "";
    private String driver;
    private String schema;
    private String catalog;


    public String url() {
        return url;
    }


    public ConnectionParameters url(String url) {
        this.url = url;
        return this;
    }


    public String username() {
        return username;
    }


    public ConnectionParameters username(String username) {
        this.username = username;
        return this;
    }


    public String password() {
        return password;
    }


    public ConnectionParameters password(String password) {
        this.password = password;
        return this;
    }


    public String driver() {
        return driver;
    }


    public ConnectionParameters driver(String driver) {
        this.driver = driver;
        return this;
    }


    public String schema() {
        return schema;
    }

    public ConnectionParameters schema(String schema) {
        this.schema = schema;
        return this;
    }

    public String catalog() {
        return catalog;
    }

    public ConnectionParameters catalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

}