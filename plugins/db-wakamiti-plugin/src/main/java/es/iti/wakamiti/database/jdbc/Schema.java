/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import java.sql.JDBCType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a database schema containing information about tables, columns, and types.
 */
public class Schema {

    /** JDBC type metadata indexed first by normalized table name and then column name. */
    public final Map<String, Map<String, JDBCType>> types = new HashMap<>();
    /** Ordered primary-key column names indexed by normalized table name. */
    public final Map<String, List<String>> pk = new HashMap<>();
    /** Physical table names indexed by the normalized names used during matching. */
    public final Map<String, String> tables = new HashMap<>();
    /** Physical column names indexed by normalized table and column names. */
    public final Map<String, Map<String, String>> columns = new HashMap<>();

}
