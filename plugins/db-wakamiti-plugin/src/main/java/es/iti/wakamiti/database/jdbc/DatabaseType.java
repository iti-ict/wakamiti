/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.database.jdbc.format.DefaultSqlFormat;
import es.iti.wakamiti.database.jdbc.format.SqlFormat;
import es.iti.wakamiti.database.jdbc.format.SqlServerFormat;


/**
 * Enumeration representing various database types along with their respective health check SQL queries.
 */
public enum DatabaseType {

    ORACLE("select 1 from dual"),
    HSQLDB("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"),
    H2(),
    SQLSERVER(new SqlServerFormat()),
    MYSQL(),
    MARIADB(),
    POSTGRESQL(),
    SQLITE(),
    DB2("select 1 from sysibm.sysdummy1"),
    AS400("select 1 from sysibm.sysdummy1"),
    DERBY("SELECT 1 FROM SYSIBM.SYSDUMMY1"),
    INFORMIX("select count(*) from systables"),
    OTHER();

    private static final String DEFAULT = "select 1";

    private final String healthCheckSql;
    private final SqlFormat format;

    /**
     * Constructs a DatabaseType enum constant with the given health check SQL query.
     *
     * @param healthCheckSql the SQL query used for health check
     */
    DatabaseType(String healthCheckSql) {
        this.healthCheckSql = healthCheckSql;
        this.format = new DefaultSqlFormat();
    }

    DatabaseType(SqlFormat format) {
        this.healthCheckSql = DEFAULT;
        this.format = format;
    }

    DatabaseType() {
        this.healthCheckSql = DEFAULT;
        this.format = new DefaultSqlFormat();
    }

    /**
     * Retrieves the DatabaseType enum constant based on the provided JDBC URL.
     *
     * @param url the JDBC URL
     * @return the corresponding DatabaseType enum constant
     * @throws WakamitiException if the JDBC URL is null or does not start with "jdbc:"
     */
    public static DatabaseType fromUrl(String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            throw new WakamitiException("Bad jdbc url");
        }
        try {
            return DatabaseType.valueOf(url.split("[:\\-]")[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            return DatabaseType.OTHER;
        }
    }

    /**
     * Retrieves the health check SQL query associated with this DatabaseType.
     *
     * @return the health check SQL query
     */
    public String healthCheck() {
        return this.healthCheckSql;
    }

    public SqlFormat formatter() {
        return this.format;
    }
}
