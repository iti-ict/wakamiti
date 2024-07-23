/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.api.WakamitiException;


/**
 * Enumeration representing various database types along with their respective health check SQL queries.
 */
public enum DatabaseType {



    ORACLE("select 1 from dual", "select DBTIMEZONE from dual"),
    HSQLDB("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
            "SELECT TIMEZONE() FROM INFORMATION_SCHEMA.SYSTEM_USERS"),
    H2("select 1", "SELECT @@system_time_zone"),
    SQLSERVER("select 1", "SELECT CONVERT(DATETIME, LEFT(SYSDATETIMEOFFSET(),19), 126)"),
    MYSQL("select 1", "SELECT @@system_time_zone"),
    MARIADB("select 1", "SELECT @@system_time_zone"),
    POSTGRESQL("select 1", "select to_char(CURRENT_TIMESTAMP(), 'TZ')"),
    SQLITE("select 1", ""),
    DB2("select 1 from sysibm.sysdummy1", ""),
    AS400("select 1 from sysibm.sysdummy1", ""),
    DERBY("SELECT 1 FROM SYSIBM.SYSDUMMY1", ""),
    INFORMIX("select count(*) from systables", ""),
    OTHER("select 1", "");

    private final String healthCheckSql;
    private final String timezoneSql;

    /**
     * Constructs a DatabaseType enum constant with the given health check SQL query.
     *
     * @param healthCheckSql the SQL query used for health check
     */
    DatabaseType(String healthCheckSql, String timezoneSql) {
        this.healthCheckSql = healthCheckSql;
        this.timezoneSql = timezoneSql;
    }

//    DatabaseType() {
//        this.healthCheckSql = "select 1";
//        this.timezoneSql = "SELECT @@system_time_zone;";
//    }

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
}
