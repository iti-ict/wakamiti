/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import java.time.LocalDateTime;

import static es.iti.wakamiti.database.DatabaseHelper.DATE_FORMATTER;
import static es.iti.wakamiti.database.DatabaseHelper.DATE_TIME_FORMATTER;


/**
 * Represents a custom Timestamp class extending {@link java.sql.Timestamp},
 * allowing formatting based on truncation preference.
 *
 * @see java.sql.Timestamp
 */
public class WakamitiTimestamp extends java.sql.Timestamp {

    private final boolean trunc;

    /**
     * Constructs a Timestamp object with the specified time and truncation preference.
     *
     * @param time  The time value
     * @param trunc Truncation preference: {@code true} for date-only, {@code false} for
     *              date-time
     */
    public WakamitiTimestamp(long time, boolean trunc) {
        super(time);
        this.trunc = trunc;
    }

    /**
     * Creates a Timestamp object from the specified LocalDateTime and truncation
     * preference.
     *
     * @param time  The LocalDateTime value
     * @param trunc Truncation preference: {@code true} for date-only, {@code false} for
     *              date-time
     * @return The Timestamp object
     */
    public static WakamitiTimestamp valueOf(LocalDateTime time, boolean trunc) {
        return new WakamitiTimestamp(java.sql.Timestamp.valueOf(time).getTime(), trunc);
    }

    /**
     * Creates a Timestamp object from the specified string representation and truncation
     * preference.
     *
     * @param time  The string representation of the timestamp
     * @param trunc Truncation preference: {@code true} for date-only, {@code false} for
     *              date-time
     * @return The Timestamp object
     */
    public static WakamitiTimestamp valueOf(String time, boolean trunc) {
        return new WakamitiTimestamp(java.sql.Timestamp.valueOf(time).getTime(), trunc);
    }

    /**
     * Returns a string representation of the Timestamp, formatted based on the
     * truncation preference.
     *
     * @return The formatted timestamp string
     */
    @Override
    public String toString() {
        if (trunc) {
            return DATE_FORMATTER.format(this.toLocalDateTime());
        } else {
            return DATE_TIME_FORMATTER.format(this.toLocalDateTime());
        }
    }

}
