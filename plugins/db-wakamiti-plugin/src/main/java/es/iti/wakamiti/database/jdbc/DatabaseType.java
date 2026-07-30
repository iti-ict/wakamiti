/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
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

    /** Oracle dialect, using {@code DUAL} for connection health checks. */
    ORACLE("select 1 from dual"),
    /** HyperSQL dialect, with a health check against its system-user view. */
    HSQLDB("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"),
    /** H2 dialect using the standard health-check query and SQL formatter. */
    H2(),
    /** Microsoft SQL Server dialect with its dedicated literal formatter. */
    SQLSERVER(new SqlServerFormat()),
    /** MySQL dialect using the standard health-check query and SQL formatter. */
    MYSQL(),
    /** MariaDB dialect using the standard health-check query and SQL formatter. */
    MARIADB(),
    /** PostgreSQL dialect using the standard health-check query and SQL formatter. */
    POSTGRESQL(),
    /** SQLite dialect using the standard health-check query and SQL formatter. */
    SQLITE(),
    /** IBM Db2 dialect, using {@code SYSIBM.SYSDUMMY1} for health checks. */
    DB2("select 1 from sysibm.sysdummy1"),
    /** IBM i (AS/400) dialect, using {@code SYSIBM.SYSDUMMY1} for health checks. */
    AS400("select 1 from sysibm.sysdummy1"),
    /** Apache Derby dialect, using {@code SYSIBM.SYSDUMMY1} for health checks. */
    DERBY("SELECT 1 FROM SYSIBM.SYSDUMMY1"),
    /** Informix dialect, whose health check queries the system table catalog. */
    INFORMIX("select count(*) from systables"),
    /** Fallback for unrecognized JDBC subprotocols. */
    OTHER();

    private static final String DEFAULT = "select 1";

    private final String healthCheckSql;
    private final SqlFormat format;

    /**
     * Constructs a DatabaseType enum constant with the given health check SQL query.
     *
     * @param healthCheckSql the SQL query used for health check
     */
    DatabaseType(
            String healthCheckSql
    ) {
        this.healthCheckSql = healthCheckSql;
        this.format = new DefaultSqlFormat();
    }

    DatabaseType(
            SqlFormat format
    ) {
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
    public static DatabaseType fromUrl(
            String url
    ) {
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

    /**
     * Returns the value formatter selected for this database dialect.
     *
     * @return converter from textual test data to JDBC-compatible values
     */
    public SqlFormat formatter() {
        return this.format;
    }

}
