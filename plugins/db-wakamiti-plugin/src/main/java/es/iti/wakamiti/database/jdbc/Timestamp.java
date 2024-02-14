/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;

import java.time.LocalDateTime;

import static es.iti.wakamiti.database.DatabaseHelper.DATE_FORMATTER;
import static es.iti.wakamiti.database.DatabaseHelper.DATE_TIME_FORMATTER;

public class Timestamp extends java.sql.Timestamp {

    private final boolean trunc;

    public Timestamp(long time, boolean trunc) {
        super(time);
        this.trunc = trunc;
    }

    @Override
    public String toString() {
        if (trunc) {
            return DATE_FORMATTER.format(this.toLocalDateTime());
        } else {
            return DATE_TIME_FORMATTER.format(this.toLocalDateTime());
        }
    }

    public static Timestamp valueOf(LocalDateTime time, boolean trunc) {
        return new Timestamp(java.sql.Timestamp.valueOf(time).getTime(), trunc);
    }

    public static Timestamp valueOf(String time, boolean trunc) {
        return new Timestamp(java.sql.Timestamp.valueOf(time).getTime(), trunc);
    }
}
