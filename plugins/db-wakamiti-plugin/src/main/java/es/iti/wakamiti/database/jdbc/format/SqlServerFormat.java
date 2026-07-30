/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc.format;


import static java.util.Objects.isNull;

import java.sql.JDBCType;


/**
 * Provides the Sql Server Format functionality used by Wakamiti.
 */
public class SqlServerFormat extends DefaultSqlFormat {

    @Override
    public Object formatValue(
            String value,
            JDBCType type
    ) {
        if (isNull(value)) {
            return null;
        }
        return switch (type) {
            case BIT, BOOLEAN -> formatInteger(value);
            default -> super.formatValue(value, type);
        };
    }

}
