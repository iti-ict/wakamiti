/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


/**
 * Represents connection parameters for database connections.
 */
public class ConnectionParameters {

    private String url;
    private String username;
    private String password = "";
    private String driver;
    private String schema;
    private String catalog;
    private boolean autoTrim = false;
    private Boolean autoCommit;

    /**
     * Retrieves the URL for the database connection.
     *
     * @return The URL
     */
    public String url() {
        return url;
    }

    /**
     * Sets the URL for the database connection.
     *
     * @param url The URL
     * @return This ConnectionParameters instance
     */
    public ConnectionParameters url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Retrieves the username for the database connection.
     *
     * @return The username
     */
    public String username() {
        return username;
    }

    /**
     * Sets the username for the database connection.
     *
     * @param username The username
     * @return This ConnectionParameters instance
     */
    public ConnectionParameters username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Retrieves the password for the database connection.
     *
     * @return The password
     */
    public String password() {
        return password;
    }

    /**
     * Sets the password for the database connection.
     *
     * @param password The password
     * @return This ConnectionParameters instance
     */
    public ConnectionParameters password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Retrieves the driver for the database connection.
     *
     * @return The driver
     */
    public String driver() {
        return driver;
    }

    /**
     * Sets the driver for the database connection.
     *
     * @param driver The driver
     * @return This ConnectionParameters instance
     */
    public ConnectionParameters driver(String driver) {
        this.driver = driver;
        return this;
    }

    /**
     * Retrieves the schema for the database connection.
     *
     * @return The schema
     */
    public String schema() {
        return schema;
    }

    /**
     * Sets the schema for the database connection.
     *
     * @param schema The schema
     * @return This ConnectionParameters instance
     */
    public ConnectionParameters schema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Retrieves the catalog for the database connection.
     *
     * @return The catalog
     */
    public String catalog() {
        return catalog;
    }

    /**
     * Sets the catalog for the database connection.
     *
     * @param catalog The catalog
     * @return This ConnectionParameters instance
     */
    public ConnectionParameters catalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    /**
     * Retrieve whether to include spaces in comparisons.
     *
     * @return {@code true} to enable auto trim, {@code false} otherwise.
     */
    public boolean autoTrim() {
        return autoTrim;
    }

    /**
     * Sets whether to include spaces in comparisons.
     *
     * @param autoTrim {@code true} to enable auto trim, {@code false} otherwise.
     */
    public ConnectionParameters autoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
        return this;
    }

    public Boolean autoCommit() {
        return autoCommit;
    }

    public ConnectionParameters autoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    /**
     * Returns a string representation of the ConnectionParameters.
     *
     * @return The string representation
     */
    @Override
    public String toString() {
        String separator = ", ";
        String equals = "=";
        StringBuilder builder = new StringBuilder("[");
        builder.append("url").append(equals).append(url).append(separator);
        builder.append("username").append(equals).append(username).append(separator);
        builder.append("password").append(equals).append(password).append(separator);
        if (driver != null) {
            builder.append("driver").append(equals).append(driver).append(separator);
        }
        if (schema != null) {
            builder.append("schema").append(equals).append(schema).append(separator);
        }
        if (catalog != null) {
            builder.append("catalog").append(equals).append(catalog).append(separator);
        }
        return builder.toString().replaceAll(separator + "$", "]");
    }
}
