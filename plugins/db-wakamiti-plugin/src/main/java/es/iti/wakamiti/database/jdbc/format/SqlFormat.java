/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc.format;


import java.sql.JDBCType;


public interface SqlFormat {

    Object formatValue(String value, JDBCType type);

}
