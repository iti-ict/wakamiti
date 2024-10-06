/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc.format;


import java.sql.JDBCType;

import static java.util.Objects.isNull;


public class SqlServerFormat extends DefaultSqlFormat {

    @Override
    public Object formatValue(String value, JDBCType type) {
        if (isNull(value)) return null;
        switch (type) {
            case BIT:
            case BOOLEAN:
                return formatInteger(value);
            default:
                return super.formatValue(value, type);
        }
    }

}
