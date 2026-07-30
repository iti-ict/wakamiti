/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc.format;


import java.sql.JDBCType;


/**
 * Defines the contract implemented by Sql Format.
 */
public interface SqlFormat {

    /**
     * Converts a textual value from a feature into a JDBC-compatible value.
     *
     * @param value source text, or {@code null} for SQL {@code NULL}
     * @param type JDBC type reported by the target column or parameter
     * @return value suitable for binding through JDBC
     */
    Object formatValue(
            String value,
            JDBCType type
    );

}
