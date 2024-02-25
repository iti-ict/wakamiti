/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.api.WakamitiException;


public enum DatabaseType {

    ORACLE("select 1 from dual"),
    HSQLDB("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"),
    H2("select 1"),
    SQLSERVER("select 1"),
    MYSQL("select 1"),
    MARIADB("Select 1"),
    POSTGRESQL("select 1"),
    SQLITE("select 1"),
    DB2("select 1 from sysibm.sysdummy1"),
    AS400("select 1 from sysibm.sysdummy1"),
    DERBY("SELECT 1 FROM SYSIBM.SYSDUMMY1"),
    INFORMIX("select count(*) from systables"),
    OTHER("select 1");

    private final String healthCheckSql;

    DatabaseType(String healthCheckSql) {
        this.healthCheckSql = healthCheckSql;
    }

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

    public String healthCheck() {
        return this.healthCheckSql;
    }
}
